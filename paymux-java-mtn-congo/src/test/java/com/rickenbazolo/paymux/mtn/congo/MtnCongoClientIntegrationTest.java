package com.rickenbazolo.paymux.mtn.congo;

import com.rickenbazolo.paymux.core.exception.TransferException;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;
import com.rickenbazolo.paymux.mtn.congo.collection.model.MtnRequestToPay;
import com.rickenbazolo.paymux.mtn.congo.model.MtnTransactionStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for MTN Congo client.
 * <p>
 * These tests require real MTN API credentials to run.
 * Set the following environment variables to enable these tests:
 * <ul>
 *   <li>MTN_CONGO_API_USER - Your MTN API User UUID</li>
 *   <li>MTN_CONGO_API_KEY - Your MTN API Key</li>
 *   <li>MTN_CONGO_SUBSCRIPTION_KEY - Your MTN Subscription Key</li>
 * </ul>
 * </p>
 * <p>
 * To run these tests:
 * <pre>
 * export MTN_CONGO_API_USER=your-api-user
 * export MTN_CONGO_API_KEY=your-api-key
 * export MTN_CONGO_SUBSCRIPTION_KEY=your-subscription-key
 * mvn test -Dtest=MtnCongoClientIntegrationTest
 * </pre>
 * </p>
 *
 * @author Ricken Bazolo
 */
@DisplayName("MTN Congo Client Integration Tests")
@Tag("integration")
class MtnCongoClientIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MtnCongoClientIntegrationTest.class);

    private MtnCongoClient client;

    @BeforeEach
    @EnabledIfEnvironmentVariable(named = "MTN_CONGO_API_USER", matches = ".+")
    void setUp() {
        // Load configuration from environment variables for real integration tests
        var config = MtnCongoConfig.fromPropertiesFile("paymux-test.yml");
        client = new MtnCongoClient(config);
    }

    @Test
    @DisplayName("Should create client with config loaded from YAML")
    void shouldCreateClientWithYamlConfig() {
        // When
        MtnCongoConfig yamlConfig = MtnCongoConfig.fromPropertiesFile("paymux-test.yml");
        MtnCongoClient yamlClient = new MtnCongoClient(yamlConfig);

        // Then
        assertThat(yamlClient).isNotNull();
        assertThat(yamlClient.getConfig()).isNotNull();
        assertThat(yamlClient.getConfig().getApiUser()).isEqualTo("test-api-user-uuid");
    }

    @Test
    @DisplayName("Should create client with config loaded from properties")
    void shouldCreateClientWithPropertiesConfig() {
        // When
        MtnCongoConfig propsConfig = MtnCongoConfig.fromPropertiesFile("paymux-test.properties");
        MtnCongoClient propsClient = new MtnCongoClient(propsConfig);

        // Then
        assertThat(propsClient).isNotNull();
        assertThat(propsClient.getConfig()).isNotNull();
        assertThat(propsClient.getConfig().getApiUser()).isEqualTo("test-api-user-uuid-properties");
    }

    @Test
    @DisplayName("Should validate request before sending")
    void shouldValidateRequestBeforeSending() {
        // When & Then - Invalid request missing payer
        assertThatThrownBy(() -> {
            MtnRequestToPay.builder()
                .amount("1000")
                .currency("XAF")
                .externalId(UUID.randomUUID().toString())
                // Missing payer
                .build();
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Payer is required");
    }

    @Test
    @DisplayName("Should validate currency is XAF")
    void shouldValidateCurrencyIsXAF() {
        // Given
        MtnCongoConfig testConfig = MtnCongoConfig.fromPropertiesFile("paymux-test.yml");
        MtnCongoClient testClient = new MtnCongoClient(testConfig);

        // Invalid request - wrong currency
        MtnRequestToPay invalidRequest = MtnRequestToPay.builder()
            .amount("1000")
            .currency("USD") // Wrong currency
            .externalId(UUID.randomUUID().toString())
            .payerPhone("069999999") // Valid Congo phone number (9 digits)
            .build();

        // When & Then
        assertThatThrownBy(() -> testClient.transfer(invalidRequest))
            .isInstanceOf(TransferException.class)
            .hasMessageContaining("XAF");
    }

    @Test
    @DisplayName("Should create valid request to pay object")
    void shouldCreateValidRequestToPayObject() {
        // Given
        String externalId = UUID.randomUUID().toString();
        String phoneNumber = "069999999"; // Valid Congo number (9 digits)
        String amount = "1000";

        // When
        MtnRequestToPay request = MtnRequestToPay.builder()
            .amount(amount)
            .currency("XAF")
            .externalId(externalId)
            .payerPhone(phoneNumber)
            .payerMessage("Test payment")
            .payeeNote("Payment for invoice #123")
            .build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getAmount()).isEqualTo(amount);
        assertThat(request.getCurrency()).isEqualTo("XAF");
        assertThat(request.getExternalId()).isEqualTo(externalId);
        assertThat(request.getPayerMessage()).isEqualTo("Test payment");
        assertThat(request.getPayeeNote()).isEqualTo("Payment for invoice #123");
    }

    @Nested
    @DisplayName("Live API Tests (Requires Real Credentials)")
    @EnabledIfEnvironmentVariable(named = "MTN_CONGO_API_USER", matches = ".+")
    class LiveApiTests {

        @Test
        @DisplayName("Should successfully initiate request to pay")
        //@Disabled("Requires real MTN sandbox credentials - enable manually for live testing")
        void shouldInitiateRequestToPay() {
            // Given
            String externalId = UUID.randomUUID().toString();
            MtnRequestToPay request = MtnRequestToPay.builder()
                .amount("100")
                .currency("XAF")
                .externalId(externalId)
                .payerPhone("068271927")
                .payerMessage("Test payment from paymux-java")
                .payeeNote("Integration test payment")
                .build();

            // When
            TransferResponse response = client.transfer(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.transactionId()).isNotNull();
            assertThat(response.status()).isIn("PENDING", "SUCCESSFUL", "FAILED");
            assertThat(response.message()).isNotNull();

            logger.debug("Transaction ID: {}", response.transactionId());
            logger.debug("Status: {}", response.status());
            logger.debug("Message: {}", response.message());
        }

        @Test
        @DisplayName("Should check transaction status")
        @Disabled("Requires real MTN sandbox credentials and valid transaction ID")
        void shouldCheckTransactionStatus() {
            // Given
            String transactionId = "<>"; // Replace with actual transaction ID

            // When
            var status = client.getTransferStatus(transactionId);

            if (status instanceof MtnTransactionStatus mtnTransactionStatus) {
                // Then
                assertThat(status).isNotNull();
                assertThat(status.transactionId()).isEqualTo(transactionId);
                assertThat(status.status()).isIn("PENDING", "SUCCESSFUL", "FAILED");

                logger.debug("Financial ID: {}", mtnTransactionStatus.getFinancialTransactionId());
                logger.debug("Transaction Status: {}", status.status());
                logger.debug("Reason : {}", mtnTransactionStatus.failureReason());
            }
        }

        @Test
        @DisplayName("Should handle failed transaction gracefully")
        @Disabled("Requires real MTN sandbox credentials")
        void shouldHandleFailedTransaction() {
            // Given - Use an invalid phone number to trigger failure
            String externalId = UUID.randomUUID().toString();
            MtnRequestToPay request = MtnRequestToPay.builder()
                .amount("100")
                .currency("XAF")
                .externalId(externalId)
                .payerPhone("000000000") // Invalid number (9 digits but non-existent)
                .payerMessage("Test failed payment")
                .build();

            // When & Then
            assertThatThrownBy(() -> client.transfer(request))
                .isInstanceOf(TransferException.class);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use sandbox URL when production is false")
        void shouldUseSandboxUrl() {
            // Given
            MtnCongoConfig sandboxConfig = MtnCongoConfig.builder()
                .apiUser("test-user")
                .apiKey("test-key")
                .subscriptionKey("test-subscription")
                .production(false)
                .build();

            // Then
            assertThat(sandboxConfig.getBaseUrl()).contains("sandbox");
        }

        @Test
        @DisplayName("Should use production URL when production is true")
        void shouldUseProductionUrl() {
            // Given
            MtnCongoConfig prodConfig = MtnCongoConfig.builder()
                .apiUser("test-user")
                .apiKey("test-key")
                .subscriptionKey("test-subscription")
                .production(true)
                .build();

            // Then
            assertThat(prodConfig.getBaseUrl()).contains("ericssonbasicapi1");
        }

        @Test
        @DisplayName("Should apply custom timeout settings")
        void shouldApplyCustomTimeouts() {
            // Given
            MtnCongoConfig customConfig = MtnCongoConfig.builder()
                .apiUser("test-user")
                .apiKey("test-key")
                .subscriptionKey("test-subscription")
                .connectionTimeout(5000)
                .requestTimeout(10000)
                .build();

            // Then
            assertThat(customConfig.getConnectionTimeout()).isEqualTo(5000);
            assertThat(customConfig.getRequestTimeout()).isEqualTo(10000);
        }
    }
}
