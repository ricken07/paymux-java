package com.rickenbazolo.paymux.pawapay.signing;

import com.rickenbazolo.paymux.pawapay.exception.PawapaySignatureException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Signs outbound PawaPay financial requests (HTTP Message Signatures,
 * <a href="https://datatracker.ietf.org/doc/rfc9421/">RFC 9421</a>) using only {@code java.security}.
 * <p>
 * PawaPay can be configured to only accept signed financial requests: {@code POST /v2/deposits},
 * {@code POST /v2/payouts}, {@code POST /v2/payouts/bulk} and {@code POST /v2/refunds}. This is a
 * second layer of security on top of the API token, enabled per merchant account in the PawaPay
 * Dashboard (System configuration &gt; API tokens &gt; Signed requests): you generate a key pair,
 * upload the <strong>public</strong> key there under a name of your choosing (the {@code keyId}),
 * and enable the feature. This class only performs the signing with the <strong>private</strong>
 * key you already hold; it does not generate keys or touch the Dashboard.
 * </p>
 * <p>
 * Each signed request carries:
 * <ul>
 *   <li>{@code Content-Digest}: SHA-256 digest of the request body;</li>
 *   <li>{@code Signature-Date}: the signing timestamp, a PawaPay custom header covered by the signature;</li>
 *   <li>{@code Signature-Input}: the covered components and parameters, e.g.
 *       {@code sig-pp=("@method" "@authority" "@path" "signature-date" "content-digest" "content-type");alg="ecdsa-p256-sha256";keyid="KEY";created=...;expires=...};</li>
 *   <li>{@code Signature}: {@code sig-pp=:<base64 signature>:}.</li>
 * </ul>
 * Supported algorithms: {@link PawapaySignatureAlgorithms#ECDSA_P256_SHA256},
 * {@link PawapaySignatureAlgorithms#ECDSA_P384_SHA384} (DER encoded),
 * {@link PawapaySignatureAlgorithms#RSA_V1_5_SHA256} and {@link PawapaySignatureAlgorithms#RSA_PSS_SHA512}.
 * </p>
 * <p>
 * Instances are thread-safe (a fresh {@link Signature} is created for every call). Obtain one
 * through {@code PawapayConfig#getRequestSigner()}, configured from
 * {@code paymux.pawapay.signing.*} properties or the {@code PawapayConfig} builder.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayRequestSigner {

    static final String LABEL = "sig-pp";
    private static final Pattern PEM_ARMOR = Pattern.compile("-----(BEGIN|END)[^-]*-----");

    private final String keyId;
    private final String algorithm;
    private final PrivateKey privateKey;
    private final Duration validity;
    private final Clock clock;

    /**
     * Creates a signer.
     *
     * @param keyId      the id of the public key you uploaded to the PawaPay Dashboard
     * @param algorithm  the signature algorithm (see {@link PawapaySignatureAlgorithms})
     * @param privateKey the private key matching the uploaded public key
     * @param validity   how long a signature remains valid ({@code expires - created})
     */
    public PawapayRequestSigner(String keyId, String algorithm, PrivateKey privateKey, Duration validity) {
        this(keyId, algorithm, privateKey, validity, Clock.systemUTC());
    }

    PawapayRequestSigner(String keyId, String algorithm, PrivateKey privateKey, Duration validity, Clock clock) {
        this.keyId = Objects.requireNonNull(keyId, "keyId");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm").toLowerCase(Locale.ROOT);
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        this.validity = Objects.requireNonNull(validity, "validity");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (!PawapaySignatureAlgorithms.isSupported(this.algorithm)) {
            throw new IllegalArgumentException("Unsupported PawaPay signature algorithm: " + algorithm);
        }
    }

    /**
     * Signs a request, producing the four headers to add to it.
     *
     * @param method      the HTTP method (e.g. {@code POST})
     * @param authority   the request authority (host, with port if non-default)
     * @param path        the absolute request path, e.g. {@code /v2/deposits}
     * @param contentType the exact value of the request's {@code Content-Type} header
     * @param body        the raw request body
     * @return the four signature headers
     * @throws PawapaySignatureException if signing fails
     */
    public SignedHeaders sign(String method, String authority, String path, String contentType, byte[] body) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(authority, "authority");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(body, "body");

        String contentDigest = "sha-256=:" + Base64.getEncoder().encodeToString(digest(body)) + ":";
        Instant now = clock.instant();
        String signatureDate = now.toString();
        long created = now.getEpochSecond();
        long expires = now.plus(validity).getEpochSecond();

        String parameters = "(\"@method\" \"@authority\" \"@path\" \"signature-date\" \"content-digest\" \"content-type\")"
            + ";alg=\"" + algorithm + "\";keyid=\"" + keyId + "\";created=" + created + ";expires=" + expires;

        String base = "\"@method\": " + method.toUpperCase(Locale.ROOT) + "\n"
            + "\"@authority\": " + authority.trim().toLowerCase(Locale.ROOT) + "\n"
            + "\"@path\": " + path + "\n"
            + "\"signature-date\": " + signatureDate + "\n"
            + "\"content-digest\": " + contentDigest + "\n"
            + "\"content-type\": " + contentType.trim() + "\n"
            + "\"@signature-params\": " + parameters;

        byte[] signatureBytes = signBase(base.getBytes(StandardCharsets.UTF_8));
        String signature = LABEL + "=:" + Base64.getEncoder().encodeToString(signatureBytes) + ":";
        String signatureInput = LABEL + "=" + parameters;

        return new SignedHeaders(contentDigest, signatureDate, signatureInput, signature);
    }

    private byte[] digest(byte[] body) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(body);
        } catch (GeneralSecurityException e) {
            throw new PawapaySignatureException("SHA-256 digest not available", e);
        }
    }

    private byte[] signBase(byte[] base) {
        try {
            Signature signer = switch (algorithm) {
                case PawapaySignatureAlgorithms.ECDSA_P256_SHA256 -> Signature.getInstance("SHA256withECDSA");
                case PawapaySignatureAlgorithms.ECDSA_P384_SHA384 -> Signature.getInstance("SHA384withECDSA");
                case PawapaySignatureAlgorithms.RSA_V1_5_SHA256 -> Signature.getInstance("SHA256withRSA");
                case PawapaySignatureAlgorithms.RSA_PSS_SHA512 -> {
                    Signature pss = Signature.getInstance("RSASSA-PSS");
                    pss.setParameter(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
                    yield pss;
                }
                default -> throw new PawapaySignatureException("Unsupported signature algorithm: " + algorithm);
            };
            signer.initSign(privateKey);
            signer.update(base);
            return signer.sign();
        } catch (GeneralSecurityException e) {
            throw new PawapaySignatureException("Unable to sign PawaPay request with algorithm " + algorithm + ": " + e.getMessage(), e);
        }
    }

    /**
     * Parses a private key supplied as PEM (PKCS8, {@code -----BEGIN PRIVATE KEY-----}) or as raw
     * base64-encoded PKCS8 DER content.
     *
     * @param pemOrBase64 the PEM text or bare base64 content; literal {@code \n} sequences are
     *                    unescaped first, so the key can be embedded in a single-line environment variable
     * @param algorithm   the signature algorithm, used to pick the key type ({@code EC} or {@code RSA})
     * @return the parsed private key
     * @throws PawapaySignatureException if the key cannot be parsed
     */
    public static PrivateKey parsePrivateKey(String pemOrBase64, String algorithm) {
        Objects.requireNonNull(pemOrBase64, "pemOrBase64");
        Objects.requireNonNull(algorithm, "algorithm");

        String unescaped = pemOrBase64.replace("\\n", "\n");
        String base64 = PEM_ARMOR.matcher(unescaped).replaceAll("").replaceAll("\\s", "");
        byte[] der;
        try {
            der = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new PawapaySignatureException("Invalid base64 in PawaPay signing private key", e);
        }

        String keyType = algorithm.toLowerCase(Locale.ROOT).startsWith("ecdsa") ? "EC" : "RSA";
        try {
            return KeyFactory.getInstance(keyType).generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException e) {
            throw new PawapaySignatureException(
                "Unable to parse PawaPay signing private key as PKCS8 " + keyType + " (convert it with "
                    + "'openssl pkcs8 -topk8 -nocrypt' if needed): " + e.getMessage(), e);
        }
    }

    /**
     * The four headers produced by {@link #sign}.
     *
     * @param contentDigest  the {@code Content-Digest} header value
     * @param signatureDate  the {@code Signature-Date} header value
     * @param signatureInput the {@code Signature-Input} header value
     * @param signature      the {@code Signature} header value
     */
    public record SignedHeaders(String contentDigest, String signatureDate, String signatureInput, String signature) {
    }
}
