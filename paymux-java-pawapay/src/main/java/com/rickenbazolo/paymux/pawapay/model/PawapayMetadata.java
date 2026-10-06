package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One metadata entry attached to a PawaPay initiation request.
 * <p>
 * PawaPay expects {@code metadata} as an array of single key objects, optionally flagged as
 * personally identifiable information:
 * <pre>
 * "metadata": [
 *   {"orderId": "ORD-123456789"},
 *   {"customerId": "customer@email.com", "isPII": true}
 * ]
 * </pre>
 * In status responses and callbacks, PawaPay returns the metadata as a flat JSON object
 * (see {@code getMetadata()} on the transaction models).
 * </p>
 *
 * @param values the JSON object of this entry
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public record PawapayMetadata(Map<String, Object> values) {

    /** Key of the PII flag. */
    public static final String IS_PII = "isPII";

    public PawapayMetadata {
        values = values == null ? Map.of() : Map.copyOf(values);
    }

    /**
     * Creates a metadata entry.
     *
     * @param key   the field name
     * @param value the field value
     * @return a new entry
     */
    public static PawapayMetadata of(String key, Object value) {
        return new PawapayMetadata(Map.of(requireKey(key), Objects.requireNonNull(value, "value")));
    }

    /**
     * Creates a metadata entry flagged as personally identifiable information.
     *
     * @param key   the field name
     * @param value the field value
     * @return a new entry
     */
    public static PawapayMetadata pii(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(requireKey(key), Objects.requireNonNull(value, "value"));
        map.put(IS_PII, Boolean.TRUE);
        return new PawapayMetadata(map);
    }

    /**
     * Creates a metadata entry from a raw JSON object.
     *
     * @param values the JSON object
     * @return a new entry
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PawapayMetadata fromJson(Map<String, Object> values) {
        return new PawapayMetadata(values);
    }

    /**
     * @return the JSON object of this entry
     */
    @JsonValue
    public Map<String, Object> toJson() {
        return values;
    }

    /**
     * @return true if this entry is flagged as PII
     */
    public boolean isPii() {
        return Boolean.TRUE.equals(values.get(IS_PII));
    }

    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Metadata key is required");
        }
        if (IS_PII.equals(key)) {
            throw new IllegalArgumentException("'" + IS_PII + "' is a reserved metadata key");
        }
        return key;
    }
}
