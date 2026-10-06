package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Source of the funds of a remittance (KYC information about the sender).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapaySourceOfFunds {

    SALARY,
    SAVINGS,
    LOTTERY,
    LOAN,
    BUSINESS_INCOME,
    GIFT,
    OTHER,
    /** A value not known by this version of the library; rejected in requests. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapaySourceOfFunds fromValue(String value) {
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
