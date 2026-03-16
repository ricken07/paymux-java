package com.rickenbazolo.paymux.core.operations.transfer;


/**
 * TransferResponseStatus class description.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */public interface TransferResponseStatus {

    /**
     * Gets the unique transaction identifier.
     * <p>
     * This ID can be used to:
     * <ul>
     *   <li>Check the transaction status later</li>
     *   <li>Reference the transaction in customer support</li>
     *   <li>Reconcile payments in your system</li>
     * </ul>
     * </p>
     *
     * @return the transaction identifier
     */
    String transactionId();

    /**
     * Gets the current status of the transfer.
     * <p>
     * Status values:
     * <ul>
     *   <li><strong>PENDING:</strong> Awaiting customer approval</li>
     *   <li><strong>SUCCESS:</strong> Payment completed successfully</li>
     *   <li><strong>FAILED:</strong> Payment failed or was declined</li>
     *   <li><strong>TIMEOUT:</strong> Customer did not respond in time</li>
     * </ul>
     * </p>
     *
     * @return the transfer status
     */
    String status();

    /**
     * Gets the reason for failure if the transaction failed.
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
