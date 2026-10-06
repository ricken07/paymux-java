package com.rickenbazolo.paymux.core.operations.refund;

/**
 * Interface representing a refund request.
 * <p>
 * This interface defines the contract for refund requests across all mobile money providers.
 * A refund returns (part of) the amount of a previously completed collection (request to pay /
 * deposit) to the customer who paid it. Each provider implementation may add provider-specific fields.
 * </p>
 * <p>
 * <strong>Required fields:</strong>
 * <ul>
 *   <li><strong>amount:</strong> The amount to refund</li>
 *   <li><strong>currency:</strong> The currency code (e.g., "XAF", "USD")</li>
 *   <li><strong>externalId:</strong> Unique identifier of this refund, provided by the client</li>
 *   <li><strong>originalTransactionId:</strong> Identifier of the transaction being refunded</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Optional fields:</strong>
 * <ul>
 *   <li><strong>description:</strong> Human-readable description of the refund</li>
 *   <li><strong>reference:</strong> Merchant reference number</li>
 * </ul>
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 * @see RefundOperation
 */
public interface RefundRequest {

    /**
     * Gets the amount to refund.
     * <p>
     * The amount is represented as a string to avoid floating-point precision issues.
     * It may be lower than or equal to the amount of the original transaction, depending
     * on the provider's partial refund support.
     * </p>
     *
     * @return the refund amount as a string (e.g., "1000.00" or "1000")
     */
    String amount();

    /**
     * Gets the currency code.
     * <p>
     * Should be a valid ISO 4217 currency code and match the currency of the original transaction.
     * </p>
     *
     * @return the three-letter currency code
     */
    String currency();

    /**
     * Gets the external refund ID.
     * <p>
     * This is a unique identifier provided by the client application to track the refund.
     * It must be unique across all refunds for the provider. Typically a UUID is used.
     * </p>
     *
     * @return the unique external refund identifier
     */
    String externalId();

    /**
     * Gets the identifier of the original transaction being refunded.
     * <p>
     * This is the transaction identifier that was returned when the collection
     * (request to pay / deposit) was initiated.
     * </p>
     *
     * @return the original transaction identifier
     */
    String originalTransactionId();

    /**
     * Gets the refund description.
     *
     * @return the refund description, or null if not provided
     */
    default String description() {
        return null;
    }

    /**
     * Gets the merchant reference.
     * <p>
     * This can be used to link the refund to an order, invoice, or other
     * business entity in your system.
     * </p>
     *
     * @return the merchant reference, or null if not provided
     */
    default String reference() {
        return null;
    }
}
