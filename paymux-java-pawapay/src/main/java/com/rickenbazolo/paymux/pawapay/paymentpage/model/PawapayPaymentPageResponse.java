package com.rickenbazolo.paymux.pawapay.paymentpage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;

/**
 * Response of {@code POST /v2/paymentpage}.
 * <p>
 * On success only {@link #getRedirectUrl()} is returned: the deposit itself is registered under
 * the {@code depositId} you supplied only once the customer presses "Pay" on the page, so there
 * is no PawaPay-assigned id or timestamp to return yet. A business rejection (e.g. an unavailable
 * provider or an out-of-bounds amount) is returned the same way as a deposit rejection, echoing
 * your {@code depositId} with {@code status = REJECTED} and a {@link #getFailureReason()}.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayPaymentPageResponse {

    private final String redirectUrl;
    private final String depositId;
    private final PawapayInitiationStatus status;
    private final PawapayFailureReason failureReason;

    @JsonCreator
    public PawapayPaymentPageResponse(@JsonProperty("redirectUrl") String redirectUrl,
                                      @JsonProperty("depositId") String depositId,
                                      @JsonProperty("status") PawapayInitiationStatus status,
                                      @JsonProperty("failureReason") PawapayFailureReason failureReason) {
        this.redirectUrl = redirectUrl;
        this.depositId = depositId;
        this.status = status;
        this.failureReason = failureReason;
    }

    /**
     * @return the URL to forward the customer to, valid for 15 minutes; present on success
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * @return the deposit id echoed back on a business rejection, or null on success
     */
    public String getDepositId() {
        return depositId;
    }

    /**
     * @return {@code REJECTED} on a business rejection, or null on success
     */
    public PawapayInitiationStatus getStatus() {
        return status;
    }

    /**
     * @return the rejection reason, present when rejected
     */
    public PawapayFailureReason getFailureReason() {
        return failureReason;
    }

    public boolean isAccepted() {
        return redirectUrl != null && !redirectUrl.isBlank();
    }

    public boolean isRejected() {
        return status == PawapayInitiationStatus.REJECTED;
    }

    /**
     * @return the failure description, or null when accepted
     */
    public String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }

    @Override
    public String toString() {
        return "PawapayPaymentPageResponse{redirectUrl='" + redirectUrl + "', depositId='" + depositId
            + "', status=" + status + ", failureReason=" + failureReason + '}';
    }
}
