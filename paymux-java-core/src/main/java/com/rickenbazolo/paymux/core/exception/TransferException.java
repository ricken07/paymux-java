package com.rickenbazolo.paymux.core.exception;

/**
 * Exception description
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
/**
 * Exception thrown when an HTTP request fails.
 */
public class TransferException extends PaymuxException {

    public TransferException(String message) {
        super(message);
    }

    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferException(int statusCode, String message) {
        super(message);
    }

    public TransferException(int statusCode, String message, Throwable cause) {
        super(message, cause);
    }
}
