package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Operational status of a provider for a given operation type.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayProviderStatus {

    /** The provider is operational and open for processing requests. */
    OPERATIONAL,
    /** The provider is having problems; payouts are enqueued and processed later. */
    DELAYED,
    /** The provider is having problems; all requests are rejected by PawaPay. */
    CLOSED,
    /** A value not known by this version of the library. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayProviderStatus fromValue(String value) {
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
