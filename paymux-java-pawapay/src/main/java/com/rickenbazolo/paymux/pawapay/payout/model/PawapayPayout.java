package com.rickenbazolo.paymux.pawapay.payout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayTransaction;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;

import java.util.Map;

/**
 * A PawaPay payout, as returned by {@code GET /v2/payouts/{payoutId}} and delivered in
 * payout callbacks (both share the same JSON shape).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayPayout extends AbstractPawapayTransaction implements CashoutResponse {

    @JsonCreator
    public PawapayPayout(@JsonProperty("payoutId") String payoutId,
                         @JsonProperty("status") PawapayTransactionStatus status,
                         @JsonProperty("amount") String amount,
                         @JsonProperty("currency") String currency,
                         @JsonProperty("country") String country,
                         @JsonProperty("recipient") PawapayParty recipient,
                         @JsonProperty("customerMessage") String customerMessage,
                         @JsonProperty("clientReferenceId") String clientReferenceId,
                         @JsonProperty("created") String created,
                         @JsonProperty("providerTransactionId") String providerTransactionId,
                         @JsonProperty("failureReason") PawapayFailureReason failureReason,
                         @JsonProperty("metadata") Map<String, Object> metadata) {
        super(payoutId, status, amount, currency, country, recipient, customerMessage, clientReferenceId,
            created, providerTransactionId, failureReason, metadata);
    }

    /**
     * @return the payout id
     */
    public String getPayoutId() {
        return getTransactionId();
    }

    /**
     * @return the customer who received the money
     */
    public PawapayParty getRecipient() {
        return getParty();
    }

    @Override
    public String transactionId() {
        return getTransactionId();
    }

    @Override
    public MoMoTransferStatus status() {
        return getStatus().toMoMoStatus();
    }

    @Override
    public String message() {
        return defaultMessage("Payout");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
