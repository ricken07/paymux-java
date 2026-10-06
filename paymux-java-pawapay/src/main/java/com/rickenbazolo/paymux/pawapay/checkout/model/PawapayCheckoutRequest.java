package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.pawapay.model.PawapayMetadata;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PawaPay checkout request ({@code POST /v2/checkouts}): forwards the customer to a hosted,
 * redirect-based payment page, tracked under a single reference for the whole payment including
 * any retries.
 * <p>
 * Not mapped onto the generic {@code TransferOperation}: a checkout's amount and payer are both
 * optional (the customer may enter them on the hosted page), which does not fit the
 * {@code amount()} / {@code recipientPhoneNumber()} contract of a generic transfer request. Use
 * {@code PawapayClient#initiateCheckout(PawapayCheckoutRequest)} directly.
 * </p>
 * <p>
 * The only required fields are {@code checkoutId} and {@code returnUrl}; every other field
 * narrows what the customer can do on the hosted page (see the builder methods).
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayCheckoutRequest request = PawapayCheckoutRequest.builder()
 *     .returnUrl("https://merchant.example.com/checkout-result")
 *     .addAmount("ZMB", "ZMW", "100")
 *     .reason("en", "GOODS PURCHASE")
 *     .clientReferenceId("ORDER-123")
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
public final class PawapayCheckoutRequest {

    /** Minimum value of {@code expiresAfter}, in minutes. */
    public static final int MIN_EXPIRES_AFTER_MINUTES = 3;
    /** Maximum value of {@code expiresAfter}, in minutes. */
    public static final int MAX_EXPIRES_AFTER_MINUTES = 60;

    private final String checkoutId;
    private final String returnUrl;
    private final PawapayReturnMethod returnMethod;
    private final PawapayCheckoutLanguage defaultLanguage;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> countries;
    private final Integer expiresAfter;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayCheckoutAmount> amounts;
    private final PawapayCheckoutPayer payer;
    private final String clientReferenceId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> reason;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<PawapayMetadata> metadata;

    private PawapayCheckoutRequest(Builder builder) {
        this.checkoutId = builder.checkoutId;
        this.returnUrl = builder.returnUrl;
        this.returnMethod = builder.returnMethod;
        this.defaultLanguage = builder.defaultLanguage;
        this.countries = List.copyOf(builder.countries);
        this.expiresAfter = builder.expiresAfter;
        this.amounts = List.copyOf(builder.amounts);
        this.payer = builder.payer;
        this.clientReferenceId = builder.clientReferenceId;
        this.reason = Map.copyOf(builder.reason);
        this.metadata = List.copyOf(builder.metadata);
    }

    public String getCheckoutId() {
        return checkoutId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public PawapayReturnMethod getReturnMethod() {
        return returnMethod;
    }

    public PawapayCheckoutLanguage getDefaultLanguage() {
        return defaultLanguage;
    }

    public List<String> getCountries() {
        return countries;
    }

    public Integer getExpiresAfter() {
        return expiresAfter;
    }

    public List<PawapayCheckoutAmount> getAmounts() {
        return amounts;
    }

    public PawapayCheckoutPayer getPayer() {
        return payer;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
    }

    public Map<String, String> getReason() {
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
        return "PawapayCheckoutRequest{checkoutId='" + checkoutId + "', returnUrl='" + returnUrl
            + "', countries=" + countries + ", amounts=" + amounts + ", clientReferenceId='" + clientReferenceId + "'}";
    }

    /**
     * Builder for {@link PawapayCheckoutRequest}.
     */
    public static final class Builder {
        private String checkoutId;
        private String returnUrl;
        private PawapayReturnMethod returnMethod;
        private PawapayCheckoutLanguage defaultLanguage;
        private final List<String> countries = new ArrayList<>();
        private Integer expiresAfter;
        private final List<PawapayCheckoutAmount> amounts = new ArrayList<>();
        private PawapayCheckoutPayer payer;
        private String clientReferenceId;
        private final Map<String, String> reason = new LinkedHashMap<>();
        private final List<PawapayMetadata> metadata = new ArrayList<>();

        /**
         * Set the checkout id (UUID v4). Generated if not set.
         *
         * @param checkoutId the unique checkout id
         * @return this builder
         */
        public Builder checkoutId(String checkoutId) {
            this.checkoutId = checkoutId;
            return this;
        }

        /**
         * Set the checkout id (UUID v4). Generated if not set.
         *
         * @param checkoutId the unique checkout id
         * @return this builder
         */
        public Builder checkoutId(UUID checkoutId) {
            this.checkoutId = checkoutId != null ? checkoutId.toString() : null;
            return this;
        }

        /**
         * Set the URL the customer is redirected to once the payment is finished or cancelled.
         *
         * @param returnUrl the return URL
         * @return this builder
         */
        public Builder returnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        /**
         * Set the URL the customer is redirected to once the payment is finished or cancelled.
         *
         * @param returnUrl the return URL
         * @return this builder
         */
        public Builder returnUrl(URI returnUrl) {
            this.returnUrl = returnUrl != null ? returnUrl.toString() : null;
            return this;
        }

        /**
         * Set how the customer is returned to {@code returnUrl}. Defaults to an automatic, immediate redirect.
         *
         * @param returnMethod the return method
         * @return this builder
         */
        public Builder returnMethod(PawapayReturnMethod returnMethod) {
            this.returnMethod = returnMethod;
            return this;
        }

        /**
         * Set the language the hosted payment page opens in. Defaults to the page's default language.
         *
         * @param defaultLanguage the language
         * @return this builder
         */
        public Builder defaultLanguage(PawapayCheckoutLanguage defaultLanguage) {
            this.defaultLanguage = defaultLanguage;
            return this;
        }

        /**
         * Restrict the countries the customer can pay from. If not set, any country configured on
         * your account is allowed.
         *
         * @param countries the ISO 3166-1 alpha-3 country codes
         * @return this builder
         */
        public Builder countries(List<String> countries) {
            this.countries.clear();
            if (countries != null) {
                this.countries.addAll(countries);
            }
            return this;
        }

        /**
         * Add an allowed country.
         *
         * @param country the ISO 3166-1 alpha-3 country code
         * @return this builder
         */
        public Builder addCountry(String country) {
            this.countries.add(country);
            return this;
        }

        /**
         * Set the number of minutes after creation when the checkout expires (3 to 60). Defaults to 15.
         *
         * @param expiresAfter the number of minutes
         * @return this builder
         */
        public Builder expiresAfter(int expiresAfter) {
            this.expiresAfter = expiresAfter;
            return this;
        }

        /**
         * Replace the fixed amount options. If not set, the customer enters the amount themselves.
         *
         * @param amounts the amount options
         * @return this builder
         */
        public Builder amounts(List<PawapayCheckoutAmount> amounts) {
            this.amounts.clear();
            if (amounts != null) {
                this.amounts.addAll(amounts);
            }
            return this;
        }

        /**
         * Add a fixed amount option for a country and currency.
         *
         * @param country  the ISO 3166-1 alpha-3 country code
         * @param currency the ISO 4217 currency code
         * @param amount   the amount
         * @return this builder
         */
        public Builder addAmount(String country, String currency, String amount) {
            this.amounts.add(new PawapayCheckoutAmount(country, currency, amount));
            return this;
        }

        /**
         * Add a fixed amount option for a country and currency.
         *
         * @param country  the ISO 3166-1 alpha-3 country code
         * @param currency the ISO 4217 currency code
         * @param amount   the amount
         * @return this builder
         */
        public Builder addAmount(String country, String currency, BigDecimal amount) {
            return addAmount(country, currency, amount != null ? amount.toPlainString() : null);
        }

        /**
         * Set the payer pre-filled for the hosted payment page.
         *
         * @param payer the payer
         * @return this builder
         */
        public Builder payer(PawapayCheckoutPayer payer) {
            this.payer = payer;
            return this;
        }

        /**
         * Pre-fill a mobile money payer.
         *
         * @param phoneNumber             the MSISDN, or null to let the customer enter it
         * @param provider                the provider code, or null to let the customer choose it
         * @param allowCustomerToOverride whether the customer may change the pre-filled details
         * @return this builder
         */
        public Builder payer(String phoneNumber, String provider, boolean allowCustomerToOverride) {
            return payer(PawapayCheckoutPayer.mmo(phoneNumber, provider, allowCustomerToOverride));
        }

        /**
         * Set your own reference for this checkout. Returned in status responses and callbacks.
         *
         * @param clientReferenceId the reference
         * @return this builder
         */
        public Builder clientReferenceId(String clientReferenceId) {
            this.clientReferenceId = clientReferenceId;
            return this;
        }

        /**
         * Replace the localized reason shown to the customer on the hosted payment page.
         *
         * @param reason the reason, keyed by language code (e.g. {@code en}, {@code fr})
         * @return this builder
         */
        public Builder reason(Map<String, String> reason) {
            this.reason.clear();
            if (reason != null) {
                this.reason.putAll(reason);
            }
            return this;
        }

        /**
         * Add a localized reason shown to the customer on the hosted payment page (4 to 22
         * alphanumeric characters or spaces).
         *
         * @param language the language code (e.g. {@code en})
         * @param reason   the localized reason
         * @return this builder
         */
        public Builder reason(String language, String reason) {
            this.reason.put(language, reason);
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
        public PawapayCheckoutRequest build() {
            if (returnUrl == null || returnUrl.isBlank()) {
                throw new IllegalStateException("Return URL is required");
            }

            checkoutId = checkoutId == null || checkoutId.isBlank()
                ? UUID.randomUUID().toString()
                : PawapayValidation.requireUuid(checkoutId, "checkoutId");
            returnUrl = validateReturnUrl(returnUrl);
            for (int i = 0; i < countries.size(); i++) {
                countries.set(i, PawapayValidation.validateCountry(countries.get(i)));
            }
            if (expiresAfter != null && (expiresAfter < MIN_EXPIRES_AFTER_MINUTES || expiresAfter > MAX_EXPIRES_AFTER_MINUTES)) {
                throw new IllegalArgumentException("expiresAfter must be between " + MIN_EXPIRES_AFTER_MINUTES
                    + " and " + MAX_EXPIRES_AFTER_MINUTES + " minutes (got " + expiresAfter + ")");
            }
            for (int i = 0; i < amounts.size(); i++) {
                amounts.set(i, validateAmountOption(amounts.get(i)));
            }
            if (payer != null) {
                validatePayer(payer);
            }
            for (String value : reason.values()) {
                PawapayValidation.validateCustomerMessage(value);
            }
            PawapayValidation.validateMetadata(metadata);

            return new PawapayCheckoutRequest(this);
        }

        private static String validateReturnUrl(String url) {
            URI uri;
            try {
                uri = URI.create(url.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Return URL is not a valid URI: " + url, e);
            }
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Return URL must be an absolute URL (got '" + url + "')");
            }
            return uri.toString();
        }

        private static PawapayCheckoutAmount validateAmountOption(PawapayCheckoutAmount option) {
            String country = PawapayValidation.validateCountry(
                PawapayValidation.requireNonBlank(option.country(), "Amount country"));
            String currency = PawapayValidation.validateCurrency(option.currency());
            String amount = PawapayValidation.validateAmount(PawapayValidation.normalizeAmount(option.amount()));
            return new PawapayCheckoutAmount(country, currency, amount);
        }

        private static void validatePayer(PawapayCheckoutPayer payer) {
            if (payer.accountDetails() == null) {
                throw new IllegalStateException("Payer account details are required when a payer is provided");
            }
            if (payer.phoneNumber() != null) {
                PawapayValidation.validatePhoneNumber(payer.phoneNumber());
            }
            if (payer.provider() != null) {
                PawapayValidation.validateProvider(payer.provider());
            }
        }
    }
}
