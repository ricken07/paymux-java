package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;

/**
 * Status returned by PawaPay when a deposit, payout or refund is initiated.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayInitiationStatus {

    /** The request has been accepted for processing; the final status will come later. */
    ACCEPTED,
    /** The request has been rejected; see the failure reason. */
    REJECTED,
    /** A request with the same id was already received; this one was ignored (idempotency). */
    DUPLICATE_IGNORED,
    /** A value not known by this version of the library. */
    UNKNOWN;

    /**
     * Lenient parsing: unknown or null values map to {@link #UNKNOWN}.
     *
     * @param value the raw value
     * @return the matching status
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayInitiationStatus fromValue(String value) {
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
     * Maps this status to the generic Paymux status.
     *
     * @return the generic status
     */
    public MoMoTransferStatus toMoMoStatus() {
        return switch (this) {
            case ACCEPTED, DUPLICATE_IGNORED -> MoMoTransferStatus.PENDING;
            case REJECTED -> MoMoTransferStatus.FAILED;
            default -> MoMoTransferStatus.UNKNOW;
        };
    }
}
