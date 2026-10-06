package com.rickenbazolo.paymux.pawapay.config;

/**
 * Property keys for PawaPay configuration.
 * <p>
 * This class defines the hierarchy of property keys used to configure the PawaPay client.
 * All properties are prefixed with {@code paymux.pawapay.} to avoid conflicts.
 * </p>
 * <p>
 * Example properties file:
 * <pre>
 * # PawaPay Configuration
 * paymux.pawapay.api-token=${PAWAPAY_API_TOKEN}
 * paymux.pawapay.production=false
 * paymux.pawapay.base-url=https://api.sandbox.pawapay.io/
 * paymux.pawapay.connection-timeout=30000
 * paymux.pawapay.request-timeout=60000

 * paymux.pawapay.public-key-cache-ttl=3600
 * paymux.pawapay.signature-clock-skew=120
 * paymux.pawapay.signing.enabled=false
 * paymux.pawapay.signing.key-id=CUSTOMER_TEST_KEY
 * paymux.pawapay.signing.algorithm=ecdsa-p256-sha256
 * paymux.pawapay.signing.private-key-path=/path/to/private-key.pem
 * paymux.pawapay.signing.validity-seconds=60
 * </pre>
 * </p>
 * <p>
 * PawaPay callbacks are configured once per merchant account in the PawaPay Dashboard,
 * not per request, so there is no callback URL property.
 * </p>
 * <p>
 * The {@code signing.*} properties configure outbound request signing (RFC 9421) for financial
 * requests (deposits, payouts, bulk payouts, refunds). This requires generating a key pair,
 * uploading the public key and enabling "Signed requests" in the PawaPay Dashboard first; the
 * library only signs with the private key you supply, it does not manage the Dashboard side.
 * Supply the private key with exactly one of {@code signing.private-key} (inline PEM or base64)
 * or {@code signing.private-key-path} (a file path), or pass a {@code java.security.PrivateKey}
 * directly through the builder.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayConfigProperties {

    /**
     * Property key prefix for all PawaPay configuration.
     */
    public static final String PREFIX = "paymux.pawapay.";

    /**
     * API token (required).
     * <p>
     * Bearer token generated from the PawaPay Dashboard. Tokens differ between the
     * sandbox and production accounts.
     * </p>
     */
    public static final String API_TOKEN = PREFIX + "api-token";

    /**
     * Production mode flag (optional, default: false).
     * <p>
     * Set to true to target {@code https://api.pawapay.io/}, false for the sandbox
     * {@code https://api.sandbox.pawapay.io/}.
     * </p>
     */
    public static final String PRODUCTION = PREFIX + "production";

    /**
     * Base URL for the PawaPay API (optional).
     * <p>
     * If not specified, the URL is derived from the production flag.
     * </p>
     */
    public static final String BASE_URL = PREFIX + "base-url";

    /**
     * HTTP connection timeout in milliseconds (optional, default: 30000).
     */
    public static final String CONNECTION_TIMEOUT = PREFIX + "connection-timeout";

    /**
     * HTTP request timeout in milliseconds (optional, default: 60000).
     */
    public static final String REQUEST_TIMEOUT = PREFIX + "request-timeout";

    /**
     * Time to live, in seconds, of the cached PawaPay public keys used to verify
     * callback signatures (optional, default: 3600).
     */
    public static final String PUBLIC_KEY_CACHE_TTL = PREFIX + "public-key-cache-ttl";

    /**
     * Tolerated clock skew, in seconds, when validating the {@code created} / {@code expires}
     * parameters of callback signatures (optional, default: 120).
     */
    public static final String SIGNATURE_CLOCK_SKEW = PREFIX + "signature-clock-skew";

    /**
     * Whether to sign outbound financial requests (optional, default: false).
     * <p>
     * Requires "Signed requests" to be enabled in the PawaPay Dashboard with the matching
     * public key uploaded there first.
     * </p>
     */
    public static final String SIGNING_ENABLED = PREFIX + "signing.enabled";

    /**
     * The id of the public key uploaded to the PawaPay Dashboard (required if signing is enabled).
     */
    public static final String SIGNING_KEY_ID = PREFIX + "signing.key-id";

    /**
     * The signature algorithm (required if signing is enabled). One of {@code ecdsa-p256-sha256},
     * {@code ecdsa-p384-sha384}, {@code rsa-v1_5-sha256} or {@code rsa-pss-sha512}.
     */
    public static final String SIGNING_ALGORITHM = PREFIX + "signing.algorithm";

    /**
     * The private key, as PEM (PKCS8) or raw base64 DER content (optional; mutually exclusive
     * with {@link #SIGNING_PRIVATE_KEY_PATH}). Literal {@code \n} sequences are unescaped, so the
     * key can be embedded in a single-line environment variable.
     */
    public static final String SIGNING_PRIVATE_KEY = PREFIX + "signing.private-key";

    /**
     * Path to a file containing the private key, as PEM (PKCS8) or raw base64 DER content
     * (optional; mutually exclusive with {@link #SIGNING_PRIVATE_KEY}).
     */
    public static final String SIGNING_PRIVATE_KEY_PATH = PREFIX + "signing.private-key-path";

    /**
     * How long a request signature remains valid, in seconds (optional, default: 60).
     */
    public static final String SIGNING_VALIDITY_SECONDS = PREFIX + "signing.validity-seconds";

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
     * Default public key cache TTL (1 hour).
     */
    public static final int DEFAULT_PUBLIC_KEY_CACHE_TTL_SECONDS = 3600;

    /**
     * Default signature clock skew (2 minutes).
     */
    public static final int DEFAULT_SIGNATURE_CLOCK_SKEW_SECONDS = 120;

    /**
     * Default outbound request signing flag (disabled).
     */
    public static final boolean DEFAULT_SIGNING_ENABLED = false;

    /**
     * Default validity of an outbound request signature (60 seconds).
     */
    public static final int DEFAULT_SIGNING_VALIDITY_SECONDS = 60;

    /**
     * Private constructor to prevent instantiation.
     */
    private PawapayConfigProperties() {
        throw new AssertionError("Constants class - do not instantiate");
    }

    /**
     * Gets all required property keys.
     *
     * @return array of required property keys
     */
    public static String[] getRequiredProperties() {
        return new String[]{
            API_TOKEN
        };
    }
}
