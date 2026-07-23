package com.rickenbazolo.paymux.core.exception;

/**
 * Exception thrown when a cash-in (deposit) operation fails.
 * <p>
 * This exception is thrown when:
 * <ul>
 *   <li>The recipient account is invalid or does not exist</li>
 *   <li>The provider's API returns an error</li>
 *   <li>Network errors occur during the request</li>
 *   <li>The merchant does not have sufficient balance or permissions</li>
 *   <li>The request contains invalid parameters</li>
 * </ul>
 * </p>
 *
 * <p>Example handling:</p>
 * <pre>{@code
 * try {
 *     CashinResponse response = client.cashin(request);
 * } catch (CashinException e) {
 *     logger.error("Cash-in failed: {}", e.getMessage());
 *     // Handle the error, possibly retry or notify the user
 * }
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class CashinException extends PaymuxException {

    /**
     * Constructs a new cash-in exception with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public CashinException(String message) {
        super(message);
    }

    /**
     * Constructs a new cash-in exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of this exception
     */
    public CashinException(String message, Throwable cause) {
        super(message, cause);
    }
}
