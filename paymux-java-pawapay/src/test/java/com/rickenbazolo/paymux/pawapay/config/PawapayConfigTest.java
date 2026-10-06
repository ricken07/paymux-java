package com.rickenbazolo.paymux.pawapay.config;

import com.rickenbazolo.paymux.core.config.ConfigurationLoader;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.signing.PawapaySignatureAlgorithms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for PawaPay configuration loading.
 */
@DisplayName("PawaPay Configuration Tests")
class PawapayConfigTest {

    @Test
    @DisplayName("Should build config with builder pattern")
    void shouldBuildConfigWithBuilder() {
        PawapayConfig config = PawapayConfig.builder()
            .apiToken("test-token")
            .production(false)
            .build();

        assertThat(config.getApiToken()).isEqualTo("test-token");
        assertThat(config.getApiKey()).isEqualTo("test-token");
        assertThat(config.getApiSecret()).isEqualTo("test-token");
        assertThat(config.isProduction()).isFalse();
        assertThat(config.getBaseUrl()).isEqualTo(PawapayConfig.SANDBOX_URL);
    }

    @Test
    @DisplayName("Should load config from YAML file")
    void shouldLoadConfigFromYamlFile() {
        PawapayConfig config = PawapayConfig.fromPropertiesFile("paymux-test.yml");

        assertThat(config.getApiToken()).isEqualTo("test-api-token-yaml");
        assertThat(config.isProduction()).isFalse();
        assertThat(config.getBaseUrl()).isEqualTo(PawapayConfig.SANDBOX_URL);
        assertThat(config.getConnectionTimeout()).isEqualTo(10000);
        assertThat(config.getRequestTimeout()).isEqualTo(20000);
        assertThat(config.getPublicKeyCacheTtl()).isEqualTo(Duration.ofSeconds(600));
        assertThat(config.getSignatureClockSkew()).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("Should load config from properties file")
    void shouldLoadConfigFromPropertiesFile() {
        PawapayConfig config = PawapayConfig.fromPropertiesFile("paymux-test.properties");

        assertThat(config.getApiToken()).isEqualTo("test-api-token-properties");
        assertThat(config.isProduction()).isFalse();
        assertThat(config.getBaseUrl()).isEqualTo("https://pawapay.example.test/");
        assertThat(config.getConnectionTimeout()).isEqualTo(10000);
        assertThat(config.getRequestTimeout()).isEqualTo(20000);
        assertThat(config.getPublicKeyCacheTtl()).isEqualTo(Duration.ofSeconds(3600));
        assertThat(config.getSignatureClockSkew()).isEqualTo(Duration.ofSeconds(120));
    }

    @Test
    @DisplayName("Should load config from a Properties object")
    void shouldLoadConfigFromPropertiesObject() {
        Properties properties = new Properties();
        properties.setProperty(PawapayConfigProperties.API_TOKEN, "token");
        properties.setProperty(PawapayConfigProperties.PRODUCTION, "true");

        PawapayConfig config = PawapayConfig.fromProperties(properties);

        assertThat(config.getApiToken()).isEqualTo("token");
        assertThat(config.isProduction()).isTrue();
        assertThat(config.getBaseUrl()).isEqualTo(PawapayConfig.PRODUCTION_URL);
    }

    @Test
    @DisplayName("Should use default values when optional properties are missing")
    void shouldUseDefaultValues() {
        PawapayConfig config = PawapayConfig.builder().apiToken("token").build();

        assertThat(config.isProduction()).isFalse();
        assertThat(config.getConnectionTimeout()).isEqualTo(30000);
        assertThat(config.getRequestTimeout()).isEqualTo(60000);
        assertThat(config.getPublicKeyCacheTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(config.getSignatureClockSkew()).isEqualTo(Duration.ofMinutes(2));
        assertThat(config.getHttpClient()).isNull();
    }

    @Test
    @DisplayName("Should set production URL when production is true")
    void shouldSetProductionUrl() {
        PawapayConfig config = PawapayConfig.builder().apiToken("token").production(true).build();

        assertThat(config.isProduction()).isTrue();
        assertThat(config.getBaseUrl()).isEqualTo("https://api.pawapay.io/");
    }

    @Test
    @DisplayName("Should throw exception when API token is missing")
    void shouldThrowExceptionWhenApiTokenMissing() {
        assertThatThrownBy(() -> PawapayConfig.builder().build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API Token is required");

        assertThatThrownBy(() -> PawapayConfig.builder().apiToken("  ").build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API Token is required");
    }

    @Test
    @DisplayName("Should throw exception when required property is missing")
    void shouldThrowExceptionWhenRequiredPropertyMissing() {
        assertThatThrownBy(() -> PawapayConfig.fromProperties(new Properties()))
            .isInstanceOf(ConfigurationLoader.ConfigurationException.class)
            .hasMessageContaining(PawapayConfigProperties.API_TOKEN);
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent file")
    void shouldThrowExceptionWhenFileNotFound() {
        assertThatThrownBy(() -> PawapayConfig.fromPropertiesFile("non-existent-file.properties"))
            .isInstanceOf(ConfigurationLoader.ConfigurationException.class);
    }

    @Test
    @DisplayName("Should ensure base URL ends with slash")
    void shouldEnsureBaseUrlEndsWithSlash() {
        PawapayConfig config = PawapayConfig.builder()
            .apiToken("token")
            .baseUrl("https://example.com")
            .build();

        assertThat(config.getBaseUrl()).isEqualTo("https://example.com/");
    }

    @Test
    @DisplayName("Should allow custom HTTP client")
    void shouldAllowCustomHttpClient() {
        var customClient = Mockito.mock(PaymuxHttpClient.class);

        PawapayConfig config = PawapayConfig.builder()
            .apiToken("token")
            .httpClient(customClient)
            .build();

        assertThat(config.getHttpClient()).isSameAs(customClient);
    }

    @Test
    @DisplayName("Should set custom timeouts and signature settings")
    void shouldSetCustomTimeouts() {
        PawapayConfig config = PawapayConfig.builder()
            .apiToken("token")
            .connectionTimeout(5000)
            .requestTimeout(15000)
            .publicKeyCacheTtlSeconds(30)
            .signatureClockSkew(Duration.ofSeconds(5))
            .build();

        assertThat(config.getConnectionTimeout()).isEqualTo(5000);
        assertThat(config.getRequestTimeout()).isEqualTo(15000);
        assertThat(config.getPublicKeyCacheTtl()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.getSignatureClockSkew()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should reject invalid timeouts")
    void shouldRejectInvalidTimeouts() {
        assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").connectionTimeout(0).build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Connection timeout");
        assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestTimeout(-1).build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Request timeout");
    }

    @Nested
    @DisplayName("Outbound request signing")
    class RequestSigningTests {

        @TempDir
        Path tempDir;

        private KeyPair keyPair;

        private KeyPair ecKeyPair() throws Exception {
            if (keyPair == null) {
                var generator = KeyPairGenerator.getInstance("EC");
                generator.initialize(new ECGenParameterSpec("secp256r1"));
                keyPair = generator.generateKeyPair();
            }
            return keyPair;
        }

        private String pemOf(KeyPair pair) {
            return "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(pair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";
        }

        @Test
        @DisplayName("Should be disabled and absent by default")
        void shouldBeDisabledByDefault() {
            PawapayConfig config = PawapayConfig.builder().apiToken("token").build();

            assertThat(config.getRequestSigner()).isNull();
        }

        @Test
        @DisplayName("Should build a signer from a direct PrivateKey object")
        void shouldBuildFromDirectPrivateKey() throws Exception {
            PawapayConfig config = PawapayConfig.builder()
                .apiToken("token")
                .requestSigningEnabled(true)
                .signingKeyId("CUSTOMER_TEST_KEY")
                .signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKey(ecKeyPair().getPrivate())
                .build();

            assertThat(config.getRequestSigner()).isNotNull();
        }

        @Test
        @DisplayName("Should build a signer from an inline PEM private key")
        void shouldBuildFromInlinePem() throws Exception {
            PawapayConfig config = PawapayConfig.builder()
                .apiToken("token")
                .requestSigningEnabled(true)
                .signingKeyId("CUSTOMER_TEST_KEY")
                .signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKeyPem(pemOf(ecKeyPair()))
                .build();

            assertThat(config.getRequestSigner()).isNotNull();
        }

        @Test
        @DisplayName("Should build a signer from a private key file path")
        void shouldBuildFromPrivateKeyFile() throws Exception {
            Path keyFile = tempDir.resolve("private-key.pem");
            Files.writeString(keyFile, pemOf(ecKeyPair()));

            PawapayConfig config = PawapayConfig.builder()
                .apiToken("token")
                .requestSigningEnabled(true)
                .signingKeyId("CUSTOMER_TEST_KEY")
                .signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKeyPath(keyFile.toString())
                .build();

            assertThat(config.getRequestSigner()).isNotNull();
        }

        @Test
        @DisplayName("Should load signing configuration from properties")
        void shouldLoadFromProperties() throws Exception {
            Path keyFile = tempDir.resolve("private-key.pem");
            Files.writeString(keyFile, pemOf(ecKeyPair()));

            Properties properties = new Properties();
            properties.setProperty(PawapayConfigProperties.API_TOKEN, "token");
            properties.setProperty(PawapayConfigProperties.SIGNING_ENABLED, "true");
            properties.setProperty(PawapayConfigProperties.SIGNING_KEY_ID, "CUSTOMER_TEST_KEY");
            properties.setProperty(PawapayConfigProperties.SIGNING_ALGORITHM, PawapaySignatureAlgorithms.ECDSA_P256_SHA256);
            properties.setProperty(PawapayConfigProperties.SIGNING_PRIVATE_KEY_PATH, keyFile.toString());

            PawapayConfig config = PawapayConfig.fromProperties(properties);

            assertThat(config.getRequestSigner()).isNotNull();
        }

        @Test
        @DisplayName("Should require a key id, an algorithm and a private key when enabled")
        void shouldRequireFieldsWhenEnabled() {
            assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestSigningEnabled(true).build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("key id");

            assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestSigningEnabled(true)
                .signingKeyId("KEY").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("algorithm");

            assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestSigningEnabled(true)
                .signingKeyId("KEY").signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256).build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("private key");
        }

        @Test
        @DisplayName("Should not validate anything when disabled, even with fields left unset")
        void shouldSkipValidationWhenDisabled() {
            PawapayConfig config = PawapayConfig.builder().apiToken("token").requestSigningEnabled(false).build();

            assertThat(config.getRequestSigner()).isNull();
        }

        @Test
        @DisplayName("Should reject an unsupported algorithm")
        void shouldRejectUnsupportedAlgorithm() throws Exception {
            assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestSigningEnabled(true)
                .signingKeyId("KEY").signingAlgorithm("hmac-sha256").signingPrivateKey(ecKeyPair().getPrivate()).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unsupported");
        }

        @Test
        @DisplayName("Should reject specifying more than one private key source")
        void shouldRejectMultipleKeySources() throws Exception {
            assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestSigningEnabled(true)
                .signingKeyId("KEY").signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKey(ecKeyPair().getPrivate())
                .signingPrivateKeyPem("also-set")
                .build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("exactly one");
        }

        @Test
        @DisplayName("Should reject an unreadable private key file")
        void shouldRejectUnreadableKeyFile() {
            assertThatThrownBy(() -> PawapayConfig.builder().apiToken("token").requestSigningEnabled(true)
                .signingKeyId("KEY").signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKeyPath(tempDir.resolve("does-not-exist.pem").toString())
                .build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Unable to read");
        }

        @Test
        @DisplayName("Should apply a custom validity window")
        void shouldApplyCustomValidity() throws Exception {
            PawapayConfig config = PawapayConfig.builder()
                .apiToken("token")
                .requestSigningEnabled(true)
                .signingKeyId("KEY")
                .signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKey(ecKeyPair().getPrivate())
                .signingValiditySeconds(30)
                .build();

            var signed = config.getRequestSigner().sign(
                "POST", "api.example.com", "/v2/deposits", "application/json", "{}".getBytes(StandardCharsets.UTF_8));
            assertThat(signed.signatureInput()).contains("expires=");
        }
    }
}
