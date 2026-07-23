/**
 * Default HTTP client implementation using {@code java.net.http}.
 * <p>
 * This package provides {@link com.rickenbazolo.paymux.http.client.DefaultPaymuxHttpClient},
 * the default implementation of {@link com.rickenbazolo.paymux.core.http.PaymuxHttpClient}
 * using the standard Java HTTP client available since Java 11.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>No framework dependency</li>
 *   <li>HTTP/2 support by default</li>
 *   <li>Both synchronous and asynchronous operations</li>
 *   <li>Configurable timeouts and redirects</li>
 *   <li>SLF4J logging integration</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * import com.rickenbazolo.paymux.http.client.DefaultPaymuxHttpClient;
 * import com.rickenbazolo.paymux.core.http.PaymuxHttpRequest;
 *
 * var client = DefaultPaymuxHttpClient.builder()
 *     .connectTimeout(5000)
 *     .requestTimeout(30000)
 *     .build();
 *
 * var request = PaymuxHttpRequest.builder()
 *     .url("https://api.example.com/data")
 *     .method("GET")
 *     .header("Accept", "application/json")
 *     .build();
 *
 * var response = client.execute(request);
 * System.out.println("Status: " + response.statusCode());
 * System.out.println("Body: " + response.bodyAsString());
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
package com.rickenbazolo.paymux.http.client;
