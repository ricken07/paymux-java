package com.rickenbazolo.paymux.pawapay.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.util.Optional;

/**
 * Response of a statement initiation ({@code POST /v2/statements}).
 *
 * @param statementId   the statement id assigned by PawaPay (present when accepted)
 * @param status        {@code ACCEPTED} or {@code REJECTED}
 * @param created       the RFC 3339 creation timestamp (present when accepted)
 * @param failureReason the rejection reason (present when rejected: {@code INVALID_CALLBACK_URL},
 *                      {@code INVALID_DATE_RANGE}, {@code WALLET_NOT_FOUND}, ...)
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayStatementResponse(
    String statementId,
    PawapayInitiationStatus status,
    String created,
    PawapayFailureReason failureReason
) {

    public PawapayStatementResponse {
        status = status != null ? status : PawapayInitiationStatus.UNKNOWN;
    }

    public boolean isAccepted() {
        return status == PawapayInitiationStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == PawapayInitiationStatus.REJECTED;
    }

    /**
     * @return the creation timestamp parsed as an instant, if present and valid
     */
    public Optional<Instant> createdInstant() {
        return PawapayDates.parseInstant(created);
    }

    /**
     * @return the failure description, or null when accepted
     */
    public String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }
}
