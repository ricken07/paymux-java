package com.rickenbazolo.paymux.mtn.congo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.core.exception.PaymuxHttpException;
import com.rickenbazolo.paymux.core.http.PaymuxHttpRequest;
import com.rickenbazolo.paymux.mtn.congo.MtnCongoConfig;
import com.rickenbazolo.paymux.mtn.congo.model.MtnAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Base64;

/**
 * Service for handling MTN Mobile Money Congo-Brazzaville authentication.
 * <p>
 * This service manages access token retrieval and caching.
 * Tokens are cached and reused until they expire to minimize API calls.
 * </p>
 * <p>
 * Thread-safe implementation with synchronized access to the token cache.
 * </p>
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class MtnAuthService {

    private static final Logger log = LoggerFactory.getLogger(MtnAuthService.class);
    private static final String TOKEN_ENDPOINT = "collection/token/";
    private static final int TOKEN_EXPIRY_BUFFER_SECONDS = 60; // Refresh 1 min before expiry

    private final MtnCongoConfig config;
    private final PaymuxHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Thread-safe token cache
    private volatile MtnAccessToken cachedToken;
    private volatile Instant tokenExpiration;
    private final Object lock = new Object();

    /**
     * Creates a new MtnAuthService.
     *
     * @param config     the MTN configuration
     * @param httpClient the HTTP client
     */
    public MtnAuthService(MtnCongoConfig config, PaymuxHttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get a valid access token.
     * <p>
     * Returns a cached token if still valid, otherwise requests a new one.
     * This method is thread-safe.
     * </p>
     *
     * @return the access token string
     * @throws PaymuxHttpException if authentication fails
     */
    public String getAccessToken() throws PaymuxHttpException {
        if (isTokenValid()) {
            log.debug("Using cached access token");
            return cachedToken.accessToken();
        }

        synchronized (lock) {
            // Double-check after acquiring lock
            if (isTokenValid()) {
                log.debug("Using cached access token (after lock)");
                return cachedToken.accessToken();
            }

            log.info("Requesting new access token");
            return refreshAccessToken();
        }
    }

    /**
     * Force refresh the access token.
     * <p>
     * This method acquires a new token regardless of the cached token's validity.
     * </p>
     *
     * @return the access token string
     * @throws PaymuxHttpException if authentication fails
     */
    public String refreshAccessToken() throws PaymuxHttpException {
        var request = buildAuthRequest();
        var response = httpClient.execute(request);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.error("Authentication failed: HTTP {}", response.statusCode());
            throw new PaymuxHttpException(
                "Failed to obtain access token: HTTP " + response.statusCode()
            );
        }

        try {
            byte[] bodyBytes = response.body().orElseThrow(() ->
                new PaymuxHttpException("Empty response body"));
            var token = objectMapper.readValue(bodyBytes, MtnAccessToken.class);

            if (!token.isValid()) {
                throw new PaymuxHttpException("Invalid access token received");
            }

            cachedToken = token;
            tokenExpiration = calculateExpiration(token);

            log.info("Access token obtained successfully, expires at {}", tokenExpiration);

            return token.accessToken();

        } catch (Exception e) {
            log.error("Failed to parse access token response", e);
            throw new PaymuxHttpException("Failed to parse access token response", e);
        }
    }

    /**
     * Check if the cached token is still valid.
     * <p>
     * A token is considered valid if:
     * <ul>
     *   <li>A cached token exists</li>
     *   <li>The token has not expired (with buffer)</li>
     * </ul>
     * </p>
     *
     * @return true if the cached token is valid
     */
    private boolean isTokenValid() {
        if (cachedToken == null || tokenExpiration == null) {
            return false;
        }

        return Instant.now().isBefore(tokenExpiration);
    }

    /**
     * Calculate the expiration time for a token.
     * <p>
     * Adds a buffer time before the actual expiration to avoid using
     * tokens that are about to expire.
     * </p>
     *
     * @param token the access token
     * @return the expiration instant
     */
    private Instant calculateExpiration(MtnAccessToken token) {
        int expiresIn = token.expiresIn() != null ? token.expiresIn() : 3600;
        int effectiveExpiry = expiresIn - TOKEN_EXPIRY_BUFFER_SECONDS;

        return Instant.now().plusSeconds(Math.max(effectiveExpiry, 0));
    }

    /**
     * Build the HTTP request for authentication.
     *
     * @return the HTTP request
     */
    private PaymuxHttpRequest buildAuthRequest() {
        String basicAuth = buildBasicAuthHeader();

        return PaymuxHttpRequest.builder()
            .url(config.getBaseUrl() + TOKEN_ENDPOINT)
            .method("POST")
            .header("Authorization", basicAuth)
            .header("Ocp-Apim-Subscription-Key", config.getSubscriptionKey())
            .header("Content-Type", "application/json")
            .build();
    }

    /**
     * Build the Basic Authentication header value.
     * <p>
     * Format: "Basic base64(apiUser:apiKey)"
     * </p>
     *
     * @return the Basic Auth header value
     */
    private String buildBasicAuthHeader() {
        String credentials = config.getApiUser() + ":" + config.getApiKey();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    /**
     * Clear the cached token.
     * <p>
     * Useful for testing or forcing a token refresh.
     * </p>
     */
    public void clearCache() {
        synchronized (lock) {
            cachedToken = null;
            tokenExpiration = null;
            log.debug("Token cache cleared");
        }
    }
}
