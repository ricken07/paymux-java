package com.rickenbazolo.paymux.pawapay.refund.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.core.operations.refund.RefundRequest;
import com.rickenbazolo.paymux.pawapay.model.PawapayMetadata;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PawaPay refund request ({@code POST /v2/refunds}): returns (part of) a completed deposit to the customer.
 * <p>
 * Implements the generic {@link RefundRequest} contract. The PawaPay refund API has no
 * customer message field; use {@link Builder#addMetadata(String, Object)} for internal notes.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayRefundRequest request = PawapayRefundRequest.builder()
 *     .depositId("f4401bd2-1568-4140-bf2d-eb77d2b2b639")
 *     .amount("1000")
 *     .currency("XAF")
 *     .clientReferenceId("REFUND-12345")
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
public final class PawapayRefundRequest implements RefundRequest {

    private final String refundId;
    private final String depositId;
    private final String amount;
    private final String currency;
    private final String clientReferenceId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayMetadata> metadata;

    private PawapayRefundRequest(Builder builder) {
        this.refundId = builder.refundId;
        this.depositId = builder.depositId;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.clientReferenceId = builder.clientReferenceId;
        this.metadata = List.copyOf(builder.metadata);
    }

    public String getRefundId() {
        return refundId;
    }

    public String getDepositId() {
        return depositId;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
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

    /**
     * @return the refund id
     */
    @Override
    public String externalId() {
        return refundId;
    }

    /**
     * @return the id of the deposit being refunded
     */
    @Override
    public String originalTransactionId() {
        return depositId;
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

    /**
     * Creates a PawaPay refund request from a generic refund request.
     *
     * @param request the generic request
     * @return a PawaPay request (the same instance if it already is one)
     */
    public static PawapayRefundRequest from(RefundRequest request) {
        if (request instanceof PawapayRefundRequest pawapayRequest) {
            return pawapayRequest;
        }
        return builder()
            .refundId(request.externalId())
            .depositId(request.originalTransactionId())
            .amount(request.amount())
            .currency(request.currency())
            .clientReferenceId(request.reference())
            .build();
    }

    @Override
    public String toString() {
        return "PawapayRefundRequest{refundId='" + refundId + "', depositId='" + depositId + "', amount='" + amount
            + "', currency='" + currency + "', clientReferenceId='" + clientReferenceId + "'}";
    }

    /**
     * Builder for {@link PawapayRefundRequest}.
     */
    public static final class Builder {
        private String refundId;
        private String depositId;
        private String amount;
        private String currency;
        private String clientReferenceId;
        private final List<PawapayMetadata> metadata = new ArrayList<>();

        /**
         * Set the refund id (UUID v4). Generated if not set.
         *
         * @param refundId the unique refund id
         * @return this builder
         */
        public Builder refundId(String refundId) {
            this.refundId = refundId;
            return this;
        }

        /**
         * Set the refund id (UUID v4). Generated if not set.
         *
         * @param refundId the unique refund id
         * @return this builder
         */
        public Builder refundId(UUID refundId) {
            this.refundId = refundId != null ? refundId.toString() : null;
            return this;
        }

        /**
         * Set the id of the completed deposit to refund.
         *
         * @param depositId the deposit id
         * @return this builder
         */
        public Builder depositId(String depositId) {
            this.depositId = depositId;
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
         * Set the ISO 4217 currency code (must match the deposit currency).
         *
         * @param currency the currency code
         * @return this builder
         */
        public Builder currency(String currency) {
            this.currency = currency;
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
        public PawapayRefundRequest build() {
            if (depositId == null || depositId.isBlank()) {
                throw new IllegalStateException("Deposit ID is required");
            }
            if (amount == null || amount.isBlank()) {
                throw new IllegalStateException("Amount is required");
            }
            if (currency == null || currency.isBlank()) {
                throw new IllegalStateException("Currency is required");
            }

            refundId = refundId == null || refundId.isBlank()
                ? UUID.randomUUID().toString()
                : PawapayValidation.requireUuid(refundId, "refundId");
            depositId = PawapayValidation.requireUuid(depositId, "depositId");
            amount = PawapayValidation.validateAmount(PawapayValidation.normalizeAmount(amount));
            currency = PawapayValidation.validateCurrency(currency);
            PawapayValidation.validateMetadata(metadata);

            return new PawapayRefundRequest(this);
        }
    }
}
