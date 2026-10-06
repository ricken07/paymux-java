package com.rickenbazolo.paymux.pawapay;

import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositRequest;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;
import com.rickenbazolo.paymux.pawapay.model.PawapayOperationType;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayActiveConfiguration;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayProviderPrediction;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for the PawaPay client against the sandbox.
 * <p>
 * These tests require a real PawaPay sandbox API token. Set the following environment variables:
 * <ul>
 *   <li>PAWAPAY_API_TOKEN - your sandbox API token (required to enable the tests)</li>
 *   <li>PAWAPAY_TEST_PHONE - a sandbox test MSISDN (optional, enables the deposit test)</li>
 *   <li>PAWAPAY_TEST_PROVIDER - the provider of that number, e.g. MTN_MOMO_ZMB (optional)</li>
 *   <li>PAWAPAY_TEST_CURRENCY - the currency of that provider, e.g. ZMW (optional)</li>
 * </ul>
 * </p>
 * <pre>
 * export PAWAPAY_API_TOKEN=your-sandbox-token
 * mvn test -pl paymux-java-pawapay -Dtest=PawapayClientIntegrationTest
 * </pre>
 *
 * @author Ricken Bazolo
 */
@DisplayName("PawaPay Client Integration Tests")
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "PAWAPAY_API_TOKEN", matches = ".+")
class PawapayClientIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PawapayClientIntegrationTest.class);

    private PawapayClient client;

    @BeforeEach
    void setUp() {
        var config = PawapayConfig.builder()
            .apiToken(System.getenv("PAWAPAY_API_TOKEN"))
            .production(false)
            .build();
        client = new PawapayClient(config);
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    @DisplayName("Should fetch the active configuration")
    void shouldFetchActiveConfiguration() {
        PawapayActiveConfiguration configuration = client.getActiveConfiguration();

        assertThat(configuration).isNotNull();
        assertThat(configuration.companyName()).isNotBlank();
        assertThat(configuration.countries()).isNotEmpty();
        logger.debug("Company: {} - countries: {}", configuration.companyName(),
            configuration.countries().stream().map(PawapayActiveConfiguration.Country::country).toList());
    }

    @Test
    @DisplayName("Should fetch availability and public keys")
    void shouldFetchAvailabilityAndPublicKeys() {
        assertThat(client.getAvailability(null, PawapayOperationType.DEPOSIT)).isNotNull();

        List<PawapayPublicKey> keys = client.getPublicKeys();
        assertThat(keys).isNotEmpty();
        assertThat(keys.get(0).key()).contains("PUBLIC KEY");
    }

    @Test
    @DisplayName("Should predict the provider of a phone number")
    void shouldPredictProvider() {
        String phone = System.getenv().getOrDefault("PAWAPAY_TEST_PHONE", "260763456789");

        PawapayProviderPrediction prediction = client.predictProvider(phone);

        assertThat(prediction.provider()).isNotBlank();
        assertThat(prediction.country()).hasSize(3);
        logger.debug("Predicted provider for {}: {} ({})", phone, prediction.provider(), prediction.country());
    }

    @Test
    @DisplayName("Should initiate a sandbox deposit")
    void shouldInitiateDeposit() {
        String phone = System.getenv("PAWAPAY_TEST_PHONE");
        String provider = System.getenv("PAWAPAY_TEST_PROVIDER");
        String currency = System.getenv("PAWAPAY_TEST_CURRENCY");
        assumeTrue(phone != null && provider != null && currency != null,
            "Set PAWAPAY_TEST_PHONE, PAWAPAY_TEST_PROVIDER and PAWAPAY_TEST_CURRENCY to run the deposit test");

        PawapayDepositRequest request = PawapayDepositRequest.builder()
            .amount("10")
            .currency(currency)
            .phoneNumber(phone)
            .provider(provider)
            .customerMessage("Paymux test")
            .addMetadata("source", "paymux-java-integration-test")
            .build();

        PawapayDepositResponse response = client.initiateDeposit(request);

        assertThat(response.getDepositId()).isEqualTo(request.getDepositId());
        assertThat(response.getStatus()).isIn(PawapayInitiationStatus.ACCEPTED, PawapayInitiationStatus.REJECTED);
        logger.debug("Deposit {} -> {} {}", response.getDepositId(), response.getStatus(), response.failureReason());

        if (response.isAccepted()) {
            var deposit = client.findDeposit(response.getDepositId());
            assertThat(deposit).isPresent();
            logger.debug("Deposit status: {}", deposit.get().getStatus());
        }
    }
}
