package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;

/**
 * Lifecycle status of a PawaPay deposit, payout or refund.
 * <pre>
 * ACCEPTED → (ENQUEUED) → PROCESSING → COMPLETED
 *                                    → FAILED
 *                       → IN_RECONCILIATION → COMPLETED / FAILED
 * </pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayTransactionStatus {

    /** Accepted by PawaPay, not yet submitted to the provider. */
    ACCEPTED,
    /** Accepted but queued because the provider is temporarily unavailable (payouts / refunds). */
    ENQUEUED,
    /** Submitted to the provider and being processed. */
    PROCESSING,
    /** The provider did not give a final status yet; PawaPay is reconciling. */
    IN_RECONCILIATION,
    /** Final status: the payment succeeded. */
    COMPLETED,
    /** Final status: the payment failed; see the failure reason. */
    FAILED,
    /** A value not known by this version of the library. */
    UNKNOWN;

    /**
     * Lenient parsing: unknown or null values map to {@link #UNKNOWN}.
     *
     * @param value the raw value
     * @return the matching status
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayTransactionStatus fromValue(String value) {
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

    /**
     * Maps this status to the generic Paymux status.
     *
     * @return the generic status
     */
    public MoMoTransferStatus toMoMoStatus() {
        return switch (this) {
            case COMPLETED -> MoMoTransferStatus.SUCCESSFUL;
            case FAILED -> MoMoTransferStatus.FAILED;
            case ACCEPTED, ENQUEUED, PROCESSING, IN_RECONCILIATION -> MoMoTransferStatus.PENDING;
            default -> MoMoTransferStatus.UNKNOW;
        };
    }
}
