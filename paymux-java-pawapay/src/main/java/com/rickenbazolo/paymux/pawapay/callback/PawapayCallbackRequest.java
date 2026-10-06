package com.rickenbazolo.paymux.pawapay.callback;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Framework-neutral view of an incoming PawaPay callback HTTP request, used for signature verification.
 * <p>
 * Build it from your web framework's request object. Pass <strong>all</strong> received headers
 * (at least {@code Content-Digest}, {@code Signature}, {@code Signature-Input}, {@code Signature-Date}
 * and {@code Content-Type}, which PawaPay covers with its signature), the authority (host) the
 * request was sent to and the request path <strong>without</strong> query string.
 * </p>
 *
 * <p>Servlet example:</p>
 * <pre>{@code
 * byte[] body = request.getInputStream().readAllBytes();
 * var builder = PawapayCallbackRequest.builder()
 *     .method(request.getMethod())
 *     .authority(request.getHeader("Host"))
 *     .path(request.getRequestURI())
 *     .body(body);
 * for (var names = request.getHeaderNames(); names.hasMoreElements(); ) {
 *     String name = names.nextElement();
 *     builder.header(name, request.getHeader(name));
 * }
 * verifier.verify(builder.build());
 * PawapayDeposit deposit = PawapayCallbacks.parseDepositCallback(body);
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayCallbackRequest {

    private final String method;
    private final String authority;
    private final String path;
    private final Map<String, String> headers;
    private final byte[] body;

    private PawapayCallbackRequest(Builder builder) {
        this.method = builder.method;
        this.authority = builder.authority;
        this.path = builder.path;
        var copy = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        copy.putAll(builder.headers);
        this.headers = Collections.unmodifiableMap(copy);
        this.body = builder.body != null ? builder.body.clone() : new byte[0];
    }

    /**
     * @return the HTTP method (e.g. {@code POST})
     */
    public String method() {
        return method;
    }

    /**
     * @return the authority (host, with port if non-default) the request was sent to
     */
    public String authority() {
        return authority;
    }

    /**
     * @return the request path without query string
     */
    public String path() {
        return path;
    }

    /**
     * @return the headers, looked up case-insensitively
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Looks up a header case-insensitively.
     *
     * @param name the header name
     * @return the header value, if present
     */
    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name));
    }

    /**
     * @return a copy of the raw body bytes
     */
    public byte[] body() {
        return body.clone();
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link PawapayCallbackRequest}.
     */
    public static final class Builder {
        private String method = "POST";
        private String authority;
        private String path;
        private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        private byte[] body;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder header(String name, String value) {
            if (name != null && value != null) {
                headers.put(name, value);
            }
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                headers.forEach(this::header);
            }
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder body(String body) {
            this.body = body != null ? body.getBytes(StandardCharsets.UTF_8) : null;
            return this;
        }

        /**
         * Build the request.
         *
         * @return a new request
         * @throws IllegalStateException if method, authority or path is missing
         */
        public PawapayCallbackRequest build() {
            if (method == null || method.isBlank()) {
                throw new IllegalStateException("HTTP method is required");
            }
            if (authority == null || authority.isBlank()) {
                throw new IllegalStateException("Authority (host) is required");
            }
            if (path == null || path.isBlank()) {
                throw new IllegalStateException("Path is required");
            }
            Objects.requireNonNull(headers, "headers");
            return new PawapayCallbackRequest(this);
        }
    }
}
