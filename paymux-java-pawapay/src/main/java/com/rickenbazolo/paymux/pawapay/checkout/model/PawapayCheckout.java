package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A PawaPay checkout, as returned by {@code GET /v2/checkouts/{checkoutId}} and delivered in
 * checkout callbacks (both share the same JSON shape).
 * <p>
 * A checkout has its own lifecycle ({@link #status()}), independent of the individual payment
 * attempts inside it. {@link #deposit()} holds the latest attempt (when one exists) and
 * {@link #depositsHistory()} the full history; at most one attempt can complete successfully.
 * {@link #depositStatus()} mirrors the status of the latest attempt.
 * </p>
 *
 * @param checkoutId            the checkout id
 * @param status                the checkout lifecycle status
 * @param redirectUrl           the URL of the hosted payment page
 * @param returnUrl             the URL the customer is sent back to
 * @param returnMethod          how the customer is returned to {@code returnUrl}
 * @param defaultLanguage       the language the hosted payment page was opened in
 * @param countries             the countries the checkout allowed (never null)
 * @param expiresAfter          minutes after creation when the checkout expires
 * @param amounts               the fixed amount options offered, if any (never null)
 * @param payer                 the pre-filled payer, if any
 * @param clientReferenceId     your reference for this checkout
 * @param created               RFC 3339 creation timestamp
 * @param providerTransactionId the transaction id on the provider side of the latest attempt
 * @param depositStatus         aggregate status mirroring the latest payment attempt
 * @param deposit                the latest payment attempt, if one exists
 * @param depositsHistory       the full history of payment attempts (never null)
 * @param metadata              the metadata provided at creation, as a flat map (never null)
 * @param reason                the localized reason shown to the customer, keyed by language (never null)
 * @param checkoutCode          the code appended to {@code returnUrl} when the customer returns
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayCheckout(
    String checkoutId,
    PawapayCheckoutStatus status,
    String redirectUrl,
    String returnUrl,
    PawapayReturnMethod returnMethod,
    PawapayCheckoutLanguage defaultLanguage,
    List<String> countries,
    Integer expiresAfter,
    List<PawapayCheckoutAmount> amounts,
    PawapayCheckoutPayer payer,
    String clientReferenceId,
    String created,
    String providerTransactionId,
    PawapayTransactionStatus depositStatus,
    PawapayCheckoutDeposit deposit,
    List<PawapayCheckoutDeposit> depositsHistory,
    Map<String, Object> metadata,
    Map<String, String> reason,
    String checkoutCode
) {

    public PawapayCheckout {
        status = status != null ? status : PawapayCheckoutStatus.UNKNOWN;
        countries = countries != null ? List.copyOf(countries) : List.of();
        amounts = amounts != null ? List.copyOf(amounts) : List.of();
        depositsHistory = depositsHistory != null ? List.copyOf(depositsHistory) : List.of();
        metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
        reason = reason != null ? Collections.unmodifiableMap(reason) : Map.of();
    }

    public boolean isCompleted() {
        return status == PawapayCheckoutStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PawapayCheckoutStatus.FAILED;
    }

    /**
     * @return true when the status is final ({@code COMPLETED}, {@code FAILED}, {@code EXPIRED} or {@code CANCELLED})
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    public Optional<Instant> createdInstant() {
        return PawapayDates.parseInstant(created);
    }
}
