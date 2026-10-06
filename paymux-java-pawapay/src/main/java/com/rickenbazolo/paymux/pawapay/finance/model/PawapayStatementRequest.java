package com.rickenbazolo.paymux.pawapay.finance.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Request to generate a wallet statement ({@code POST /v2/statements}).
 * <p>
 * The statement is generated asynchronously: PawaPay posts a callback to the {@code callbackUrl}
 * given in the request (this is the only PawaPay operation with a per-request callback URL)
 * with the download link once the CSV file is ready. The date range may not exceed 31 days
 * and the callback URL must use HTTPS.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayStatementRequest request = PawapayStatementRequest.builder()
 *     .wallet(PawapayWallet.of("COG", "XAF"))
 *     .callbackUrl("https://merchant.com/callbacks/pawapay/statements")
 *     .startDate(LocalDateTime.of(2025, 5, 1, 0, 0))
 *     .endDate(LocalDateTime.of(2025, 5, 31, 23, 59, 59))
 *     .compressed(true)
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
public final class PawapayStatementRequest {

    /** Maximum length of a statement period. */
    public static final Duration MAX_PERIOD = Duration.ofDays(31);

    private final PawapayWallet wallet;
    private final String callbackUrl;
    private final String startDate;
    private final String endDate;
    private final Boolean compressed;

    private PawapayStatementRequest(Builder builder) {
        this.wallet = builder.wallet;
        this.callbackUrl = builder.callbackUrl;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.compressed = builder.compressed;
    }

    public PawapayWallet getWallet() {
        return wallet;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * @return the start of the period, as a UTC zone-less RFC 3339 timestamp
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * @return the end of the period, as a UTC zone-less RFC 3339 timestamp
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * @return whether the CSV is gzip compressed, or null to use the PawaPay default (not compressed)
     */
    public Boolean getCompressed() {
        return compressed;
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
        return "PawapayStatementRequest{wallet=" + wallet + ", callbackUrl='" + callbackUrl + "', startDate='" + startDate
            + "', endDate='" + endDate + "', compressed=" + compressed + '}';
    }

    /**
     * Builder for {@link PawapayStatementRequest}.
     */
    public static final class Builder {
        private PawapayWallet wallet;
        private String country;
        private String currency;
        private String provider;
        private String callbackUrl;
        private String startDate;
        private String endDate;
        private Boolean compressed;

        /**
         * Set the wallet. Overrides {@link #country(String)}, {@link #currency(String)} and {@link #provider(String)}.
         *
         * @param wallet the wallet
         * @return this builder
         */
        public Builder wallet(PawapayWallet wallet) {
            this.wallet = wallet;
            return this;
        }

        /**
         * Set the wallet country (ISO 3166-1 alpha-3).
         *
         * @param country the country code
         * @return this builder
         */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /**
         * Set the wallet currency (ISO 4217).
         *
         * @param currency the currency code
         * @return this builder
         */
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        /**
         * Set the provider of a provider-specific wallet.
         *
         * @param provider the provider code
         * @return this builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Set the HTTPS URL PawaPay will post the statement callback to.
         *
         * @param callbackUrl the callback URL
         * @return this builder
         */
        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        /**
         * Set the HTTPS URL PawaPay will post the statement callback to.
         *
         * @param callbackUrl the callback URL
         * @return this builder
         */
        public Builder callbackUrl(URI callbackUrl) {
            this.callbackUrl = callbackUrl != null ? callbackUrl.toString() : null;
            return this;
        }

        /**
         * Set the start of the period as a raw RFC 3339 timestamp (e.g. {@code 2025-05-10T10:00:00}).
         *
         * @param startDate the start timestamp
         * @return this builder
         */
        public Builder startDate(String startDate) {
            this.startDate = startDate;
            return this;
        }

        /**
         * Set the start of the period (interpreted as UTC).
         *
         * @param startDate the start date-time
         * @return this builder
         */
        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate != null ? PawapayDates.formatUtcLocalDateTime(startDate) : null;
            return this;
        }

        /**
         * Set the start of the period.
         *
         * @param startDate the start instant
         * @return this builder
         */
        public Builder startDate(Instant startDate) {
            this.startDate = startDate != null ? PawapayDates.formatUtcLocalDateTime(startDate) : null;
            return this;
        }

        /**
         * Set the end of the period as a raw RFC 3339 timestamp (e.g. {@code 2025-05-11T10:00:00}).
         *
         * @param endDate the end timestamp
         * @return this builder
         */
        public Builder endDate(String endDate) {
            this.endDate = endDate;
            return this;
        }

        /**
         * Set the end of the period (interpreted as UTC).
         *
         * @param endDate the end date-time
         * @return this builder
         */
        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate != null ? PawapayDates.formatUtcLocalDateTime(endDate) : null;
            return this;
        }

        /**
         * Set the end of the period.
         *
         * @param endDate the end instant
         * @return this builder
         */
        public Builder endDate(Instant endDate) {
            this.endDate = endDate != null ? PawapayDates.formatUtcLocalDateTime(endDate) : null;
            return this;
        }

        /**
         * Request a gzip compressed statement ({@code .csv.gz}).
         *
         * @param compressed true to compress
         * @return this builder
         */
        public Builder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        /**
         * Build the request, validating it locally against the PawaPay constraints.
         *
         * @return a new request
         * @throws IllegalStateException    if a required field is missing
         * @throws IllegalArgumentException if a field is invalid
         */
        public PawapayStatementRequest build() {
            if (wallet == null) {
                if (country == null || country.isBlank()) {
                    throw new IllegalStateException("Wallet country is required");
                }
                if (currency == null || currency.isBlank()) {
                    throw new IllegalStateException("Wallet currency is required");
                }
                wallet = PawapayWallet.of(country, currency, provider == null || provider.isBlank() ? null : provider);
            }
            if (callbackUrl == null || callbackUrl.isBlank()) {
                throw new IllegalStateException("Callback URL is required");
            }
            if (startDate == null || startDate.isBlank()) {
                throw new IllegalStateException("Start date is required");
            }
            if (endDate == null || endDate.isBlank()) {
                throw new IllegalStateException("End date is required");
            }

            PawapayValidation.requireNonBlank(wallet.country(), "Wallet country");
            PawapayValidation.validateCountry(wallet.country());
            PawapayValidation.validateCurrency(wallet.currency());
            callbackUrl = validateCallbackUrl(callbackUrl);
            validatePeriod(startDate, endDate);

            return new PawapayStatementRequest(this);
        }

        private static String validateCallbackUrl(String url) {
            URI uri;
            try {
                uri = URI.create(url.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Callback URL is not a valid URI: " + url, e);
            }
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException("Callback URL must be an absolute HTTPS URL (got '" + url + "')");
            }
            return uri.toString();
        }

        private static void validatePeriod(String start, String end) {
            Instant startInstant = PawapayDates.parseInstant(start)
                .orElseThrow(() -> new IllegalArgumentException("Invalid start date '" + start + "': expected an RFC 3339 timestamp"));
            Instant endInstant = PawapayDates.parseInstant(end)
                .orElseThrow(() -> new IllegalArgumentException("Invalid end date '" + end + "': expected an RFC 3339 timestamp"));
            if (!endInstant.isAfter(startInstant)) {
                throw new IllegalArgumentException("End date must be after start date");
            }
            if (Duration.between(startInstant, endInstant).compareTo(MAX_PERIOD) > 0) {
                throw new IllegalArgumentException("Statement period cannot exceed " + MAX_PERIOD.toDays() + " days");
            }
            if (LocalDateTime.ofInstant(startInstant, ZoneOffset.UTC).getYear() < 2000) {
                throw new IllegalArgumentException("Start date is out of range: " + start);
            }
        }
    }
}
