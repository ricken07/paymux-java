package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type of identification document used to KYC the sender of a remittance.
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayIdentificationType {

    NATIONAL_ID,
    PASSPORT,
    DRIVING_LICENSE,
    SOCIAL_SECURITY_ID,
    RESIDENCE_PERMIT,
    /** A value not known by this version of the library; rejected in requests. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayIdentificationType fromValue(String value) {
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
