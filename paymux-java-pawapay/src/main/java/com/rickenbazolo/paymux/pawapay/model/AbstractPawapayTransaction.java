package com.rickenbazolo.paymux.pawapay.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Common shape of a PawaPay deposit, payout or refund as returned by the status endpoints
 * and delivered in callbacks.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public abstract class AbstractPawapayTransaction {

    private final String transactionId;
    private final PawapayTransactionStatus status;
    private final String amount;
    private final String currency;
    private final String country;
    private final PawapayParty party;
    private final String customerMessage;
    private final String clientReferenceId;
    private final String created;
    private final String providerTransactionId;
    private final PawapayFailureReason failureReason;
    private final Map<String, Object> metadata;

    protected AbstractPawapayTransaction(String transactionId, PawapayTransactionStatus status, String amount,
                                         String currency, String country, PawapayParty party,
                                         String customerMessage, String clientReferenceId, String created,
                                         String providerTransactionId, PawapayFailureReason failureReason,
                                         Map<String, Object> metadata) {
        this.transactionId = transactionId;
        this.status = status != null ? status : PawapayTransactionStatus.UNKNOWN;
        this.amount = amount;
        this.currency = currency;
        this.country = country;
        this.party = party;
        this.customerMessage = customerMessage;
        this.clientReferenceId = clientReferenceId;
        this.created = created;
        this.providerTransactionId = providerTransactionId;
        this.failureReason = failureReason;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
    }

    /**
     * @return the id of the deposit, payout or refund
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @return the PawaPay lifecycle status
     */
    public PawapayTransactionStatus getStatus() {
        return status;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    /**
     * @return the ISO 3166-1 alpha-3 country code
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return the payer (deposit) or recipient (payout / refund)
     */
    protected PawapayParty getParty() {
        return party;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
    }

    /**
     * @return the RFC 3339 creation timestamp
     */
    public String getCreated() {
        return created;
    }

    /**
     * @return the creation timestamp parsed as an instant, if present and valid
     */
    public Optional<Instant> createdInstant() {
        return AbstractPawapayInitiationResponse.parseInstant(created);
    }

    /**
     * @return the transaction id on the provider side, present once completed
     */
    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    /**
     * @return the failure reason, present when the status is {@code FAILED}
     */
    public PawapayFailureReason getFailureReason() {
        return failureReason;
    }

    /**
     * @return the metadata provided at initiation, as a flat map (never null)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isCompleted() {
        return status == PawapayTransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PawapayTransactionStatus.FAILED;
    }

    /**
     * @return true when the status is final ({@code COMPLETED} or {@code FAILED})
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    protected String describeFailure() {
        return failureReason != null ? failureReason.describe() : null;
    }

    protected String defaultMessage(String noun) {
        return switch (status) {
            case COMPLETED -> noun + " completed successfully.";
            case FAILED -> {
                String reason = describeFailure();
                yield noun + " failed" + (reason != null ? ": " + reason : ".");
            }
            case ACCEPTED -> noun + " accepted by PawaPay, waiting to be processed.";
            case ENQUEUED -> noun + " enqueued; the provider is temporarily unavailable.";
            case PROCESSING -> noun + " is being processed by the provider.";
            case IN_RECONCILIATION -> noun + " is being reconciled with the provider.";
            default -> noun + " has an unknown status.";
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{transactionId='" + transactionId + "', status=" + status
            + ", amount='" + amount + "', currency='" + currency + "', country='" + country
            + "', party=" + party + ", providerTransactionId='" + providerTransactionId
            + "', failureReason=" + failureReason + '}';
    }
}
