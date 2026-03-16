package com.rickenbazolo.paymux.airtel.congo.config;

/**
 * Property keys for MTN Congo Mobile Money configuration.
 * <p>
 * This class defines the hierarchy of property keys used to configure the MTN Congo client.
 * All properties should be prefixed with "paymux.mtn.congo." to avoid conflicts.
 * </p>
 * <p>
 * Example properties file:
 * <pre>
 * # MTN Congo Configuration
 * paymux.mtn.congo.api-user=dceae61b-c281-44e2-9df9-3bc3968f4baa
 * paymux.mtn.congo.api-key=0ea4abb5d7b54beab0c38ab092bcf814
 * paymux.mtn.congo.subscription-key=2d046e84720f4ddeac06d5f76804b5bf
 * paymux.mtn.congo.environment=mtncongo
 * paymux.mtn.congo.production=false
 * paymux.mtn.congo.base-url=https://ericssonbasicapi1.azure-api.net/
 * paymux.mtn.congo.callback-url=https://your-webhook-url.com/mtn/callback
 * paymux.mtn.congo.connection-timeout=30000
 * paymux.mtn.congo.request-timeout=60000
 * </pre>
 * </p>
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public final class MtnCongoConfigProperties {

    /**
     * Property key prefix for all MTN Congo configuration.
     */
    public static final String PREFIX = "paymux.mtn.congo.";

    /**
     * API User UUID (required).
     * <p>
     * This is the UUID provided by MTN when you register your application.
     * </p>
     */
    public static final String API_USER = PREFIX + "api-user";

    /**
     * API Key (required).
     * <p>
     * This is the API key/password associated with your API User.
     * </p>
     */
    public static final String API_KEY = PREFIX + "api-key";

    /**
     * Ocp-Apim-Subscription-Key (required).
     * <p>
     * This is the subscription key for the MTN Mobile Money API.
     * You get this when you subscribe to the API in the MTN developer portal.
     * </p>
     */
    public static final String SUBSCRIPTION_KEY = PREFIX + "subscription-key";

    /**
     * Target environment (optional, default: "mtncongo").
     * <p>
     * This identifies the environment in which your API user was created.
     * Common values: "mtncongo", "sandbox"
     * </p>
     */
    public static final String ENVIRONMENT = PREFIX + "environment";

    /**
     * Production mode flag (optional, default: false).
     * <p>
     * Set to true when using the production API, false for sandbox.
     * </p>
     */
    public static final String PRODUCTION = PREFIX + "production";

    /**
     * Base URL for the MTN API (optional).
     * <p>
     * If not specified, the default URL will be used based on the production flag.
     * Sandbox: https://ericssonbasicapi1.azure-api.net/
     * Production: https://ericssonbasicapi1.azure-api.net/
     * </p>
     */
    public static final String BASE_URL = PREFIX + "base-url";

    /**
     * Callback URL for transaction notifications (optional).
     * <p>
     * If specified, MTN will send HTTP POST notifications to this URL
     * when transaction status changes.
     * </p>
     */
    public static final String CALLBACK_URL = PREFIX + "callback-url";

    /**
     * HTTP connection timeout in milliseconds (optional, default: 30000).
     * <p>
     * Maximum time to wait when establishing a connection to the MTN API.
     * </p>
     */
    public static final String CONNECTION_TIMEOUT = PREFIX + "connection-timeout";

    /**
     * HTTP request timeout in milliseconds (optional, default: 60000).
     * <p>
     * Maximum time to wait for a response from the MTN API.
     * </p>
     */
    public static final String REQUEST_TIMEOUT = PREFIX + "request-timeout";

    /**
     * Default environment value.
     */
    public static final String DEFAULT_ENVIRONMENT = "mtncongo";

    /**
     * Default production flag.
     */
    public static final boolean DEFAULT_PRODUCTION = false;

    /**
     * Default connection timeout (30 seconds).
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    /**
     * Default request timeout (60 seconds).
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 60000;

    /**
     * Private constructor to prevent instantiation.
     */
    private MtnCongoConfigProperties() {
        throw new AssertionError("Constants class - do not instantiate");
    }

    /**
     * Gets all required property keys.
     *
     * @return array of required property keys
     */
    public static String[] getRequiredProperties() {
        return new String[]{
            API_USER,
            API_KEY,
            SUBSCRIPTION_KEY
        };
    }
}
