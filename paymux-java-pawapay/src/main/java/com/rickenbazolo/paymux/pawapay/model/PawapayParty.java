package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payer (deposit) or recipient (payout / refund) of a PawaPay transaction.
 * <p>
 * At the moment PawaPay only supports mobile money accounts ({@code type = "MMO"}).
 * </p>
 *
 * @param type           the account type, currently always {@link #TYPE_MMO}
 * @param accountDetails the mobile money account details
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayParty(String type, PawapayAccountDetails accountDetails) {

    /** Mobile Money Operator account type. */
    public static final String TYPE_MMO = "MMO";

    /**
     * Creates a mobile money party.
     *
     * @param phoneNumber the MSISDN (digits only, country code included)
     * @param provider    the PawaPay provider code
     * @return a new party
     */
    public static PawapayParty mmo(String phoneNumber, String provider) {
        return new PawapayParty(TYPE_MMO, new PawapayAccountDetails(phoneNumber, provider));
    }

    /**
     * @return the phone number, or null if account details are absent
     */
    public String phoneNumber() {
        return accountDetails != null ? accountDetails.phoneNumber() : null;
    }

    /**
     * @return the provider code, or null if account details are absent
     */
    public String provider() {
        return accountDetails != null ? accountDetails.provider() : null;
    }
}
