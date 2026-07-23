package com.rickenbazolo.paymux.core.http;

import com.rickenbazolo.paymux.core.exception.PaymuxHttpException;

import java.util.concurrent.CompletableFuture;

/**
 * Core HTTP client abstraction for Mobile Money API requests.
 * This interface allows pluggable HTTP client implementations.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public interface PaymuxHttpClient {

    /**
     * Execute an HTTP request synchronously.
     *
     * @param request the HTTP request to execute
     * @return the HTTP response
     * @throws PaymuxHttpException if an error occurs during request execution
     */
    PaymuxHttpResponse execute(PaymuxHttpRequest request) throws PaymuxHttpException;

    /**
     * Execute an HTTP request asynchronously.
     *
     * @param request the HTTP request to execute
     * @return a CompletableFuture containing the HTTP response
     */
    CompletableFuture<PaymuxHttpResponse> executeAsync(PaymuxHttpRequest request);

    /**
     * Close the HTTP client and release any resources.
     */
    void close();
}
