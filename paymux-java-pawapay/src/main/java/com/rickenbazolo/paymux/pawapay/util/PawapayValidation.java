package com.rickenbazolo.paymux.pawapay.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validation utilities for PawaPay requests.
 * <p>
 * The rules mirror the constraints published in the PawaPay v2 API reference so that
 * obviously invalid requests are rejected locally, before any network call.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayValidation {

    /** PawaPay amount pattern: no leading zeros (except below 1), up to 3 decimals, no trailing zeros. */
    public static final Pattern AMOUNT_PATTERN = Pattern.compile("^([0]|([1-9][0-9]{0,17}))([.][0-9]{0,3}[1-9])?$");
    /** MSISDN: digits only, country code included, no leading zero, no '+', 7 to 15 digits. */
    public static final Pattern PHONE_PATTERN = Pattern.compile("^[1-9][0-9]{6,14}$");
    /** ISO 4217 code: three uppercase letters. */
    public static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    /** ISO 3166-1 alpha-3 code: three uppercase letters. */
    public static final Pattern COUNTRY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    /** Customer message: 4 to 22 alphanumeric characters or spaces. */
    public static final Pattern CUSTOMER_MESSAGE_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]{4,22}$");
    /** Pre-authorisation code: 1 to 36 alphanumeric characters. */
    public static final Pattern PRE_AUTHORISATION_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,36}$");
    /** Maximum number of metadata entries. */
    public static final int MAX_METADATA_FIELDS = 10;
    /** Maximum number of payouts in a bulk request. */
    public static final int MAX_BULK_PAYOUTS = 20;

    private PawapayValidation() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Ensures a value is neither null nor blank.
     *
     * @param value the value
     * @param field the field name used in the error message
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is null or blank
     */
    public static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    /**
     * Ensures a value is a canonical 36-character UUID.
     *
     * @param value the value
     * @param field the field name used in the error message
     * @return the value
     * @throws IllegalArgumentException if the value is not a UUID
     */
    public static String requireUuid(String value, String field) {
        String v = requireNonBlank(value, field);
        if (v.length() != 36) {
            throw new IllegalArgumentException(field + " must be a 36 characters UUID (got '" + v + "')");
        }
        try {
            UUID.fromString(v);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " must be a valid UUID (got '" + v + "')", e);
        }
        return v;
    }

    /**
     * Normalizes an amount to the PawaPay canonical form.
     * <p>
     * Trailing decimal zeros are removed ({@code "15.00"} becomes {@code "15"},
     * {@code "10.50"} becomes {@code "10.5"}) because PawaPay rejects them.
     * </p>
     *
     * @param amount the amount as a string
     * @return the normalized amount
     * @throws IllegalArgumentException if the amount is not a number
     */
    public static String normalizeAmount(String amount) {
        String v = requireNonBlank(amount, "Amount");
        try {
            BigDecimal decimal = new BigDecimal(v);
            BigDecimal stripped = decimal.stripTrailingZeros();
            if (stripped.scale() < 0) {
                stripped = stripped.setScale(0);
            }
            return stripped.toPlainString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amount, e);
        }
    }

    /**
     * Validates an already normalized amount.
     *
     * @param amount the amount
     * @return the amount
     * @throws IllegalArgumentException if the amount is not positive or does not match the PawaPay pattern
     */
    public static String validateAmount(String amount) {
        String v = requireNonBlank(amount, "Amount");
        if (!AMOUNT_PATTERN.matcher(v).matches()) {
            throw new IllegalArgumentException(
                "Invalid amount '" + v + "': must match " + AMOUNT_PATTERN.pattern()
                    + " (no leading zeros, at most 3 decimals, no trailing zeros)");
        }
        if (new BigDecimal(v).signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive (got " + v + ")");
        }
        return v;
    }

    /**
     * Validates a phone number in PawaPay MSISDN format.
     *
     * @param phoneNumber the phone number
     * @return the phone number
     * @throws IllegalArgumentException if the phone number is invalid
     */
    public static String validatePhoneNumber(String phoneNumber) {
        String v = requireNonBlank(phoneNumber, "Phone number");
        if (!PHONE_PATTERN.matcher(v).matches()) {
            throw new IllegalArgumentException(
                "Invalid phone number '" + v + "': digits only, country code included, no leading zero or '+' (e.g. 242065551234)");
        }
        return v;
    }

    /**
     * Validates an ISO 4217 currency code.
     *
     * @param currency the currency code
     * @return the currency code
     * @throws IllegalArgumentException if the currency code is invalid
     */
    public static String validateCurrency(String currency) {
        String v = requireNonBlank(currency, "Currency");
        if (!CURRENCY_PATTERN.matcher(v).matches()) {
            throw new IllegalArgumentException("Invalid currency '" + v + "': must be a 3 letters uppercase ISO 4217 code");
        }
        return v;
    }

    /**
     * Validates an optional ISO 3166-1 alpha-3 country code.
     *
     * @param country the country code, may be null
     * @return the country code, or null
     * @throws IllegalArgumentException if the country code is invalid
     */
    public static String validateCountry(String country) {
        if (country == null) {
            return null;
        }
        if (!COUNTRY_PATTERN.matcher(country).matches()) {
            throw new IllegalArgumentException("Invalid country '" + country + "': must be a 3 letters uppercase ISO 3166-1 alpha-3 code");
        }
        return country;
    }

    /**
     * Validates a provider code (e.g. {@code MTN_MOMO_COG}).
     *
     * @param provider the provider code
     * @return the provider code
     * @throws IllegalArgumentException if the provider code is blank
     */
    public static String validateProvider(String provider) {
        return requireNonBlank(provider, "Provider");
    }

    /**
     * Validates an optional customer message.
     *
     * @param customerMessage the customer message, may be null
     * @return the customer message, or null
     * @throws IllegalArgumentException if the message is invalid
     */
    public static String validateCustomerMessage(String customerMessage) {
        if (customerMessage == null) {
            return null;
        }
        if (!CUSTOMER_MESSAGE_PATTERN.matcher(customerMessage).matches()) {
            throw new IllegalArgumentException(
                "Invalid customer message '" + customerMessage + "': 4 to 22 alphanumeric characters or spaces");
        }
        return customerMessage;
    }

    /**
     * Validates an optional pre-authorisation code.
     *
     * @param code the pre-authorisation code, may be null
     * @return the code, or null
     * @throws IllegalArgumentException if the code is invalid
     */
    public static String validatePreAuthorisationCode(String code) {
        if (code == null) {
            return null;
        }
        if (!PRE_AUTHORISATION_CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                "Invalid pre-authorisation code '" + code + "': 1 to 36 alphanumeric characters");
        }
        return code;
    }

    /**
     * Validates a required free text field with a maximum length.
     *
     * @param value     the value
     * @param field     the field name used in the error message
     * @param maxLength the maximum length
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is blank or too long
     */
    public static String validateText(String value, String field, int maxLength) {
        String v = requireNonBlank(value, field);
        if (v.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters (got " + v.length() + ")");
        }
        return v;
    }

    /**
     * Validates an optional free text field with a maximum length.
     *
     * @param value     the value, may be null
     * @param field     the field name used in the error message
     * @param maxLength the maximum length
     * @return the value, or null
     * @throws IllegalArgumentException if the value is too long
     */
    public static String validateOptionalText(String value, String field, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters (got " + value.length() + ")");
        }
        return value;
    }

    /**
     * Validates a required non-negative decimal number given as a string (e.g. an FX rate or a fee).
     *
     * @param value the value
     * @param field the field name used in the error message
     * @return the value
     * @throws IllegalArgumentException if the value is blank, not a number or negative
     */
    public static String validateDecimal(String value, String field) {
        String v = requireNonBlank(value, field);
        try {
            if (new BigDecimal(v).signum() < 0) {
                throw new IllegalArgumentException(field + " must not be negative (got " + v + ")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a decimal number (got '" + v + "')", e);
        }
        return v;
    }

    /**
     * Validates an optional ISO 8601 date ({@code yyyy-MM-dd}).
     *
     * @param value the value, may be null
     * @param field the field name used in the error message
     * @return the value, or null
     * @throws IllegalArgumentException if the value is not a valid date
     */
    public static String validateOptionalDate(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(field + " must be an ISO 8601 date (yyyy-MM-dd), got '" + value + "'", e);
        }
        return value;
    }

    /**
     * Validates the number of metadata entries.
     *
     * @param metadata the metadata entries, may be null
     * @throws IllegalArgumentException if there are more than {@link #MAX_METADATA_FIELDS} entries
     */
    public static void validateMetadata(Collection<?> metadata) {
        if (metadata != null && metadata.size() > MAX_METADATA_FIELDS) {
            throw new IllegalArgumentException(
                "Too many metadata fields: " + metadata.size() + " (maximum " + MAX_METADATA_FIELDS + ")");
        }
    }
}
