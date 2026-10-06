package com.rickenbazolo.paymux.pawapay.deposit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponseStatus;
import com.rickenbazolo.paymux.pawapay.model.AbstractPawapayTransaction;
import com.rickenbazolo.paymux.pawapay.model.PawapayFailureReason;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;

import java.util.Map;

/**
 * A PawaPay deposit, as returned by {@code GET /v2/deposits/{depositId}} and delivered in
 * deposit callbacks (both share the same JSON shape).
 * <p>
 * {@link #status()} exposes the generic Paymux status name ({@code PENDING}, {@code SUCCESSFUL},
 * {@code FAILED}); {@link #getStatus()} exposes the raw PawaPay lifecycle status.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PawapayDeposit extends AbstractPawapayTransaction implements TransferResponseStatus {

    @JsonCreator
    public PawapayDeposit(@JsonProperty("depositId") String depositId,
                          @JsonProperty("status") PawapayTransactionStatus status,
                          @JsonProperty("amount") String amount,
                          @JsonProperty("currency") String currency,
                          @JsonProperty("country") String country,
                          @JsonProperty("payer") PawapayParty payer,
                          @JsonProperty("customerMessage") String customerMessage,
                          @JsonProperty("clientReferenceId") String clientReferenceId,
                          @JsonProperty("created") String created,
                          @JsonProperty("providerTransactionId") String providerTransactionId,
                          @JsonProperty("failureReason") PawapayFailureReason failureReason,
                          @JsonProperty("metadata") Map<String, Object> metadata) {
        super(depositId, status, amount, currency, country, payer, customerMessage, clientReferenceId,
            created, providerTransactionId, failureReason, metadata);
    }

    /**
     * @return the deposit id
     */
    public String getDepositId() {
        return getTransactionId();
    }

    /**
     * @return the customer who paid
     */
    public PawapayParty getPayer() {
        return getParty();
    }

    @Override
    public String transactionId() {
        return getTransactionId();
    }

    @Override
    public String status() {
        return getStatus().toMoMoStatus().name();
    }

    @Override
    public String failureReason() {
        return describeFailure();
    }

    /**
     * @return a human-readable description of the current status
     */
    public String message() {
        return defaultMessage("Deposit");
    }
}
