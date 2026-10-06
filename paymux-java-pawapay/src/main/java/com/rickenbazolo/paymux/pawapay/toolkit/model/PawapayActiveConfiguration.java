package com.rickenbazolo.paymux.pawapay.toolkit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rickenbazolo.paymux.pawapay.model.PawapayOperationType;
import com.rickenbazolo.paymux.pawapay.model.PawapayProviderStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Active configuration of the merchant account ({@code GET /v2/active-conf}): the countries,
 * providers, currencies and operation types enabled, with their limits and current status.
 *
 * @param companyName            the merchant company name
 * @param signatureConfiguration the signature settings of the account
 * @param countries              the enabled countries (never null)
 * @author Ricken Bazolo
 * @since 0.0.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PawapayActiveConfiguration(
    String companyName,
    SignatureConfiguration signatureConfiguration,
    List<Country> countries
) {

    public PawapayActiveConfiguration {
        countries = countries == null ? List.of() : List.copyOf(countries);
    }

    /**
     * Finds a country by ISO 3166-1 alpha-3 code.
     *
     * @param countryCode the country code (e.g. {@code COG})
     * @return the country configuration, if enabled
     */
    public Optional<Country> country(String countryCode) {
        return countries.stream().filter(c -> countryCode != null && countryCode.equalsIgnoreCase(c.country())).findFirst();
    }

    /**
     * Finds a provider configuration across all countries.
     *
     * @param providerCode the provider code (e.g. {@code MTN_MOMO_COG})
     * @return the provider configuration, if enabled
     */
    public Optional<Provider> provider(String providerCode) {
        return countries.stream()
            .flatMap(c -> c.providers().stream())
            .filter(p -> providerCode != null && providerCode.equalsIgnoreCase(p.provider()))
            .findFirst();
    }

    /**
     * Tells whether a provider is operational for an operation type in a given currency.
     *
     * @param providerCode  the provider code
     * @param currency      the ISO 4217 currency code
     * @param operationType the operation type
     * @return true if the operation is configured and {@code OPERATIONAL}
     */
    public boolean isOperational(String providerCode, String currency, PawapayOperationType operationType) {
        return provider(providerCode)
            .flatMap(p -> p.currency(currency))
            .flatMap(c -> c.operationType(operationType))
            .map(o -> o.status() == PawapayProviderStatus.OPERATIONAL)
            .orElse(false);
    }

    /**
     * Signature settings of the merchant account.
     *
     * @param signedRequestsOnly true if PawaPay only accepts signed financial requests
     * @param signedCallbacks    true if PawaPay signs its callbacks
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SignatureConfiguration(boolean signedRequestsOnly, boolean signedCallbacks) {
    }

    /**
     * A country enabled on the account.
     *
     * @param country     the ISO 3166-1 alpha-3 code
     * @param displayName localized names keyed by language ({@code en}, {@code fr})
     * @param prefix      the international calling code
     * @param flag        the flag image URL
     * @param providers   the enabled providers (never null)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Country(String country, Map<String, String> displayName, String prefix, String flag,
                          List<Provider> providers) {

        public Country {
            displayName = displayName == null ? Map.of() : Map.copyOf(displayName);
            providers = providers == null ? List.of() : List.copyOf(providers);
        }

        /**
         * Finds a provider of this country.
         *
         * @param providerCode the provider code
         * @return the provider configuration, if enabled
         */
        public Optional<Provider> provider(String providerCode) {
            return providers.stream().filter(p -> providerCode != null && providerCode.equalsIgnoreCase(p.provider())).findFirst();
        }
    }

    /**
     * A provider enabled on the account.
     *
     * @param provider                the provider code
     * @param displayName             the common provider name
     * @param logo                    the logo URL
     * @param nameDisplayedToCustomer the merchant name shown to the customer
     * @param currencies              the enabled currencies (never null)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Provider(String provider, String displayName, String logo, String nameDisplayedToCustomer,
                           List<Currency> currencies) {

        public Provider {
            currencies = currencies == null ? List.of() : List.copyOf(currencies);
        }

        /**
         * Finds a currency of this provider.
         *
         * @param currencyCode the ISO 4217 code
         * @return the currency configuration, if enabled
         */
        public Optional<Currency> currency(String currencyCode) {
            return currencies.stream().filter(c -> currencyCode != null && currencyCode.equalsIgnoreCase(c.currency())).findFirst();
        }
    }

    /**
     * A currency enabled for a provider.
     *
     * @param currency       the ISO 4217 code
     * @param displayName    the currency abbreviation shown to customers
     * @param operationTypes the operation configurations keyed by operation type name
     *                       ({@code DEPOSIT}, {@code PAYOUT}, {@code REFUND}, ...) (never null)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Currency(String currency, String displayName, Map<String, OperationTypeConfiguration> operationTypes) {

        public Currency {
            operationTypes = operationTypes == null ? Map.of() : Map.copyOf(operationTypes);
        }

        /**
         * Finds the configuration of an operation type.
         *
         * @param operationType the operation type
         * @return the configuration, if the operation is enabled
         */
        public Optional<OperationTypeConfiguration> operationType(PawapayOperationType operationType) {
            return operationType == null ? Optional.empty() : Optional.ofNullable(operationTypes.get(operationType.name()));
        }
    }

    /**
     * Configuration of one operation type for a provider and currency.
     *
     * @param authType           {@code PROVIDER_AUTH}, {@code PREAUTH} or {@code REDIRECT_AUTH} (deposits)
     * @param pinPrompt          {@code AUTOMATIC} or {@code MANUAL} (deposits)
     * @param pinPromptRevivable whether the PIN prompt can be revived (deposits)
     * @param minAmount          the minimum amount
     * @param maxAmount          the maximum amount
     * @param decimalsInAmount   {@code TWO_PLACES} or {@code NONE}
     * @param status             the current operational status
     * @param callbackUrl        the callback URL configured in the dashboard
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OperationTypeConfiguration(String authType, String pinPrompt, Boolean pinPromptRevivable,
                                             String minAmount, String maxAmount, String decimalsInAmount,
                                             PawapayProviderStatus status, String callbackUrl) {
    }
}
