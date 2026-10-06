package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;

/**
 * Payer details pre-filled for a checkout's hosted payment page.
 * <p>
 * Unlike {@link PawapayParty}, the account details here carry an explicit
 * {@code allowCustomerToOverride} flag: when {@code true} the customer can still change the
 * pre-filled wallet, when {@code false} the wallet is locked. This flag is required whenever a
 * payer is provided; {@code phoneNumber} and {@code provider} are optional.
 * </p>
 *
 * @param type           the account type, currently always {@link PawapayParty#TYPE_MMO}
 * @param accountDetails the mobile money account details
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawapayCheckoutPayer(String type, AccountDetails accountDetails) {

    /**
     * Creates a mobile money checkout payer.
     *
     * @param phoneNumber             the MSISDN, or null to let the customer enter it
     * @param provider                the PawaPay provider code, or null to let the customer choose it
     * @param allowCustomerToOverride whether the customer may change the pre-filled details
     * @return a new checkout payer
     */
    public static PawapayCheckoutPayer mmo(String phoneNumber, String provider, boolean allowCustomerToOverride) {
        return new PawapayCheckoutPayer(PawapayParty.TYPE_MMO, new AccountDetails(phoneNumber, provider, allowCustomerToOverride));
    }

    public String phoneNumber() {
        return accountDetails != null ? accountDetails.phoneNumber() : null;
    }

    public String provider() {
        return accountDetails != null ? accountDetails.provider() : null;
    }

    public boolean allowCustomerToOverride() {
        return accountDetails != null && accountDetails.allowCustomerToOverride();
    }

    /**
     * @param phoneNumber             the MSISDN, may be null
     * @param provider                the PawaPay provider code, may be null
     * @param allowCustomerToOverride whether the customer may change the pre-filled details (required)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AccountDetails(String phoneNumber, String provider, boolean allowCustomerToOverride) {
    }
}
