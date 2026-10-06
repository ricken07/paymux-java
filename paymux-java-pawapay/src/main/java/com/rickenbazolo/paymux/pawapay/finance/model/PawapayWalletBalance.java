package com.rickenbazolo.paymux.pawapay.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Balance of one PawaPay wallet ({@code GET /v2/wallet-balances}).
 *
 * @param country  the ISO 3166-1 alpha-3 country code
 * @param balance  the current balance as a decimal string
 * @param currency the ISO 4217 currency code
 * @param provider the provider code when the wallet is provider-specific, otherwise empty
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayWalletBalance(String country, String balance, String currency, String provider) {
}
