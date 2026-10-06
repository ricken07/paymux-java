package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.pawapay.model.PawapayAccountDetails;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;

/**
 * Recipient of a remittance: a mobile money account plus the recipient's name.
 *
 * @param type             the account type, currently always {@link PawapayParty#TYPE_MMO}
 * @param accountDetails   the mobile money account details
 * @param recipientDetails the recipient's name
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawapayRemittanceRecipient(String type, PawapayAccountDetails accountDetails, Details recipientDetails) {

    /**
     * Creates a mobile money recipient.
     *
     * @param phoneNumber the MSISDN (digits only, country code included)
     * @param provider    the PawaPay provider code
     * @param firstName   the recipient's first name
     * @param lastName    the recipient's last name
     * @return a new recipient
     */
    public static PawapayRemittanceRecipient mmo(String phoneNumber, String provider, String firstName, String lastName) {
        return new PawapayRemittanceRecipient(PawapayParty.TYPE_MMO,
            new PawapayAccountDetails(phoneNumber, provider), new Details(firstName, lastName));
    }

    public String phoneNumber() {
        return accountDetails != null ? accountDetails.phoneNumber() : null;
    }

    public String provider() {
        return accountDetails != null ? accountDetails.provider() : null;
    }

    /**
     * @return the account part of this recipient, as a generic party
     */
    public PawapayParty toParty() {
        return new PawapayParty(type, accountDetails);
    }

    /**
     * Name of the recipient.
     *
     * @param firstName the first name (at most 64 characters)
     * @param lastName  the last name (at most 64 characters)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Details(String firstName, String lastName) {
    }
}
