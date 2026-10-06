package com.rickenbazolo.paymux.pawapay.deposit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayInitiationResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;

/**
 * Response of a deposit initiation ({@code POST /v2/deposits}).
 * <p>
 * {@link #status()} exposes the generic Paymux status name ({@code PENDING} for
 * {@code ACCEPTED} / {@code DUPLICATE_IGNORED}, {@code FAILED} for {@code REJECTED});
 * {@link #getStatus()} exposes the raw PawaPay status.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayDepositResponse extends AbstractPawapayInitiationResponse implements TransferResponse {

    @JsonCreator
    public PawapayDepositResponse(@JsonProperty("depositId") String depositId,
                                  @JsonProperty("status") PawapayInitiationStatus status,
                                  @JsonProperty("created") String created,
                                  @JsonProperty("failureReason") PawapayFailureReason failureReason) {
        super(depositId, status, created, failureReason);
    }

    /**
     * @return the deposit id
     */
    public String getDepositId() {
        return getTransactionId();
    }

    @Override
    public String transactionId() {
        return getTransactionId();
    }

    @Override
    public String status() {
        return getStatus().toMoMoStatus().name();
    }

    @Override
    public String message() {
        return defaultMessage("Deposit");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
