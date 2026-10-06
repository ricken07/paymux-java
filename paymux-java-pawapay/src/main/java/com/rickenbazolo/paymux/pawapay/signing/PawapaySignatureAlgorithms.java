package com.rickenbazolo.paymux.pawapay.signing;

/**
 * Signature algorithms accepted by PawaPay for signed financial requests and signed callbacks
 * (<a href="https://datatracker.ietf.org/doc/rfc9421/">RFC 9421</a> HTTP Message Signatures).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapaySignatureAlgorithms {

    /** ECDSA using curve P-256 and SHA-256. */
    public static final String ECDSA_P256_SHA256 = "ecdsa-p256-sha256";
    /** ECDSA using curve P-384 and SHA-384. */
    public static final String ECDSA_P384_SHA384 = "ecdsa-p384-sha384";
    /** RSASSA-PKCS1-v1_5 using SHA-256. */
    public static final String RSA_V1_5_SHA256 = "rsa-v1_5-sha256";
    /** RSASSA-PSS using SHA-512. */
    public static final String RSA_PSS_SHA512 = "rsa-pss-sha512";

    private PawapaySignatureAlgorithms() {
        throw new AssertionError("Constants class - do not instantiate");
    }

    /**
     * Tells whether a value is one of the algorithms accepted by PawaPay.
     *
     * @param algorithm the algorithm, as it appears in the {@code alg} parameter of a signature
     * @return true if supported
     */
    public static boolean isSupported(String algorithm) {
        if (algorithm == null) {
            return false;
        }
        return switch (algorithm.toLowerCase(java.util.Locale.ROOT)) {
            case ECDSA_P256_SHA256, ECDSA_P384_SHA384, RSA_V1_5_SHA256, RSA_PSS_SHA512 -> true;
            default -> false;
        };
    }
}
