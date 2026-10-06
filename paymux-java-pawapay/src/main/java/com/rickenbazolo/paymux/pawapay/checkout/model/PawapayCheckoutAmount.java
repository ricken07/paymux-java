package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One fixed amount option of a checkout, for a given country and currency.
 * <p>
 * When a checkout's {@code amounts} list is non-empty, the customer can only pay one of the
 * offered combinations; the hosted payment page shows the entry matching the country the
 * customer selects. Each country can appear more than once with a different currency.
 * </p>
 *
 * @param country  the ISO 3166-1 alpha-3 country code this amount applies to
 * @param currency the ISO 4217 currency code
 * @param amount   the fixed amount
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawapayCheckoutAmount(String country, String currency, String amount) {
}
