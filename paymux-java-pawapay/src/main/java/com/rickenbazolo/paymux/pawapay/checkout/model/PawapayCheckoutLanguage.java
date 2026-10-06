package com.rickenbazolo.paymux.pawapay.checkout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Language of a checkout's hosted payment page. The wire value is lower-case ({@code en}, {@code fr}).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayCheckoutLanguage {

    EN, FR,
    /** A value not known by this version of the library; rejected in requests. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayCheckoutLanguage fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
