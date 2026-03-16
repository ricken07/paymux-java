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
public class PaymuxHttpException extends PaymuxException {

    public PaymuxHttpException(String message) {
        super(message);
    }

    public PaymuxHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymuxHttpException(int statusCode, String message) {
        super(message);
    }

    public PaymuxHttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
    }
}
