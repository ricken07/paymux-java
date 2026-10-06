package com.rickenbazolo.paymux.pawapay.service;

import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckout;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutExpiry;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutRequest;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutResponse;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.util.Objects;
import java.util.Optional;

/**
 * PawaPay checkout endpoints: initiate, check status and expire.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayCheckoutService extends PawapayApiSupport {

    static final String CHECKOUTS = "v2/checkouts";
    static final String CHECKOUT_STATUS = "v2/checkouts/";
    static final String EXPIRE_SUFFIX = "/expire";

    public PawapayCheckoutService(PawapayConfig config, PaymuxHttpClient httpClient) {
        super(config, httpClient);
    }

    /**
     * Initiates a checkout ({@code POST /v2/checkouts}).
     *
     * @param request the checkout request
     * @return the initiation response ({@code ACCEPTED} with a {@code redirectUrl}, {@code REJECTED} or {@code DUPLICATE_IGNORED})
     */
    public PawapayCheckoutResponse initiate(PawapayCheckoutRequest request) {
        Objects.requireNonNull(request, "request");
        log.debug("Initiating PawaPay checkout {}", request.getCheckoutId());
        var result = read(post(CHECKOUTS, request), PawapayCheckoutResponse.class);
        log.debug("PawaPay checkout {} initiation status: {}", request.getCheckoutId(), result.getStatus());
        return result;
    }

    /**
     * Checks a checkout status ({@code GET /v2/checkouts/{checkoutId}}).
     *
     * @param checkoutId the checkout id
     * @return the checkout, or empty if PawaPay does not know this id
     */
    public Optional<PawapayCheckout> find(String checkoutId) {
        String id = PawapayValidation.requireUuid(checkoutId, "checkoutId");
        log.debug("Checking PawaPay checkout {}", id);
        return readSearchResult(get(CHECKOUT_STATUS + encode(id)), PawapayCheckout.class);
    }

    /**
     * Expires a checkout so it can no longer be used ({@code POST /v2/checkouts/{checkoutId}/expire}).
     *
     * @param checkoutId the checkout id
     * @return the expiry response
     * @throws com.rickenbazolo.paymux.pawapay.exception.PawapayApiException if the checkout is unknown to PawaPay (HTTP 404)
     */
    public PawapayCheckoutExpiry expire(String checkoutId) {
        String id = PawapayValidation.requireUuid(checkoutId, "checkoutId");
        log.debug("Expiring PawaPay checkout {}", id);
        return read(post(CHECKOUT_STATUS + encode(id) + EXPIRE_SUFFIX, null), PawapayCheckoutExpiry.class);
    }
}
