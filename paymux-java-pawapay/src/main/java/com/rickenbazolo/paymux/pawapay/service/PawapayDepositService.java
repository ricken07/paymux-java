package com.rickenbazolo.paymux.pawapay.service;

import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDeposit;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositRequest;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionResponse;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.Objects;
import java.util.Optional;

/**
 * PawaPay deposit endpoints: initiate, check status and resend callback.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayDepositService extends PawapayApiSupport {

    static final String DEPOSITS = "v2/deposits";
    static final String DEPOSIT_STATUS = "v2/deposits/";
    static final String RESEND_CALLBACK = "v2/deposits/resend-callback/";

    public PawapayDepositService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Initiates a deposit ({@code POST /v2/deposits}).
     *
     * @param request the deposit request
     * @return the initiation response ({@code ACCEPTED}, {@code REJECTED} or {@code DUPLICATE_IGNORED})
     */
    public PawapayDepositResponse initiate(PawapayDepositRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Initiating PawaPay deposit {}", request.getDepositId());
        var response = postSigned(DEPOSITS, request);
        var result = read(response, PawapayDepositResponse.class);
        log.debug("PawaPay deposit {} initiation status: {}", request.getDepositId(), result.getStatus());
        return result;
    }

    /**
     * Checks a deposit status ({@code GET /v2/deposits/{depositId}}).
     *
     * @param depositId the deposit id
     * @return the deposit, or empty if PawaPay does not know this id
     */
    public Optional<PawapayDeposit> find(String depositId) {
        String id = PawapayValidation.requireUuid(depositId, "depositId");
        log.debug("Checking PawaPay deposit {}", id);
        return readSearchResult(get(DEPOSIT_STATUS + encode(id)), PawapayDeposit.class);
    }

    /**
     * Asks PawaPay to resend the callback of a final deposit ({@code POST /v2/deposits/resend-callback/{depositId}}).
     *
     * @param depositId the deposit id
     * @return the action response
     */
    public PawapayActionResponse resendCallback(String depositId) {
        String id = PawapayValidation.requireUuid(depositId, "depositId");
        log.debug("Requesting PawaPay deposit callback resend for {}", id);
        return read(post(RESEND_CALLBACK + encode(id), null), PawapayActionResponse.class);
    }
}
