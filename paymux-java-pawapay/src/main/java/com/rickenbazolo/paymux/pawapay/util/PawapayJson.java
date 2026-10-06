package com.rickenbazolo.paymux.pawapay.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickenbazolo.paymux.core.exception.PaymuxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Shared JSON configuration for the PawaPay module.
 * <p>
 * The mapper ignores unknown properties, omits null values and is safe to share
 * (Jackson {@link ObjectMapper} is thread-safe once configured).
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayJson {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private PawapayJson() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * @return the shared, pre-configured object mapper
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Serializes a value to a JSON string.
     *
     * @param value the value to serialize
     * @return the JSON string
     * @throws PaymuxException if serialization fails
     */
    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new PaymuxException("Failed to serialize PawaPay request", e);
        }
    }

    /**
     * Deserializes JSON bytes into the given type.
     *
     * @param json the JSON bytes
     * @param type the target type
     * @param <T>  the target type
     * @return the deserialized value
     * @throws PaymuxException if deserialization fails
     */
    public static <T> T read(byte[] json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new PaymuxException("Failed to parse PawaPay JSON as " + type.getSimpleName(), e);
        }
    }

    /**
     * Deserializes a JSON string into the given type.
     *
     * @param json the JSON string
     * @param type the target type
     * @param <T>  the target type
     * @return the deserialized value
     * @throws PaymuxException if deserialization fails
     */
    public static <T> T read(String json, Class<T> type) {
        return read(json.getBytes(StandardCharsets.UTF_8), type);
    }

    /**
     * Deserializes JSON bytes into a generic type.
     *
     * @param json the JSON bytes
     * @param type the target type reference
     * @param <T>  the target type
     * @return the deserialized value
     * @throws PaymuxException if deserialization fails
     */
    public static <T> T read(byte[] json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new PaymuxException("Failed to parse PawaPay JSON as " + type.getType().getTypeName(), e);
        }
    }

    /**
     * Parses JSON bytes into a tree.
     *
     * @param json the JSON bytes
     * @return the JSON tree
     * @throws PaymuxException if parsing fails
     */
    public static JsonNode tree(byte[] json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new PaymuxException("Failed to parse PawaPay JSON", e);
        }
    }
}
