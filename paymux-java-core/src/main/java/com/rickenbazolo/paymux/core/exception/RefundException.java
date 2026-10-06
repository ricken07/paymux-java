package com.rickenbazolo.paymux.core.exception;

/**
 * Exception thrown when a refund operation fails.
 * <p>
 * This exception is thrown when:
 * <ul>
 *   <li>The original transaction cannot be found or is not refundable</li>
 *   <li>The provider's API returns an error</li>
 *   <li>Network errors occur during the request</li>
 *   <li>The merchant wallet does not have sufficient balance</li>
 *   <li>The request contains invalid parameters</li>
 * </ul>
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class RefundException extends PaymuxException {

    public RefundException(String message) {
        super(message);
    }

    public RefundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefundException(int statusCode, String message) {
        super(message);
    }

    public RefundException(int statusCode, String message, Throwable cause) {
        super(message, cause);
    }
}
