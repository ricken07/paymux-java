package com.rickenbazolo.paymux.pawapay.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionResponse;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayout;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutRequest;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutResponse;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PawaPay payout endpoints: initiate (single and bulk), check status, resend callback and
 * cancel an enqueued payout.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayPayoutService extends PawapayApiSupport {

    static final String PAYOUTS = "v2/payouts";
    static final String BULK_PAYOUTS = "v2/payouts/bulk";
    static final String PAYOUT_STATUS = "v2/payouts/";
    static final String RESEND_CALLBACK = "v2/payouts/resend-callback/";
    static final String FAIL_ENQUEUED = "v2/payouts/fail-enqueued/";

    private static final TypeReference<List<PawapayPayoutResponse>> PAYOUT_RESPONSES = new TypeReference<>() {
    };

    public PawapayPayoutService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Initiates a payout ({@code POST /v2/payouts}).
     *
     * @param request the payout request
     * @return the initiation response ({@code ACCEPTED}, {@code REJECTED} or {@code DUPLICATE_IGNORED})
     */
    public PawapayPayoutResponse initiate(PawapayPayoutRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Initiating PawaPay payout {}", request.getPayoutId());
        var result = read(postSigned(PAYOUTS, request), PawapayPayoutResponse.class);
        log.debug("PawaPay payout {} initiation status: {}", request.getPayoutId(), result.getStatus());
        return result;
    }

    /**
     * Initiates up to {@value PawapayValidation#MAX_BULK_PAYOUTS} payouts in one call ({@code POST /v2/payouts/bulk}).
     * Each payout is evaluated independently and gets its own response and callback.
     *
     * @param requests the payout requests
     * @return one initiation response per request, in the same order
     * @throws IllegalArgumentException if the list is empty or too large
     */
    public List<PawapayPayoutResponse> initiateBulk(List<PawapayPayoutRequest> requests) {
        Objects.requireNonNull(requests, "requests");
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("At least one payout is required");
        }
        if (requests.size() > PawapayValidation.MAX_BULK_PAYOUTS) {
            throw new IllegalArgumentException(
                "Too many payouts: " + requests.size() + " (maximum " + PawapayValidation.MAX_BULK_PAYOUTS + ")");
        }
        log.debug("Initiating {} PawaPay payouts in bulk", requests.size());
        return read(postSigned(BULK_PAYOUTS, requests), PAYOUT_RESPONSES);
    }

    /**
     * Checks a payout status ({@code GET /v2/payouts/{payoutId}}).
     *
     * @param payoutId the payout id
     * @return the payout, or empty if PawaPay does not know this id
     */
    public Optional<PawapayPayout> find(String payoutId) {
        String id = PawapayValidation.requireUuid(payoutId, "payoutId");
        log.debug("Checking PawaPay payout {}", id);
        return readSearchResult(get(PAYOUT_STATUS + encode(id)), PawapayPayout.class);
    }

    /**
     * Asks PawaPay to resend the callback of a final payout ({@code POST /v2/payouts/resend-callback/{payoutId}}).
     *
     * @param payoutId the payout id
     * @return the action response
     */
    public PawapayActionResponse resendCallback(String payoutId) {
        String id = PawapayValidation.requireUuid(payoutId, "payoutId");
        log.debug("Requesting PawaPay payout callback resend for {}", id);
        return read(post(RESEND_CALLBACK + encode(id), null), PawapayActionResponse.class);
    }

    /**
     * Cancels an {@code ENQUEUED} payout ({@code POST /v2/payouts/fail-enqueued/{payoutId}}).
     * The payout ends {@code FAILED} with the {@code MANUALLY_CANCELLED} failure code.
     *
     * @param payoutId the payout id
     * @return the action response ({@code REJECTED} with {@code INVALID_STATE} if the payout is not enqueued)
     */
    public PawapayActionResponse cancelEnqueued(String payoutId) {
        String id = PawapayValidation.requireUuid(payoutId, "payoutId");
        log.debug("Cancelling enqueued PawaPay payout {}", id);
        return read(post(FAIL_ENQUEUED + encode(id), null), PawapayActionResponse.class);
    }
}
