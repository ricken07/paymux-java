package com.rickenbazolo.paymux.pawapay;

import com.rickenbazolo.paymux.core.MobileMoneyClient;
import com.rickenbazolo.paymux.core.exception.CashoutException;
import com.rickenbazolo.paymux.core.exception.RefundException;
import com.rickenbazolo.paymux.core.exception.TransferException;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutOperation;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutRequest;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutResponse;
import com.rickenbazolo.paymux.core.operations.refund.RefundOperation;
import com.rickenbazolo.paymux.core.operations.refund.RefundRequest;
import com.rickenbazolo.paymux.core.operations.refund.RefundResponse;
import com.rickenbazolo.paymux.core.operations.transfer.TransferOperation;
import com.rickenbazolo.paymux.core.operations.transfer.TransferRequest;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponseStatus;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckout;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutExpiry;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutRequest;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutResponse;
import com.rickenbazolo.paymux.http.client.DefaultPaymuxHttpClient;
import com.rickenbazolo.paymux.pawapay.callback.PawapayCallbackSignatureVerifier;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDeposit;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositRequest;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositResponse;
import com.rickenbazolo.paymux.pawapay.exception.PawapayNotFoundException;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatement;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementRequest;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementResponse;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayWalletBalance;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayOperationType;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageRequest;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageResponse;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayout;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutRequest;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutResponse;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefund;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundRequest;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundResponse;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittance;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceRequest;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceResponse;
import com.rickenbazolo.paymux.pawapay.service.PawapayCheckoutService;
import com.rickenbazolo.paymux.pawapay.service.PawapayDepositService;
import com.rickenbazolo.paymux.pawapay.service.PawapayFinanceService;
import com.rickenbazolo.paymux.pawapay.service.PawapayPaymentPageService;
import com.rickenbazolo.paymux.pawapay.service.PawapayPayoutService;
import com.rickenbazolo.paymux.pawapay.service.PawapayRefundService;
import com.rickenbazolo.paymux.pawapay.service.PawapayRemittanceService;
import com.rickenbazolo.paymux.pawapay.service.PawapayToolkitService;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayActiveConfiguration;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayAvailability;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayProviderPrediction;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Client for the PawaPay Merchant API v2.
 * <p>
 * PawaPay aggregates many Mobile Money providers behind one API. This client exposes:
 * <ul>
 *   <li>the generic Paymux operations: {@link TransferOperation} (PawaPay <em>deposits</em>: the
 *       customer pays the merchant), {@link CashoutOperation} (PawaPay <em>payouts</em> and
 *       <em>remittances</em>: the merchant pays a customer) and {@link RefundOperation}
 *       (PawaPay <em>refunds</em> of completed deposits);</li>
 *   <li>the PawaPay-specific operations with their typed models: bulk payouts, remittances, hosted
 *       checkouts and payment page sessions, callback resend, cancellation of enqueued payments,
 *       active configuration, provider availability, provider prediction, public keys, wallet
 *       balances and wallet statements;</li>
 *   <li>a {@link PawapayCallbackSignatureVerifier} for signed callbacks.</li>
 * </ul>
 * </p>
 * <p>
 * The generic operations wrap PawaPay errors in the core exceptions ({@link TransferException},
 * {@link CashoutException}, {@link RefundException}); the original
 * {@link com.rickenbazolo.paymux.pawapay.exception.PawapayApiException} or
 * {@link PawapayNotFoundException} is available through {@code getCause()}. The PawaPay-specific
 * operations throw those exceptions directly.
 * </p>
 * <p>
 * PawaPay processes payments asynchronously: an initiation returns {@code ACCEPTED} and the final
 * status ({@code COMPLETED} / {@code FAILED}) arrives through the callback configured in the PawaPay
 * Dashboard, or by polling the status endpoints. Reconciliation, retries and persistence are left to
 * the application.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PawapayConfig config = PawapayConfig.fromPropertiesFile("paymux.yml");
 *
 * try (PawapayClient client = new PawapayClient(config)) {
 *     PawapayDepositRequest request = PawapayDepositRequest.builder()
 *         .amount("1000")
 *         .currency("XAF")
 *         .phoneNumber("242065551234")
 *         .provider(PawapayProviders.MTN_MOMO_COG)
 *         .customerMessage("Order 12345")
 *         .build();
 *
 *     PawapayDepositResponse response = client.initiateDeposit(request);
 *     if (response.isAccepted()) {
 *         Optional<PawapayDeposit> deposit = client.findDeposit(response.getDepositId());
 *     }
 * }
 * }</pre>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public class PawapayClient implements MobileMoneyClient<PawapayConfig>, TransferOperation, CashoutOperation, RefundOperation {

    private static final Logger log = LoggerFactory.getLogger(PawapayClient.class);

    private final PawapayConfig config;
    private final PaymuxHttpClient httpClient;
    private final PawapayDepositService depositService;
    private final PawapayPayoutService payoutService;
    private final PawapayRefundService refundService;
    private final PawapayRemittanceService remittanceService;
    private final PawapayToolkitService toolkitService;
    private final PawapayFinanceService financeService;
    private final PawapayCheckoutService checkoutService;
    private final PawapayPaymentPageService paymentPageService;

    private volatile PawapayCallbackSignatureVerifier signatureVerifier;

    /**
     * Creates a new PawaPay client with the specified configuration.
     *
     * @param config the PawaPay configuration
     */
    public PawapayClient(PawapayConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.httpClient = config.getHttpClient() != null
            ? config.getHttpClient()
            : createDefaultHttpClient();

        this.depositService = new PawapayDepositService(config, httpClient);
        this.payoutService = new PawapayPayoutService(config, httpClient);
        this.refundService = new PawapayRefundService(config, httpClient);
        this.remittanceService = new PawapayRemittanceService(config, httpClient);
        this.toolkitService = new PawapayToolkitService(config, httpClient);
        this.financeService = new PawapayFinanceService(config, httpClient);
        this.checkoutService = new PawapayCheckoutService(config, httpClient);
        this.paymentPageService = new PawapayPaymentPageService(config, httpClient);

        log.info("PawapayClient initialized for {} ({})", config.getBaseUrl(), config.isProduction() ? "production" : "sandbox");
    }

    // ---- TransferOperation (deposits) -----------------------------------------------------

    /**
     * Initiates a deposit (request to pay). The request must be a {@link PawapayDepositRequest}
     * because PawaPay needs the provider code of the payer.
     *
     * @param request the deposit request
     * @return the initiation response; {@code status()} is {@code PENDING} when accepted, {@code FAILED} when rejected
     * @throws TransferException if the request is not a {@link PawapayDepositRequest} or the call fails
     */
    @Override
    public TransferResponse transfer(TransferRequest request) throws TransferException {
        if (request == null) {
            throw new TransferException("Transfer request cannot be null");
        }
        if (!(request instanceof PawapayDepositRequest depositRequest)) {
            throw new TransferException("PawaPay transfers require a PawapayDepositRequest (the payer provider is mandatory), got "
                + request.getClass().getName());
        }
        log.info("Initiating PawaPay deposit {}: {} {} from {}", depositRequest.getDepositId(),
            depositRequest.getAmount(), depositRequest.getCurrency(), depositRequest.getPayer().provider());
        try {
            return depositService.initiate(depositRequest);
        } catch (RuntimeException e) {
            log.error("PawaPay deposit {} failed", depositRequest.getDepositId(), e);
            throw new TransferException("Deposit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a deposit.
     *
     * @param transactionId the deposit id
     * @return the deposit (a {@link PawapayDeposit})
     * @throws TransferException if the call fails, or if PawaPay does not know the id
     *                           (cause: {@link PawapayNotFoundException})
     */
    @Override
    public TransferResponseStatus getTransferStatus(String transactionId) throws TransferException {
        try {
            return depositService.find(transactionId)
                .orElseThrow(() -> new PawapayNotFoundException("deposit", transactionId));
        } catch (RuntimeException e) {
            log.error("Failed to get PawaPay deposit {}", transactionId, e);
            throw new TransferException("Failed to get deposit status: " + e.getMessage(), e);
        }
    }

    // ---- CashoutOperation (payouts) -------------------------------------------------------

    /**
     * Initiates a payout or a remittance. The request must be a {@link PawapayPayoutRequest} (domestic
     * payout) or a {@link PawapayRemittanceRequest} (international transfer with sender KYC) because
     * PawaPay needs the provider code of the recipient.
     *
     * @param request the payout or remittance request
     * @return the initiation response; {@code status()} is {@code PENDING} when accepted, {@code FAILED} when rejected
     * @throws CashoutException if the request is of another type or the call fails
     */
    @Override
    public CashoutResponse cashout(CashoutRequest request) throws CashoutException {
        if (request == null) {
            throw new CashoutException("Cashout request cannot be null");
        }
        if (request instanceof PawapayPayoutRequest payoutRequest) {
            log.info("Initiating PawaPay payout {}: {} {} to {}", payoutRequest.getPayoutId(),
                payoutRequest.getAmount(), payoutRequest.getCurrency(), payoutRequest.getRecipient().provider());
            try {
                return payoutService.initiate(payoutRequest);
            } catch (RuntimeException e) {
                log.error("PawaPay payout {} failed", payoutRequest.getPayoutId(), e);
                throw new CashoutException("Cashout failed: " + e.getMessage(), e);
            }
        }
        if (request instanceof PawapayRemittanceRequest remittanceRequest) {
            log.info("Initiating PawaPay remittance {}: {} {} to {}", remittanceRequest.getRemittanceId(),
                remittanceRequest.getAmount(), remittanceRequest.getCurrency(), remittanceRequest.getRecipient().provider());
            try {
                return remittanceService.initiate(remittanceRequest);
            } catch (RuntimeException e) {
                log.error("PawaPay remittance {} failed", remittanceRequest.getRemittanceId(), e);
                throw new CashoutException("Cashout failed: " + e.getMessage(), e);
            }
        }
        throw new CashoutException("PawaPay cashouts require a PawapayPayoutRequest or a PawapayRemittanceRequest "
            + "(the recipient provider is mandatory), got " + request.getClass().getName());
    }

    /**
     * Retrieves a payout.
     *
     * @param transactionId the payout id
     * @return the payout (a {@link PawapayPayout})
     * @throws CashoutException if the call fails, or if PawaPay does not know the id
     *                          (cause: {@link PawapayNotFoundException})
     */
    @Override
    public CashoutResponse getCashoutStatus(String transactionId) throws CashoutException {
        try {
            return payoutService.find(transactionId)
                .orElseThrow(() -> new PawapayNotFoundException("payout", transactionId));
        } catch (RuntimeException e) {
            log.error("Failed to get PawaPay payout {}", transactionId, e);
            throw new CashoutException("Failed to get cashout status: " + e.getMessage(), e);
        }
    }

    // ---- RefundOperation ------------------------------------------------------------------

    /**
     * Initiates a refund of a completed deposit. Any {@link RefundRequest} is accepted:
     * {@code externalId} is the refund id, {@code originalTransactionId} the deposit id.
     *
     * @param request the refund request
     * @return the initiation response; {@code status()} is {@code PENDING} when accepted, {@code FAILED} when rejected
     * @throws RefundException if the call fails
     */
    @Override
    public RefundResponse refund(RefundRequest request) throws RefundException {
        if (request == null) {
            throw new RefundException("Refund request cannot be null");
        }
        PawapayRefundRequest refundRequest;
        try {
            refundRequest = PawapayRefundRequest.from(request);
        } catch (RuntimeException e) {
            throw new RefundException("Invalid refund request: " + e.getMessage(), e);
        }
        log.info("Initiating PawaPay refund {} of deposit {}: {} {}", refundRequest.getRefundId(),
            refundRequest.getDepositId(), refundRequest.getAmount(), refundRequest.getCurrency());
        try {
            return refundService.initiate(refundRequest);
        } catch (RuntimeException e) {
            log.error("PawaPay refund {} failed", refundRequest.getRefundId(), e);
            throw new RefundException("Refund failed: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a refund.
     *
     * @param transactionId the refund id
     * @return the refund (a {@link PawapayRefund})
     * @throws RefundException if the call fails, or if PawaPay does not know the id
     *                         (cause: {@link PawapayNotFoundException})
     */
    @Override
    public RefundResponse getRefundStatus(String transactionId) throws RefundException {
        try {
            return refundService.find(transactionId)
                .orElseThrow(() -> new PawapayNotFoundException("refund", transactionId));
        } catch (RuntimeException e) {
            log.error("Failed to get PawaPay refund {}", transactionId, e);
            throw new RefundException("Failed to get refund status: " + e.getMessage(), e);
        }
    }

    // ---- PawaPay deposits -----------------------------------------------------------------

    /**
     * Initiates a deposit ({@code POST /v2/deposits}).
     *
     * @param request the deposit request
     * @return the typed initiation response
     */
    public PawapayDepositResponse initiateDeposit(PawapayDepositRequest request) {
        return depositService.initiate(request);
    }

    /**
     * Checks a deposit ({@code GET /v2/deposits/{depositId}}).
     *
     * @param depositId the deposit id
     * @return the deposit, or empty if unknown to PawaPay
     */
    public Optional<PawapayDeposit> findDeposit(String depositId) {
        return depositService.find(depositId);
    }

    /**
     * Asks PawaPay to resend the callback of a final deposit.
     *
     * @param depositId the deposit id
     * @return the action response
     */
    public PawapayActionResponse resendDepositCallback(String depositId) {
        return depositService.resendCallback(depositId);
    }

    // ---- PawaPay payouts ------------------------------------------------------------------

    /**
     * Initiates a payout ({@code POST /v2/payouts}).
     *
     * @param request the payout request
     * @return the typed initiation response
     */
    public PawapayPayoutResponse initiatePayout(PawapayPayoutRequest request) {
        return payoutService.initiate(request);
    }

    /**
     * Initiates up to 20 payouts in one call ({@code POST /v2/payouts/bulk}).
     *
     * @param requests the payout requests
     * @return one response per request, in order
     */
    public List<PawapayPayoutResponse> initiateBulkPayouts(List<PawapayPayoutRequest> requests) {
        return payoutService.initiateBulk(requests);
    }

    /**
     * Checks a payout ({@code GET /v2/payouts/{payoutId}}).
     *
     * @param payoutId the payout id
     * @return the payout, or empty if unknown to PawaPay
     */
    public Optional<PawapayPayout> findPayout(String payoutId) {
        return payoutService.find(payoutId);
    }

    /**
     * Asks PawaPay to resend the callback of a final payout.
     *
     * @param payoutId the payout id
     * @return the action response
     */
    public PawapayActionResponse resendPayoutCallback(String payoutId) {
        return payoutService.resendCallback(payoutId);
    }

    /**
     * Cancels an enqueued payout.
     *
     * @param payoutId the payout id
     * @return the action response
     */
    public PawapayActionResponse cancelEnqueuedPayout(String payoutId) {
        return payoutService.cancelEnqueued(payoutId);
    }

    // ---- PawaPay refunds ------------------------------------------------------------------

    /**
     * Initiates a refund ({@code POST /v2/refunds}).
     *
     * @param request the refund request
     * @return the typed initiation response
     */
    public PawapayRefundResponse initiateRefund(PawapayRefundRequest request) {
        return refundService.initiate(request);
    }

    /**
     * Checks a refund ({@code GET /v2/refunds/{refundId}}).
     *
     * @param refundId the refund id
     * @return the refund, or empty if unknown to PawaPay
     */
    public Optional<PawapayRefund> findRefund(String refundId) {
        return refundService.find(refundId);
    }

    /**
     * Asks PawaPay to resend the callback of a final refund.
     *
     * @param refundId the refund id
     * @return the action response
     */
    public PawapayActionResponse resendRefundCallback(String refundId) {
        return refundService.resendCallback(refundId);
    }

    /**
     * Cancels an enqueued refund.
     *
     * @param refundId the refund id
     * @return the action response
     */
    public PawapayActionResponse cancelEnqueuedRefund(String refundId) {
        return refundService.cancelEnqueued(refundId);
    }

    // ---- PawaPay remittances --------------------------------------------------------------

    /**
     * Initiates a remittance ({@code POST /v2/remittances}).
     *
     * @param request the remittance request
     * @return the typed initiation response
     */
    public PawapayRemittanceResponse initiateRemittance(PawapayRemittanceRequest request) {
        return remittanceService.initiate(request);
    }

    /**
     * Checks a remittance ({@code GET /v2/remittances/{remittanceId}}).
     *
     * @param remittanceId the remittance id
     * @return the remittance, or empty if unknown to PawaPay
     */
    public Optional<PawapayRemittance> findRemittance(String remittanceId) {
        return remittanceService.find(remittanceId);
    }

    /**
     * Asks PawaPay to resend the callback of a final remittance.
     *
     * @param remittanceId the remittance id
     * @return the action response
     */
    public PawapayActionResponse resendRemittanceCallback(String remittanceId) {
        return remittanceService.resendCallback(remittanceId);
    }

    /**
     * Cancels an enqueued remittance.
     *
     * @param remittanceId the remittance id
     * @return the action response
     */
    public PawapayActionResponse cancelEnqueuedRemittance(String remittanceId) {
        return remittanceService.cancelEnqueued(remittanceId);
    }

    // ---- PawaPay checkouts ------------------------------------------------------------------

    /**
     * Initiates a checkout ({@code POST /v2/checkouts}): a hosted, redirect-based payment page
     * tracked under a single reference for the whole payment, including any customer retries.
     *
     * @param request the checkout request
     * @return the typed initiation response, with a {@code redirectUrl} when accepted
     */
    public PawapayCheckoutResponse initiateCheckout(PawapayCheckoutRequest request) {
        return checkoutService.initiate(request);
    }

    /**
     * Checks a checkout ({@code GET /v2/checkouts/{checkoutId}}).
     *
     * @param checkoutId the checkout id
     * @return the checkout, with its lifecycle status and payment attempts, or empty if unknown to PawaPay
     */
    public Optional<PawapayCheckout> findCheckout(String checkoutId) {
        return checkoutService.find(checkoutId);
    }

    /**
     * Expires a checkout so the customer can no longer make new payment attempts on it
     * ({@code POST /v2/checkouts/{checkoutId}/expire}).
     *
     * @param checkoutId the checkout id
     * @return the expiry response
     */
    public PawapayCheckoutExpiry expireCheckout(String checkoutId) {
        return checkoutService.expire(checkoutId);
    }

    // ---- PawaPay payment page ---------------------------------------------------------------

    /**
     * Creates a payment page session ({@code POST /v2/paymentpage}): a simpler, fixed 15-minute
     * hosted deposit session without retries or its own lifecycle. Once the customer presses
     * "Pay", track the resulting deposit the usual way, through the deposit callback or
     * {@link #findDeposit(String)}.
     *
     * @param request the payment page request
     * @return the response: a {@code redirectUrl} on success, or a business rejection
     */
    public PawapayPaymentPageResponse initiatePaymentPage(PawapayPaymentPageRequest request) {
        return paymentPageService.create(request);
    }

    // ---- PawaPay toolkit ------------------------------------------------------------------

    /**
     * Fetches the whole active configuration of the merchant account.
     *
     * @return the active configuration
     */
    public PawapayActiveConfiguration getActiveConfiguration() {
        return toolkitService.activeConfiguration(null, null);
    }

    /**
     * Fetches the active configuration, filtered.
     *
     * @param country       optional ISO 3166-1 alpha-3 country code
     * @param operationType optional operation type
     * @return the active configuration
     */
    public PawapayActiveConfiguration getActiveConfiguration(String country, PawapayOperationType operationType) {
        return toolkitService.activeConfiguration(country, operationType);
    }

    /**
     * Fetches the current availability of all providers.
     *
     * @return the availability per country
     */
    public List<PawapayAvailability> getAvailability() {
        return toolkitService.availability(null, null);
    }

    /**
     * Fetches the current provider availability, filtered.
     *
     * @param country       optional ISO 3166-1 alpha-3 country code
     * @param operationType optional operation type
     * @return the availability per country
     */
    public List<PawapayAvailability> getAvailability(String country, PawapayOperationType operationType) {
        return toolkitService.availability(country, operationType);
    }

    /**
     * Predicts the provider of a phone number.
     *
     * @param phoneNumber the phone number with country code
     * @return the prediction
     */
    public PawapayProviderPrediction predictProvider(String phoneNumber) {
        return toolkitService.predictProvider(phoneNumber);
    }

    /**
     * Fetches the public keys PawaPay uses to sign callbacks.
     *
     * @return the public keys
     */
    public List<PawapayPublicKey> getPublicKeys() {
        return toolkitService.publicKeys();
    }

    // ---- PawaPay finances -----------------------------------------------------------------

    /**
     * Fetches the balances of all wallets ({@code GET /v2/wallet-balances}).
     *
     * @return the wallet balances
     */
    public List<PawapayWalletBalance> getWalletBalances() {
        return financeService.walletBalances(null);
    }

    /**
     * Fetches the wallet balances of one country.
     *
     * @param country the ISO 3166-1 alpha-3 country code
     * @return the wallet balances
     */
    public List<PawapayWalletBalance> getWalletBalances(String country) {
        return financeService.walletBalances(country);
    }

    /**
     * Requests the generation of a wallet statement ({@code POST /v2/statements}). The result is
     * posted to the callback URL of the request; it can also be polled with {@link #findStatement(String)}.
     *
     * @param request the statement request
     * @return the initiation response ({@code ACCEPTED} with the statement id, or {@code REJECTED})
     */
    public PawapayStatementResponse initiateStatement(PawapayStatementRequest request) {
        return financeService.initiateStatement(request);
    }

    /**
     * Checks a statement ({@code GET /v2/statements/{statementId}}).
     *
     * @param statementId the statement id
     * @return the statement, or empty if unknown to PawaPay
     */
    public Optional<PawapayStatement> findStatement(String statementId) {
        return financeService.findStatement(statementId);
    }

    // ---- Callbacks ------------------------------------------------------------------------

    /**
     * Returns the callback signature verifier of this client (created lazily, shared, thread-safe).
     * It fetches the PawaPay public keys through this client and uses the cache TTL and clock skew
     * of the configuration.
     *
     * @return the signature verifier
     */
    public PawapayCallbackSignatureVerifier callbackSignatureVerifier() {
        PawapayCallbackSignatureVerifier verifier = signatureVerifier;
        if (verifier == null) {
            synchronized (this) {
                verifier = signatureVerifier;
                if (verifier == null) {
                    verifier = new PawapayCallbackSignatureVerifier(
                        this::getPublicKeys, config.getPublicKeyCacheTtl(), config.getSignatureClockSkew());
                    signatureVerifier = verifier;
                }
            }
        }
        return verifier;
    }

    // ---- MobileMoneyClient ----------------------------------------------------------------

    @Override
    public PawapayConfig getConfig() {
        return config;
    }

    @Override
    public PaymuxHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public void close() {
        log.info("Closing PawapayClient");
        if (httpClient != null) {
            httpClient.close();
        }
    }

    private PaymuxHttpClient createDefaultHttpClient() {
        return DefaultPaymuxHttpClient.builder()
            .connectTimeout(config.getConnectionTimeout())
            .requestTimeout(config.getRequestTimeout())
            .build();
    }
}
