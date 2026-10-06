package com.rickenbazolo.paymux.pawapay.exception;

import com.rickenbazolo.paymux.core.exception.PaymuxHttpException;

/**
 * Exception thrown when the PawaPay API answers with a non-2xx HTTP status.
 * <p>
 * PawaPay error bodies have the shape
 * {@code {"status": "REJECTED", "failureReason": {"failureCode": "...", "failureMessage": "..."}}}.
 * When present, the failure code and message are extracted and exposed by this exception.
 * </p>
 * <p>
 * Note that a {@code 200 OK} carrying {@code "status": "REJECTED"} (business rejection of an
 * initiation) is <strong>not</strong> reported through this exception: the rejection is returned
 * as a regular response object so that the application can decide what to do.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayApiException extends PaymuxHttpException {

    private final int statusCode;
    private final String failureCode;
    private final String failureMessage;
    private final String responseBody;

    public PawapayApiException(int statusCode, String failureCode, String failureMessage, String responseBody) {
        super(statusCode, buildMessage(statusCode, failureCode, failureMessage));
        this.statusCode = statusCode;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.responseBody = responseBody;
    }

    private static String buildMessage(int statusCode, String failureCode, String failureMessage) {
        var sb = new StringBuilder("PawaPay API error: HTTP ").append(statusCode);
        if (failureCode != null && !failureCode.isBlank()) {
            sb.append(" [").append(failureCode).append(']');
        }
        if (failureMessage != null && !failureMessage.isBlank()) {
            sb.append(' ').append(failureMessage);
        }
        return sb.toString();
    }

    /**
     * @return the HTTP status code returned by PawaPay
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the PawaPay failure code (e.g. {@code AUTHENTICATION_ERROR}), or null if absent
     */
    public String getFailureCode() {
        return failureCode;
    }

    /**
     * @return the PawaPay failure message, or null if absent
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * @return the raw response body, or null if empty
     */
    public String getResponseBody() {
        return responseBody;
    }
}
