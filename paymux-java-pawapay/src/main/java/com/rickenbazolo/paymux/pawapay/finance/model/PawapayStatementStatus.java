package com.rickenbazolo.paymux.pawapay.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Generation status of a wallet statement.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayStatementStatus {

    /** The statement is being generated. */
    PROCESSING,
    /** The statement is ready; see the download URL (valid until its expiry). */
    COMPLETED,
    /** The statement could not be generated; see the failure reason. */
    FAILED,
    /** A value not known by this version of the library. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayStatementStatus fromValue(String value) {
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
     * @return true for {@link #COMPLETED} and {@link #FAILED}
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED;
    }
}
