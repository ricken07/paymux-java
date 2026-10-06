package com.rickenbazolo.paymux.core.operations.refund;

import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;

/**
 * Interface representing a refund response.
 * <p>
 * This interface defines the contract for refund responses across all mobile money providers.
 * Each provider implementation may expose additional provider-specific fields.
 * </p>
 *
 * <p>Transaction flow:</p>
 * <pre>
 * 1. PENDING     → Refund accepted and being processed by the provider
 * 2. SUCCESSFUL  → Refund completed
 *    or
 *    FAILED      → Refund rejected or failed
 * </pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 * @see RefundOperation
 * @see MoMoTransferStatus
 */
public interface RefundResponse {

    /**
     * Gets the unique refund identifier.
     *
     * @return the refund identifier
     */
    String transactionId();

    /**
     * Gets the current status of the refund.
     *
     * @return the refund status
     */
    MoMoTransferStatus status();

    /**
     * Gets a human-readable message describing the refund status.
     *
     * @return the status message
     */
    String message();

    /**
     * Gets the reason for failure if the refund failed.
     * <p>
     * Only populated when status is FAILED. May contain provider-specific
     * error codes or descriptions.
     * </p>
     *
     * @return the failure reason, or null if not failed
     */
    default String failureReason() {
        return null;
    }
}
