package com.rickenbazolo.paymux.core.operations.cashin;

import com.rickenbazolo.paymux.core.exception.CashinException;

/**
 * Interface for cash-in (deposit) operations.
 * <p>
 * This operation represents depositing money into a mobile money account.
 * It's typically used for:
 * <ul>
 *   <li>Wallet top-ups</li>
 *   <li>Refunds to customer accounts</li>
 *   <li>Disbursements and payouts</li>
 *   <li>Salary payments</li>
 *   <li>Agent deposits</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Note:</strong> This operation typically requires special permissions
 * and may not be available in all markets or to all merchant types. Contact your
 * mobile money provider to enable this functionality.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * CashinRequest request = CashinRequest.builder()
 *     .amount("5000")
 *     .currency("XAF")
 *     .externalId("unique-refund-id")
 *     .recipientPhoneNumber("242065551234")
 *     .description("Refund for order #12345")
 *     .build();
 *
 * CashinResponse response = client.cashin(request);
 * if (response.getStatus() == CashinStatus.SUCCESS) {
 *     System.out.println("Money deposited successfully");
 * }
 * }</pre>
 *

 * @author Ricken Bazolo * @since 0.1.0
 * @see CashinRequest
 * @see CashinResponse
 */
public interface CashinOperation {

    /**
     * Deposits money into a mobile money account.
     * <p>
     * This operation credits the specified amount to the recipient's mobile money wallet.
     * Unlike transfers (request to pay), cash-in operations typically complete immediately
     * without requiring customer approval.
     * </p>
     *
     * @param request the cash-in request containing amount, recipient, and other details
     * @return the cash-in response with transaction ID and status
     * @throws CashinException if the cash-in operation fails
     * @throws IllegalArgumentException if the request contains invalid parameters
     */
    CashinResponse cashin(CashinRequest request) throws CashinException;

    /**
     * Retrieves the status of a previously initiated cash-in.
     * <p>
     * While cash-ins typically complete immediately, this method can be used to
     * verify the final status or handle edge cases where the initial response
     * indicated a pending state.
     * </p>
     *
     * @param transactionId the unique transaction identifier
     * @return the current status of the cash-in
     * @throws CashinException if the status check fails
     */
    CashinResponse getCashinStatus(String transactionId) throws CashinException;
}
