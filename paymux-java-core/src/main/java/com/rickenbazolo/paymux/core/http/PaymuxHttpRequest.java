package com.rickenbazolo.paymux.core.http;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Represents an HTTP request with method, URL, headers, and optional body.
 */
/**
 * PaymuxHttpRequest class description.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */public record PaymuxHttpRequest(String method, String url, Map<String, String> headers, byte[] bodyData) {

    public PaymuxHttpRequest(Builder builder) {
        this(builder.method, builder.url, Map.copyOf(builder.headers), builder.body);
    }

    public Optional<byte[]> body() {
        return Optional.ofNullable(bodyData);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String method = "GET";
        private String url;
        private final Map<String, String> headers = new HashMap<>();
        private byte[] body;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
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

        public PaymuxHttpRequest build() {
            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("URL is required");
            }
            return new PaymuxHttpRequest(this);
        }
    }
}
