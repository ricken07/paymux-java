package com.rickenbazolo.paymux.core.operations.cashout;

/**
 * Interface representing a cash-out (withdrawal/disbursement) request.
 * <p>
 * This interface defines the contract for cashout requests across all mobile money providers.
 * Each provider implementation should provide a concrete implementation that may include
 * additional provider-specific fields.
 * </p>
 * <p>
 * <strong>Required fields:</strong>
 * <ul>
 *   <li><strong>amount:</strong> The amount to disburse</li>
 *   <li><strong>currency:</strong> The currency code (e.g., "XAF", "USD")</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Optional fields:</strong>
 * <ul>
 *   <li><strong>recipientPhoneNumber:</strong> The phone number to credit</li>
 *   <li><strong>reference:</strong> Merchant reference number</li>
 *   <li><strong>description:</strong> Human-readable description</li>
 * </ul>
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 * @see CashoutOperation
 */
public interface CashoutRequest {

    /**
     * Gets the amount to disburse.
     * <p>
     * The amount is represented as a string to avoid floating-point precision issues.
     * </p>
     *
     * @return the disbursement amount as a string (e.g., "15000.00")
     */
    String amount();

    /**
     * Gets the currency code.
     * <p>
     * Should be a valid ISO 4217 currency code (e.g., "XAF", "USD").
     * </p>
     *
     * @return the three-letter currency code
     */
    String currency();

    /**
     * Gets the recipient's phone number.
     *
     * @return the recipient's phone number, or null if not applicable
     */
    default String recipientPhoneNumber() {
        return null;
    }

    /**
     * Gets the merchant reference.
     *
     * @return the merchant reference, or null if not provided
     */
    default String reference() {
        return null;
    }

    /**
     * Gets the transaction description.
     *
     * @return the transaction description, or null if not provided
     */
    default String description() {
        return null;
    }
}
