package com.rickenbazolo.paymux.pawapay.model;

/**
 * Well-known PawaPay provider codes.
 * <p>
 * Provider codes follow the {@code <OPERATOR>_<ISO-3166-1 alpha-3 country>} convention.
 * This list is a convenience for the most common Central and West African providers; the
 * authoritative, up-to-date list is returned by the active configuration endpoint
 * ({@code PawapayClient#getActiveConfiguration()}) and documented at
 * <a href="https://docs.pawapay.io/v2/docs/providers">docs.pawapay.io/v2/docs/providers</a>.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayProviders {

    // Congo-Brazzaville (COG, XAF)
    public static final String MTN_MOMO_COG = "MTN_MOMO_COG";
    public static final String AIRTEL_COG = "AIRTEL_COG";

    // Democratic Republic of Congo (COD, CDF / USD)
    public static final String VODACOM_MPESA_COD = "VODACOM_MPESA_COD";
    public static final String AIRTEL_COD = "AIRTEL_COD";
    public static final String ORANGE_COD = "ORANGE_COD";

    // Cameroon (CMR, XAF)
    public static final String MTN_MOMO_CMR = "MTN_MOMO_CMR";
    public static final String ORANGE_CMR = "ORANGE_CMR";

    // Gabon (GAB, XAF)
    public static final String AIRTEL_GAB = "AIRTEL_GAB";

    // Benin (BEN, XOF)
    public static final String MTN_MOMO_BEN = "MTN_MOMO_BEN";
    public static final String MOOV_BEN = "MOOV_BEN";

    // Côte d'Ivoire (CIV, XOF)
    public static final String MTN_MOMO_CIV = "MTN_MOMO_CIV";
    public static final String ORANGE_CIV = "ORANGE_CIV";
    public static final String WAVE_CIV = "WAVE_CIV";

    // Senegal (SEN, XOF)
    public static final String FREE_SEN = "FREE_SEN";
    public static final String ORANGE_SEN = "ORANGE_SEN";
    public static final String WAVE_SEN = "WAVE_SEN";

    // Zambia (ZMB, ZMW)
    public static final String MTN_MOMO_ZMB = "MTN_MOMO_ZMB";
    public static final String AIRTEL_OAPI_ZMB = "AIRTEL_OAPI_ZMB";
    public static final String ZAMTEL_ZMB = "ZAMTEL_ZMB";

    private PawapayProviders() {
        throw new AssertionError("Constants class - do not instantiate");
    }

    /**
     * Extracts the ISO 3166-1 alpha-3 country code from a provider code.
     *
     * @param provider the provider code (e.g. {@code MTN_MOMO_COG})
     * @return the country code (e.g. {@code COG}), or null if the code has no country suffix
     */
    public static String countryOf(String provider) {
        if (provider == null) {
            return null;
        }
        int idx = provider.lastIndexOf('_');
        if (idx < 0 || provider.length() - idx - 1 != 3) {
            return null;
        }
        String suffix = provider.substring(idx + 1);
        return suffix.chars().allMatch(Character::isUpperCase) ? suffix : null;
    }
}
