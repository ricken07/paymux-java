package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response of an action request: resend callback or cancel an enqueued payout / refund / remittance.
 * <p>
 * PawaPay names the identifier after the resource ({@code depositId}, {@code payoutId},
 * {@code refundId} or {@code remittanceId}); it is exposed here uniformly as {@code transactionId}.
 * </p>
 *
 * @param transactionId the id of the deposit, payout, refund or remittance the action was performed on
 * @param status        {@code ACCEPTED} or {@code REJECTED}
 * @param failureReason the rejection reason, present when the status is {@code REJECTED}
 *                      (typically {@code NOT_FOUND} or {@code INVALID_STATE})
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayActionResponse(
    @JsonProperty("depositId") @JsonAlias({"payoutId", "refundId", "remittanceId"}) String transactionId,
    PawapayActionStatus status,
    PawapayFailureReason failureReason
) {

    /**
     * @return true if PawaPay accepted the action
     */
    public boolean isAccepted() {
        return status == PawapayActionStatus.ACCEPTED;
    }

    /**
     * @return the failure description, or null when accepted
     */
    public String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }
}
