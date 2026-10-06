package com.rickenbazolo.paymux.pawapay.refund.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.refund.RefundResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayInitiationResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;

/**
 * Response of a refund initiation ({@code POST /v2/refunds}).
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
public final class PawapayRefundResponse extends AbstractPawapayInitiationResponse implements RefundResponse {

    @JsonCreator
    public PawapayRefundResponse(@JsonProperty("refundId") String refundId,
                                 @JsonProperty("status") PawapayInitiationStatus status,
                                 @JsonProperty("created") String created,
                                 @JsonProperty("failureReason") PawapayFailureReason failureReason) {
        super(refundId, status, created, failureReason);
    }

    /**
     * @return the refund id
     */
    public String getRefundId() {
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
        return defaultMessage("Refund");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
