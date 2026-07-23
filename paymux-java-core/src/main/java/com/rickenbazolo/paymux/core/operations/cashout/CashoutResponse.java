package com.rickenbazolo.paymux.core.operations.cashout;

import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;

/**
 * Interface representing a cash-out (withdrawal/disbursement) response.
 * <p>
 * This interface defines the contract for cashout responses across all mobile money providers.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 * @see CashoutOperation
 */
public interface CashoutResponse {

    /**
     * Gets the unique transaction identifier.
     *
     * @return the transaction identifier
     */
    String transactionId();

    /**
     * Gets the current status of the cashout.
     *
     * @return the cashout status
     */
    default MoMoTransferStatus status() {
        return MoMoTransferStatus.SUCCESSFUL;
    }

    /**
     * Gets a human-readable message describing the transaction status.
     *
     * @return the status message
     */
    String message();

    /**
     * Gets the reason for failure if the transaction failed.
     *
     * @return the failure reason, or null if not failed
     */
    default String failureReason() {
        return null;
    }
}
