package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Lifecycle status of a PawaPay checkout.
 * <p>
 * This is distinct from the initiation status ({@link com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus})
 * returned when the checkout is created. Once accepted, a checkout enters this lifecycle at
 * {@link #WAITING_PAYMENT}.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayCheckoutStatus {

    /** The checkout and its hosted payment page have been created and are waiting for the customer to pay. */
    WAITING_PAYMENT,
    /** A payment attempt is in progress and the checkout is still being processed. */
    PROCESSING,
    /** The checkout has completed successfully. Final status. */
    COMPLETED,
    /** The checkout has failed; inspect the latest deposit or its history for the reason. Final status. */
    FAILED,
    /** The checkout has expired and can no longer be used. Final status. */
    EXPIRED,
    /** The customer cancelled the payment on the hosted payment page. Final status. */
    CANCELLED,
    /** A value not known by this version of the library. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayCheckoutStatus fromValue(String value) {
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

    /**
     * @return true for {@link #COMPLETED}, {@link #FAILED}, {@link #EXPIRED} and {@link #CANCELLED}
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == EXPIRED || this == CANCELLED;
    }
}
