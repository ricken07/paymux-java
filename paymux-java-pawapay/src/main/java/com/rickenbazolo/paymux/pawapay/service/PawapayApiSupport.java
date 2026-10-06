package com.rickenbazolo.paymux.pawapay.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.rickenbazolo.paymux.core.exception.PaymuxException;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.core.http.PaymuxHttpRequest;
import com.rickenbazolo.paymux.core.http.PaymuxHttpResponse;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.exception.PawapayApiException;
import com.rickenbazolo.paymux.pawapay.signing.PawapayRequestSigner;
import com.rickenbazolo.paymux.pawapay.util.PawapayJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Base class of the PawaPay API services: request building, authentication, error handling
 * and JSON parsing.
 * <p>
 * Conventions:
 * <ul>
 *   <li>the configured base URL ends with {@code /}; endpoint paths are relative ({@code v2/deposits});</li>
 *   <li>every request carries {@code Authorization: Bearer <token>} and {@code Accept: application/json};</li>
 *   <li>a non-2xx HTTP status raises a {@link PawapayApiException} carrying the PawaPay failure code;</li>
 *   <li>a 2xx status with {@code "status": "REJECTED"} is returned as a regular response;</li>
 *   <li>{@link #postSigned(String, Object)} additionally signs the request (RFC 9421) when
 *       {@code PawapayConfig#getRequestSigner()} is configured, for the financial endpoints that
 *       accept it (deposits, payouts, bulk payouts, refunds).</li>
 * </ul>
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
abstract class PawapayApiSupport {

    static final String SEARCH_STATUS_FOUND = "FOUND";
    static final String SEARCH_STATUS_NOT_FOUND = "NOT_FOUND";

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final PawapayConfig config;
    protected final PaymuxHttpClient httpClient;
    private final String authority;

    protected PawapayApiSupport(PawapayConfig config, PaymuxHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.authority = URI.create(config.getBaseUrl()).getAuthority();
    }

    /**
     * Executes a GET request.
     *
     * @param path the relative path, optionally followed by a query string
     * @return the successful response
     * @throws PawapayApiException on a non-2xx status
     */
    protected PaymuxHttpResponse get(String path) {
        return execute(request("GET", path, null));
    }

    /**
     * Executes a POST request.
     *
     * @param path the relative path
     * @param body the JSON body, or null for an empty body
     * @return the successful response
     * @throws PawapayApiException on a non-2xx status
     */
    protected PaymuxHttpResponse post(String path, Object body) {
        return execute(request("POST", path, body));
    }

    /**
     * Executes a signed POST request: a financial request that PawaPay accepts a signature for
     * ({@code POST /v2/deposits}, {@code POST /v2/payouts}, {@code POST /v2/payouts/bulk},
     * {@code POST /v2/refunds}). Adds the RFC 9421 signature headers when
     * {@code PawapayConfig#getRequestSigner()} is configured; otherwise behaves exactly like
     * {@link #post(String, Object)}.
     *
     * @param path the relative path
     * @param body the JSON body
     * @return the successful response
     * @throws PawapayApiException on a non-2xx status
     */
    protected PaymuxHttpResponse postSigned(String path, Object body) {
        Objects.requireNonNull(body, "body");
        byte[] bodyBytes = PawapayJson.write(body).getBytes(StandardCharsets.UTF_8);

        var builder = PaymuxHttpRequest.builder()
            .method("POST")
            .url(config.getBaseUrl() + path)
            .header("Authorization", "Bearer " + config.getApiToken())
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .body(bodyBytes);

        PawapayRequestSigner signer = config.getRequestSigner();
        if (signer != null) {
            var signed = signer.sign("POST", authority, "/" + path, "application/json", bodyBytes);
            builder.header("Content-Digest", signed.contentDigest())
                .header("Signature-Date", signed.signatureDate())
                .header("Signature-Input", signed.signatureInput())
                .header("Signature", signed.signature());
        }
        return execute(builder.build());
    }

    private PaymuxHttpRequest request(String method, String path, Object body) {
        var builder = PaymuxHttpRequest.builder()
            .method(method)
            .url(config.getBaseUrl() + path)
            .header("Authorization", "Bearer " + config.getApiToken())
            .header("Accept", "application/json");

        if (body != null) {
            builder.header("Content-Type", "application/json")
                .body(PawapayJson.write(body).getBytes(StandardCharsets.UTF_8));
        }
        return builder.build();
    }

    private PaymuxHttpResponse execute(PaymuxHttpRequest request) {
        log.debug("PawaPay {} {}", request.method(), request.url());
        var response = httpClient.execute(request);
        if (!response.isSuccessful()) {
            var exception = toApiException(response);
            log.warn("PawaPay {} {} failed: {}", request.method(), request.url(), exception.getMessage());
            throw exception;
        }
        return response;
    }

    /**
     * Parses the response body into the given type.
     *
     * @param response the response
     * @param type     the target type
     * @param <T>      the target type
     * @return the parsed value
     * @throws PaymuxException if the body is empty or cannot be parsed
     */
    protected <T> T read(PaymuxHttpResponse response, Class<T> type) {
        return PawapayJson.read(requireBody(response), type);
    }

    /**
     * Parses the response body into the given generic type.
     *
     * @param response the response
     * @param type     the target type reference
     * @param <T>      the target type
     * @return the parsed value
     * @throws PaymuxException if the body is empty or cannot be parsed
     */
    protected <T> T read(PaymuxHttpResponse response, TypeReference<T> type) {
        return PawapayJson.read(requireBody(response), type);
    }

    /**
     * Parses a search result envelope ({@code {"status": "FOUND", "data": {...}}} or
     * {@code {"status": "NOT_FOUND"}}).
     *
     * @param response the response
     * @param type     the type of the {@code data} object
     * @param <T>      the type of the {@code data} object
     * @return the data when found, empty when not found
     * @throws PaymuxException if the body is empty, malformed or has an unexpected status
     */
    protected <T> Optional<T> readSearchResult(PaymuxHttpResponse response, Class<T> type) {
        JsonNode root = PawapayJson.tree(requireBody(response));
        String status = root.path("status").asText(null);

        if (SEARCH_STATUS_NOT_FOUND.equals(status)) {
            return Optional.empty();
        }
        if (SEARCH_STATUS_FOUND.equals(status)) {
            JsonNode data = root.get("data");
            if (data == null || data.isNull()) {
                throw new PaymuxException("PawaPay search result is FOUND but has no data");
            }
            try {
                return Optional.of(PawapayJson.mapper().treeToValue(data, type));
            } catch (IOException e) {
                throw new PaymuxException("Failed to parse PawaPay " + type.getSimpleName(), e);
            }
        }
        throw new PaymuxException("Unexpected PawaPay search status: " + status);
    }

    private byte[] requireBody(PaymuxHttpResponse response) {
        return response.body()
            .filter(bytes -> bytes.length > 0)
            .orElseThrow(() -> new PaymuxException("Empty PawaPay response body (HTTP " + response.statusCode() + ")"));
    }

    /**
     * Builds a query string from non-null parameters.
     *
     * @param params the parameters, in order
     * @return the query string starting with {@code ?}, or an empty string when there is no parameter
     */
    protected static String queryString(Map<String, String> params) {
        var joiner = new StringJoiner("&", "?", "");
        joiner.setEmptyValue("");
        params.forEach((name, value) -> {
            if (value != null && !value.isBlank()) {
                joiner.add(encode(name) + "=" + encode(value));
            }
        });
        return joiner.toString();
    }

    /**
     * URL-encodes a path segment or query component.
     *
     * @param value the raw value
     * @return the encoded value
     */
    protected static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static PawapayApiException toApiException(PaymuxHttpResponse response) {
        String body = response.bodyAsString();
        String failureCode = null;
        String failureMessage = null;

        if (!body.isBlank()) {
            try {
                JsonNode root = PawapayJson.tree(body.getBytes(StandardCharsets.UTF_8));
                JsonNode reason = root.path("failureReason");
                failureCode = reason.path("failureCode").asText(null);
                failureMessage = reason.path("failureMessage").asText(null);
            } catch (PaymuxException ignored) {
                // not a JSON body: keep the raw text only
            }
        }
        return new PawapayApiException(response.statusCode(), failureCode, failureMessage, body.isBlank() ? null : body);
    }
}
