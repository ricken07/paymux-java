package com.rickenbazolo.paymux.pawapay.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Parsing and formatting of the RFC 3339 timestamps used by PawaPay.
 * <p>
 * PawaPay uses zoned timestamps for platform events ({@code 2020-02-21T17:32:29Z}) and
 * zone-less timestamps, interpreted as UTC, for statement date ranges ({@code 2025-05-10T10:00:00}).
 * The models keep the raw strings; these helpers convert them on demand.
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayDates {

    private PawapayDates() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Parses a timestamp into an instant. Zone-less values are interpreted as UTC.
     *
     * @param value the raw timestamp, may be null
     * @return the instant, or empty if absent or unparsable
     */
    public static Optional<Instant> parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String v = value.trim();
        try {
            return Optional.of(OffsetDateTime.parse(v).toInstant());
        } catch (DateTimeParseException ignored) {
            // not an offset date-time
        }
        try {
            return Optional.of(Instant.parse(v));
        } catch (DateTimeParseException ignored) {
            // not an instant
        }
        try {
            return Optional.of(LocalDateTime.parse(v).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses a timestamp into a UTC local date-time.
     *
     * @param value the raw timestamp, may be null
     * @return the UTC local date-time, or empty if absent or unparsable
     */
    public static Optional<LocalDateTime> parseUtcLocalDateTime(String value) {
        return parseInstant(value).map(instant -> LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    /**
     * Formats a local date-time the way PawaPay expects statement boundaries ({@code 2025-05-10T10:00:00}).
     *
     * @param dateTime the date-time, interpreted as UTC
     * @return the formatted value
     */
    public static String formatUtcLocalDateTime(LocalDateTime dateTime) {
        return dateTime.withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Formats an instant as a UTC zone-less date-time ({@code 2025-05-10T10:00:00}).
     *
     * @param instant the instant
     * @return the formatted value
     */
    public static String formatUtcLocalDateTime(Instant instant) {
        return formatUtcLocalDateTime(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }
}
