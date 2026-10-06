package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayInitiationResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.util.Optional;

/**
 * Response of a checkout initiation ({@code POST /v2/checkouts}).
 * <p>
 * On {@link #isAccepted()}, {@link #getRedirectUrl()} is the URL to forward the customer to, and
 * {@link #getCheckoutCode()} must be stored alongside the checkout id: it is appended to your
 * {@code returnUrl} as a query parameter when the customer is redirected back, and is how you
 * find the checkout (and the id you stored for it) that the customer is returning from.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayCheckoutResponse extends AbstractPawapayInitiationResponse {

    private final String redirectUrl;
    private final String expiresAt;
    private final String checkoutCode;

    @JsonCreator
    public PawapayCheckoutResponse(@JsonProperty("checkoutId") String checkoutId,
                                   @JsonProperty("status") PawapayInitiationStatus status,
                                   @JsonProperty("redirectUrl") String redirectUrl,
                                   @JsonProperty("created") String created,
                                   @JsonProperty("expiresAt") String expiresAt,
                                   @JsonProperty("checkoutCode") String checkoutCode,
                                   @JsonProperty("failureReason") PawapayFailureReason failureReason) {
        super(checkoutId, status, created, failureReason);
        this.redirectUrl = redirectUrl;
        this.expiresAt = expiresAt;
        this.checkoutCode = checkoutCode;
    }

    /**
     * @return the checkout id
     */
    public String getCheckoutId() {
        return getTransactionId();
    }

    /**
     * @return the URL to forward the customer to, present when accepted
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * @return the RFC 3339 expiry timestamp, present when accepted
     */
    public String getExpiresAt() {
        return expiresAt;
    }

    public Optional<Instant> expiresAtInstant() {
        return PawapayDates.parseInstant(expiresAt);
    }

    /**
     * @return the code appended to {@code returnUrl} when the customer returns, present when accepted
     */
    public String getCheckoutCode() {
        return checkoutCode;
    }

    @Override
    public String toString() {
        return "PawapayCheckoutResponse{checkoutId='" + getCheckoutId() + "', status=" + getStatus()
            + ", redirectUrl='" + redirectUrl + "', checkoutCode='" + checkoutCode + "'}";
    }
}
