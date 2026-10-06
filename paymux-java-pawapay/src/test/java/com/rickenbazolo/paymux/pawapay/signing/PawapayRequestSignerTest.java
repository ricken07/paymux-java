package com.rickenbazolo.paymux.pawapay.signing;

import com.rickenbazolo.paymux.pawapay.callback.PawapayCallbackRequest;
import com.rickenbazolo.paymux.pawapay.callback.PawapayCallbackSignatureVerifier;
import com.rickenbazolo.paymux.pawapay.exception.PawapaySignatureException;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PawapayRequestSigner}. Signed output is cross-verified with a real
 * {@link PawapayCallbackSignatureVerifier} (built with the paired public key), treating the
 * produced headers as if they were an inbound request signed the same way - this exercises the
 * exact same RFC 9421 base-string construction and signature check the existing, already tested
 * verifier uses for callbacks.
 */
@DisplayName("PawaPay Request Signer Tests")
class PawapayRequestSignerTest {

    private static final String KEY_ID = "CUSTOMER_TEST_KEY";
    private static final String AUTHORITY = "api.sandbox.pawapay.io";
    private static final String PATH = "/v2/deposits";
    private static final String CONTENT_TYPE = "application/json";
    private static final String BODY = "{\"depositId\":\"f4401bd2-1568-4140-bf2d-eb77d2b2b639\",\"amount\":\"15\"}";

    @Test
    @DisplayName("Should produce headers that verify against the paired public key (ECDSA P-256)")
    void shouldSignVerifiableWithEcdsaP256() throws Exception {
        var keyPair = generateEcKeyPair("secp256r1");
        var signer = new PawapayRequestSigner(KEY_ID, PawapaySignatureAlgorithms.ECDSA_P256_SHA256,
            keyPair.getPrivate(), Duration.ofSeconds(60));

        var signed = signer.sign("POST", AUTHORITY, PATH, CONTENT_TYPE, BODY.getBytes(StandardCharsets.UTF_8));

        assertVerifiable(signed, keyPair.getPublic());
    }

    @Test
    @DisplayName("Should produce headers that verify against the paired public key (ECDSA P-384)")
    void shouldSignVerifiableWithEcdsaP384() throws Exception {
        var keyPair = generateEcKeyPair("secp384r1");
        var signer = new PawapayRequestSigner(KEY_ID, PawapaySignatureAlgorithms.ECDSA_P384_SHA384,
            keyPair.getPrivate(), Duration.ofSeconds(60));

        var signed = signer.sign("POST", AUTHORITY, PATH, CONTENT_TYPE, BODY.getBytes(StandardCharsets.UTF_8));

        assertVerifiable(signed, keyPair.getPublic());
    }

    @Test
    @DisplayName("Should produce headers that verify against the paired public key (RSA v1.5 and PSS)")
    void shouldSignVerifiableWithRsa() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        var v15Signer = new PawapayRequestSigner(KEY_ID, PawapaySignatureAlgorithms.RSA_V1_5_SHA256,
            keyPair.getPrivate(), Duration.ofSeconds(60));
        assertVerifiable(v15Signer.sign("POST", AUTHORITY, PATH, CONTENT_TYPE, BODY.getBytes(StandardCharsets.UTF_8)), keyPair.getPublic());

        var pssSigner = new PawapayRequestSigner(KEY_ID, PawapaySignatureAlgorithms.RSA_PSS_SHA512,
            keyPair.getPrivate(), Duration.ofSeconds(60));
        assertVerifiable(pssSigner.sign("POST", AUTHORITY, PATH, CONTENT_TYPE, BODY.getBytes(StandardCharsets.UTF_8)), keyPair.getPublic());
    }

    @Test
    @DisplayName("Should reject verification when the body was tampered with after signing")
    void shouldFailVerificationOnTamperedBody() throws Exception {
        var keyPair = generateEcKeyPair("secp256r1");
        var signer = new PawapayRequestSigner(KEY_ID, PawapaySignatureAlgorithms.ECDSA_P256_SHA256,
            keyPair.getPrivate(), Duration.ofSeconds(60));

        var signed = signer.sign("POST", AUTHORITY, PATH, CONTENT_TYPE, BODY.getBytes(StandardCharsets.UTF_8));

        var callbackRequest = PawapayCallbackRequest.builder()
            .method("POST").authority(AUTHORITY).path(PATH)
            .header("Content-Type", CONTENT_TYPE)
            .header("Signature-Date", signed.signatureDate())
            .header("Content-Digest", signed.contentDigest())
            .header("Signature-Input", signed.signatureInput())
            .header("Signature", signed.signature())
            .body((BODY + "tampered").getBytes(StandardCharsets.UTF_8))
            .build();

        var verifier = verifierFor(KEY_ID, keyPair.getPublic());
        assertThat(verifier.isValid(callbackRequest)).isFalse();
    }

    @Test
    @DisplayName("Should structure the Content-Digest, Signature-Input and Signature headers correctly")
    void shouldStructureHeadersCorrectly() throws Exception {
        Instant now = Instant.parse("2024-05-02T15:36:45.000000Z");
        var keyPair = generateEcKeyPair("secp256r1");
        var signer = new PawapayRequestSigner(KEY_ID, PawapaySignatureAlgorithms.ECDSA_P256_SHA256,
            keyPair.getPrivate(), Duration.ofSeconds(60), Clock.fixed(now, ZoneOffset.UTC));

        var signed = signer.sign("POST", AUTHORITY, PATH, CONTENT_TYPE, BODY.getBytes(StandardCharsets.UTF_8));

        byte[] expectedDigest = MessageDigest.getInstance("SHA-256").digest(BODY.getBytes(StandardCharsets.UTF_8));
        assertThat(signed.contentDigest()).isEqualTo("sha-256=:" + Base64.getEncoder().encodeToString(expectedDigest) + ":");
        assertThat(signed.signatureDate()).isEqualTo(now.toString());
        assertThat(signed.signatureInput()).isEqualTo(
            "sig-pp=(\"@method\" \"@authority\" \"@path\" \"signature-date\" \"content-digest\" \"content-type\")"
                + ";alg=\"ecdsa-p256-sha256\";keyid=\"" + KEY_ID + "\";created=" + now.getEpochSecond()
                + ";expires=" + now.plusSeconds(60).getEpochSecond());
        assertThat(signed.signature()).startsWith("sig-pp=:").endsWith(":");

        Matcher matcher = Pattern.compile("sig-pp=:([A-Za-z0-9+/=]+):").matcher(signed.signature());
        assertThat(matcher.matches()).isTrue();
        byte[] signatureBytes = Base64.getDecoder().decode(matcher.group(1));
        assertThat(signatureBytes[0]).isEqualTo((byte) 0x30); // DER SEQUENCE, not raw P1363 r||s
    }

    @Test
    @DisplayName("Should reject an unsupported algorithm")
    void shouldRejectUnsupportedAlgorithm() throws Exception {
        var keyPair = generateEcKeyPair("secp256r1");
        assertThatThrownBy(() -> new PawapayRequestSigner("KEY", "hmac-sha256", keyPair.getPrivate(), Duration.ofSeconds(60)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unsupported");
    }

    @Test
    @DisplayName("Should parse a PEM PKCS8 private key and a raw base64 one, for EC and RSA")
    void shouldParsePrivateKeyFormats() throws Exception {
        var ecKeyPair = generateEcKeyPair("secp256r1");
        String pem = toPkcs8Pem(ecKeyPair.getPrivate());
        String base64 = Base64.getEncoder().encodeToString(ecKeyPair.getPrivate().getEncoded());

        PrivateKey fromPem = PawapayRequestSigner.parsePrivateKey(pem, PawapaySignatureAlgorithms.ECDSA_P256_SHA256);
        PrivateKey fromBase64 = PawapayRequestSigner.parsePrivateKey(base64, PawapaySignatureAlgorithms.ECDSA_P256_SHA256);
        PrivateKey fromEscapedPem = PawapayRequestSigner.parsePrivateKey(pem.replace("\n", "\\n"), PawapaySignatureAlgorithms.ECDSA_P256_SHA256);

        assertThat(fromPem.getEncoded()).isEqualTo(ecKeyPair.getPrivate().getEncoded());
        assertThat(fromBase64.getEncoded()).isEqualTo(ecKeyPair.getPrivate().getEncoded());
        assertThat(fromEscapedPem.getEncoded()).isEqualTo(ecKeyPair.getPrivate().getEncoded());

        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        PrivateKey rsaKey = generator.generateKeyPair().getPrivate();
        PrivateKey rsaFromPem = PawapayRequestSigner.parsePrivateKey(toPkcs8Pem(rsaKey), PawapaySignatureAlgorithms.RSA_PSS_SHA512);
        assertThat(rsaFromPem.getEncoded()).isEqualTo(rsaKey.getEncoded());
    }

    @Test
    @DisplayName("Should reject an unparsable private key")
    void shouldRejectUnparsablePrivateKey() {
        assertThatThrownBy(() -> PawapayRequestSigner.parsePrivateKey("not base64 at all!!", PawapaySignatureAlgorithms.ECDSA_P256_SHA256))
            .isInstanceOf(PawapaySignatureException.class);
    }

    // ---- helpers ----------------------------------------------------------------------------

    private static void assertVerifiable(PawapayRequestSigner.SignedHeaders signed, PublicKey publicKey) {
        var callbackRequest = PawapayCallbackRequest.builder()
            .method("POST")
            .authority(AUTHORITY)
            .path(PATH)
            .header("Content-Type", CONTENT_TYPE)
            .header("Signature-Date", signed.signatureDate())
            .header("Content-Digest", signed.contentDigest())
            .header("Signature-Input", signed.signatureInput())
            .header("Signature", signed.signature())
            .body(BODY.getBytes(StandardCharsets.UTF_8))
            .build();

        var verifier = verifierFor(KEY_ID, publicKey);
        verifier.verify(callbackRequest); // throws PawapaySignatureException if invalid
        assertThat(verifier.isValid(callbackRequest)).isTrue();
    }

    private static PawapayCallbackSignatureVerifier verifierFor(String keyId, PublicKey publicKey) {
        return new PawapayCallbackSignatureVerifier(
            () -> List.of(new PawapayPublicKey(keyId, toX509Pem(publicKey))),
            Duration.ofHours(1), Duration.ofSeconds(5));
    }

    private static KeyPair generateEcKeyPair(String curve) throws Exception {
        var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec(curve));
        return generator.generateKeyPair();
    }

    private static String toPkcs8Pem(PrivateKey key) {
        return "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(key.getEncoded())
            + "\n-----END PRIVATE KEY-----";
    }

    private static String toX509Pem(PublicKey key) {
        return "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(key.getEncoded())
            + "\n-----END PUBLIC KEY-----";
    }
}
