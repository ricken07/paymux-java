package com.rickenbazolo.paymux.core.operations.cashin;

import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;

/**
 * Interface representing a cash-in (deposit) response.
 * <p>
 * This interface defines the contract for cash-in responses across all mobile money providers.
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
 * Cash-in operations typically complete immediately:
 * 1. SUCCESS  → Money deposited successfully
 *    or
 *    FAILED   → Deposit failed (invalid account, system error, etc.)
 *
 * In some cases, cash-ins may be pending:
 * 1. PENDING  → Being processed
 * 2. SUCCESS  → Completed
 *    or
 *    FAILED   → Failed after processing
 * </pre>
 *

 * @author Ricken Bazolo * @since 0.1.0
 * @see CashinOperation
 * @see MoMoTransferStatus
 */
public interface CashinResponse {

    /**
     * Gets the unique transaction identifier.
     * <p>
     * This ID can be used to:
     * <ul>
     *   <li>Check the transaction status later (if needed)</li>
     *   <li>Reference the transaction in customer support</li>
     *   <li>Reconcile deposits in your system</li>
     * </ul>
     * </p>
     *
     * @return the transaction identifier
     */
    String transactionId();

    /**
     * Gets the current status of the cash-in.
     * <p>
     * Status values:
     * <ul>
     *   <li><strong>PENDING:</strong> Being processed (rare for cash-ins)</li>
     *   <li><strong>SUCCESS:</strong> Deposit completed successfully</li>
     *   <li><strong>FAILED:</strong> Deposit failed</li>
     * </ul>
     * </p>
     *
     * @return the cash-in status
     */
    MoMoTransferStatus status();

    /**
     * Gets a human-readable message describing the transaction status.
     * <p>
     * Examples:
     * <ul>
     *   <li>"Money deposited successfully."</li>
     *   <li>"Deposit is being processed."</li>
     *   <li>"Deposit failed: Invalid account."</li>
     *   <li>"Deposit failed: System error."</li>
     * </ul>
     * </p>
     *
     * @return the status message
     */
    String message();

    /**
     * Gets the reason for failure if the deposit failed.
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
