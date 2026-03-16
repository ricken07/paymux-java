package com.rickenbazolo.paymux.core.exception;

/**
 * Exception thrown when a cash-out (disbursement/withdrawal) operation fails.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class CashoutException extends PaymuxException {

    public CashoutException(String message) {
        super(message);
    }

    public CashoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public CashoutException(int statusCode, String message) {
        super(message);
    }

    public CashoutException(int statusCode, String message, Throwable cause) {
        super(message, cause);
    }
}
