package com.rickenbazolo.paymux.pawapay.service;

import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionResponse;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittance;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceRequest;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceResponse;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.Objects;
import java.util.Optional;

/**
 * PawaPay remittance endpoints: initiate, check status, resend callback and cancel an enqueued remittance.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayRemittanceService extends PawapayApiSupport {

    static final String REMITTANCES = "v2/remittances";
    static final String REMITTANCE_STATUS = "v2/remittances/";
    static final String RESEND_CALLBACK = "v2/remittances/resend-callback/";
    static final String FAIL_ENQUEUED = "v2/remittances/fail-enqueued/";

    public PawapayRemittanceService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Initiates a remittance ({@code POST /v2/remittances}).
     *
     * @param request the remittance request
     * @return the initiation response ({@code ACCEPTED}, {@code REJECTED} or {@code DUPLICATE_IGNORED})
     */
    public PawapayRemittanceResponse initiate(PawapayRemittanceRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Initiating PawaPay remittance {}", request.getRemittanceId());
        var result = read(post(REMITTANCES, request), PawapayRemittanceResponse.class);
        log.debug("PawaPay remittance {} initiation status: {}", request.getRemittanceId(), result.getStatus());
        return result;
    }

    /**
     * Checks a remittance status ({@code GET /v2/remittances/{remittanceId}}).
     *
     * @param remittanceId the remittance id
     * @return the remittance, or empty if PawaPay does not know this id
     */
    public Optional<PawapayRemittance> find(String remittanceId) {
        String id = PawapayValidation.requireUuid(remittanceId, "remittanceId");
        log.debug("Checking PawaPay remittance {}", id);
        return readSearchResult(get(REMITTANCE_STATUS + encode(id)), PawapayRemittance.class);
    }

    /**
     * Asks PawaPay to resend the callback of a final remittance ({@code POST /v2/remittances/resend-callback/{remittanceId}}).
     *
     * @param remittanceId the remittance id
     * @return the action response
     */
    public PawapayActionResponse resendCallback(String remittanceId) {
        String id = PawapayValidation.requireUuid(remittanceId, "remittanceId");
        log.debug("Requesting PawaPay remittance callback resend for {}", id);
        return read(post(RESEND_CALLBACK + encode(id), null), PawapayActionResponse.class);
    }

    /**
     * Cancels an {@code ENQUEUED} remittance ({@code POST /v2/remittances/fail-enqueued/{remittanceId}}).
     *
     * @param remittanceId the remittance id
     * @return the action response ({@code REJECTED} with {@code INVALID_STATE} if the remittance is not enqueued)
     */
    public PawapayActionResponse cancelEnqueued(String remittanceId) {
        String id = PawapayValidation.requireUuid(remittanceId, "remittanceId");
        log.debug("Cancelling enqueued PawaPay remittance {}", id);
        return read(post(FAIL_ENQUEUED + encode(id), null), PawapayActionResponse.class);
    }
}
