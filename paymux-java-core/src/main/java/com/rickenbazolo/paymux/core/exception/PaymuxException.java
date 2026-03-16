package com.rickenbazolo.paymux.core.exception;

/**
 * PaymuxException class description.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class PaymuxException extends RuntimeException {

    public PaymuxException(String message) {
        super(message);
    }

    public PaymuxException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymuxException(Throwable cause) {
        super(cause);
    }
}
