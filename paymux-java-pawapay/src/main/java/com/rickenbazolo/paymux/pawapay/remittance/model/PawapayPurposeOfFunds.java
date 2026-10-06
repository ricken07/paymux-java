package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Intended purpose of the funds of a remittance (KYC information about the sender).
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayPurposeOfFunds {

    FAMILY_SUPPORT,
    MEDICAL_EXPENSES,
    TUITION_FEES,
    EDUCATION_SUPPORT,
    GIFT_AND_OTHER_DONATIONS,
    HOME_IMPROVEMENT,
    DEBT_SETTLEMENT,
    REAL_ESTATE,
    TAXES,
    SALARY,
    SAVINGS,
    PERSONAL_TRANSFER,
    OTHER,
    /** A value not known by this version of the library; rejected in requests. */
    UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayPurposeOfFunds fromValue(String value) {
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
