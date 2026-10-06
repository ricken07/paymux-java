package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Controls how the customer is returned from a checkout's hosted payment page to the
 * {@code returnUrl} once the payment is finished or cancelled. The customer is always returned
 * eventually; this only affects the experience.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayReturnMethod {

    /** The customer is redirected back immediately, with no extra screen. */
    INSTANT,
    /** A short countdown is shown, then the customer is redirected automatically. */
    COUNTDOWN,
    /** The checkout page waits until the customer presses "Return to merchant". */
    CUSTOMER_ACTION,
    /** A value not known by this version of the library. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayReturnMethod fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
