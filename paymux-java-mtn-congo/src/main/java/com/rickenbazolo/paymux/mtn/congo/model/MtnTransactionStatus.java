package com.rickenbazolo.paymux.mtn.congo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponseStatus;

/**
 * Represents the status of an MTN Mobile Money transaction.
 * <p>
 * This model is returned when querying the status of a Request to Pay transaction.
 * </p>
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class MtnTransactionStatus implements TransferResponseStatus {

    @JsonProperty("financialTransactionId")
    private String financialTransactionId;

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("payer")
    private Payer payer;

    @JsonProperty("payerMessage")
    private String payerMessage;

    @JsonProperty("payeeNote")
    private String payeeNote;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reason")
    private String reason;

    // Default constructor for Jackson
    public MtnTransactionStatus() {
    }

    public String getFinancialTransactionId() {
        return financialTransactionId;
    }

    public void setFinancialTransactionId(String financialTransactionId) {
        this.financialTransactionId = financialTransactionId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    public String getPayerMessage() {
        return payerMessage;
    }

    public void setPayerMessage(String payerMessage) {
        this.payerMessage = payerMessage;
    }

    public String getPayeeNote() {
        return payeeNote;
    }

    public void setPayeeNote(String payeeNote) {
        this.payeeNote = payeeNote;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Check if the transaction is pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    /**
     * Check if the transaction is successful.
     *
     * @return true if status is SUCCESSFUL
     */
    public boolean isSuccessful() {
        return "SUCCESSFUL".equalsIgnoreCase(status);
    }

    /**
     * Check if the transaction has failed.
     *
     * @return true if status is FAILED
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }

    @Override
    public String transactionId() {
        return this.externalId;
    }

    @Override
    public String status() {
        return this.status;
    }

    @Override
    public String failureReason() {
        return this.reason;
    }

    /**
     * Payer information in the transaction status.
     */
    public static class Payer {
        @JsonProperty("partyIdType")
        private String partyIdType;

        @JsonProperty("partyId")
        private String partyId;

        public Payer() {
        }

        public String getPartyIdType() {
            return partyIdType;
        }

        public void setPartyIdType(String partyIdType) {
            this.partyIdType = partyIdType;
        }

        public String getPartyId() {
            return partyId;
        }

        public void setPartyId(String partyId) {
            this.partyId = partyId;
        }
    }
}
