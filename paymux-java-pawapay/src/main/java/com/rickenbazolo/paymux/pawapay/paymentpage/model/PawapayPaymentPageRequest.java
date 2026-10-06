package com.rickenbazolo.paymux.pawapay.paymentpage.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.pawapay.model.PawapayMetadata;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request for a deposit via the PawaPay payment page ({@code POST /v2/paymentpage}): a
 * fixed 15-minute hosted session, simpler than a {@code Checkout} but without retries or its own
 * lifecycle.
 * <p>
 * Not mapped onto the generic {@code TransferOperation}: like the checkout, the amount and phone
 * number are both optional (the customer may enter them on the page). Use
 * {@code PawapayClient#initiatePaymentPage(PawapayPaymentPageRequest)} directly. Once the customer
 * presses "Pay", a regular PawaPay deposit is registered under {@link #getDepositId()}: track it
 * the same way as any other deposit, through the deposit callback or
 * {@code PawapayClient#findDeposit(String)}. If the customer abandons the page, no deposit is ever
 * created and the id stays {@code NOT_FOUND}.
 * </p>
 * <p>
 * The only required fields are {@code depositId} and {@code returnUrl}. A fixed {@code amount}
 * requires a {@code country} to be set as well.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayPaymentPageRequest request = PawapayPaymentPageRequest.builder()
 *     .returnUrl("https://merchant.com/returnUrl")
 *     .amount("100").country("GHA").currency("GHS")
 *     .reason("Demo payment")
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
public final class PawapayPaymentPageRequest {

    /** Minimum length of the optional {@code reason} field. */
    public static final int MIN_REASON_LENGTH = 1;
    /** Maximum length of the optional {@code reason} field. */
    public static final int MAX_REASON_LENGTH = 50;

    private final String depositId;
    private final String returnUrl;
    private final String customerMessage;
    private final AmountDetails amountDetails;
    private final String phoneNumber;
    private final PawapayLanguage language;
    private final String country;
    private final String reason;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayMetadata> metadata;

    private PawapayPaymentPageRequest(Builder builder) {
        this.depositId = builder.depositId;
        this.returnUrl = builder.returnUrl;
        this.customerMessage = builder.customerMessage;
        this.amountDetails = builder.amount != null ? new AmountDetails(builder.amount, builder.currency) : null;
        this.phoneNumber = builder.phoneNumber;
        this.language = builder.language;
        this.country = builder.country;
        this.reason = builder.reason;
        this.metadata = List.copyOf(builder.metadata);
    }

    public String getDepositId() {
        return depositId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public AmountDetails getAmountDetails() {
        return amountDetails;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public PawapayLanguage getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getReason() {
        return reason;
    }

    public List<PawapayMetadata> getMetadata() {
        return metadata;
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
        return "PawapayPaymentPageRequest{depositId='" + depositId + "', returnUrl='" + returnUrl
            + "', amountDetails=" + amountDetails + ", country='" + country + "'}";
    }

    /**
     * The fixed amount and currency of a payment page session, when set together.
     *
     * @param amount   the fixed amount
     * @param currency the ISO 4217 currency code
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AmountDetails(String amount, String currency) {
    }

    /**
     * Builder for {@link PawapayPaymentPageRequest}.
     */
    public static final class Builder {
        private String depositId;
        private String returnUrl;
        private String customerMessage;
        private String amount;
        private String currency;
        private String phoneNumber;
        private PawapayLanguage language;
        private String country;
        private String reason;
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
         * Set the URL the customer is redirected to after the payment process.
         *
         * @param returnUrl the return URL
         * @return this builder
         */
        public Builder returnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        /**
         * Set the URL the customer is redirected to after the payment process.
         *
         * @param returnUrl the return URL
         * @return this builder
         */
        public Builder returnUrl(URI returnUrl) {
            this.returnUrl = returnUrl != null ? returnUrl.toString() : null;
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
         * Fix the amount the customer must pay. Requires {@link #country(String)} to be set as well.
         *
         * @param amount   the amount
         * @param currency the ISO 4217 currency code
         * @return this builder
         */
        public Builder amount(String amount, String currency) {
            this.amount = amount;
            this.currency = currency;
            return this;
        }

        /**
         * Fix the amount the customer must pay. Requires {@link #country(String)} to be set as well.
         *
         * @param amount   the amount
         * @param currency the ISO 4217 currency code
         * @return this builder
         */
        public Builder amount(BigDecimal amount, String currency) {
            return amount(amount != null ? amount.toPlainString() : null, currency);
        }

        /**
         * Fix the mobile money wallet the customer must pay from.
         *
         * @param phoneNumber the MSISDN
         * @return this builder
         */
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * Set the language the payment page opens in.
         *
         * @param language the language
         * @return this builder
         */
        public Builder language(PawapayLanguage language) {
            this.language = language;
            return this;
        }

        /**
         * Restrict the payment to a single country. Required when fixing an {@link #amount(String, String)}.
         *
         * @param country the ISO 3166-1 alpha-3 country code
         * @return this builder
         */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /**
         * Set the text shown to the customer describing what they are paying for (1 to 50 characters).
         *
         * @param reason the reason
         * @return this builder
         */
        public Builder reason(String reason) {
            this.reason = reason;
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
        public PawapayPaymentPageRequest build() {
            if (returnUrl == null || returnUrl.isBlank()) {
                throw new IllegalStateException("Return URL is required");
            }
            if (amount != null && (country == null || country.isBlank())) {
                throw new IllegalStateException("Country is required when fixing an amount");
            }

            depositId = depositId == null || depositId.isBlank()
                ? UUID.randomUUID().toString()
                : PawapayValidation.requireUuid(depositId, "depositId");
            returnUrl = validateReturnUrl(returnUrl);
            customerMessage = PawapayValidation.validateCustomerMessage(customerMessage);
            if (amount != null) {
                amount = PawapayValidation.validateAmount(PawapayValidation.normalizeAmount(amount));
                currency = PawapayValidation.validateCurrency(currency);
            }
            if (phoneNumber != null) {
                PawapayValidation.validatePhoneNumber(phoneNumber);
            }
            if (country != null) {
                country = PawapayValidation.validateCountry(country);
            }
            if (reason != null) {
                if (reason.length() < MIN_REASON_LENGTH || reason.length() > MAX_REASON_LENGTH) {
                    throw new IllegalArgumentException(
                        "Reason must be " + MIN_REASON_LENGTH + " to " + MAX_REASON_LENGTH + " characters (got " + reason.length() + ")");
                }
            }
            PawapayValidation.validateMetadata(metadata);

            return new PawapayPaymentPageRequest(this);
        }

        private static String validateReturnUrl(String url) {
            java.net.URI uri;
            try {
                uri = java.net.URI.create(url.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Return URL is not a valid URI: " + url, e);
            }
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Return URL must be an absolute URL (got '" + url + "')");
            }
            return uri.toString();
        }
    }
}
