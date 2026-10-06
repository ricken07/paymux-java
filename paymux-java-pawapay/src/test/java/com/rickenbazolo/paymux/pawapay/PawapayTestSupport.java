package com.rickenbazolo.paymux.pawapay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickenbazolo.paymux.core.http.PaymuxHttpRequest;
import com.rickenbazolo.paymux.core.http.PaymuxHttpResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Helpers shared by the PawaPay unit tests.
 */
public final class PawapayTestSupport {

    public static final ObjectMapper JSON = new ObjectMapper();

    private PawapayTestSupport() {
    }

    public static PaymuxHttpResponse jsonResponse(int status, String body) {
        return PaymuxHttpResponse.builder()
            .statusCode(status)
            .header("Content-Type", "application/json")
            .body(body.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    public static PaymuxHttpResponse emptyResponse(int status) {
        return PaymuxHttpResponse.builder().statusCode(status).build();
    }

    public static JsonNode bodyOf(PaymuxHttpRequest request) {
        try {
            return JSON.readTree(request.body().orElseThrow(() -> new AssertionError("request has no body")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonNode parse(String json) {
        try {
            return JSON.readTree(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
