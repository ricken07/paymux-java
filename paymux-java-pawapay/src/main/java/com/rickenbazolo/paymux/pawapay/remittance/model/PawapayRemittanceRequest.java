package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutRequest;
import com.rickenbazolo.paymux.pawapay.model.PawapayMetadata;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PawaPay remittance request ({@code POST /v2/remittances}): an international money transfer paid out
 * to a mobile money account, with the KYC details of the sender.
 * <p>
 * Implements the generic {@link CashoutRequest} contract and is accepted by
 * {@code PawapayClient#cashout(CashoutRequest)}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * var sender = new PawapayRemittanceSender(
 *     new PawapayRemittanceSender.TransactionDetails("TX-123", "100", "USD", "23.88", "1",
 *         PawapayPurposeOfFunds.FAMILY_SUPPORT, PawapaySourceOfFunds.SALARY),
 *     PawapayRemittanceSender.SenderDetails.builder()
 *         .firstName("Jane").lastName("Doe").nationality("USA").phoneNumber("12124567890")
 *         .address("1476 Sandhill Rd", "84058", "Orem", "USA")
 *         .identification(PawapayIdentificationType.PASSPORT, "E00007730")
 *         .build());
 *
 * PawapayRemittanceRequest request = PawapayRemittanceRequest.builder()
 *     .amount("15")
 *     .currency("ZMW")
 *     .phoneNumber("260763456789")
 *     .provider("MTN_MOMO_ZMB")
 *     .recipientName("John", "Doe")
 *     .sender(sender)
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
public final class PawapayRemittanceRequest implements CashoutRequest {

    /** Maximum length of the free text fields of the sender and recipient. */
    public static final int MAX_TEXT_LENGTH = 64;

    private final String remittanceId;
    private final PawapayRemittanceRecipient recipient;
    private final PawapayRemittanceSender sender;
    private final String amount;
    private final String currency;
    private final String customerMessage;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayMetadata> metadata;

    private PawapayRemittanceRequest(Builder builder) {
        this.remittanceId = builder.remittanceId;
        this.recipient = builder.recipient;
        this.sender = builder.sender;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.customerMessage = builder.customerMessage;
        this.metadata = List.copyOf(builder.metadata);
    }

    public String getRemittanceId() {
        return remittanceId;
    }

    public PawapayRemittanceRecipient getRecipient() {
        return recipient;
    }

    public PawapayRemittanceSender getSender() {
        return sender;
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

    /**
     * @return the sender's transaction reference
     */
    @Override
    public String reference() {
        return sender.transactionDetails().transactionReference();
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
        return "PawapayRemittanceRequest{remittanceId='" + remittanceId + "', recipient=" + recipient + ", amount='" + amount
            + "', currency='" + currency + "', transactionReference='" + reference() + "'}";
    }

    /**
     * Builder for {@link PawapayRemittanceRequest}.
     */
    public static final class Builder {
        private String remittanceId;
        private String phoneNumber;
        private String provider;
        private String recipientFirstName;
        private String recipientLastName;
        private PawapayRemittanceRecipient recipient;
        private PawapayRemittanceSender sender;
        private String amount;
        private String currency;
        private String customerMessage;
        private final List<PawapayMetadata> metadata = new ArrayList<>();

        /**
         * Set the remittance id (UUID v4). Generated if not set.
         *
         * @param remittanceId the unique remittance id
         * @return this builder
         */
        public Builder remittanceId(String remittanceId) {
            this.remittanceId = remittanceId;
            return this;
        }

        /**
         * Set the remittance id (UUID v4). Generated if not set.
         *
         * @param remittanceId the unique remittance id
         * @return this builder
         */
        public Builder remittanceId(UUID remittanceId) {
            this.remittanceId = remittanceId != null ? remittanceId.toString() : null;
            return this;
        }

        /**
         * Set the amount paid out to the recipient, as a string. Trailing decimal zeros are removed.
         *
         * @param amount the amount
         * @return this builder
         */
        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Set the amount paid out to the recipient.
         *
         * @param amount the amount
         * @return this builder
         */
        public Builder amount(BigDecimal amount) {
            this.amount = amount != null ? amount.toPlainString() : null;
            return this;
        }

        /**
         * Set the ISO 4217 currency of the payout to the recipient.
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
         * Set the recipient's name.
         *
         * @param firstName the first name
         * @param lastName  the last name
         * @return this builder
         */
        public Builder recipientName(String firstName, String lastName) {
            this.recipientFirstName = firstName;
            this.recipientLastName = lastName;
            return this;
        }

        /**
         * Set the recipient directly. Overrides {@link #phoneNumber(String)}, {@link #provider(String)}
         * and {@link #recipientName(String, String)}.
         *
         * @param recipient the recipient
         * @return this builder
         */
        public Builder recipient(PawapayRemittanceRecipient recipient) {
            this.recipient = recipient;
            return this;
        }

        /**
         * Set the sender (originating transaction and KYC details).
         *
         * @param sender the sender
         * @return this builder
         */
        public Builder sender(PawapayRemittanceSender sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Set the sender from its two parts.
         *
         * @param transactionDetails the originating transaction
         * @param senderDetails      the identity of the sender
         * @return this builder
         */
        public Builder sender(PawapayRemittanceSender.TransactionDetails transactionDetails,
                              PawapayRemittanceSender.SenderDetails senderDetails) {
            return sender(new PawapayRemittanceSender(transactionDetails, senderDetails));
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

        public Builder addMetadata(PawapayMetadata metadata) {
            this.metadata.add(metadata);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            return addMetadata(PawapayMetadata.of(key, value));
        }

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
        public PawapayRemittanceRequest build() {
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
                if (recipientFirstName == null || recipientFirstName.isBlank()
                    || recipientLastName == null || recipientLastName.isBlank()) {
                    throw new IllegalStateException("Recipient first name and last name are required");
                }
                recipient = PawapayRemittanceRecipient.mmo(phoneNumber, provider, recipientFirstName, recipientLastName);
            }
            if (sender == null) {
                throw new IllegalStateException("Sender is required");
            }

            remittanceId = remittanceId == null || remittanceId.isBlank()
                ? UUID.randomUUID().toString()
                : PawapayValidation.requireUuid(remittanceId, "remittanceId");
            amount = PawapayValidation.validateAmount(PawapayValidation.normalizeAmount(amount));
            currency = PawapayValidation.validateCurrency(currency);
            customerMessage = PawapayValidation.validateCustomerMessage(customerMessage);
            PawapayValidation.validateMetadata(metadata);
            validateRecipient(recipient);
            validateSender(sender);

            return new PawapayRemittanceRequest(this);
        }

        private static void validateRecipient(PawapayRemittanceRecipient recipient) {
            PawapayValidation.validatePhoneNumber(recipient.phoneNumber());
            PawapayValidation.validateProvider(recipient.provider());
            if (recipient.recipientDetails() == null) {
                throw new IllegalStateException("Recipient details (first name, last name) are required");
            }
            PawapayValidation.validateText(recipient.recipientDetails().firstName(), "Recipient first name", MAX_TEXT_LENGTH);
            PawapayValidation.validateText(recipient.recipientDetails().lastName(), "Recipient last name", MAX_TEXT_LENGTH);
        }

        private static void validateSender(PawapayRemittanceSender sender) {
            var tx = sender.transactionDetails();
            if (tx == null) {
                throw new IllegalStateException("Sender transaction details are required");
            }
            PawapayValidation.validateText(tx.transactionReference(), "Sender transaction reference", MAX_TEXT_LENGTH);
            PawapayValidation.validateDecimal(tx.originalAmount(), "Sender original amount");
            PawapayValidation.validateCurrency(tx.originalCurrency());
            PawapayValidation.validateDecimal(tx.buyFxRate(), "Sender buy FX rate");
            PawapayValidation.validateDecimal(tx.senderFees(), "Sender fees");
            requireKnown(tx.purposeOfFunds(), tx.purposeOfFunds() == PawapayPurposeOfFunds.UNKNOWN, "Purpose of funds");
            requireKnown(tx.sourceOfFunds(), tx.sourceOfFunds() == PawapaySourceOfFunds.UNKNOWN, "Source of funds");

            var details = sender.senderDetails();
            if (details == null) {
                throw new IllegalStateException("Sender details are required");
            }
            PawapayValidation.validateText(details.firstName(), "Sender first name", MAX_TEXT_LENGTH);
            PawapayValidation.validateText(details.lastName(), "Sender last name", MAX_TEXT_LENGTH);
            PawapayValidation.requireNonBlank(details.nationality(), "Sender nationality");
            PawapayValidation.validateCountry(details.nationality());
            PawapayValidation.validateText(details.phoneNumber(), "Sender phone number", MAX_TEXT_LENGTH);
            if (details.address() == null) {
                throw new IllegalStateException("Sender address is required");
            }
            PawapayValidation.validateText(details.address().addressLine(), "Sender address line", MAX_TEXT_LENGTH);
            PawapayValidation.validateText(details.address().postalCode(), "Sender postal code", MAX_TEXT_LENGTH);
            PawapayValidation.validateText(details.address().city(), "Sender city", MAX_TEXT_LENGTH);
            PawapayValidation.requireNonBlank(details.address().country(), "Sender country");
            PawapayValidation.validateCountry(details.address().country());
            if (details.identification() == null) {
                throw new IllegalStateException("Sender identification is required");
            }
            requireKnown(details.identification().type(),
                details.identification().type() == PawapayIdentificationType.UNKNOWN, "Identification type");
            PawapayValidation.validateText(details.identification().number(), "Sender identification number", MAX_TEXT_LENGTH);
            if (details.gender() == PawapayGender.UNKNOWN) {
                throw new IllegalArgumentException("Gender UNKNOWN cannot be sent to PawaPay");
            }
            if (details.relationshipRecipient() == PawapayRelationship.UNKNOWN) {
                throw new IllegalArgumentException("Relationship UNKNOWN cannot be sent to PawaPay");
            }
            PawapayValidation.validateOptionalText(details.placeOfBirth(), "Sender place of birth", MAX_TEXT_LENGTH);
            PawapayValidation.validateOptionalText(details.occupation(), "Sender occupation", MAX_TEXT_LENGTH);
            PawapayValidation.validateOptionalDate(details.dateOfBirth(), "Sender date of birth");
        }

        private static void requireKnown(Object value, boolean unknown, String field) {
            if (value == null) {
                throw new IllegalStateException(field + " is required");
            }
            if (unknown) {
                throw new IllegalArgumentException(field + " UNKNOWN cannot be sent to PawaPay");
            }
        }
    }
}
