package com.rickenbazolo.paymux.pawapay.service;

import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionResponse;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefund;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundRequest;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundResponse;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.Objects;
import java.util.Optional;

/**
 * PawaPay refund endpoints: initiate, check status, resend callback and cancel an enqueued refund.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayRefundService extends PawapayApiSupport {

    static final String REFUNDS = "v2/refunds";
    static final String REFUND_STATUS = "v2/refunds/";
    static final String RESEND_CALLBACK = "v2/refunds/resend-callback/";
    static final String FAIL_ENQUEUED = "v2/refunds/fail-enqueued/";

    public PawapayRefundService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Initiates a refund ({@code POST /v2/refunds}).
     *
     * @param request the refund request
     * @return the initiation response ({@code ACCEPTED}, {@code REJECTED} or {@code DUPLICATE_IGNORED})
     */
    public PawapayRefundResponse initiate(PawapayRefundRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Initiating PawaPay refund {} of deposit {}", request.getRefundId(), request.getDepositId());
        var result = read(postSigned(REFUNDS, request), PawapayRefundResponse.class);
        log.debug("PawaPay refund {} initiation status: {}", request.getRefundId(), result.getStatus());
        return result;
    }

    /**
     * Checks a refund status ({@code GET /v2/refunds/{refundId}}).
     *
     * @param refundId the refund id
     * @return the refund, or empty if PawaPay does not know this id
     */
    public Optional<PawapayRefund> find(String refundId) {
        String id = PawapayValidation.requireUuid(refundId, "refundId");
        log.debug("Checking PawaPay refund {}", id);
        return readSearchResult(get(REFUND_STATUS + encode(id)), PawapayRefund.class);
    }

    /**
     * Asks PawaPay to resend the callback of a final refund ({@code POST /v2/refunds/resend-callback/{refundId}}).
     *
     * @param refundId the refund id
     * @return the action response
     */
    public PawapayActionResponse resendCallback(String refundId) {
        String id = PawapayValidation.requireUuid(refundId, "refundId");
        log.debug("Requesting PawaPay refund callback resend for {}", id);
        return read(post(RESEND_CALLBACK + encode(id), null), PawapayActionResponse.class);
    }

    /**
     * Cancels an {@code ENQUEUED} refund ({@code POST /v2/refunds/fail-enqueued/{refundId}}).
     *
     * @param refundId the refund id
     * @return the action response ({@code REJECTED} with {@code INVALID_STATE} if the refund is not enqueued)
     */
    public PawapayActionResponse cancelEnqueued(String refundId) {
        String id = PawapayValidation.requireUuid(refundId, "refundId");
        log.debug("Cancelling enqueued PawaPay refund {}", id);
        return read(post(FAIL_ENQUEUED + encode(id), null), PawapayActionResponse.class);
    }
}
