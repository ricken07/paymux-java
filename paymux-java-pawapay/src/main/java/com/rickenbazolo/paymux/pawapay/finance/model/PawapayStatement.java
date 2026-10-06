package com.rickenbazolo.paymux.pawapay.finance.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A wallet statement, as returned by {@code GET /v2/statements/{statementId}} and delivered in
 * statement callbacks (both share the same JSON shape).
 * <p>
 * When {@code COMPLETED}, the CSV (or {@code .csv.gz}) file can be fetched from
 * {@link #downloadUrl()} until {@link #downloadUrlExpiresAt()}; after that a new statement must
 * be generated. Downloading the file is left to the application.
 * </p>
 *
 * @param statementId          the statement id
 * @param status               the generation status
 * @param wallet               the wallet the statement was generated for
 * @param created              RFC 3339 timestamp of the request
 * @param startDate            start of the period (UTC, zone-less)
 * @param endDate              end of the period (UTC, zone-less)
 * @param fileSize             size of the generated file in bytes, when completed
 * @param downloadUrl          URL of the generated file, when completed
 * @param downloadUrlExpiresAt expiry of the download URL, when completed
 * @param completedAt          when the generation completed
 * @param failedAt             when the generation failed
 * @param failureReason        the failure reason, when failed
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayStatement(
    String statementId,
    PawapayStatementStatus status,
    PawapayWallet wallet,
    String created,
    String startDate,
    String endDate,
    @JsonAlias("fileSIze") Long fileSize,
    String downloadUrl,
    String downloadUrlExpiresAt,
    String completedAt,
    String failedAt,
    PawapayFailureReason failureReason
) {

    public PawapayStatement {
        status = status != null ? status : PawapayStatementStatus.UNKNOWN;
    }

    public boolean isCompleted() {
        return status == PawapayStatementStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PawapayStatementStatus.FAILED;
    }

    /**
     * @return true when the status is final ({@code COMPLETED} or {@code FAILED})
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    /**
     * @return true when a download URL is present and not expired
     */
    public boolean isDownloadable() {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            return false;
        }
        return downloadUrlExpiresAtInstant().map(expiry -> expiry.isAfter(Instant.now())).orElse(true);
    }

    public Optional<Instant> createdInstant() {
        return PawapayDates.parseInstant(created);
    }

    public Optional<LocalDateTime> startDateTime() {
        return PawapayDates.parseUtcLocalDateTime(startDate);
    }

    public Optional<LocalDateTime> endDateTime() {
        return PawapayDates.parseUtcLocalDateTime(endDate);
    }

    public Optional<Instant> downloadUrlExpiresAtInstant() {
        return PawapayDates.parseInstant(downloadUrlExpiresAt);
    }

    public Optional<Instant> completedAtInstant() {
        return PawapayDates.parseInstant(completedAt);
    }

    public Optional<Instant> failedAtInstant() {
        return PawapayDates.parseInstant(failedAt);
    }

    /**
     * @return the failure description, or null when not failed
     */
    public String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }
}
