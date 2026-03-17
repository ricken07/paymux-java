package com.rickenbazolo.paymux.mtn.congo.util;

/**
 * Validation utilities for MTN Mobile Money operations.
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public final class MtnValidation {

    private MtnValidation() {
        // Utility class
    }

    /**
     * Validate a Congo phone number.
     * <p>
     * Valid phone numbers must:
     * <ul>
     *   <li>Be exactly 9 digits</li>
     *   <li>Contain only numeric characters</li>
     * </ul>
     * </p>
     *
     * @param phoneNumber the phone number to validate (9 digits, no country code)
     * @throws IllegalArgumentException if the phone number is invalid
     */
    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }

        if (phoneNumber.length() != 9) {
            throw new IllegalArgumentException(
                "Invalid MTN phone number: must contain exactly 9 digits (got " + phoneNumber.length() + ")"
            );
        }

        if (!phoneNumber.matches("\\d{9}")) {
            throw new IllegalArgumentException(
                "Invalid MTN phone number: must contain only digits"
            );
        }
    }

    /**
     * Format a Congo phone number by adding the country code.
     * <p>
     * Converts a 9-digit phone number to the format required by MTN API: "242XXXXXXXXX"
     * </p>
     *
     * @param phoneNumber the phone number (9 digits)
     * @return the formatted phone number with country code
     * @throws IllegalArgumentException if the phone number is invalid
     *
     */
    public static String formatPhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        return "242" + phoneNumber;
    }

    /**
     * Validate an amount.
     * <p>
     * Amount must be positive.
     * </p>
     *
     * @param amount the amount to validate
     * @throws IllegalArgumentException if the amount is invalid
     */
    public static void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive (got " + amount + ")");
        }
    }

    /**
     * Validate an external ID (UUID).
     * <p>
     * External ID must not be null or blank.
     * </p>
     *
     * @param externalId the external ID to validate
     * @throws IllegalArgumentException if the external ID is invalid
     */
    public static void validateExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("External ID cannot be null or blank");
        }
    }
}
