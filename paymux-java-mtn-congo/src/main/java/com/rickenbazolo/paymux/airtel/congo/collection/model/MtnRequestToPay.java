package com.rickenbazolo.paymux.airtel.congo.collection.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.core.operations.transfer.TransferRequest;

/**
 * Represents an MTN Mobile Money Request to Pay request.
 * <p>
 * This model is used to initiate a payment request to a customer's mobile money account.
 * </p>
 * @author Ricken Bazolo
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MtnRequestToPay implements TransferRequest {

    private final String amount;
    private final String currency;
    private final String externalId;
    private final Payer payer;
    private final String payerMessage;
    private final String payeeNote;

    private MtnRequestToPay(Builder builder) {
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.externalId = builder.externalId;
        this.payer = builder.payer;
        this.payerMessage = builder.payerMessage;
        this.payeeNote = builder.payeeNote;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getExternalId() {
        return externalId;
    }

    public Payer getPayer() {
        return payer;
    }

    public String getPayerMessage() {
        return payerMessage;
    }

    public String getPayeeNote() {
        return payeeNote;
    }

    @Override
    public String amount() {
        return amount;
    }

    @Override
    public String currency() {
        return currency;
    }

    @Override
    public String externalId() {
        return externalId;
    }

    @Override
    public String recipientPhoneNumber() {
        return payer.partyId;
    }

    /**
     * Creates a new builder for MtnRequestToPay.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents the payer information in a Request to Pay.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Payer {
        private final String partyIdType;
        private final String partyId;

        private Payer(String partyIdType, String partyId) {
            this.partyIdType = partyIdType;
            this.partyId = partyId;
        }

        public String getPartyIdType() {
            return partyIdType;
        }

        public String getPartyId() {
            return partyId;
        }

        /**
         * Create a Payer with MSISDN type (phone number).
         *
         * @param phoneNumber the phone number with country code (e.g., "242065551234")
         * @return a new Payer instance
         */
        public static Payer msisdn(String phoneNumber) {
            return new Payer("MSISDN", phoneNumber);
        }
    }

    /**
     * Builder for MtnRequestToPay.
     */
    public static class Builder {
        private String amount;
        private String currency = "XAF"; // Default currency for Congo
        private String externalId;
        private Payer payer;
        private String payerMessage;
        private String payeeNote;

        /**
         * Set the amount to pay.
         * <p>
         * Note: Amount must be a string representation of the number.
         * </p>
         *
         * @param amount the amount as string
         * @return this builder
         */
        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Set the amount to pay from a double value.
         *
         * @param amount the amount as double
         * @return this builder
         */
        public Builder amount(double amount) {
            this.amount = String.valueOf(amount);
            return this;
        }

        /**
         * Set the currency.
         * <p>
         * Default is "XAF" for Congo-Brazzaville.
         * </p>
         *
         * @param currency the currency code
         * @return this builder
         */
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        /**
         * Set the external ID (reference ID).
         * <p>
         * This should be a unique UUID for each transaction.
         * </p>
         *
         * @param externalId the external ID
         * @return this builder
         */
        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        /**
         * Set the payer information.
         *
         * @param payer the payer
         * @return this builder
         */
        public Builder payer(Payer payer) {
            this.payer = payer;
            return this;
        }

        /**
         * Set the payer using a phone number with country code.
         * <p>
         * The phone number should include the country code (e.g., "242065551234").
         * </p>
         *
         * @param phoneNumber the phone number with country code
         * @return this builder
         */
        public Builder payerPhone(String phoneNumber) {
            this.payer = Payer.msisdn(phoneNumber);
            return this;
        }

        /**
         * Set the message to display to the payer.
         *
         * @param payerMessage the message for the payer
         * @return this builder
         */
        public Builder payerMessage(String payerMessage) {
            this.payerMessage = payerMessage;
            return this;
        }

        /**
         * Set the note for the payee (merchant).
         *
         * @param payeeNote the note for the payee
         * @return this builder
         */
        public Builder payeeNote(String payeeNote) {
            this.payeeNote = payeeNote;
            return this;
        }

        /**
         * Build the MtnRequestToPay instance.
         *
         * @return a new MtnRequestToPay instance
         * @throws IllegalStateException if required fields are missing
         */
        public MtnRequestToPay build() {
            if (amount == null || amount.isBlank()) {
                throw new IllegalStateException("Amount is required");
            }
            if (currency == null || currency.isBlank()) {
                throw new IllegalStateException("Currency is required");
            }
            if (externalId == null || externalId.isBlank()) {
                throw new IllegalStateException("External ID is required");
            }
            if (payer == null) {
                throw new IllegalStateException("Payer is required");
            }

            return new MtnRequestToPay(this);
        }
    }
}
