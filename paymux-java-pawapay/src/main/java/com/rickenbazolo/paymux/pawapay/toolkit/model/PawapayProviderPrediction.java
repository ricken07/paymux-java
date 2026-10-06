package com.rickenbazolo.paymux.pawapay.toolkit.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Result of {@code POST /v2/predict-provider}: the most likely provider of a phone number.
 * <p>
 * The prediction is based on number ranges and can be wrong when numbers are ported between
 * operators (about 0.1% on average, more in some countries). Use it as a hint, and let the
 * customer confirm when possible.
 * </p>
 *
 * @param country     the ISO 3166-1 alpha-3 country code
 * @param provider    the predicted provider code (e.g. {@code MTN_MOMO_COG})
 * @param phoneNumber the sanitized phone number in PawaPay MSISDN format
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayProviderPrediction(String country, String provider, @JsonAlias("msisdn") String phoneNumber) {
}
