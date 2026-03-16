package com.rickenbazolo.paymux.core.enums;

/**
 * Enumeration of supported currencies for Mobile Money operations.
 * Defines the currencies that can be used across different Mobile Money providers.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public enum MoMoCurrency {

    XAF("XAF"),
    EURO("EURO");

    private final String value;

    MoMoCurrency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
