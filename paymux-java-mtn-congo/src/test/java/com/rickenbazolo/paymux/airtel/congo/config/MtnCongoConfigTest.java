package com.rickenbazolo.paymux.airtel.congo.config;

import com.rickenbazolo.paymux.core.config.ConfigurationLoader;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.airtel.congo.MtnCongoConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MTN Congo configuration loading.
 *
 * @author Ricken Bazolo
 */
@DisplayName("MTN Congo Configuration Tests")
class MtnCongoConfigTest {

    @Test
    @DisplayName("Should build config with builder pattern")
    void shouldBuildConfigWithBuilder() {
        // Given
        String apiUser = "test-api-user";
        String apiKey = "test-api-key";
        String subscriptionKey = "test-subscription-key";

        // When
        MtnCongoConfig config = MtnCongoConfig.builder()
            .apiUser(apiUser)
            .apiKey(apiKey)
            .subscriptionKey(subscriptionKey)
            .environment("sandbox")
            .production(false)
            .build();

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getApiUser()).isEqualTo(apiUser);
        assertThat(config.getApiKey()).isEqualTo(apiKey);
        assertThat(config.getSubscriptionKey()).isEqualTo(subscriptionKey);
        assertThat(config.getEnvironment()).isEqualTo("sandbox");
        assertThat(config.isProduction()).isFalse();
        assertThat(config.getBaseUrl()).contains("sandbox");
    }

    @Test
    @DisplayName("Should load config from YAML file")
    void shouldLoadConfigFromYamlFile() {
        // When
        MtnCongoConfig config = MtnCongoConfig.fromPropertiesFile("paymux-test.yml");

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getApiUser()).isEqualTo("test-api-user-uuid");
        assertThat(config.getApiKey()).isEqualTo("test-api-key");
        assertThat(config.getSubscriptionKey()).isEqualTo("test-subscription-key");
        assertThat(config.getEnvironment()).isEqualTo("sandbox");
        assertThat(config.isProduction()).isFalse();
        assertThat(config.getConnectionTimeout()).isEqualTo(10000);
        assertThat(config.getRequestTimeout()).isEqualTo(20000);
        assertThat(config.getCallbackUrl()).isEqualTo("https://webhook.site/test-callback");
    }

    @Test
    @DisplayName("Should load config from properties file")
    void shouldLoadConfigFromPropertiesFile() {
        // When
        MtnCongoConfig config = MtnCongoConfig.fromPropertiesFile("paymux-test.properties");

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getApiUser()).isEqualTo("test-api-user-uuid-properties");
        assertThat(config.getApiKey()).isEqualTo("test-api-key-properties");
        assertThat(config.getSubscriptionKey()).isEqualTo("test-subscription-key-properties");
        assertThat(config.getEnvironment()).isEqualTo("sandbox");
        assertThat(config.isProduction()).isFalse();
    }

    @Test
    @DisplayName("Should use default values when optional properties are missing")
    void shouldUseDefaultValues() {
        // When
        MtnCongoConfig config = MtnCongoConfig.builder()
            .apiUser("test-user")
            .apiKey("test-key")
            .subscriptionKey("test-subscription")
            .build();

        // Then
        assertThat(config.getEnvironment()).isEqualTo("sandbox");
        assertThat(config.isProduction()).isFalse();
        assertThat(config.getConnectionTimeout()).isEqualTo(30000);
        assertThat(config.getRequestTimeout()).isEqualTo(60000);
        assertThat(config.getCallbackUrl()).isNull();
    }

    @Test
    @DisplayName("Should set production URL when production is true")
    void shouldSetProductionUrl() {
        // When
        MtnCongoConfig config = MtnCongoConfig.builder()
            .apiUser("test-user")
            .apiKey("test-key")
            .subscriptionKey("test-subscription")
            .production(true)
            .build();

        // Then
        assertThat(config.isProduction()).isTrue();
        assertThat(config.getBaseUrl()).contains("ericssonbasicapi1");
    }

    @Test
    @DisplayName("Should throw exception when API user is missing")
    void shouldThrowExceptionWhenApiUserMissing() {
        // When & Then
        assertThatThrownBy(() ->
            MtnCongoConfig.builder()
                .apiKey("test-key")
                .subscriptionKey("test-subscription")
                .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("API User is required");
    }

    @Test
    @DisplayName("Should throw exception when API key is missing")
    void shouldThrowExceptionWhenApiKeyMissing() {
        // When & Then
        assertThatThrownBy(() ->
            MtnCongoConfig.builder()
                .apiUser("test-user")
                .subscriptionKey("test-subscription")
                .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("API Key is required");
    }

    @Test
    @DisplayName("Should throw exception when subscription key is missing")
    void shouldThrowExceptionWhenSubscriptionKeyMissing() {
        // When & Then
        assertThatThrownBy(() ->
            MtnCongoConfig.builder()
                .apiUser("test-user")
                .apiKey("test-key")
                .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Subscription Key is required");
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent file")
    void shouldThrowExceptionWhenFileNotFound() {
        // When & Then
        assertThatThrownBy(() ->
            MtnCongoConfig.fromPropertiesFile("non-existent-file.yml")
        )
        .isInstanceOf(ConfigurationLoader.ConfigurationException.class)
        .hasMessageContaining("Failed to parse YAML file");
    }

    @Test
    @DisplayName("Should ensure base URL ends with slash")
    void shouldEnsureBaseUrlEndsWithSlash() {
        // When
        MtnCongoConfig config = MtnCongoConfig.builder()
            .apiUser("test-user")
            .apiKey("test-key")
            .subscriptionKey("test-subscription")
            .baseUrl("https://example.com")
            .build();

        // Then
        assertThat(config.getBaseUrl()).endsWith("/");
    }

    @Test
    @DisplayName("Should allow custom HTTP client")
    void shouldAllowCustomHttpClient() {
        // Given
        var customClient = Mockito.mock(PaymuxHttpClient.class);

        // When
        MtnCongoConfig config = MtnCongoConfig.builder()
            .apiUser("test-user")
            .apiKey("test-key")
            .subscriptionKey("test-subscription")
            .httpClient(customClient)
            .build();

        // Then
        assertThat(config.getHttpClient()).isSameAs(customClient);
    }

    @Test
    @DisplayName("Should set custom timeouts")
    void shouldSetCustomTimeouts() {
        // When
        MtnCongoConfig config = MtnCongoConfig.builder()
            .apiUser("test-user")
            .apiKey("test-key")
            .subscriptionKey("test-subscription")
            .connectionTimeout(5000)
            .requestTimeout(15000)
            .build();

        // Then
        assertThat(config.getConnectionTimeout()).isEqualTo(5000);
        assertThat(config.getRequestTimeout()).isEqualTo(15000);
    }
}
