package com.rickenbazolo.paymux.pawapay.callback;

import com.rickenbazolo.paymux.core.exception.PaymuxException;
import com.fasterxml.jackson.databind.JsonNode;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckout;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDeposit;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatement;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayout;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefund;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittance;
import com.rickenbazolo.paymux.pawapay.util.PawapayJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Parsing of PawaPay callback payloads.
 * <p>
 * PawaPay posts a callback to the URL configured in the dashboard when a deposit, payout,
 * refund, remittance or checkout reaches a final status ({@code COMPLETED} or {@code FAILED}; {@code PROCESSING} is
 * also sent for providers using the {@code REDIRECT_AUTH} flow), and to the URL given in the
 * request when a wallet statement is ready. The payload has the same shape as the object
 * returned by the corresponding status endpoint.
 * </p>
 * <p>
 * Your endpoint must answer {@code 200 OK} and be idempotent: PawaPay retries deliveries for
 * 15 minutes. Verify the signature first when signed callbacks are enabled
 * (see {@link PawapayCallbackSignatureVerifier}).
 * </p>
 *
 * @author Ricken Bazolo
 * @since 0.0.6
 */
public final class PawapayCallbacks {

    private PawapayCallbacks() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Parses a deposit callback.
     *
     * @param body the raw JSON body
     * @return the deposit
     * @throws PaymuxException if the payload is not a valid deposit callback
     */
    public static PawapayDeposit parseDepositCallback(byte[] body) {
        PawapayDeposit deposit = parse(body, PawapayDeposit.class, "deposit");
        if (deposit.getDepositId() == null || deposit.getDepositId().isBlank()) {
            throw new PaymuxException("Invalid PawaPay deposit callback: missing depositId");
        }
        return deposit;
    }

    /**
     * Parses a deposit callback.
     *
     * @param body the raw JSON body
     * @return the deposit
     * @throws PaymuxException if the payload is not a valid deposit callback
     */
    public static PawapayDeposit parseDepositCallback(String body) {
        return parseDepositCallback(bytes(body));
    }

    /**
     * Parses a payout callback.
     *
     * @param body the raw JSON body
     * @return the payout
     * @throws PaymuxException if the payload is not a valid payout callback
     */
    public static PawapayPayout parsePayoutCallback(byte[] body) {
        PawapayPayout payout = parse(body, PawapayPayout.class, "payout");
        if (payout.getPayoutId() == null || payout.getPayoutId().isBlank()) {
            throw new PaymuxException("Invalid PawaPay payout callback: missing payoutId");
        }
        return payout;
    }

    /**
     * Parses a payout callback.
     *
     * @param body the raw JSON body
     * @return the payout
     * @throws PaymuxException if the payload is not a valid payout callback
     */
    public static PawapayPayout parsePayoutCallback(String body) {
        return parsePayoutCallback(bytes(body));
    }

    /**
     * Parses a refund callback.
     *
     * @param body the raw JSON body
     * @return the refund
     * @throws PaymuxException if the payload is not a valid refund callback
     */
    public static PawapayRefund parseRefundCallback(byte[] body) {
        PawapayRefund refund = parse(body, PawapayRefund.class, "refund");
        if (refund.getRefundId() == null || refund.getRefundId().isBlank()) {
            throw new PaymuxException("Invalid PawaPay refund callback: missing refundId");
        }
        return refund;
    }

    /**
     * Parses a refund callback.
     *
     * @param body the raw JSON body
     * @return the refund
     * @throws PaymuxException if the payload is not a valid refund callback
     */
    public static PawapayRefund parseRefundCallback(String body) {
        return parseRefundCallback(bytes(body));
    }

    /**
     * Parses a remittance callback.
     *
     * @param body the raw JSON body
     * @return the remittance
     * @throws PaymuxException if the payload is not a valid remittance callback
     */
    public static PawapayRemittance parseRemittanceCallback(byte[] body) {
        PawapayRemittance remittance = parse(body, PawapayRemittance.class, "remittance");
        if (remittance.getRemittanceId() == null || remittance.getRemittanceId().isBlank()) {
            throw new PaymuxException("Invalid PawaPay remittance callback: missing remittanceId");
        }
        return remittance;
    }

    /**
     * Parses a remittance callback.
     *
     * @param body the raw JSON body
     * @return the remittance
     * @throws PaymuxException if the payload is not a valid remittance callback
     */
    public static PawapayRemittance parseRemittanceCallback(String body) {
        return parseRemittanceCallback(bytes(body));
    }

    /**
     * Parses a checkout callback.
     * <p>
     * Sent when a checkout reaches a final status ({@code COMPLETED}, {@code FAILED},
     * {@code EXPIRED} or {@code CANCELLED}), with the same shape as
     * {@code PawapayClient#findCheckout(String)}. A payment page session has no callback of its
     * own: track it as a regular deposit with {@link #parseDepositCallback(byte[])}.
     * </p>
     *
     * @param body the raw JSON body
     * @return the checkout
     * @throws PaymuxException if the payload is not a valid checkout callback
     */
    public static PawapayCheckout parseCheckoutCallback(byte[] body) {
        PawapayCheckout checkout = parse(body, PawapayCheckout.class, "checkout");
        if (checkout.checkoutId() == null || checkout.checkoutId().isBlank()) {
            throw new PaymuxException("Invalid PawaPay checkout callback: missing checkoutId");
        }
        return checkout;
    }

    /**
     * Parses a checkout callback.
     *
     * @param body the raw JSON body
     * @return the checkout
     * @throws PaymuxException if the payload is not a valid checkout callback
     */
    public static PawapayCheckout parseCheckoutCallback(String body) {
        return parseCheckoutCallback(bytes(body));
    }

    /**
     * Parses a statement callback. Both the bare statement object and the
     * {@code {"status": ..., "data": {...}}} envelope of the status endpoint are accepted.
     *
     * @param body the raw JSON body
     * @return the statement
     * @throws PaymuxException if the payload is not a valid statement callback
     */
    public static PawapayStatement parseStatementCallback(byte[] body) {
        if (body == null || body.length == 0) {
            throw new PaymuxException("Invalid PawaPay statement callback: empty body");
        }
        PawapayStatement statement;
        try {
            JsonNode root = PawapayJson.tree(body);
            JsonNode payload = !root.has("statementId") && root.path("data").isObject() ? root.get("data") : root;
            statement = PawapayJson.mapper().treeToValue(payload, PawapayStatement.class);
        } catch (PaymuxException | IOException e) {
            throw new PaymuxException("Invalid PawaPay statement callback payload: " + e.getMessage(), e);
        }
        if (statement.statementId() == null || statement.statementId().isBlank()) {
            throw new PaymuxException("Invalid PawaPay statement callback: missing statementId");
        }
        return statement;
    }

    /**
     * Parses a statement callback.
     *
     * @param body the raw JSON body
     * @return the statement
     * @throws PaymuxException if the payload is not a valid statement callback
     */
    public static PawapayStatement parseStatementCallback(String body) {
        return parseStatementCallback(bytes(body));
    }

    private static <T> T parse(byte[] body, Class<T> type, String kind) {
        if (body == null || body.length == 0) {
            throw new PaymuxException("Invalid PawaPay " + kind + " callback: empty body");
        }
        try {
            return PawapayJson.read(body, type);
        } catch (PaymuxException e) {
            throw new PaymuxException("Invalid PawaPay " + kind + " callback payload: " + e.getMessage(), e);
        }
    }

    private static byte[] bytes(String body) {
        return body != null ? body.getBytes(StandardCharsets.UTF_8) : null;
    }
}
