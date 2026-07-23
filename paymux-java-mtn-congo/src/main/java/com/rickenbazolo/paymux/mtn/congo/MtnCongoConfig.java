package com.rickenbazolo.paymux.mtn.congo;

import com.rickenbazolo.paymux.core.MobileMoneyConfig;
import com.rickenbazolo.paymux.core.config.ConfigurationLoader;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.mtn.congo.config.MtnCongoConfigProperties;

import java.util.Properties;

/**
 * Configuration for MTN Mobile Money Congo client.
 * <p>
 * This configuration class holds all necessary credentials and settings
 * to interact with the MTN Mobile Money API for Congo-Brazzaville.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * var config = MtnCongoConfig.builder()
 *     .apiUser("your-api-user-uuid")
 *     .apiKey("your-api-key")
 *     .subscriptionKey("your-subscription-key")
 *     .environment("mtncongo")
 *     .production(false)  // Use sandbox
 *     .build();
 * }</pre>
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class MtnCongoConfig implements MobileMoneyConfig {

    private static final String SANDBOX_URL = "https://sandbox.momodeveloper.mtn.com/";
    private static final String PRODUCTION_URL = "https://ericssonbasicapi1.azure-api.net/";
    private static final String SANDBOX_ENVIRONMENT = "sandbox";
    private static final String PRODUCTION_ENVIRONMENT = "mtncongo";

    private final String apiUser;
    private final String apiKey;
    private final String subscriptionKey;
    private final String environment;
    private final String baseUrl;
    private final String callbackUrl;
    private final boolean production;
    private final PaymuxHttpClient httpClient;
    private final int connectionTimeout;
    private final int requestTimeout;

    private MtnCongoConfig(Builder builder) {
        this.apiUser = builder.apiUser;
        this.apiKey = builder.apiKey;
        this.subscriptionKey = builder.subscriptionKey;
        this.environment = builder.environment;
        this.baseUrl = builder.baseUrl;
        this.callbackUrl = builder.callbackUrl;
        this.production = builder.production;
        this.httpClient = builder.httpClient;
        this.connectionTimeout = builder.connectionTimeout;
        this.requestTimeout = builder.requestTimeout;
    }

    /**
     * Get the API User (UUID) for MTN Mobile Money.
     *
     * @return the API user UUID
     */
    public String getApiUser() {
        return apiUser;
    }

    /**
     * Get the API Key for MTN Mobile Money.
     *
     * @return the API key
     */
    @Override
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the API Secret (same as API key for MTN).
     *
     * @return the API secret
     */
    @Override
    public String getApiSecret() {
        return apiKey; // For MTN, secret is the same as key
    }

    /**
     * Get the Subscription Key (Ocp-Apim-Subscription-Key header).
     *
     * @return the subscription key
     */
    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    /**
     * Get the target environment (e.g., "mtncongo", "sandbox").
     *
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get the callback URL for transaction notifications.
     *
     * @return the callback URL, or null if not set
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @Override
    public boolean isProduction() {
        return production;
    }

    @Override
    public PaymuxHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Creates a new builder for MtnCongoConfig.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a configuration from properties loaded from the classpath.
     * <p>
     * This method will attempt to load properties from (in order of precedence):
     * <ul>
     *   <li>paymux.yml</li>
     *   <li>application.yml / application.yaml</li>
     *   <li>paymux.properties</li>
     *   <li>application.properties</li>
     * </ul>
     * YAML files take precedence over properties files.
     * All required MTN Congo properties must be present, or an exception will be thrown.
     * </p>
     *
     * @return a new MtnCongoConfig instance
     * @throws ConfigurationLoader.ConfigurationException if required properties are missing
     */
    public static MtnCongoConfig fromProperties() {
        Properties properties = ConfigurationLoader.loadFromClasspath();
        return fromProperties(properties);
    }

    /**
     * Creates a configuration from the specified configuration file.
     * <p>
     * Supports both .properties and .yaml/.yml files.
     * The file type is detected based on the extension.
     * </p>
     *
     * @param fileName the name of the configuration file on the classpath (.properties, .yml, or .yaml)
     * @return a new MtnCongoConfig instance
     * @throws ConfigurationLoader.ConfigurationException if the file cannot be loaded or required properties are missing
     */
    public static MtnCongoConfig fromPropertiesFile(String fileName) {
        Properties properties;

        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            properties = ConfigurationLoader.loadYamlFromClasspath(fileName);
        } else {
            properties = ConfigurationLoader.loadFromClasspath(fileName);
        }

        return fromProperties(properties);
    }

    /**
     * Creates a configuration from a Properties object.
     *
     * @param properties the properties object
     * @return a new MtnCongoConfig instance
     * @throws ConfigurationLoader.ConfigurationException if required properties are missing
     */
    public static MtnCongoConfig fromProperties(Properties properties) {
        // Validate required properties first
        ConfigurationLoader.validateRequiredProperties(
            properties,
            MtnCongoConfigProperties.getRequiredProperties()
        );

        return builder()
            .apiUser(ConfigurationLoader.getRequiredProperty(properties, MtnCongoConfigProperties.API_USER))
            .apiKey(ConfigurationLoader.getRequiredProperty(properties, MtnCongoConfigProperties.API_KEY))
            .subscriptionKey(ConfigurationLoader.getRequiredProperty(properties, MtnCongoConfigProperties.SUBSCRIPTION_KEY))
            .environment(ConfigurationLoader.getProperty(properties, MtnCongoConfigProperties.ENVIRONMENT, MtnCongoConfigProperties.DEFAULT_ENVIRONMENT))
            .production(ConfigurationLoader.getBooleanProperty(properties, MtnCongoConfigProperties.PRODUCTION, MtnCongoConfigProperties.DEFAULT_PRODUCTION))
            .baseUrl(ConfigurationLoader.getProperty(properties, MtnCongoConfigProperties.BASE_URL, null))
            .callbackUrl(ConfigurationLoader.getProperty(properties, MtnCongoConfigProperties.CALLBACK_URL, null))
            .connectionTimeout(ConfigurationLoader.getIntProperty(properties, MtnCongoConfigProperties.CONNECTION_TIMEOUT, MtnCongoConfigProperties.DEFAULT_CONNECTION_TIMEOUT))
            .requestTimeout(ConfigurationLoader.getIntProperty(properties, MtnCongoConfigProperties.REQUEST_TIMEOUT, MtnCongoConfigProperties.DEFAULT_REQUEST_TIMEOUT))
            .build();
    }

    /**
     * Builder for MtnCongoConfig.
     */
    public static class Builder {
        private String apiUser;
        private String apiKey;
        private String subscriptionKey;
        private String environment;
        private String baseUrl;
        private String callbackUrl;
        private boolean production = false;
        private PaymuxHttpClient httpClient;
        private int connectionTimeout = 30000;
        private int requestTimeout = 60000;

        /**
         * Set the API User (UUID).
         * <p>
         * This is the UUID generated when you create an API user in the MTN Developer Portal.
         * </p>
         *
         * @param apiUser the API user UUID
         * @return this builder
         */
        public Builder apiUser(String apiUser) {
            this.apiUser = apiUser;
            return this;
        }

        /**
         * Set the API Key.
         * <p>
         * This is the key associated with your API user.
         * </p>
         *
         * @param apiKey the API key
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Set the Subscription Key (Ocp-Apim-Subscription-Key).
         * <p>
         * This is the subscription key for the MTN Mobile Money API.
         * You get this from your subscription in the MTN Developer Portal.
         * </p>
         *
         * @param subscriptionKey the subscription key
         * @return this builder
         */
        public Builder subscriptionKey(String subscriptionKey) {
            this.subscriptionKey = subscriptionKey;
            return this;
        }

        /**
         * Set the target environment.
         * <p>
         * For Congo-Brazzaville, use "mtncongo".
         * For sandbox testing, use "sandbox".
         * </p>
         *
         * @param environment the environment name
         * @return this builder
         */
        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        /**
         * Set a custom base URL.
         * <p>
         * If not set, the default URL will be chosen based on the production flag.
         * </p>
         *
         * @param baseUrl the base URL
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Set the callback URL for transaction notifications.
         * <p>
         * This URL will receive POST notifications when transaction status changes.
         * </p>
         *
         * @param callbackUrl the callback URL
         * @return this builder
         */
        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        /**
         * Set whether to use production environment.
         * <p>
         * If true, uses production URL. If false, uses sandbox URL.
         * </p>
         *
         * @param production true for production, false for sandbox
         * @return this builder
         */
        public Builder production(boolean production) {
            this.production = production;
            return this;
        }

        /**
         * Set a custom HTTP client.
         * <p>
         * If not set, the default HTTP client will be used.
         * </p>
         *
         * @param httpClient the HTTP client
         * @return this builder
         */
        public Builder httpClient(PaymuxHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Set the connection timeout in milliseconds.
         *
         * @param connectionTimeout the connection timeout
         * @return this builder
         */
        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Set the request timeout in milliseconds.
         *
         * @param requestTimeout the request timeout
         * @return this builder
         */
        public Builder requestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Build the MtnCongoConfig instance.
         *
         * @return a new MtnCongoConfig instance
         * @throws IllegalStateException if required fields are missing
         */
        public MtnCongoConfig build() {
            if (apiUser == null || apiUser.isBlank()) {
                throw new IllegalStateException("API User is required");
            }
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("API Key is required");
            }
            if (subscriptionKey == null || subscriptionKey.isBlank()) {
                throw new IllegalStateException("Subscription Key is required");
            }

            if (baseUrl == null || baseUrl.isBlank()) {
                baseUrl = production ? PRODUCTION_URL : SANDBOX_URL;
            }

            if (environment == null || environment.isBlank()) {
                environment = production ? PRODUCTION_ENVIRONMENT : SANDBOX_ENVIRONMENT;
            }

            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            return new MtnCongoConfig(this);
        }
    }
}
