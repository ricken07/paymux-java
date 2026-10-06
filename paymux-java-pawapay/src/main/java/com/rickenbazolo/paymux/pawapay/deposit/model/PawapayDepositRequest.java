package com.rickenbazolo.paymux.pawapay.deposit.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.core.operations.transfer.TransferRequest;
import com.rickenbazolo.paymux.pawapay.model.PawapayMetadata;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PawaPay deposit request ({@code POST /v2/deposits}): the customer is asked to pay the merchant.
 * <p>
 * A deposit is the PawaPay equivalent of a "request to pay" and implements the generic
 * {@link TransferRequest} contract. The customer receives a prompt on their phone (or an
 * authorisation flow, depending on the provider) and approves the payment.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayDepositRequest request = PawapayDepositRequest.builder()
 *     .amount("1000")
 *     .currency("XAF")
 *     .phoneNumber("242065551234")
 *     .provider(PawapayProviders.MTN_MOMO_COG)
 *     .customerMessage("Order 12345")
 *     .clientReferenceId("ORDER-12345")
 *     .addMetadata("orderId", "12345")
 *     .build();
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE)
public final class PawapayDepositRequest implements TransferRequest {

    private final String depositId;
    private final PawapayParty payer;
    private final String amount;
    private final String currency;
    private final String preAuthorisationCode;
    private final String clientReferenceId;
    private final String customerMessage;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayMetadata> metadata;

    private PawapayDepositRequest(Builder builder) {
        this.depositId = builder.depositId;
        this.payer = builder.payer;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.preAuthorisationCode = builder.preAuthorisationCode;
        this.clientReferenceId = builder.clientReferenceId;
        this.customerMessage = builder.customerMessage;
        this.metadata = List.copyOf(builder.metadata);
    }

    public String getDepositId() {
        return depositId;
    }

    public PawapayParty getPayer() {
        return payer;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPreAuthorisationCode() {
        return preAuthorisationCode;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public List<PawapayMetadata> getMetadata() {
        return metadata;
    }

    @Override
    public String amount() {
        return amount;
    }

    @Override
    public String currency() {
        return currency;
    }

    /**
     * @return the deposit id
     */
    @Override
    public String externalId() {
        return depositId;
    }

    /**
     * @return the payer's phone number (the customer being charged)
     */
    @Override
    public String recipientPhoneNumber() {
        return payer.phoneNumber();
    }

    @Override
    public String description() {
        return customerMessage;
    }

    @Override
    public String reference() {
        return clientReferenceId;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "PawapayDepositRequest{depositId='" + depositId + "', payer=" + payer + ", amount='" + amount
            + "', currency='" + currency + "', clientReferenceId='" + clientReferenceId + "'}";
    }

    /**
     * Builder for {@link PawapayDepositRequest}.
     */
    public static final class Builder {
        private String depositId;
        private String phoneNumber;
        private String provider;
        private PawapayParty payer;
        private String amount;
        private String currency;
        private String preAuthorisationCode;
        private String clientReferenceId;
        private String customerMessage;
        private final List<PawapayMetadata> metadata = new ArrayList<>();

        /**
         * Set the deposit id (UUID v4). Generated if not set.
         *
         * @param depositId the unique deposit id
         * @return this builder
         */
        public Builder depositId(String depositId) {
            this.depositId = depositId;
            return this;
        }

        /**
         * Set the deposit id (UUID v4). Generated if not set.
         *
         * @param depositId the unique deposit id
         * @return this builder
         */
        public Builder depositId(UUID depositId) {
            this.depositId = depositId != null ? depositId.toString() : null;
            return this;
        }

        /**
         * Set the amount as a string. Trailing decimal zeros are removed.
         *
         * @param amount the amount (e.g. "1000" or "10.5")
         * @return this builder
         */
        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Set the amount from a decimal value.
         *
         * @param amount the amount
         * @return this builder
         */
        public Builder amount(BigDecimal amount) {
            this.amount = amount != null ? amount.toPlainString() : null;
            return this;
        }

        /**
         * Set the ISO 4217 currency code (e.g. "XAF").
         *
         * @param currency the currency code
         * @return this builder
         */
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        /**
         * Set the payer's phone number in MSISDN format (digits only, country code included, e.g. "242065551234").
         *
         * @param phoneNumber the phone number
         * @return this builder
         */
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * Set the payer's provider code (e.g. {@code MTN_MOMO_COG}).
         *
         * @param provider the provider code
         * @return this builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Set the payer directly. Overrides {@link #phoneNumber(String)} and {@link #provider(String)}.
         *
         * @param payer the payer
         * @return this builder
         */
        public Builder payer(PawapayParty payer) {
            this.payer = payer;
            return this;
        }

        /**
         * Set the pre-authorisation code (OTP) required by providers using the {@code PREAUTH} authorisation type.
         *
         * @param preAuthorisationCode the code, 1 to 36 alphanumeric characters
         * @return this builder
         */
        public Builder preAuthorisationCode(String preAuthorisationCode) {
            this.preAuthorisationCode = preAuthorisationCode;
            return this;
        }

        /**
         * Set your own reference (invoice id, order id, ...). Returned in status responses and callbacks.
         *
         * @param clientReferenceId the reference
         * @return this builder
         */
        public Builder clientReferenceId(String clientReferenceId) {
            this.clientReferenceId = clientReferenceId;
            return this;
        }

        /**
         * Set the message shown to the customer (4 to 22 alphanumeric characters or spaces).
         *
         * @param customerMessage the message
         * @return this builder
         */
        public Builder customerMessage(String customerMessage) {
            this.customerMessage = customerMessage;
            return this;
        }

        /**
         * Replace the metadata entries (at most 10).
         *
         * @param metadata the metadata entries
         * @return this builder
         */
        public Builder metadata(List<PawapayMetadata> metadata) {
            this.metadata.clear();
            if (metadata != null) {
                this.metadata.addAll(metadata);
            }
            return this;
        }

        /**
         * Add a metadata entry.
         *
         * @param metadata the entry
         * @return this builder
         */
        public Builder addMetadata(PawapayMetadata metadata) {
            this.metadata.add(metadata);
            return this;
        }

        /**
         * Add a metadata entry.
         *
         * @param key   the field name
         * @param value the field value
         * @return this builder
         */
        public Builder addMetadata(String key, Object value) {
            return addMetadata(PawapayMetadata.of(key, value));
        }

        /**
         * Add a metadata entry flagged as personally identifiable information.
         *
         * @param key   the field name
         * @param value the field value
         * @return this builder
         */
        public Builder addPiiMetadata(String key, Object value) {
            return addMetadata(PawapayMetadata.pii(key, value));
        }

        /**
         * Build the request, validating it locally against the PawaPay constraints.
         *
         * @return a new request
         * @throws IllegalStateException    if a required field is missing
         * @throws IllegalArgumentException if a field is invalid
         */
        public PawapayDepositRequest build() {
            if (amount == null || amount.isBlank()) {
                throw new IllegalStateException("Amount is required");
            }
            if (currency == null || currency.isBlank()) {
                throw new IllegalStateException("Currency is required");
            }
            if (payer == null) {
                if (phoneNumber == null || phoneNumber.isBlank()) {
                    throw new IllegalStateException("Payer phone number is required");
                }
                if (provider == null || provider.isBlank()) {
                    throw new IllegalStateException("Payer provider is required");
                }
                payer = PawapayParty.mmo(phoneNumber, provider);
            }

            depositId = depositId == null || depositId.isBlank()
                ? UUID.randomUUID().toString()
                : PawapayValidation.requireUuid(depositId, "depositId");
            amount = PawapayValidation.validateAmount(PawapayValidation.normalizeAmount(amount));
            currency = PawapayValidation.validateCurrency(currency);
            PawapayValidation.validatePhoneNumber(payer.phoneNumber());
            PawapayValidation.validateProvider(payer.provider());
            preAuthorisationCode = PawapayValidation.validatePreAuthorisationCode(preAuthorisationCode);
            customerMessage = PawapayValidation.validateCustomerMessage(customerMessage);
            PawapayValidation.validateMetadata(metadata);

            return new PawapayDepositRequest(this);
        }
    }
}
