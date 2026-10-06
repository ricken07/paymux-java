package com.rickenbazolo.paymux.pawapay.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Identifies one PawaPay wallet: a country and currency, optionally restricted to a provider
 * when the account has provider-specific wallets. The wallets of an account are listed by
 * {@code GET /v2/wallet-balances}.
 *
 * @param country  the ISO 3166-1 alpha-3 country code (e.g. {@code COG})
 * @param currency the ISO 4217 currency code (e.g. {@code XAF})
 * @param provider the provider code for provider-specific wallets, or null
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawapayWallet(String country, String currency, String provider) {

    /**
     * Creates a shared (non provider-specific) wallet reference.
     *
     * @param country  the ISO 3166-1 alpha-3 country code
     * @param currency the ISO 4217 currency code
     * @return the wallet reference
     */
    public static PawapayWallet of(String country, String currency) {
        return new PawapayWallet(country, currency, null);
    }

    /**
     * Creates a provider-specific wallet reference.
     *
     * @param country  the ISO 3166-1 alpha-3 country code
     * @param currency the ISO 4217 currency code
     * @param provider the provider code
     * @return the wallet reference
     */
    public static PawapayWallet of(String country, String currency, String provider) {
        return new PawapayWallet(country, currency, provider);
    }
}
