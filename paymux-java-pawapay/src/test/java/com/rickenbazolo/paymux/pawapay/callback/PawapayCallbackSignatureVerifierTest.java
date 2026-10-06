package com.rickenbazolo.paymux.pawapay.callback;

import com.rickenbazolo.paymux.pawapay.exception.PawapaySignatureException;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the RFC 9421 callback signature verifier, using real key pairs and signatures.
 */
@DisplayName("PawaPay Callback Signature Verifier Tests")
class PawapayCallbackSignatureVerifierTest {

    private static final String KEY_ID = "HTTP_EC_P256_KEY:1";
    private static final String LABEL = "sig-pp";
    private static final long CREATED = 1714653405L;
    private static final long EXPIRES = 1714653465L;
    private static final Instant NOW = Instant.ofEpochSecond(1714653430L);
    private static final String COMPONENTS = "(\"@method\" \"@authority\" \"@path\" \"signature-date\" \"content-digest\" \"content-type\")";
    private static final String BODY = "{\"depositId\":\"f4401bd2-1568-4140-bf2d-eb77d2b2b639\",\"status\":\"COMPLETED\",\"amount\":\"15\"}";
    private static final String AUTHORITY = "payments.example.com";
    private static final String PATH = "/callbacks/pawapay/deposits";
    private static final String SIGNATURE_DATE = "2024-05-02T12:36:45Z";

    private KeyPair ecKeyPair;
    private CountingKeySource keySource;
    private PawapayCallbackSignatureVerifier verifier;

    @BeforeEach
    void setUp() throws Exception {
        var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        ecKeyPair = generator.generateKeyPair();
        keySource = new CountingKeySource(List.of(new PawapayPublicKey(KEY_ID, pem(ecKeyPair))));
        verifier = verifierAt(NOW);
    }

    private PawapayCallbackSignatureVerifier verifierAt(Instant now) {
        return new PawapayCallbackSignatureVerifier(keySource, Duration.ofHours(1), Duration.ofSeconds(120),
            Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Should verify a valid ECDSA P-256 signature (DER encoded)")
    void shouldVerifyValidDerSignature() throws Exception {
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        verifier.verify(request);

        assertThat(verifier.isValid(request)).isTrue();
        assertThat(keySource.calls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should verify a valid ECDSA P-256 signature (raw r||s encoded)")
    void shouldVerifyValidP1363Signature() throws Exception {
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSAinP1363Format", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        verifier.verify(request);
    }

    @Test
    @DisplayName("Should verify RSA signatures")
    void shouldVerifyRsaSignatures() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair rsa = generator.generateKeyPair();
        keySource = new CountingKeySource(List.of(new PawapayPublicKey("RSA_KEY:1", pem(rsa))));
        verifier = verifierAt(NOW);

        verifier.verify(signedRequest(BODY, "SHA256withRSA", rsa.getPrivate(), "rsa-v1_5-sha256", "RSA_KEY:1"));
        verifier.verify(signedRequest(BODY, "RSASSA-PSS", rsa.getPrivate(), "rsa-pss-sha512", "RSA_KEY:1"));
    }

    @Test
    @DisplayName("Should accept sha-512 content digests and mixed-case authority")
    void shouldAcceptSha512DigestAndMixedCaseAuthority() throws Exception {
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID,
            "sha-512", "Payments.Example.COM");

        verifier.verify(request);
    }

    @Test
    @DisplayName("Should reject a tampered body before looking at keys")
    void shouldRejectTamperedBody() throws Exception {
        PawapayCallbackRequest signed = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);
        PawapayCallbackRequest tampered = PawapayCallbackRequest.builder()
            .method(signed.method()).authority(signed.authority()).path(signed.path())
            .headers(signed.headers())
            .body(BODY.replace("15", "1500"))
            .build();

        assertThatThrownBy(() -> verifier.verify(tampered))
            .isInstanceOf(PawapaySignatureException.class)
            .hasMessageContaining("digest does not match");
        assertThat(keySource.calls.get()).isZero();
        assertThat(verifier.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Should reject a signature made with another key")
    void shouldRejectWrongKey() throws Exception {
        var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair other = generator.generateKeyPair();
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", other.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        assertThatThrownBy(() -> verifier.verify(request))
            .isInstanceOf(PawapaySignatureException.class)
            .hasMessageContaining("signature is invalid");
    }

    @Test
    @DisplayName("Should reject a signed header that was modified")
    void shouldRejectModifiedSignedHeader() throws Exception {
        PawapayCallbackRequest signed = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);
        PawapayCallbackRequest modified = PawapayCallbackRequest.builder()
            .method(signed.method()).authority(signed.authority()).path("/callbacks/pawapay/payouts")
            .headers(signed.headers())
            .body(BODY)
            .build();

        assertThatThrownBy(() -> verifier.verify(modified))
            .isInstanceOf(PawapaySignatureException.class)
            .hasMessageContaining("signature is invalid");
    }

    @Test
    @DisplayName("Should reject missing signature headers")
    void shouldRejectMissingHeaders() throws Exception {
        PawapayCallbackRequest signed = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        assertThatThrownBy(() -> verifier.verify(without(signed, "Content-Digest")))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Missing Content-Digest");
        assertThatThrownBy(() -> verifier.verify(without(signed, "Signature")))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Missing Signature header");
        assertThatThrownBy(() -> verifier.verify(without(signed, "Signature-Input")))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Missing Signature-Input");
        assertThatThrownBy(() -> verifier.verify(without(signed, "Signature-Date")))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("signature-date");
    }

    @Test
    @DisplayName("Should reject an unsupported digest algorithm")
    void shouldRejectUnsupportedDigest() throws Exception {
        PawapayCallbackRequest signed = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);
        PawapayCallbackRequest request = with(signed, "Content-Digest", "md5=:abcd:");

        assertThatThrownBy(() -> verifier.verify(request))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Unsupported callback digest algorithm");
    }

    @Test
    @DisplayName("Should reject expired and future signatures within the clock skew")
    void shouldEnforceValidityWindow() throws Exception {
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        // 100 seconds after expiry: within the 120 seconds skew
        verifierAt(Instant.ofEpochSecond(EXPIRES + 100)).verify(request);

        assertThatThrownBy(() -> verifierAt(Instant.ofEpochSecond(EXPIRES + 140)).verify(request))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("expired");
        assertThatThrownBy(() -> verifierAt(Instant.ofEpochSecond(CREATED - 140)).verify(request))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("created in the future");
    }

    @Test
    @DisplayName("Should reject a Signature header whose label does not match Signature-Input")
    void shouldRejectLabelMismatch() throws Exception {
        PawapayCallbackRequest signed = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);
        String signature = signed.header("Signature").orElseThrow().replace(LABEL + "=", "sig-other=");

        assertThatThrownBy(() -> verifier.verify(with(signed, "Signature", signature)))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("no entry for label 'sig-pp'");
    }

    @Test
    @DisplayName("Should reject an unsupported signature algorithm")
    void shouldRejectUnsupportedAlgorithm() throws Exception {
        PawapayCallbackRequest signed = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);
        String input = signed.header("Signature-Input").orElseThrow().replace("ecdsa-p256-sha256", "hmac-sha256");

        assertThatThrownBy(() -> verifier.verify(with(signed, "Signature-Input", input)))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Unsupported signature algorithm");
    }

    @Test
    @DisplayName("Should refresh keys once for an unknown key id and then fail")
    void shouldRefreshForUnknownKeyId() throws Exception {
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", "HTTP_EC_P256_KEY:2");

        assertThatThrownBy(() -> verifier.verify(request))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Unknown PawaPay callback signing key");
        assertThat(keySource.calls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should cache public keys and reload after clearCache")
    void shouldCachePublicKeys() throws Exception {
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        verifier.verify(request);
        verifier.verify(request);
        assertThat(keySource.calls.get()).isEqualTo(1);

        verifier.clearCache();
        verifier.verify(request);
        assertThat(keySource.calls.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should report key loading failures")
    void shouldReportKeyLoadingFailure() throws Exception {
        keySource = new CountingKeySource(null);
        verifier = verifierAt(NOW);
        PawapayCallbackRequest request = signedRequest(BODY, "SHA256withECDSA", ecKeyPair.getPrivate(), "ecdsa-p256-sha256", KEY_ID);

        assertThatThrownBy(() -> verifier.verify(request))
            .isInstanceOf(PawapaySignatureException.class).hasMessageContaining("Unable to load PawaPay public keys");
    }

    @Test
    @DisplayName("Should parse the Signature-Input example from the PawaPay documentation")
    void shouldParseDocumentationExample() {
        String header = "sig-pp=(\"@method\" \"@authority\" \"@path\" \"signature-date\" \"content-digest\" \"content-type\")"
            + ";alg=\"ecdsa-p256-sha256\";keyid=\"CUSTOMER_TEST_KEY\";created=1714653405;expires=1714653465";

        var input = PawapayCallbackSignatureVerifier.parseSignatureInput(header);

        assertThat(input.label()).isEqualTo("sig-pp");
        assertThat(input.components()).containsExactly("@method", "@authority", "@path", "signature-date", "content-digest", "content-type");
        assertThat(input.algorithm()).isEqualTo("ecdsa-p256-sha256");
        assertThat(input.keyId()).isEqualTo("CUSTOMER_TEST_KEY");
        assertThat(input.created()).isEqualTo(1714653405L);
        assertThat(input.expires()).isEqualTo(1714653465L);
        assertThat(input.parameters()).isEqualTo(header.substring("sig-pp=".length()));

        byte[] signature = PawapayCallbackSignatureVerifier.extractSignature(
            "sig-pp=:MEQCIHoWKI71ADMmqwtwW48CHgfbDWdVItVMNlXTFJjoxmEDAiBTY30Le4wQd3RXqvmYubVwrxuP7Tz1SeZcnsNdHqjJDg==:", "sig-pp");
        assertThat(signature).hasSize(70);
        assertThat(signature[0]).isEqualTo((byte) 0x30);
    }

    // ---- helpers --------------------------------------------------------------------------

    private PawapayCallbackRequest signedRequest(String body, String jcaAlgorithm, PrivateKey privateKey,
                                                 String alg, String keyId) throws Exception {
        return signedRequest(body, jcaAlgorithm, privateKey, alg, keyId, "sha-256", AUTHORITY);
    }

    private PawapayCallbackRequest signedRequest(String body, String jcaAlgorithm, PrivateKey privateKey,
                                                 String alg, String keyId, String digestAlgorithm,
                                                 String authority) throws Exception {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String digestValue = digestAlgorithm + "=:" + Base64.getEncoder().encodeToString(
            MessageDigest.getInstance(digestAlgorithm.equals("sha-256") ? "SHA-256" : "SHA-512").digest(bodyBytes)) + ":";
        String parameters = COMPONENTS + ";alg=\"" + alg + "\";keyid=\"" + keyId + "\";created=" + CREATED + ";expires=" + EXPIRES;

        String base = "\"@method\": POST\n"
            + "\"@authority\": " + authority.toLowerCase() + "\n"
            + "\"@path\": " + PATH + "\n"
            + "\"signature-date\": " + SIGNATURE_DATE + "\n"
            + "\"content-digest\": " + digestValue + "\n"
            + "\"content-type\": application/json\n"
            + "\"@signature-params\": " + parameters;

        Signature signer = Signature.getInstance(jcaAlgorithm);
        if ("RSASSA-PSS".equals(jcaAlgorithm)) {
            signer.setParameter(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
        }
        signer.initSign(privateKey);
        signer.update(base.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());

        return PawapayCallbackRequest.builder()
            .method("POST")
            .authority(authority)
            .path(PATH)
            .header("Content-Type", "application/json")
            .header("Signature-Date", SIGNATURE_DATE)
            .header("Content-Digest", digestValue)
            .header("Signature-Input", LABEL + "=" + parameters)
            .header("Signature", LABEL + "=:" + signature + ":")
            .header("User-Agent", "pawapay-callbacks")
            .body(bodyBytes)
            .build();
    }

    private static PawapayCallbackRequest without(PawapayCallbackRequest request, String header) {
        var builder = PawapayCallbackRequest.builder()
            .method(request.method()).authority(request.authority()).path(request.path()).body(request.body());
        request.headers().forEach((name, value) -> {
            if (!name.equalsIgnoreCase(header)) {
                builder.header(name, value);
            }
        });
        return builder.build();
    }

    private static PawapayCallbackRequest with(PawapayCallbackRequest request, String header, String value) {
        return PawapayCallbackRequest.builder()
            .method(request.method()).authority(request.authority()).path(request.path()).body(request.body())
            .headers(request.headers())
            .header(header, value)
            .build();
    }

    private static String pem(KeyPair keyPair) {
        return "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(keyPair.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    }

    private static final class CountingKeySource implements PawapayCallbackSignatureVerifier.PublicKeySource {
        private final List<PawapayPublicKey> keys;
        private final AtomicInteger calls = new AtomicInteger();

        private CountingKeySource(List<PawapayPublicKey> keys) {
            this.keys = keys != null ? new ArrayList<>(keys) : null;
        }

        @Override
        public List<PawapayPublicKey> fetchPublicKeys() {
            calls.incrementAndGet();
            if (keys == null) {
                throw new IllegalStateException("public key endpoint unavailable");
            }
            return keys;
        }
    }
}
