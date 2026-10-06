package com.rickenbazolo.paymux.core.operations.refund;

import com.rickenbazolo.paymux.core.exception.RefundException;

/**
 * Interface for refund operations.
 * <p>
 * This operation represents returning money to a customer for a previously completed
 * collection (request to pay / deposit). It's typically used for:
 * <ul>
 *   <li>Order cancellations</li>
 *   <li>Partial or full reimbursements</li>
 *   <li>Dispute resolutions</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Note:</strong> Refunds are usually processed asynchronously by the provider.
 * The initial response typically indicates a PENDING status; the final status is
 * obtained through {@link #getRefundStatus(String)} or provider callbacks.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * RefundResponse response = client.refund(request);
 * if (response.status() == MoMoTransferStatus.PENDING) {
 *     // Poll getRefundStatus(...) or wait for the provider callback
 * }
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 * @see RefundRequest
 * @see RefundResponse
 */
public interface RefundOperation {

    /**
     * Initiates a refund of a previously completed transaction.
     *
     * @param request the refund request containing amount, original transaction and other details
     * @return the refund response with refund ID and initial status
     * @throws RefundException if the refund request fails
     * @throws IllegalArgumentException if the request contains invalid parameters
     */
    RefundResponse refund(RefundRequest request) throws RefundException;

    /**
     * Retrieves the status of a previously initiated refund.
     *
     * @param transactionId the unique refund identifier
     * @return the current status of the refund
     * @throws RefundException if the status check fails or the refund is unknown to the provider
     */
    RefundResponse getRefundStatus(String transactionId) throws RefundException;
}
