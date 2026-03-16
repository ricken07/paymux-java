package com.rickenbazolo.paymux.core.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Represents an HTTP response with status code, headers, and body.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class PaymuxHttpResponse {

    private final int statusCode;
    private final Map<String, String> headers;
    private final byte[] body;

    private PaymuxHttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
    }

    public int statusCode() {
        return statusCode;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Optional<byte[]> body() {
        return Optional.ofNullable(body);
    }

    public String bodyAsString() {
        return body != null ? new String(body, StandardCharsets.UTF_8) : "";
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int statusCode;
        private final Map<String, String> headers = new HashMap<>();
        private byte[] body;

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder body(String body) {
            this.body = body.getBytes();
            return this;
        }

        public PaymuxHttpResponse build() {
            return new PaymuxHttpResponse(this);
        }
    }
}
