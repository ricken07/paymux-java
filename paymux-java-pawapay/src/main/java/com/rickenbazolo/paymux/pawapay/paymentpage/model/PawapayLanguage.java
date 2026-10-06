package com.rickenbazolo.paymux.pawapay.paymentpage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Language of a payment page. The wire value is upper-case ({@code EN}, {@code FR}), unlike the
 * lower-case {@code en}/{@code fr} used by checkouts.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayLanguage {

    EN, FR,
    /** A value not known by this version of the library; rejected in requests. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayLanguage fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
