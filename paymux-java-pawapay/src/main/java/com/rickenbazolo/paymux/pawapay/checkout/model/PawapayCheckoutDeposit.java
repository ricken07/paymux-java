package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * One payment attempt inside a checkout.
 * <p>
 * A checkout can contain multiple attempts (the customer retries after a failure); at most one
 * can complete successfully. This shape is used both for the checkout's latest attempt
 * ({@code deposit}) and for each entry of its full history ({@code depositsHistory}). Each attempt
 * also behaves like a standard PawaPay deposit and can be looked up with the same id through
 * {@code PawapayClient#findDeposit(String)}.
 * </p>
 *
 * @param depositId             the deposit id of this attempt
 * @param status                the attempt status ({@code ACCEPTED}, {@code PROCESSING}, {@code COMPLETED} or {@code FAILED})
 * @param created               RFC 3339 creation timestamp
 * @param providerTransactionId the transaction id on the provider side, present once processed
 * @param failureReason         the failure reason, present when the status is {@code FAILED}
 * @param amount                the amount actually paid in this attempt
 * @param currency              the ISO 4217 currency of this attempt
 * @param country               the ISO 3166-1 alpha-3 country of this attempt
 * @param payer                 the customer's mobile money account used for this attempt
 * @param customerMessage       the narration shown to the customer
 * @param metadata              the metadata attached at checkout creation, as a flat map (never null)
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayCheckoutDeposit(
    String depositId,
    PawapayTransactionStatus status,
    String created,
    String providerTransactionId,
    PawapayFailureReason failureReason,
    String amount,
    String currency,
    String country,
    PawapayParty payer,
    String customerMessage,
    Map<String, Object> metadata
) {

    public PawapayCheckoutDeposit {
        status = status != null ? status : PawapayTransactionStatus.UNKNOWN;
        metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
    }

    public boolean isCompleted() {
        return status == PawapayTransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PawapayTransactionStatus.FAILED;
    }

    public Optional<Instant> createdInstant() {
        return PawapayDates.parseInstant(created);
    }

    /**
     * @return the failure description, or null when not failed
     */
    public String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }
}
