package com.rickenbazolo.paymux.pawapay.callback;

import com.rickenbazolo.paymux.pawapay.exception.PawapaySignatureException;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verifies the signature of PawaPay callbacks (HTTP Message Signatures,
 * <a href="https://datatracker.ietf.org/doc/rfc9421/">RFC 9421</a>) using only {@code java.security}.
 * <p>
 * When signed callbacks are enabled in the PawaPay Dashboard, each callback carries:
 * <ul>
 *   <li>{@code Content-Digest}: {@code sha-256=:<base64>:} or {@code sha-512=:<base64>:} digest of the body;</li>
 *   <li>{@code Signature-Input}: the covered components and parameters, e.g.
 *       {@code sig-pp=("@method" "@authority" "@path" "signature-date" "content-digest" "content-type");alg="ecdsa-p256-sha256";keyid="KEY";created=1714653405;expires=1714653465};</li>
 *   <li>{@code Signature}: {@code sig-pp=:<base64 signature>:};</li>
 *   <li>{@code Signature-Date}: the signing timestamp (a PawaPay custom header, covered by the signature).</li>
 * </ul>
 * Verification checks the body digest first, then the {@code created} / {@code expires} window
 * (with the configured clock skew), then the signature itself against the PawaPay public key
 * identified by {@code keyid}. Public keys are fetched from {@code GET /v2/public-key/http}
 * and cached for the configured TTL; an unknown key id triggers a refresh.
 * </p>
 * <p>
 * Supported algorithms: {@code ecdsa-p256-sha256}, {@code ecdsa-p384-sha384} (DER or raw r||s
 * signatures), {@code rsa-v1_5-sha256} and {@code rsa-pss-sha512}.
 * </p>
 * <p>
 * Instances are thread-safe. Obtain one through {@code PawapayClient#callbackSignatureVerifier()}
 * or build one with your own {@link PublicKeySource}.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayCallbackSignatureVerifier {

    /**
     * Source of the PawaPay public keys.
     */
    @FunctionalInterface
    public interface PublicKeySource {
        /**
         * Fetches the current PawaPay public keys.
         *
         * @return the public keys
         */
        List<PawapayPublicKey> fetchPublicKeys();
    }

    static final String HEADER_CONTENT_DIGEST = "Content-Digest";
    static final String HEADER_SIGNATURE = "Signature";
    static final String HEADER_SIGNATURE_INPUT = "Signature-Input";

    private static final Logger log = LoggerFactory.getLogger(PawapayCallbackSignatureVerifier.class);

    private static final Duration MIN_REFRESH_INTERVAL = Duration.ofSeconds(30);
    private static final Pattern DIGEST_PATTERN = Pattern.compile("(sha-256|sha-512)=:([A-Za-z0-9+/=]+):", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern PARAM_ALG = Pattern.compile(";\\s*alg=\"([^\"]*)\"");
    private static final Pattern PARAM_KEYID = Pattern.compile(";\\s*keyid=\"([^\"]*)\"");
    private static final Pattern PARAM_CREATED = Pattern.compile(";\\s*created=(\\d+)");
    private static final Pattern PARAM_EXPIRES = Pattern.compile(";\\s*expires=(\\d+)");
    private static final Pattern PEM_ARMOR = Pattern.compile("-----(BEGIN|END)[^-]*-----");

    private final PublicKeySource keySource;
    private final Duration cacheTtl;
    private final Duration clockSkew;
    private final Clock clock;

    private final Map<String, PublicKey> keys = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private volatile Instant keysExpireAt = Instant.EPOCH;
    private volatile Instant lastRefresh = Instant.EPOCH;

    /**
     * Creates a verifier.
     *
     * @param keySource the source of PawaPay public keys (typically {@code client::getPublicKeys})
     * @param cacheTtl  how long fetched public keys are cached
     * @param clockSkew tolerated clock skew when validating the signature window
     */
    public PawapayCallbackSignatureVerifier(PublicKeySource keySource, Duration cacheTtl, Duration clockSkew) {
        this(keySource, cacheTtl, clockSkew, Clock.systemUTC());
    }

    PawapayCallbackSignatureVerifier(PublicKeySource keySource, Duration cacheTtl, Duration clockSkew, Clock clock) {
        this.keySource = Objects.requireNonNull(keySource, "keySource");
        this.cacheTtl = Objects.requireNonNull(cacheTtl, "cacheTtl");
        this.clockSkew = Objects.requireNonNull(clockSkew, "clockSkew");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Verifies the callback signature.
     *
     * @param request the incoming callback request
     * @throws PawapaySignatureException if the request is not properly signed
     */
    public void verify(PawapayCallbackRequest request) throws PawapaySignatureException {
        Objects.requireNonNull(request, "request");

        byte[] body = request.body();
        verifyContentDigest(request, body);

        String signatureInputHeader = requireHeader(request, HEADER_SIGNATURE_INPUT);
        String signatureHeader = requireHeader(request, HEADER_SIGNATURE);

        SignatureInput input = parseSignatureInput(signatureInputHeader);
        validateWindow(input);

        byte[] signature = extractSignature(signatureHeader, input.label());
        String base = buildSignatureBase(request, input);
        PublicKey key = resolveKey(input.keyId());

        if (!verifySignature(input.algorithm(), key, base.getBytes(StandardCharsets.UTF_8), signature)) {
            throw new PawapaySignatureException("PawaPay callback signature is invalid");
        }
        log.debug("PawaPay callback signature verified (keyid={}, alg={})", input.keyId(), input.algorithm());
    }

    /**
     * Tells whether the callback signature is valid, without throwing.
     *
     * @param request the incoming callback request
     * @return true if the signature is valid
     */
    public boolean isValid(PawapayCallbackRequest request) {
        try {
            verify(request);
            return true;
        } catch (PawapaySignatureException e) {
            log.warn("PawaPay callback signature rejected: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clears the cached public keys; the next verification fetches them again.
     */
    public void clearCache() {
        synchronized (lock) {
            keys.clear();
            keysExpireAt = Instant.EPOCH;
            lastRefresh = Instant.EPOCH;
        }
    }

    // ---- Content-Digest -------------------------------------------------------------------

    private static void verifyContentDigest(PawapayCallbackRequest request, byte[] body) {
        String header = requireHeader(request, HEADER_CONTENT_DIGEST);
        Matcher matcher = DIGEST_PATTERN.matcher(header);
        boolean matched = false;

        while (matcher.find()) {
            matched = true;
            String algorithm = matcher.group(1).toLowerCase(Locale.ROOT);
            byte[] expected = decodeBase64(matcher.group(2), "Content-Digest");
            byte[] actual = digest(algorithm.equals("sha-256") ? "SHA-256" : "SHA-512", body);
            if (!MessageDigest.isEqual(expected, actual)) {
                throw new PawapaySignatureException("PawaPay callback body digest does not match (" + algorithm + ")");
            }
        }
        if (!matched) {
            throw new PawapaySignatureException("Unsupported callback digest algorithm in Content-Digest: " + header);
        }
    }

    private static byte[] digest(String algorithm, byte[] body) {
        try {
            return MessageDigest.getInstance(algorithm).digest(body);
        } catch (GeneralSecurityException e) {
            throw new PawapaySignatureException("Digest algorithm not available: " + algorithm, e);
        }
    }

    // ---- Signature-Input / Signature ------------------------------------------------------

    record SignatureInput(String label, List<String> components, String parameters,
                          String algorithm, String keyId, Long created, Long expires) {
    }

    static SignatureInput parseSignatureInput(String header) {
        int eq = header.indexOf('=');
        if (eq <= 0) {
            throw new PawapaySignatureException("Malformed Signature-Input header: missing label");
        }
        String label = header.substring(0, eq).trim();
        String rest = header.substring(eq + 1).trim();
        if (!rest.startsWith("(")) {
            throw new PawapaySignatureException("Malformed Signature-Input header: missing covered components");
        }
        int close = rest.indexOf(')');
        if (close < 0) {
            throw new PawapaySignatureException("Malformed Signature-Input header: unterminated covered components");
        }

        String parameters = rest.substring(0, endOfMember(rest, close + 1));
        String inner = rest.substring(1, close);
        List<String> components = new ArrayList<>();
        Matcher matcher = COMPONENT_PATTERN.matcher(inner);
        while (matcher.find()) {
            components.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }
        if (components.isEmpty()) {
            throw new PawapaySignatureException("Malformed Signature-Input header: no covered components");
        }

        String params = parameters.substring(close + 1);
        String algorithm = group(PARAM_ALG, params);
        String keyId = group(PARAM_KEYID, params);
        String created = group(PARAM_CREATED, params);
        String expires = group(PARAM_EXPIRES, params);

        if (algorithm == null || algorithm.isBlank()) {
            throw new PawapaySignatureException("Signature-Input header is missing the 'alg' parameter");
        }
        if (keyId == null || keyId.isBlank()) {
            throw new PawapaySignatureException("Signature-Input header is missing the 'keyid' parameter");
        }
        if (created == null) {
            throw new PawapaySignatureException("Signature-Input header is missing the 'created' parameter");
        }
        return new SignatureInput(label, List.copyOf(components), parameters, algorithm, keyId,
            parseTimestamp(created, "created"), expires != null ? parseTimestamp(expires, "expires") : null);
    }

    /** Finds the end of the first dictionary member: the first top-level comma, or the end of the string. */
    private static int endOfMember(String value, int from) {
        boolean quoted = false;
        for (int i = from; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                return i;
            }
        }
        return value.length();
    }

    private static String group(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static long parseTimestamp(String value, String name) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new PawapaySignatureException("Signature-Input header has an invalid '" + name + "' timestamp: " + value);
        }
    }

    private void validateWindow(SignatureInput input) {
        Instant now = clock.instant();
        Instant created = Instant.ofEpochSecond(input.created());
        if (created.isAfter(now.plus(clockSkew))) {
            throw new PawapaySignatureException("PawaPay callback signature was created in the future: " + created);
        }
        if (input.expires() != null) {
            Instant expires = Instant.ofEpochSecond(input.expires());
            if (!expires.isAfter(created)) {
                throw new PawapaySignatureException("PawaPay callback signature has an invalid validity window");
            }
            if (expires.isBefore(now.minus(clockSkew))) {
                throw new PawapaySignatureException("PawaPay callback signature expired at " + expires);
            }
        }
    }

    static byte[] extractSignature(String signatureHeader, String label) {
        Pattern pattern = Pattern.compile("(?:^|,)\\s*" + Pattern.quote(label) + "=:([A-Za-z0-9+/=]+):");
        Matcher matcher = pattern.matcher(signatureHeader);
        if (!matcher.find()) {
            throw new PawapaySignatureException("Signature header has no entry for label '" + label + "'");
        }
        return decodeBase64(matcher.group(1), HEADER_SIGNATURE);
    }

    static String buildSignatureBase(PawapayCallbackRequest request, SignatureInput input) {
        var base = new StringBuilder();
        for (String component : input.components()) {
            base.append('"').append(component).append("\": ").append(componentValue(request, component)).append('\n');
        }
        base.append("\"@signature-params\": ").append(input.parameters());
        return base.toString();
    }

    private static String componentValue(PawapayCallbackRequest request, String component) {
        if (component.startsWith("@")) {
            return switch (component) {
                case "@method" -> request.method().toUpperCase(Locale.ROOT);
                case "@authority" -> request.authority().trim().toLowerCase(Locale.ROOT);
                case "@path" -> request.path();
                default -> throw new PawapaySignatureException("Unsupported derived component in signature: " + component);
            };
        }
        return request.header(component)
            .map(String::trim)
            .orElseThrow(() -> new PawapaySignatureException("Signed header '" + component + "' is missing from the callback"));
    }

    // ---- Keys -----------------------------------------------------------------------------

    private PublicKey resolveKey(String keyId) {
        Instant now = clock.instant();
        PublicKey key = now.isBefore(keysExpireAt) ? keys.get(keyId) : null;
        if (key == null) {
            refreshKeys(keyId);
            key = keys.get(keyId);
        }
        if (key == null) {
            throw new PawapaySignatureException("Unknown PawaPay callback signing key: " + keyId);
        }
        return key;
    }

    private void refreshKeys(String keyId) {
        synchronized (lock) {
            Instant now = clock.instant();
            boolean cacheValid = now.isBefore(keysExpireAt);
            if (cacheValid && keys.containsKey(keyId)) {
                return; // refreshed by another thread meanwhile
            }
            if (cacheValid && now.isBefore(lastRefresh.plus(MIN_REFRESH_INTERVAL))) {
                return; // unknown key id, but we refreshed very recently: do not hammer the API
            }

            List<PawapayPublicKey> fetched;
            try {
                fetched = keySource.fetchPublicKeys();
            } catch (RuntimeException e) {
                throw new PawapaySignatureException("Unable to load PawaPay public keys: " + e.getMessage(), e);
            }

            Map<String, PublicKey> parsed = new ConcurrentHashMap<>();
            for (PawapayPublicKey publicKey : fetched != null ? fetched : List.<PawapayPublicKey>of()) {
                if (publicKey.id() != null && publicKey.key() != null) {
                    parsed.put(publicKey.id(), parsePublicKey(publicKey.key(), publicKey.id()));
                }
            }
            keys.clear();
            keys.putAll(parsed);
            lastRefresh = now;
            keysExpireAt = now.plus(cacheTtl);
            log.info("Loaded {} PawaPay public key(s)", parsed.size());
        }
    }

    static PublicKey parsePublicKey(String pem, String keyId) {
        String base64 = PEM_ARMOR.matcher(pem).replaceAll("").replaceAll("\\s", "");
        byte[] der = decodeBase64(base64, "public key " + keyId);
        var spec = new X509EncodedKeySpec(der);
        for (String algorithm : List.of("EC", "RSA")) {
            try {
                return KeyFactory.getInstance(algorithm).generatePublic(spec);
            } catch (GeneralSecurityException ignored) {
                // try the next key type
            }
        }
        throw new PawapaySignatureException("Unsupported PawaPay public key format for key " + keyId);
    }

    // ---- Signature verification -----------------------------------------------------------

    static boolean verifySignature(String algorithm, PublicKey key, byte[] data, byte[] signature) {
        try {
            Signature verifier = switch (algorithm.toLowerCase(Locale.ROOT)) {
                case "ecdsa-p256-sha256" -> Signature.getInstance(
                    signature.length == 64 ? "SHA256withECDSAinP1363Format" : "SHA256withECDSA");
                case "ecdsa-p384-sha384" -> Signature.getInstance(
                    signature.length == 96 ? "SHA384withECDSAinP1363Format" : "SHA384withECDSA");
                case "rsa-v1_5-sha256" -> Signature.getInstance("SHA256withRSA");
                case "rsa-pss-sha512" -> {
                    Signature pss = Signature.getInstance("RSASSA-PSS");
                    pss.setParameter(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
                    yield pss;
                }
                default -> throw new PawapaySignatureException("Unsupported signature algorithm: " + algorithm);
            };
            verifier.initVerify(key);
            verifier.update(data);
            return verifier.verify(signature);
        } catch (SignatureException e) {
            return false; // malformed signature bytes: treat as invalid
        } catch (GeneralSecurityException e) {
            throw new PawapaySignatureException("Unable to verify signature with algorithm " + algorithm + ": " + e.getMessage(), e);
        }
    }

    // ---- Helpers --------------------------------------------------------------------------

    private static String requireHeader(PawapayCallbackRequest request, String name) {
        return request.header(name)
            .filter(v -> !v.isBlank())
            .orElseThrow(() -> new PawapaySignatureException("Missing " + name + " header on PawaPay callback"));
    }

    private static byte[] decodeBase64(String value, String what) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new PawapaySignatureException("Invalid base64 in " + what, e);
        }
    }
}
