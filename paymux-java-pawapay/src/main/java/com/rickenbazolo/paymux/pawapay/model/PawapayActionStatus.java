package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status of an action request (resend callback, cancel enqueued payment).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayActionStatus {

    /** The action request has been accepted by PawaPay for processing. */
    ACCEPTED,
    /** The action request has been rejected by PawaPay; see the failure reason. */
    REJECTED,
    /** A value not known by this version of the library. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayActionStatus fromValue(String value) {
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
