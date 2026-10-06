package com.rickenbazolo.paymux.pawapay.exception;

import com.rickenbazolo.paymux.core.exception.PaymuxException;

/**
 * Exception thrown when a PawaPay status lookup answers {@code "status": "NOT_FOUND"}.
 * <p>
 * This lets applications distinguish an unknown transaction id from network or
 * authentication failures. When raised through a core operation
 * ({@code getTransferStatus}, {@code getCashoutStatus}, {@code getRefundStatus}) it is
 * wrapped in the corresponding core exception and available through {@code getCause()}.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayNotFoundException extends PaymuxException {

    private final String resource;
    private final String transactionId;

    public PawapayNotFoundException(String resource, String transactionId) {
        super("PawaPay " + resource + " not found: " + transactionId);
        this.resource = resource;
        this.transactionId = transactionId;
    }

    /**
     * @return the kind of resource looked up ("deposit", "payout" or "refund")
     */
    public String getResource() {
        return resource;
    }

    /**
     * @return the transaction identifier that was looked up
     */
    public String getTransactionId() {
        return transactionId;
    }
}
