package com.rickenbazolo.paymux.pawapay.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.model.PawapayOperationType;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayActiveConfiguration;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayAvailability;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayProviderPrediction;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PawaPay toolkit endpoints: active configuration, provider availability,
 * provider prediction and public keys.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayToolkitService extends PawapayApiSupport {

    static final String ACTIVE_CONFIGURATION = "v2/active-conf";
    static final String AVAILABILITY = "v2/availability";
    static final String PREDICT_PROVIDER = "v2/predict-provider";
    static final String PUBLIC_KEYS = "v2/public-key/http";

    private static final TypeReference<List<PawapayAvailability>> AVAILABILITIES = new TypeReference<>() {
    };
    private static final TypeReference<List<PawapayPublicKey>> PUBLIC_KEY_LIST = new TypeReference<>() {
    };

    public PawapayToolkitService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Fetches the active configuration of the merchant account ({@code GET /v2/active-conf}).
     *
     * @param country       optional ISO 3166-1 alpha-3 country filter
     * @param operationType optional operation type filter
     * @return the active configuration
     */
    public PawapayActiveConfiguration activeConfiguration(String country, PawapayOperationType operationType) {
        var query = queryString(filters(country, operationType));
        log.debug("Fetching PawaPay active configuration{}", query);
        return read(get(ACTIVE_CONFIGURATION + query), PawapayActiveConfiguration.class);
    }

    /**
     * Fetches the current provider availability ({@code GET /v2/availability}).
     *
     * @param country       optional ISO 3166-1 alpha-3 country filter
     * @param operationType optional operation type filter
     * @return the availability per country
     */
    public List<PawapayAvailability> availability(String country, PawapayOperationType operationType) {
        var query = queryString(filters(country, operationType));
        log.debug("Fetching PawaPay availability{}", query);
        return read(get(AVAILABILITY + query), AVAILABILITIES);
    }

    /**
     * Predicts the provider of a phone number ({@code POST /v2/predict-provider}).
     *
     * @param phoneNumber the phone number with country code; PawaPay tolerates {@code +}, spaces and dashes
     * @return the prediction
     */
    public PawapayProviderPrediction predictProvider(String phoneNumber) {
        String phone = PawapayValidation.requireNonBlank(phoneNumber, "Phone number");
        log.debug("Predicting PawaPay provider");
        return read(post(PREDICT_PROVIDER, Map.of("phoneNumber", phone)), PawapayProviderPrediction.class);
    }

    /**
     * Fetches the public keys PawaPay uses to sign callbacks ({@code GET /v2/public-key/http}).
     *
     * @return the public keys
     */
    public List<PawapayPublicKey> publicKeys() {
        log.debug("Fetching PawaPay public keys");
        return read(get(PUBLIC_KEYS), PUBLIC_KEY_LIST);
    }

    private static Map<String, String> filters(String country, PawapayOperationType operationType) {
        if (operationType == PawapayOperationType.UNKNOWN) {
            throw new IllegalArgumentException("Operation type UNKNOWN cannot be used as a filter");
        }
        var params = new LinkedHashMap<String, String>();
        params.put("country", PawapayValidation.validateCountry(country));
        params.put("operationType", operationType != null ? operationType.name() : null);
        return params;
    }
}
