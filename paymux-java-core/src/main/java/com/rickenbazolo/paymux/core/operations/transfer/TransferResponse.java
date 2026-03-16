package com.rickenbazolo.paymux.core.operations.transfer;

import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;

/**
 * Interface representing a transfer (request to pay) response.
 * <p>
 * This interface defines the contract for transfer responses across all mobile money providers.
 * Each provider implementation should provide a concrete implementation that may include
 * additional provider-specific fields.
 * </p>
 * <p>
 * The response typically contains:
 * <ul>
 *   <li><strong>transactionId:</strong> Unique identifier for tracking</li>
 *   <li><strong>status:</strong> Current status of the transaction</li>
 *   <li><strong>message:</strong> Human-readable status message</li>
 * </ul>
 * </p>
 *
 * <p>Transaction flow:</p>
 * <pre>
 * 1. PENDING  → Customer receives USSD prompt
 * 2. PENDING  → Customer reviews and approves/declines
 * 3. SUCCESS  → Payment completed
 *    or
 *    FAILED   → Payment declined/failed
 * </pre>
 *

 * @author Ricken Bazolo * @since 0.1.0
 * @see TransferOperation
 * @see MoMoTransferStatus
 */
public interface TransferResponse {

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
     * Gets a human-readable message describing the transaction status.
     * <p>
     * Examples:
     * <ul>
     *   <li>"Payment request initiated. Customer will receive a USSD prompt."</li>
     *   <li>"Payment completed successfully."</li>
     *   <li>"Payment declined by customer."</li>
     *   <li>"Insufficient balance."</li>
     * </ul>
     * </p>
     *
     * @return the status message
     */
    String message();

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
