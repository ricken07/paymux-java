package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayTransaction;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;

import java.util.Map;

/**
 * A PawaPay remittance, as returned by {@code GET /v2/remittances/{remittanceId}} and delivered in
 * remittance callbacks (both share the same JSON shape).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayRemittance extends AbstractPawapayTransaction implements CashoutResponse {

    private final PawapayRemittanceRecipient recipient;
    private final PawapayRemittanceSender sender;

    @JsonCreator
    public PawapayRemittance(@JsonProperty("remittanceId") String remittanceId,
                             @JsonProperty("status") PawapayTransactionStatus status,
                             @JsonProperty("amount") String amount,
                             @JsonProperty("currency") String currency,
                             @JsonProperty("country") String country,
                             @JsonProperty("recipient") PawapayRemittanceRecipient recipient,
                             @JsonProperty("sender") PawapayRemittanceSender sender,
                             @JsonProperty("customerMessage") String customerMessage,
                             @JsonProperty("clientReferenceId") String clientReferenceId,
                             @JsonProperty("created") String created,
                             @JsonProperty("providerTransactionId") String providerTransactionId,
                             @JsonProperty("failureReason") PawapayFailureReason failureReason,
                             @JsonProperty("metadata") Map<String, Object> metadata) {
        super(remittanceId, status, amount, currency, country, recipient != null ? recipient.toParty() : null,
            customerMessage, clientReferenceId, created, providerTransactionId, failureReason, metadata);
        this.recipient = recipient;
        this.sender = sender;
    }

    /**
     * @return the remittance id
     */
    public String getRemittanceId() {
        return getTransactionId();
    }

    /**
     * @return the recipient, with account details and name
     */
    public PawapayRemittanceRecipient getRecipient() {
        return recipient;
    }

    /**
     * @return the sender, when PawaPay includes it
     */
    public PawapayRemittanceSender getSender() {
        return sender;
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
        return defaultMessage("Remittance");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
