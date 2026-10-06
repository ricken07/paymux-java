package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mobile money account details of a payer or recipient.
 *
 * @param phoneNumber the MSISDN (digits only, country code included, e.g. {@code 242065551234})
 * @param provider    the PawaPay provider code (e.g. {@code MTN_MOMO_COG})
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayAccountDetails(@JsonAlias("msisdn") String phoneNumber, String provider) {
}
