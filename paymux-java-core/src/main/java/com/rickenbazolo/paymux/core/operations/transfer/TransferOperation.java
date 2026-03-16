package com.rickenbazolo.paymux.core.operations.transfer;

import com.rickenbazolo.paymux.core.exception.TransferException;

/**
 * Interface for money transfer operations (Request to Pay / Collection).
 * <p>
 * This operation represents initiating a payment request to a customer's mobile money account.
 * The customer receives a USSD prompt on their phone to approve or decline the payment.
 * </p>
 * <p>
 * This is typically used for:
 * <ul>
 *   <li>E-commerce checkout payments</li>
 *   <li>Bill payments</li>
 *   <li>Service payments</li>
 *   <li>Person-to-business payments</li>
 * </ul>
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TransferRequest request = TransferRequest.builder()
 *     .amount("1000")
 *     .currency("XAF")
 *     .externalId("unique-transaction-id")
 *     .recipientPhoneNumber("242065551234")
 *     .description("Payment for order #12345")
 *     .build();
 *
 * TransferResponse response = client.transfer(request);
 * if (response.getStatus() == TransferStatus.PENDING) {
 *     // Wait for customer to approve on their phone
 *     // You can check status later or use webhooks
 * }
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 * @see TransferRequest
 * @see TransferResponse
 */
public interface TransferOperation {

    /**
     * Initiates a money transfer (request to pay) to a mobile money account.
     * <p>
     * This sends a payment request to the recipient's phone. The recipient must
     * approve the transaction via USSD prompt. The response typically indicates
     * a PENDING status, and you should check the transaction status later or
     * use webhooks to receive updates.
     * </p>
     *
     * @param request the transfer request containing amount, recipient, and other details
     * @return the transfer response with transaction ID and initial status
     * @throws TransferException if the transfer request fails
     * @throws IllegalArgumentException if the request contains invalid parameters
     */
    TransferResponse transfer(TransferRequest request) throws TransferException;

    /**
     * Retrieves the status of a previously initiated transfer.
     * <p>
     * Use this method to check if a PENDING transfer has been approved or declined
     * by the customer.
     * </p>
     *
     * @param transactionId the unique transaction identifier
     * @return the current status of the transfer
     * @throws TransferException if the status check fails
     */
    TransferResponseStatus getTransferStatus(String transactionId) throws TransferException;
}
