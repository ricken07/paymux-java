package com.rickenbazolo.paymux.pawapay.payout.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutRequest;
import com.rickenbazolo.paymux.pawapay.model.PawapayMetadata;
import com.rickenbazolo.paymux.pawapay.model.PawapayParty;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PawaPay payout request ({@code POST /v2/payouts}): the merchant sends money to a customer.
 * <p>
 * Implements the generic {@link CashoutRequest} contract.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayPayoutRequest request = PawapayPayoutRequest.builder()
 *     .amount("15000")
 *     .currency("XAF")
 *     .phoneNumber("242065551234")
 *     .provider(PawapayProviders.MTN_MOMO_COG)
 *     .customerMessage("Salary 2024 01")
 *     .clientReferenceId("PAYOUT-2024-001")
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
public final class PawapayPayoutRequest implements CashoutRequest {

    private final String payoutId;
    private final PawapayParty recipient;
    private final String amount;
    private final String currency;
    private final String customerMessage;
    private final String clientReferenceId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayMetadata> metadata;

    private PawapayPayoutRequest(Builder builder) {
        this.payoutId = builder.payoutId;
        this.recipient = builder.recipient;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.customerMessage = builder.customerMessage;
        this.clientReferenceId = builder.clientReferenceId;
        this.metadata = List.copyOf(builder.metadata);
    }

    public String getPayoutId() {
        return payoutId;
    }

    public PawapayParty getRecipient() {
        return recipient;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
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

    @Override
    public String recipientPhoneNumber() {
        return recipient.phoneNumber();
    }

    @Override
    public String reference() {
        return clientReferenceId;
    }

    @Override
    public String description() {
        return customerMessage;
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
        return "PawapayPayoutRequest{payoutId='" + payoutId + "', recipient=" + recipient + ", amount='" + amount
            + "', currency='" + currency + "', clientReferenceId='" + clientReferenceId + "'}";
    }

    /**
     * Builder for {@link PawapayPayoutRequest}.
     */
    public static final class Builder {
        private String payoutId;
        private String phoneNumber;
        private String provider;
        private PawapayParty recipient;
        private String amount;
        private String currency;
        private String customerMessage;
        private String clientReferenceId;
        private final List<PawapayMetadata> metadata = new ArrayList<>();

        /**
         * Set the payout id (UUID v4). Generated if not set.
         *
         * @param payoutId the unique payout id
         * @return this builder
         */
        public Builder payoutId(String payoutId) {
            this.payoutId = payoutId;
            return this;
        }

        /**
         * Set the payout id (UUID v4). Generated if not set.
         *
         * @param payoutId the unique payout id
         * @return this builder
         */
        public Builder payoutId(UUID payoutId) {
            this.payoutId = payoutId != null ? payoutId.toString() : null;
            return this;
        }

        /**
         * Set the amount as a string. Trailing decimal zeros are removed.
         *
         * @param amount the amount
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
         * Set the ISO 4217 currency code.
         *
         * @param currency the currency code
         * @return this builder
         */
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        /**
         * Set the recipient's phone number in MSISDN format (digits only, country code included).
         *
         * @param phoneNumber the phone number
         * @return this builder
         */
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * Set the recipient's provider code (e.g. {@code MTN_MOMO_COG}).
         *
         * @param provider the provider code
         * @return this builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Set the recipient directly. Overrides {@link #phoneNumber(String)} and {@link #provider(String)}.
         *
         * @param recipient the recipient
         * @return this builder
         */
        public Builder recipient(PawapayParty recipient) {
            this.recipient = recipient;
            return this;
        }

        /**
         * Set the message shown to the recipient (4 to 22 alphanumeric characters or spaces).
         *
         * @param customerMessage the message
         * @return this builder
         */
        public Builder customerMessage(String customerMessage) {
            this.customerMessage = customerMessage;
            return this;
        }

        /**
         * Set your own reference. Returned in status responses and callbacks.
         *
         * @param clientReferenceId the reference
         * @return this builder
         */
        public Builder clientReferenceId(String clientReferenceId) {
            this.clientReferenceId = clientReferenceId;
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
        public PawapayPayoutRequest build() {
            if (amount == null || amount.isBlank()) {
                throw new IllegalStateException("Amount is required");
            }
            if (currency == null || currency.isBlank()) {
                throw new IllegalStateException("Currency is required");
            }
            if (recipient == null) {
                if (phoneNumber == null || phoneNumber.isBlank()) {
                    throw new IllegalStateException("Recipient phone number is required");
                }
                if (provider == null || provider.isBlank()) {
                    throw new IllegalStateException("Recipient provider is required");
                }
                recipient = PawapayParty.mmo(phoneNumber, provider);
            }

            payoutId = payoutId == null || payoutId.isBlank()
                ? UUID.randomUUID().toString()
                : PawapayValidation.requireUuid(payoutId, "payoutId");
            amount = PawapayValidation.validateAmount(PawapayValidation.normalizeAmount(amount));
            currency = PawapayValidation.validateCurrency(currency);
            PawapayValidation.validatePhoneNumber(recipient.phoneNumber());
            PawapayValidation.validateProvider(recipient.provider());
            customerMessage = PawapayValidation.validateCustomerMessage(customerMessage);
            PawapayValidation.validateMetadata(metadata);

            return new PawapayPayoutRequest(this);
        }
    }
}
