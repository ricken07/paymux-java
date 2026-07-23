package com.rickenbazolo.paymux.core;

import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;

/**
 * Base configuration interface for Mobile Money clients.
 * Each operator module will extend this with specific configuration options.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public interface MobileMoneyConfig {

    /**
     * Get the base URL for the Mobile Money API.
     *
     * @return the base URL
     */
    String getBaseUrl();

    /**
     * Get the API key or client ID.
     *
     * @return the API key
     */
    String getApiKey();

    /**
     * Get the API secret or client secret.
     *
     * @return the API secret
     */
    String getApiSecret();

    /**
     * Get the HTTP client to use for requests.
     * If null, a default implementation will be used.
     *
     * @return the HTTP client, or null to use default
     */
    PaymuxHttpClient getHttpClient();

    /**
     * Get the connection timeout in milliseconds.
     *
     * @return the connection timeout
     */
    default int getConnectionTimeout() {
        return 30000; // 30 seconds default
    }

    /**
     * Get the request timeout in milliseconds.
     *
     * @return the request timeout
     */
    default int getRequestTimeout() {
        return 60000; // 60 seconds default
    }

    /**
     * Check if the configuration is for production environment.
     *
     * @return true if production, false for sandbox
     */
    default boolean isProduction() {
        return false;
    }
}
