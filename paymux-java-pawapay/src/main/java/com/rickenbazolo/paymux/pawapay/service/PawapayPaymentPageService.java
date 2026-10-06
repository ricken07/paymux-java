package com.rickenbazolo.paymux.pawapay.service;

import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageRequest;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageResponse;

import java.util.Objects;

/**
 * PawaPay payment page endpoint: create a hosted, redirect-based deposit session.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayPaymentPageService extends PawapayApiSupport {

    static final String PAYMENT_PAGE = "v2/paymentpage";

    public PawapayPaymentPageService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Creates a payment page session ({@code POST /v2/paymentpage}).
     *
     * @param request the payment page request
     * @return the response: a {@code redirectUrl} on success, or a business rejection
     */
    public PawapayPaymentPageResponse create(PawapayPaymentPageRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Creating PawaPay payment page session for deposit {}", request.getDepositId());
        var result = read(post(PAYMENT_PAGE, request), PawapayPaymentPageResponse.class);
        log.debug("PawaPay payment page session for deposit {}: {}", request.getDepositId(),
            result.isAccepted() ? "created" : "rejected");
        return result;
    }
}
