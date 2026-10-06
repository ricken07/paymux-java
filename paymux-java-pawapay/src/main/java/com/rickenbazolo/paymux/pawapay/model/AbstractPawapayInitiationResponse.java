package com.rickenbazolo.paymux.pawapay.model;

import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.util.Optional;

/**
 * Common shape of the response returned when a deposit, payout or refund is initiated.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public abstract class AbstractPawapayInitiationResponse {

    private final String transactionId;
    private final PawapayInitiationStatus status;
    private final String created;
    private final PawapayFailureReason failureReason;

    protected AbstractPawapayInitiationResponse(String transactionId, PawapayInitiationStatus status,
                                                String created, PawapayFailureReason failureReason) {
        this.transactionId = transactionId;
        this.status = status != null ? status : PawapayInitiationStatus.UNKNOWN;
        this.created = created;
        this.failureReason = failureReason;
    }

    /**
     * @return the id of the deposit, payout or refund, as provided in the request
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @return the PawaPay initiation status
     */
    public PawapayInitiationStatus getStatus() {
        return status;
    }

    /**
     * @return the RFC 3339 creation timestamp, or null when rejected
     */
    public String getCreated() {
        return created;
    }

    /**
     * @return the creation timestamp parsed as an instant, if present and valid
     */
    public Optional<Instant> createdInstant() {
        return parseInstant(created);
    }

    /**
     * @return the rejection reason, present when the status is {@code REJECTED}
     */
    public PawapayFailureReason getFailureReason() {
        return failureReason;
    }

    public boolean isAccepted() {
        return status == PawapayInitiationStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == PawapayInitiationStatus.REJECTED;
    }

    public boolean isDuplicateIgnored() {
        return status == PawapayInitiationStatus.DUPLICATE_IGNORED;
    }

    protected String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }

    protected String defaultMessage(String noun) {
        return switch (status) {
            case ACCEPTED -> noun + " accepted by PawaPay and is being processed.";
            case DUPLICATE_IGNORED -> noun + " with this id was already initiated; duplicate ignored.";
            case REJECTED -> {
                String reason = describeFailure();
                yield noun + " rejected by PawaPay" + (reason != null ? ": " + reason : ".");
            }
            default -> noun + " returned an unknown status.";
        };
    }

    static Optional<Instant> parseInstant(String value) {
        return PawapayDates.parseInstant(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{transactionId='" + transactionId + "', status=" + status
            + ", created='" + created + "', failureReason=" + failureReason + '}';
    }
}
