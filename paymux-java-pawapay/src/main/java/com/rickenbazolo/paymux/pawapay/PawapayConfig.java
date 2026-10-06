package com.rickenbazolo.paymux.pawapay;

import com.rickenbazolo.paymux.core.MobileMoneyConfig;
import com.rickenbazolo.paymux.core.config.ConfigurationLoader;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.config.PawapayConfigProperties;
import com.rickenbazolo.paymux.pawapay.signing.PawapayRequestSigner;
import com.rickenbazolo.paymux.pawapay.signing.PawapaySignatureAlgorithms;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuration for the PawaPay client.
 * <p>
 * PawaPay is a Mobile Money aggregator: a single API token gives access to many
 * providers (MTN, Airtel, Orange, ...) across many African countries. The provider is
 * selected per transaction through the {@code provider} code (e.g. {@code MTN_MOMO_COG}).
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * var config = PawapayConfig.builder()
 *     .apiToken("your-api-token")
 *     .production(false)  // Use sandbox
 *     .build();
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayConfig implements MobileMoneyConfig {

    /** Sandbox base URL. */
    public static final String SANDBOX_URL = "https://api.sandbox.pawapay.io/";
    /** Production base URL. */
    public static final String PRODUCTION_URL = "https://api.pawapay.io/";

    private final String apiToken;
    private final String baseUrl;
    private final boolean production;
    private final PaymuxHttpClient httpClient;
    private final int connectionTimeout;
    private final int requestTimeout;
    private final Duration publicKeyCacheTtl;
    private final Duration signatureClockSkew;
    private final PawapayRequestSigner requestSigner;

    private PawapayConfig(Builder builder) {
        this.apiToken = builder.apiToken;
        this.baseUrl = builder.baseUrl;
        this.production = builder.production;
        this.httpClient = builder.httpClient;
        this.connectionTimeout = builder.connectionTimeout;
        this.requestTimeout = builder.requestTimeout;
        this.publicKeyCacheTtl = builder.publicKeyCacheTtl;
        this.signatureClockSkew = builder.signatureClockSkew;
        this.requestSigner = builder.buildRequestSigner();
    }

    /**
     * Get the PawaPay API token (bearer token).
     *
     * @return the API token
     */
    public String getApiToken() {
        return apiToken;
    }

    /**
     * Get the API key. For PawaPay this is the API token.
     *
     * @return the API token
     */
    @Override
    public String getApiKey() {
        return apiToken;
    }

    /**
     * Get the API secret. PawaPay uses a single bearer token, so this is the API token as well.
     *
     * @return the API token
     */
    @Override
    public String getApiSecret() {
        return apiToken;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
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
     * Get the time to live of the cached PawaPay public keys used for callback signature verification.
     *
     * @return the cache TTL
     */
    public Duration getPublicKeyCacheTtl() {
        return publicKeyCacheTtl;
    }

    /**
     * Get the tolerated clock skew when validating callback signature timestamps.
     *
     * @return the clock skew
     */
    public Duration getSignatureClockSkew() {
        return signatureClockSkew;
    }

    /**
     * Get the signer for outbound financial requests (RFC 9421), or null if signing is disabled
     * or not configured.
     * <p>
     * When present, use it to sign {@code POST /v2/deposits}, {@code POST /v2/payouts},
     * {@code POST /v2/payouts/bulk} and {@code POST /v2/refunds} requests - the only PawaPay
     * endpoints that accept signed requests.
     * </p>
     *
     * @return the request signer, or null
     */
    public PawapayRequestSigner getRequestSigner() {
        return requestSigner;
    }

    /**
     * Creates a new builder for PawapayConfig.
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
     * </p>
     *
     * @return a new PawapayConfig instance
     * @throws ConfigurationLoader.ConfigurationException if required properties are missing
     */
    public static PawapayConfig fromProperties() {
        Properties properties = ConfigurationLoader.loadFromClasspath();
        return fromProperties(properties);
    }

    /**
     * Creates a configuration from the specified configuration file.
     * <p>
     * Supports both .properties and .yaml/.yml files. The file type is detected from the extension.
     * </p>
     *
     * @param fileName the name of the configuration file on the classpath
     * @return a new PawapayConfig instance
     * @throws ConfigurationLoader.ConfigurationException if the file cannot be loaded or required properties are missing
     */
    public static PawapayConfig fromPropertiesFile(String fileName) {
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
     * @return a new PawapayConfig instance
     * @throws ConfigurationLoader.ConfigurationException if required properties are missing
     */
    public static PawapayConfig fromProperties(Properties properties) {
        ConfigurationLoader.validateRequiredProperties(
            properties,
            PawapayConfigProperties.getRequiredProperties()
        );

        return builder()
            .apiToken(ConfigurationLoader.getRequiredProperty(properties, PawapayConfigProperties.API_TOKEN))
            .production(ConfigurationLoader.getBooleanProperty(properties, PawapayConfigProperties.PRODUCTION, PawapayConfigProperties.DEFAULT_PRODUCTION))
            .baseUrl(ConfigurationLoader.getProperty(properties, PawapayConfigProperties.BASE_URL, null))
            .connectionTimeout(ConfigurationLoader.getIntProperty(properties, PawapayConfigProperties.CONNECTION_TIMEOUT, PawapayConfigProperties.DEFAULT_CONNECTION_TIMEOUT))
            .requestTimeout(ConfigurationLoader.getIntProperty(properties, PawapayConfigProperties.REQUEST_TIMEOUT, PawapayConfigProperties.DEFAULT_REQUEST_TIMEOUT))
            .publicKeyCacheTtlSeconds(ConfigurationLoader.getIntProperty(properties, PawapayConfigProperties.PUBLIC_KEY_CACHE_TTL, PawapayConfigProperties.DEFAULT_PUBLIC_KEY_CACHE_TTL_SECONDS))
            .signatureClockSkewSeconds(ConfigurationLoader.getIntProperty(properties, PawapayConfigProperties.SIGNATURE_CLOCK_SKEW, PawapayConfigProperties.DEFAULT_SIGNATURE_CLOCK_SKEW_SECONDS))
            .requestSigningEnabled(ConfigurationLoader.getBooleanProperty(properties, PawapayConfigProperties.SIGNING_ENABLED, PawapayConfigProperties.DEFAULT_SIGNING_ENABLED))
            .signingKeyId(ConfigurationLoader.getProperty(properties, PawapayConfigProperties.SIGNING_KEY_ID, null))
            .signingAlgorithm(ConfigurationLoader.getProperty(properties, PawapayConfigProperties.SIGNING_ALGORITHM, null))
            .signingPrivateKeyPem(ConfigurationLoader.getProperty(properties, PawapayConfigProperties.SIGNING_PRIVATE_KEY, null))
            .signingPrivateKeyPath(ConfigurationLoader.getProperty(properties, PawapayConfigProperties.SIGNING_PRIVATE_KEY_PATH, null))
            .signingValiditySeconds(ConfigurationLoader.getIntProperty(properties, PawapayConfigProperties.SIGNING_VALIDITY_SECONDS, PawapayConfigProperties.DEFAULT_SIGNING_VALIDITY_SECONDS))
            .build();
    }

    /**
     * Builder for PawapayConfig.
     */
    public static class Builder {
        private String apiToken;
        private String baseUrl;
        private boolean production = PawapayConfigProperties.DEFAULT_PRODUCTION;
        private PaymuxHttpClient httpClient;
        private int connectionTimeout = PawapayConfigProperties.DEFAULT_CONNECTION_TIMEOUT;
        private int requestTimeout = PawapayConfigProperties.DEFAULT_REQUEST_TIMEOUT;
        private Duration publicKeyCacheTtl = Duration.ofSeconds(PawapayConfigProperties.DEFAULT_PUBLIC_KEY_CACHE_TTL_SECONDS);
        private Duration signatureClockSkew = Duration.ofSeconds(PawapayConfigProperties.DEFAULT_SIGNATURE_CLOCK_SKEW_SECONDS);
        private boolean signingEnabled = PawapayConfigProperties.DEFAULT_SIGNING_ENABLED;
        private String signingKeyId;
        private String signingAlgorithm;
        private PrivateKey signingPrivateKey;
        private String signingPrivateKeyPem;
        private String signingPrivateKeyPath;
        private int signingValiditySeconds = PawapayConfigProperties.DEFAULT_SIGNING_VALIDITY_SECONDS;

        /**
         * Set the API token generated from the PawaPay Dashboard.
         *
         * @param apiToken the bearer token
         * @return this builder
         */
        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
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
         * Set whether to use the production environment.
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
         * Set the time to live of the cached PawaPay public keys.
         *
         * @param publicKeyCacheTtl the cache TTL
         * @return this builder
         */
        public Builder publicKeyCacheTtl(Duration publicKeyCacheTtl) {
            this.publicKeyCacheTtl = publicKeyCacheTtl;
            return this;
        }

        /**
         * Set the time to live of the cached PawaPay public keys, in seconds.
         *
         * @param seconds the cache TTL in seconds
         * @return this builder
         */
        public Builder publicKeyCacheTtlSeconds(int seconds) {
            return publicKeyCacheTtl(Duration.ofSeconds(seconds));
        }

        /**
         * Set the tolerated clock skew when validating callback signature timestamps.
         *
         * @param signatureClockSkew the clock skew
         * @return this builder
         */
        public Builder signatureClockSkew(Duration signatureClockSkew) {
            this.signatureClockSkew = signatureClockSkew;
            return this;
        }

        /**
         * Set the tolerated clock skew, in seconds.
         *
         * @param seconds the clock skew in seconds
         * @return this builder
         */
        public Builder signatureClockSkewSeconds(int seconds) {
            return signatureClockSkew(Duration.ofSeconds(seconds));
        }

        /**
         * Set whether to sign outbound financial requests (deposits, payouts, bulk payouts,
         * refunds) with RFC 9421 HTTP Message Signatures.
         * <p>
         * Requires "Signed requests" to be enabled in the PawaPay Dashboard with the matching
         * public key uploaded there; the library does not manage that side, it only signs with
         * the private key you supply through {@link #signingPrivateKey(PrivateKey)},
         * {@link #signingPrivateKeyPem(String)} or {@link #signingPrivateKeyPath(String)}.
         * </p>
         *
         * @param signingEnabled true to sign outbound financial requests
         * @return this builder
         */
        public Builder requestSigningEnabled(boolean signingEnabled) {
            this.signingEnabled = signingEnabled;
            return this;
        }

        /**
         * Set the id of the public key uploaded to the PawaPay Dashboard. Required when
         * {@link #requestSigningEnabled(boolean) signing is enabled}.
         *
         * @param signingKeyId the key id
         * @return this builder
         */
        public Builder signingKeyId(String signingKeyId) {
            this.signingKeyId = signingKeyId;
            return this;
        }

        /**
         * Set the signature algorithm. Required when signing is enabled.
         *
         * @param signingAlgorithm one of {@link PawapaySignatureAlgorithms}
         * @return this builder
         */
        public Builder signingAlgorithm(String signingAlgorithm) {
            this.signingAlgorithm = signingAlgorithm;
            return this;
        }

        /**
         * Set the private key directly, e.g. one already loaded from a KMS or HSM. Mutually
         * exclusive with {@link #signingPrivateKeyPem(String)} and {@link #signingPrivateKeyPath(String)}.
         *
         * @param signingPrivateKey the private key
         * @return this builder
         */
        public Builder signingPrivateKey(PrivateKey signingPrivateKey) {
            this.signingPrivateKey = signingPrivateKey;
            return this;
        }

        /**
         * Set the private key as PEM (PKCS8, {@code -----BEGIN PRIVATE KEY-----}) or raw base64
         * DER content. Mutually exclusive with {@link #signingPrivateKey(PrivateKey)} and
         * {@link #signingPrivateKeyPath(String)}.
         *
         * @param signingPrivateKeyPem the PEM text or base64 content, or null/blank to skip
         * @return this builder
         */
        public Builder signingPrivateKeyPem(String signingPrivateKeyPem) {
            this.signingPrivateKeyPem = signingPrivateKeyPem;
            return this;
        }

        /**
         * Set the path to a file containing the private key, as PEM (PKCS8) or raw base64 DER
         * content. Mutually exclusive with {@link #signingPrivateKey(PrivateKey)} and
         * {@link #signingPrivateKeyPem(String)}.
         *
         * @param signingPrivateKeyPath the file path, or null/blank to skip
         * @return this builder
         */
        public Builder signingPrivateKeyPath(String signingPrivateKeyPath) {
            this.signingPrivateKeyPath = signingPrivateKeyPath;
            return this;
        }

        /**
         * Set how long an outbound request signature remains valid, in seconds.
         *
         * @param seconds the validity, in seconds
         * @return this builder
         */
        public Builder signingValiditySeconds(int seconds) {
            this.signingValiditySeconds = seconds;
            return this;
        }

        /**
         * Build the PawapayConfig instance.
         *
         * @return a new PawapayConfig instance
         * @throws IllegalStateException if required fields are missing
         */
        public PawapayConfig build() {
            if (apiToken == null || apiToken.isBlank()) {
                throw new IllegalStateException("API Token is required");
            }
            if (connectionTimeout <= 0) {
                throw new IllegalStateException("Connection timeout must be positive");
            }
            if (requestTimeout <= 0) {
                throw new IllegalStateException("Request timeout must be positive");
            }
            if (publicKeyCacheTtl == null || publicKeyCacheTtl.isNegative()) {
                throw new IllegalStateException("Public key cache TTL must be zero or positive");
            }
            if (signatureClockSkew == null || signatureClockSkew.isNegative()) {
                throw new IllegalStateException("Signature clock skew must be zero or positive");
            }

            if (baseUrl == null || baseUrl.isBlank()) {
                baseUrl = production ? PRODUCTION_URL : SANDBOX_URL;
            }

            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            if (signingEnabled) {
                validateSigningConfiguration();
            }

            return new PawapayConfig(this);
        }

        private void validateSigningConfiguration() {
            if (signingKeyId == null || signingKeyId.isBlank()) {
                throw new IllegalStateException("Signing key id is required when request signing is enabled");
            }
            if (signingAlgorithm == null || signingAlgorithm.isBlank()) {
                throw new IllegalStateException("Signing algorithm is required when request signing is enabled");
            }
            if (!PawapaySignatureAlgorithms.isSupported(signingAlgorithm)) {
                throw new IllegalArgumentException("Unsupported signing algorithm '" + signingAlgorithm
                    + "': expected one of ecdsa-p256-sha256, ecdsa-p384-sha384, rsa-v1_5-sha256, rsa-pss-sha512");
            }
            if (signingValiditySeconds <= 0) {
                throw new IllegalStateException("Signing validity must be positive");
            }

            int sources = (signingPrivateKey != null ? 1 : 0)
                + (signingPrivateKeyPem != null && !signingPrivateKeyPem.isBlank() ? 1 : 0)
                + (signingPrivateKeyPath != null && !signingPrivateKeyPath.isBlank() ? 1 : 0);
            if (sources == 0) {
                throw new IllegalStateException(
                    "A signing private key is required when request signing is enabled: "
                        + "set signingPrivateKey, signingPrivateKeyPem or signingPrivateKeyPath");
            }
            if (sources > 1) {
                throw new IllegalStateException(
                    "Specify exactly one of signingPrivateKey, signingPrivateKeyPem or signingPrivateKeyPath, not several");
            }
        }

        /**
         * Resolves the configured private key, reading a file or parsing PEM/base64 content if needed.
         *
         * @return the resolved private key, or null if a key was not supplied
         */
        private PrivateKey resolveSigningPrivateKey() {
            if (signingPrivateKey != null) {
                return signingPrivateKey;
            }
            if (signingPrivateKeyPath != null && !signingPrivateKeyPath.isBlank()) {
                String content;
                try {
                    content = Files.readString(Path.of(signingPrivateKeyPath));
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to read PawaPay signing private key file: " + signingPrivateKeyPath, e);
                } catch (UncheckedIOException e) {
                    throw new IllegalStateException("Unable to read PawaPay signing private key file: " + signingPrivateKeyPath, e.getCause());
                }
                return PawapayRequestSigner.parsePrivateKey(content, signingAlgorithm);
            }
            if (signingPrivateKeyPem != null && !signingPrivateKeyPem.isBlank()) {
                return PawapayRequestSigner.parsePrivateKey(signingPrivateKeyPem, signingAlgorithm);
            }
            return null;
        }

        /**
         * Builds the request signer for this configuration, or returns null if signing is disabled.
         *
         * @return the request signer, or null
         */
        private PawapayRequestSigner buildRequestSigner() {
            if (!signingEnabled) {
                return null;
            }
            PrivateKey privateKey = resolveSigningPrivateKey();
            return new PawapayRequestSigner(signingKeyId, signingAlgorithm, privateKey, Duration.ofSeconds(signingValiditySeconds));
        }
    }
}
