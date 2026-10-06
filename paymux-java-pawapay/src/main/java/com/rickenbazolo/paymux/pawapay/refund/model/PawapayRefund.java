package com.rickenbazolo.paymux.pawapay.refund.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.refund.RefundResponse;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayTransaction;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;

import java.util.Map;

/**
 * A PawaPay refund, as returned by {@code GET /v2/refunds/{refundId}} and delivered in
 * refund callbacks (both share the same JSON shape).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayRefund extends AbstractPawapayTransaction implements RefundResponse {

    private final String depositId;

    @JsonCreator
    public PawapayRefund(@JsonProperty("refundId") String refundId,
                         @JsonProperty("depositId") String depositId,
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
        super(refundId, status, amount, currency, country, recipient, customerMessage, clientReferenceId,
            created, providerTransactionId, failureReason, metadata);
        this.depositId = depositId;
    }

    /**
     * @return the refund id
     */
    public String getRefundId() {
        return getTransactionId();
    }

    /**
     * @return the id of the refunded deposit, when PawaPay includes it
     */
    public String getDepositId() {
        return depositId;
    }

    /**
     * @return the customer who received the refund
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
        return defaultMessage("Refund");
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }
}
