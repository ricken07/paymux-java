package com.rickenbazolo.paymux.pawapay.remittance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Relationship between the sender and the recipient of a remittance.
 * <p>
 * The wire value of {@link #BROTHER_IN_LAW} is {@code BORTHER_IN_LAW}, as spelled by the PawaPay API.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public enum PawapayRelationship {

    FATHER, MOTHER, SON, DAUGHTER, BROTHER, SISTER, HUSBAND, WIFE, PARTNER, FRIEND, AUNT, UNCLE, COUSIN,
    NEPHEW, NIECE, GRANDFATHER, GRANDMOTHER, GRANDSON, GRANDDAUGHTER, STEPCHILD, DAUGHTER_IN_LAW, SON_IN_LAW,
    BROTHER_IN_LAW("BORTHER_IN_LAW"), SISTER_IN_LAW, MOTHER_IN_LAW, GUARDIAN, SELF,
    /** A value not known by this version of the library; rejected in requests. */
    UNKNOWN;

    private final String wireValue;

    PawapayRelationship() {
        this.wireValue = name();
    }

    PawapayRelationship(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayRelationship fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String v = value.trim().toUpperCase();
        for (PawapayRelationship relationship : values()) {
            if (relationship.wireValue.equals(v) || relationship.name().equals(v)) {
                return relationship;
            }
        }
        return UNKNOWN;
    }

    /**
     * @return the value sent to PawaPay
     */
    @JsonValue
    public String toValue() {
        return wireValue;
    }
}
