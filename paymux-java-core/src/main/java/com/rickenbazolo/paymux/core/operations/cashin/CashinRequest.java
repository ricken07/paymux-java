package com.rickenbazolo.paymux.core.operations.cashin;

/**
 * Interface representing a cash-in (deposit) request.
 * <p>
 * This interface defines the contract for cash-in requests across all mobile money providers.
 * Each provider implementation should provide a concrete implementation that may include
 * additional provider-specific fields.
 * </p>
 * <p>
 * <strong>Required fields:</strong>
 * <ul>
 *   <li><strong>amount:</strong> The amount to deposit</li>
 *   <li><strong>currency:</strong> The currency code (e.g., "XAF", "USD")</li>
 *   <li><strong>externalId:</strong> Unique transaction identifier provided by the client</li>
 *   <li><strong>recipientPhoneNumber:</strong> The phone number to credit</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Optional fields:</strong>
 * <ul>
 *   <li><strong>description:</strong> Human-readable description of the transaction</li>
 *   <li><strong>reference:</strong> Merchant reference number</li>
 * </ul>
 * </p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * public class MtnCashinRequest implements CashinRequest {
 *     private final String amount;
 *     private final String currency;
 *     private final String externalId;
 *     private final String recipientPhoneNumber;
 *     private final String description;
 *     private final String payeeNote;  // MTN-specific field
 *
 *     // getters...
 * }
 * }</pre>
 *

 * @author Ricken Bazolo * @since 0.1.0
 * @see CashinOperation
 */
public interface CashinRequest {

    /**
     * Gets the amount to deposit.
     * <p>
     * The amount is represented as a string to avoid floating-point precision issues.
     * Different providers may have different decimal precision requirements.
     * </p>
     *
     * @return the deposit amount as a string (e.g., "5000.00")
     */
    String amount();

    /**
     * Gets the currency code.
     * <p>
     * Should be a valid ISO 4217 currency code (e.g., "XAF" for Central African Franc,
     * "USD" for US Dollar).
     * </p>
     *
     * @return the three-letter currency code
     */
    String currency();

    /**
     * Gets the external transaction ID.
     * <p>
     * This is a unique identifier provided by the client application to track the transaction.
     * It must be unique across all transactions for the provider. Typically a UUID is used.
     * </p>
     *
     * @return the unique external transaction identifier
     */
    String externalId();

    /**
     * Gets the recipient's phone number.
     * <p>
     * The format depends on the provider's requirements:
     * <ul>
     *   <li>Some providers require country code prefix (e.g., "242065551234")</li>
     *   <li>Others accept local format (e.g., "065551234")</li>
     * </ul>
     * Check your provider's documentation for the exact format.
     * </p>
     *
     * @return the recipient's phone number
     */
    String recipientPhoneNumber();

    /**
     * Gets the transaction description.
     * <p>
     * This may be shown to the customer in their transaction history.
     * </p>
     *
     * @return the transaction description, or null if not provided
     */
    default String description() {
        return null;
    }

    /**
     * Gets the merchant reference.
     * <p>
     * This can be used to link the transaction to a refund, payout, or other
     * business entity in your system.
     * </p>
     *
     * @return the merchant reference, or null if not provided
     */
    default String reference() {
        return null;
    }
}
