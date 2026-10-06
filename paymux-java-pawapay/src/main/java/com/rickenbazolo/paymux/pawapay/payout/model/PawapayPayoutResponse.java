package com.rickenbazolo.paymux.pawapay.payout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayInitiationResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;

/**
 * Response of a payout initiation ({@code POST /v2/payouts}, one item of {@code POST /v2/payouts/bulk}).
 * <p>
 * {@link #status()} exposes the generic Paymux status ({@code PENDING} for
 * {@code ACCEPTED} / {@code DUPLICATE_IGNORED}, {@code FAILED} for {@code REJECTED});
 * {@link #getStatus()} exposes the raw PawaPay status.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayPayoutResponse extends AbstractPawapayInitiationResponse implements CashoutResponse {

    @JsonCreator
    public PawapayPayoutResponse(@JsonProperty("payoutId") String payoutId,
                                 @JsonProperty("status") PawapayInitiationStatus status,
                                 @JsonProperty("created") String created,
                                 @JsonProperty("failureReason") PawapayFailureReason failureReason) {
        super(payoutId, status, created, failureReason);
    }

    /**
     * @return the payout id
     */
    public String getPayoutId() {
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
        return defaultMessage("Payout");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
