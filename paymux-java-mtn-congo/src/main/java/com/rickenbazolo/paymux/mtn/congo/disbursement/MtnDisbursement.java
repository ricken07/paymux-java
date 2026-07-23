package com.rickenbazolo.paymux.mtn.congo.disbursement;

import com.rickenbazolo.paymux.core.operations.cashin.CashinRequest;

/**
 * @author Ricken Bazolo
 */
public class MtnDisbursement implements CashinRequest {
    private String amount;
    private String currency;
    private String externalId;

    @Override
    public String amount() {
        return amount;
    }

    @Override
    public String currency() {
        return currency;
    }

    @Override
    public String externalId() {
        return externalId;
    }

    @Override
    public String recipientPhoneNumber() {
        return "";
    }
}
