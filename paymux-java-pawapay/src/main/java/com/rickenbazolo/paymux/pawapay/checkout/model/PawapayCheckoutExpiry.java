package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.util.Optional;

/**
 * Response of {@code POST /v2/checkouts/{checkoutId}/expire}.
 * <p>
 * After a checkout has expired, the customer can no longer make new payment attempts on it.
 * </p>
 *
 * @param checkoutId the checkout id
 * @param status     the checkout lifecycle status after expiry (normally {@link PawapayCheckoutStatus#EXPIRED})
 * @param expiredAt  RFC 3339 timestamp of the expiry
 * @param reason     the reason reported for the expiry (e.g. {@code MANUAL_EXPIRY})
 * @param expiredBy  the actor that expired the checkout (e.g. {@code API})
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayCheckoutExpiry(String checkoutId, PawapayCheckoutStatus status, String expiredAt,
                                    String reason, String expiredBy) {

    public PawapayCheckoutExpiry {
        status = status != null ? status : PawapayCheckoutStatus.UNKNOWN;
    }

    public Optional<Instant> expiredAtInstant() {
        return PawapayDates.parseInstant(expiredAt);
    }
}
