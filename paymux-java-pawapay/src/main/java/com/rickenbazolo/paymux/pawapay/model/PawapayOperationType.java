package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * PawaPay operation types, used to filter the active configuration and provider availability.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayOperationType {

    DEPOSIT,
    PAYOUT,
    REFUND,
    REMITTANCE,
    PUSH_DEPOSIT,
    NAME_LOOKUP,
    /** A value not known by this version of the library. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayOperationType fromValue(String value) {
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
