package com.rickenbazolo.paymux.mtn.congo;

import com.rickenbazolo.paymux.core.MobileMoneyClient;
import com.rickenbazolo.paymux.core.enums.MoMoCurrency;
import com.rickenbazolo.paymux.core.exception.TransferException;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.core.operations.transfer.TransferOperation;
import com.rickenbazolo.paymux.core.operations.transfer.TransferRequest;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponseStatus;
import com.rickenbazolo.paymux.http.client.DefaultPaymuxHttpClient;
import com.rickenbazolo.paymux.mtn.congo.collection.model.MtnRequestToPay;
import com.rickenbazolo.paymux.mtn.congo.service.MtnAuthService;
import com.rickenbazolo.paymux.mtn.congo.service.MtnTransactionService;
import com.rickenbazolo.paymux.mtn.congo.util.MtnValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for MTN Mobile Money Congo operations.
 * <p>
 * This client provides a high-level interface for interacting with the MTN Mobile Money API
 * for Congo-Brazzaville. It supports Transfer (Request to Pay / Collection) operations.
 * </p>
 * <p>
 * MTN Request to Pay allows merchants to request payment from customers. The customer
 * receives a USSD prompt on their phone to approve or decline the payment.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * MtnCongoConfig config = MtnCongoConfig.builder()
 *     .apiUser("your-api-user-uuid")
 *     .apiKey("your-api-key")
 *     .subscriptionKey("your-subscription-key")
 *     .environment("mtncongo")
 *     .production(false)
 *     .build();
 *
 * MtnCongoClient client = new MtnCongoClient(config);
 *
 * // Create your implementation of TransferRequest with the required fields
 * // (amount, currency, externalId, recipientPhoneNumber)
 *
 * TransferResponse response = client.transfer(request);
 * System.out.println("Transaction ID: " + response.transactionId());
 * System.out.println("Status: " + response.status());
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public class MtnCongoClient implements MobileMoneyClient<MtnCongoConfig>, TransferOperation {

    private static final Logger log = LoggerFactory.getLogger(MtnCongoClient.class);

    private final MtnCongoConfig config;
    private final PaymuxHttpClient httpClient;
    private final MtnAuthService authService;
    private final MtnTransactionService transactionService;

    /**
     * Creates a new MTN Congo client with the specified configuration.
     *
     * @param config the MTN Congo configuration
     */
    public MtnCongoClient(MtnCongoConfig config) {
        this.config = config;
        this.httpClient = config.getHttpClient() != null
            ? config.getHttpClient()
            : createDefaultHttpClient();

        this.authService = new MtnAuthService(config, httpClient);
        this.transactionService = new MtnTransactionService(config, httpClient);

        log.info("MtnCongoClient initialized for environment: {}, production: {}",
            config.getEnvironment(), config.isProduction());
    }

    @Override
    public TransferResponse transfer(TransferRequest transferRequest) throws TransferException {
        log.info("Initiating transfer (request to pay): {} {} to {}",
                transferRequest.amount(), transferRequest.currency(), transferRequest.recipientPhoneNumber());

        try {
            // Validate request
            validateTransferRequest(transferRequest);

            // Get access token
            var accessToken = authService.getAccessToken();

            if (transferRequest instanceof MtnRequestToPay mtnRequest) {
                // Build MTN request using the externalId from the request
                var mtnRequestTopay = buildMtnRequest(mtnRequest);

                // Execute request to pay
                String externalId = transactionService.requestToPay(mtnRequestTopay, accessToken);

                // Build response
                return new MtnTransferResponse(
                        externalId,
                        MoMoTransferStatus.PENDING.name(),
                        "Payment request initiated. Customer will receive a USSD prompt to approve."
                );
            }
            throw new TransferException("Invalid transfer request");
        } catch (Exception e) {
            log.error("Transfer failed", e);
            throw new TransferException("Transfer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public TransferResponseStatus getTransferStatus(String transactionId) throws TransferException {
        log.debug("Getting transfer status for: {}", transactionId);

        try {
            String accessToken = authService.getAccessToken();
            return transactionService.getTransactionStatus(transactionId, accessToken);
        } catch (Exception e) {
            log.error("Failed to get transfer status", e);
            throw new TransferException("Failed to get transfer status: " + e.getMessage(), e);
        }
    }

    @Override
    public MtnCongoConfig getConfig() {
        return config;
    }

    @Override
    public PaymuxHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public void close() {
        log.info("Closing MtnCongoClient");
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * Create a default HTTP client with configuration from config.
     *
     * @return a new HTTP client
     */
    private PaymuxHttpClient createDefaultHttpClient() {
        return DefaultPaymuxHttpClient.builder()
            .connectTimeout(config.getConnectionTimeout())
            .requestTimeout(config.getRequestTimeout())
            .build();
    }

    /**
     * Validate a transfer request.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transfer request cannot be null");
        }

        if (request.recipientPhoneNumber() == null) {
            throw new IllegalArgumentException("Recipient phone number is required");
        }

        // Validate phone number (expects 9 digits without country code)
        String phoneNumber = request.recipientPhoneNumber();

        // Remove country code if present
        if (phoneNumber.startsWith("242")) {
            phoneNumber = phoneNumber.substring(3);
        }
        MtnValidation.validatePhoneNumber(phoneNumber);

        // Validate amount
        if (request.amount() == null || request.amount().isBlank()) {
            throw new IllegalArgumentException("Amount is required");
        }

        try {
            double amount = Double.parseDouble(request.amount());
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + request.amount());
        }

        // Validate currency
        if (!MoMoCurrency.XAF.getValue().equalsIgnoreCase(request.currency())) {
            throw new IllegalArgumentException("Currency must be XAF for Congo");
        }

        // Validate externalId
        if (request.externalId() == null || request.externalId().isBlank()) {
            throw new IllegalArgumentException("External ID is required");
        }
    }

    /**
     * Build an MTN request from a transfer request.
     * <p>
     * This method uses the externalId provided in the request rather than generating a new one.
     * </p>
     *
     * @param request the transfer request
     * @return the MTN request to pay
     */
    private MtnRequestToPay buildMtnRequest(MtnRequestToPay request) {

        var externalId = request.externalId();

        var phoneNumber = request.recipientPhoneNumber();
        if (!phoneNumber.startsWith("242")) {
            phoneNumber = MtnValidation.formatPhoneNumber(phoneNumber);
        }

        return MtnRequestToPay.builder()
            .amount(request.amount())
            .currency(request.currency())
            .externalId(externalId)
            .payerPhone(phoneNumber)
            .payerMessage(request.getPayerMessage() != null
                ? request.getPayerMessage()
                : "")
            .payeeNote(request.getPayeeNote() != null
                ? request.getPayeeNote()
                : "")
            .build();
    }

    /**
     * Map MTN transaction status to MoMo transfer status.
     *
     * @param mtnStatus the MTN status string
     * @return the corresponding MoMo transfer status
     */
    private MoMoTransferStatus mapMtnStatus(String mtnStatus) {
        if (mtnStatus == null) {
            return MoMoTransferStatus.PENDING;
        }

        return switch (mtnStatus.toUpperCase()) {
            case "SUCCESSFUL" -> MoMoTransferStatus.SUCCESSFUL;
            case "FAILED" -> MoMoTransferStatus.FAILED;
            case "PENDING" -> MoMoTransferStatus.PENDING;
            default -> MoMoTransferStatus.UNKNOW;
        };
    }

    /**
     * Implementation of TransferResponse for MTN Congo.
     */
    private record MtnTransferResponse(
        String transactionId,
        String status,
        String message
    ) implements TransferResponse {
        @Override
        public String failureReason() {
            return status.equals("FAILED") ? message : null;
        }
    }
}
