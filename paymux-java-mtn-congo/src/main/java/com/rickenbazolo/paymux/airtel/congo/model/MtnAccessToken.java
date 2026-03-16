package com.rickenbazolo.paymux.airtel.congo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an MTN Mobile Money access token response.
 * <p>
 * Access tokens are required for all API calls except authentication.
 * Tokens expire after a certain period (typically 3600 seconds).
 * </p>
 *
 * @param accessToken the access token string
 * @param tokenType   the token type (usually "Bearer")
 * @param expiresIn   the number of seconds until the token expires
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public record MtnAccessToken(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") Integer expiresIn
) {
    /**
     * Check if this token response is valid.
     *
     * @return true if access token is present
     */
    public boolean isValid() {
        return accessToken != null && !accessToken.isBlank();
    }
}
