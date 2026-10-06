package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayInitiationResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;

/**
 * Response of a remittance initiation ({@code POST /v2/remittances}).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayRemittanceResponse extends AbstractPawapayInitiationResponse implements CashoutResponse {

    @JsonCreator
    public PawapayRemittanceResponse(@JsonProperty("remittanceId") String remittanceId,
                                     @JsonProperty("status") PawapayInitiationStatus status,
                                     @JsonProperty("created") String created,
                                     @JsonProperty("failureReason") PawapayFailureReason failureReason) {
        super(remittanceId, status, created, failureReason);
    }

    /**
     * @return the remittance id
     */
    public String getRemittanceId() {
        return getTransactionId();
    }

    @Override
    public String transactionId() {
        return getTransactionId();
    }

    @Override
    public MoMoTransferStatus status() {
        return getStatus().toMoMoStatus();
    }

    @Override
    public String message() {
        return defaultMessage("Remittance");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
