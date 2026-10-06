package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Reason of a PawaPay rejection or failure.
 * <p>
 * Failure codes are an open set maintained by PawaPay. Frequently seen codes include:
 * <ul>
 *   <li>Initiation: {@code INVALID_PHONE_NUMBER}, {@code INVALID_AMOUNT}, {@code AMOUNT_OUT_OF_BOUNDS},
 *       {@code INVALID_CURRENCY}, {@code INVALID_PROVIDER}, {@code PROVIDER_TEMPORARILY_UNAVAILABLE},
 *       {@code DEPOSITS_NOT_ALLOWED}, {@code PAYOUTS_NOT_ALLOWED}, {@code REFUNDS_NOT_ALLOWED}, {@code REMITTANCES_NOT_ALLOWED},
 *       {@code PAWAPAY_WALLET_OUT_OF_FUNDS}, {@code NOT_FOUND}, {@code INVALID_STATE}</li>
 *   <li>Authentication: {@code NO_AUTHENTICATION}, {@code AUTHENTICATION_ERROR},
 *       {@code AUTHORISATION_ERROR}, {@code HTTP_SIGNATURE_ERROR}</li>
 *   <li>Deposit processing: {@code PAYER_NOT_FOUND}, {@code PAYMENT_NOT_APPROVED},
 *       {@code PAYER_LIMIT_REACHED}, {@code PAYMENT_IN_PROGRESS}, {@code INSUFFICIENT_BALANCE},
 *       {@code WALLET_LIMIT_REACHED}, {@code UNSPECIFIED_FAILURE}, {@code UNKNOWN_ERROR}</li>
 *   <li>Payout / refund / remittance processing: {@code PAWAPAY_WALLET_OUT_OF_FUNDS}, {@code RECIPIENT_NOT_FOUND},
 *       {@code WALLET_LIMIT_REACHED}, {@code MANUALLY_CANCELLED}, {@code UNSPECIFIED_FAILURE},
 *       {@code UNKNOWN_ERROR}</li>
 * </ul>
 * </p>
 *
 * @param failureCode    the PawaPay failure code
 * @param failureMessage the human-readable description
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayFailureReason(String failureCode, String failureMessage) {

    /**
     * Formats the reason as {@code "CODE: message"} (or whichever part is present).
     *
     * @return a single line description, or null if both parts are absent
     */
    public String describe() {
        boolean hasCode = failureCode != null && !failureCode.isBlank();
        boolean hasMessage = failureMessage != null && !failureMessage.isBlank();
        if (hasCode && hasMessage) {
            return failureCode + ": " + failureMessage;
        }
        if (hasCode) {
            return failureCode;
        }
        return hasMessage ? failureMessage : null;
    }
}
