package com.rickenbazolo.paymux.core.operations.cashout;

import com.rickenbazolo.paymux.core.exception.CashoutException;

/**
 * Interface for cash-out (withdrawal/disbursement) operations.
 * <p>
 * This operation represents sending money from a merchant account to a mobile money account.
 * It's typically used for:
 * <ul>
 *   <li>Payouts to merchants or partners</li>
 *   <li>Salary disbursements</li>
 *   <li>Commission payments</li>
 *   <li>Prize distributions</li>
 *   <li>Vendor payments</li>
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
 * CashoutRequest request = CashoutRequest.builder()
 *     .amount("15000")
 *     .currency("XAF")
 *     .recipientPhoneNumber("242065551234")
 *     .reference("PAYOUT-2024-001")
 *     .build();
 *
 * CashoutResponse response = client.cashout(request);
 * System.out.println("Payout ID: " + response.transactionId());
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 * @see CashoutRequest
 * @see CashoutResponse
 */
public interface CashoutOperation {

    /**
     * Sends money from the merchant account to a mobile money account.
     * <p>
     * This operation debits the merchant's balance and credits the recipient's
     * mobile money wallet. Typically completes immediately without customer approval.
     * </p>
     *
     * @param request the cashout request containing amount, recipient, and other details
     * @return the cashout response with transaction ID and status
     * @throws CashoutException if the cashout operation fails
     * @throws IllegalArgumentException if the request contains invalid parameters
     */
    CashoutResponse cashout(CashoutRequest request) throws CashoutException;
}
