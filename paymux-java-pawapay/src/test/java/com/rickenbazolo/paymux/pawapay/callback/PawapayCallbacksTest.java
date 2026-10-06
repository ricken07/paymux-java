package com.rickenbazolo.paymux.pawapay.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.exception.PaymuxException;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDeposit;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckout;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutStatus;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatement;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementStatus;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayout;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefund;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for PawaPay callback parsing.
 */
@DisplayName("PawaPay Callbacks Tests")
class PawapayCallbacksTest {

    private static final String DEPOSIT_COMPLETED = """
        {
          "depositId": "f4401bd2-1568-4140-bf2d-eb77d2b2b639",
          "status": "COMPLETED",
          "amount": "15",
          "currency": "ZMW",
          "country": "ZMB",
          "payer": {
            "type": "MMO",
            "accountDetails": {
              "phoneNumber": "260763456789",
              "provider": "MTN_MOMO_ZMB"
            }
          },
          "created": "2020-02-21T17:32:29Z",
          "customerMessage": "Note of 4 to 22 chars",
          "clientReferenceId": "REF-987654321",
          "providerTransactionId": "ABC123",
          "metadata": {
            "orderId": "ORD-123456789",
            "customerId": "customer@email.com"
          },
          "somethingNewFromPawapay": {"nested": true}
        }
        """;

    private static final String DEPOSIT_FAILED = """
        {
          "depositId": "8917c345-4791-4285-a416-62f24b6982db",
          "status": "FAILED",
          "amount": "123.00",
          "currency": "ZMW",
          "country": "ZMB",
          "payer": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
          "created": "2020-10-19T08:17:01+00:00",
          "failureReason": {
            "failureCode": "PAYMENT_NOT_APPROVED",
            "failureMessage": "Customer did not approve the authorisation for this payment"
          }
        }
        """;

    @Test
    @DisplayName("Should parse a completed deposit callback")
    void shouldParseCompletedDeposit() {
        PawapayDeposit deposit = PawapayCallbacks.parseDepositCallback(DEPOSIT_COMPLETED);

        assertThat(deposit.getDepositId()).isEqualTo("f4401bd2-1568-4140-bf2d-eb77d2b2b639");
        assertThat(deposit.getStatus()).isEqualTo(PawapayTransactionStatus.COMPLETED);
        assertThat(deposit.isCompleted()).isTrue();
        assertThat(deposit.isFinal()).isTrue();
        assertThat(deposit.getAmount()).isEqualTo("15");
        assertThat(deposit.getCurrency()).isEqualTo("ZMW");
        assertThat(deposit.getCountry()).isEqualTo("ZMB");
        assertThat(deposit.getPayer().phoneNumber()).isEqualTo("260763456789");
        assertThat(deposit.getPayer().provider()).isEqualTo("MTN_MOMO_ZMB");
        assertThat(deposit.getCustomerMessage()).isEqualTo("Note of 4 to 22 chars");
        assertThat(deposit.getClientReferenceId()).isEqualTo("REF-987654321");
        assertThat(deposit.getProviderTransactionId()).isEqualTo("ABC123");
        assertThat(deposit.getMetadata()).containsEntry("orderId", "ORD-123456789").containsEntry("customerId", "customer@email.com");
        assertThat(deposit.createdInstant()).contains(Instant.parse("2020-02-21T17:32:29Z"));
        assertThat(deposit.getFailureReason()).isNull();

        // core contract
        assertThat(deposit.transactionId()).isEqualTo(deposit.getDepositId());
        assertThat(deposit.status()).isEqualTo(MoMoTransferStatus.SUCCESSFUL.name());
        assertThat(deposit.failureReason()).isNull();
        assertThat(deposit.message()).contains("completed");
    }

    @Test
    @DisplayName("Should parse a failed deposit callback with an offset timestamp")
    void shouldParseFailedDeposit() {
        PawapayDeposit deposit = PawapayCallbacks.parseDepositCallback(DEPOSIT_FAILED.getBytes());

        assertThat(deposit.getStatus()).isEqualTo(PawapayTransactionStatus.FAILED);
        assertThat(deposit.status()).isEqualTo("FAILED");
        assertThat(deposit.failureReason()).isEqualTo("PAYMENT_NOT_APPROVED: Customer did not approve the authorisation for this payment");
        assertThat(deposit.getFailureReason().failureCode()).isEqualTo("PAYMENT_NOT_APPROVED");
        assertThat(deposit.createdInstant()).contains(Instant.parse("2020-10-19T08:17:01Z"));
        assertThat(deposit.getMetadata()).isEmpty();
        assertThat(deposit.message()).contains("PAYMENT_NOT_APPROVED");
    }

    @Test
    @DisplayName("Should parse a payout callback")
    void shouldParsePayout() {
        String json = """
            {
              "payoutId": "f4401bd2-1568-4140-bf2d-eb77d2b2b639",
              "status": "FAILED",
              "amount": "15",
              "currency": "ZMW",
              "country": "ZMB",
              "recipient": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
              "created": "2020-02-21T17:32:29Z",
              "failureReason": {"failureCode": "MANUALLY_CANCELLED", "failureMessage": "Cancelled from the dashboard"}
            }
            """;

        PawapayPayout payout = PawapayCallbacks.parsePayoutCallback(json);

        assertThat(payout.getPayoutId()).isEqualTo("f4401bd2-1568-4140-bf2d-eb77d2b2b639");
        assertThat(payout.getRecipient().provider()).isEqualTo("MTN_MOMO_ZMB");
        assertThat(payout.status()).isEqualTo(MoMoTransferStatus.FAILED);
        assertThat(payout.failureReason()).startsWith("MANUALLY_CANCELLED");
        assertThat(payout.message()).contains("failed");
    }

    @Test
    @DisplayName("Should parse a refund callback")
    void shouldParseRefund() {
        String json = """
            {
              "refundId": "f4401bd2-1568-4140-bf2d-eb77d2b2b639",
              "depositId": "8917c345-4791-4285-a416-62f24b6982db",
              "status": "COMPLETED",
              "amount": "15",
              "currency": "ZMW",
              "country": "ZMB",
              "recipient": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
              "created": "2020-02-21T17:32:29Z",
              "providerTransactionId": "ABC123",
              "metadata": {"orderId": "ORD-123456789"}
            }
            """;

        PawapayRefund refund = PawapayCallbacks.parseRefundCallback(json);

        assertThat(refund.getRefundId()).isEqualTo("f4401bd2-1568-4140-bf2d-eb77d2b2b639");
        assertThat(refund.getDepositId()).isEqualTo("8917c345-4791-4285-a416-62f24b6982db");
        assertThat(refund.status()).isEqualTo(MoMoTransferStatus.SUCCESSFUL);
        assertThat(refund.getMetadata()).containsEntry("orderId", "ORD-123456789");
        assertThat(refund.getRecipient().phoneNumber()).isEqualTo("260763456789");
    }

    @Test
    @DisplayName("Should parse a remittance callback with its sender")
    void shouldParseRemittanceCallback() {
        String json = """
            {"remittanceId":"f4401bd2-1568-4140-bf2d-eb77d2b2b639","status":"COMPLETED","amount":"15","currency":"ZMW","country":"ZMB",
             "recipient":{"type":"MMO","accountDetails":{"phoneNumber":"260763456789","provider":"MTN_MOMO_ZMB"},"recipientDetails":{"firstName":"John","lastName":"Doe"}},
             "sender":{"transactionDetails":{"transactionReference":"de83150a-5916-48a2-b048-bd85e022cb55","originalAmount":"100","originalCurrency":"USD","buyFxRate":"23.88","senderFees":"1","purposeOfFunds":"FAMILY_SUPPORT","sourceOfFunds":"SALARY"},
                       "senderDetails":{"firstName":"Jane","lastName":"Doe","nationality":"USA","phoneNumber":"12124567890","address":{"addressLine":"1476 Sandhill Rd","postalCode":"84058","city":"Orem","country":"USA"},"identification":{"type":"PASSPORT","number":"E00007730"},"gender":"FEMALE","dateOfBirth":"1977-12-31","placeOfBirth":"USA","occupation":"Project manager","relationshipRecipient":"PARTNER"}},
             "created":"2020-02-21T17:32:29Z","customerMessage":"Note of 4 to 22 chars","providerTransactionId":"ABC123","metadata":{"orderId":"ORD-123456789","customerId":"customer@email.com"}}
            """;

        PawapayRemittance remittance = PawapayCallbacks.parseRemittanceCallback(json);

        assertThat(remittance.getRemittanceId()).isEqualTo("f4401bd2-1568-4140-bf2d-eb77d2b2b639");
        assertThat(remittance.status()).isEqualTo(MoMoTransferStatus.SUCCESSFUL);
        assertThat(remittance.getRecipient().recipientDetails().lastName()).isEqualTo("Doe");
        assertThat(remittance.getRecipient().toParty().phoneNumber()).isEqualTo("260763456789");
        assertThat(remittance.getSender().senderDetails().address().city()).isEqualTo("Orem");
        assertThat(remittance.getSender().transactionDetails().originalCurrency()).isEqualTo("USD");
        assertThat(remittance.getMetadata()).containsEntry("customerId", "customer@email.com");
        assertThat(remittance.getProviderTransactionId()).isEqualTo("ABC123");

        assertThatThrownBy(() -> PawapayCallbacks.parseRemittanceCallback("{\"status\": \"COMPLETED\"}"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("missing remittanceId");
    }

    @Test
    @DisplayName("Should parse a checkout callback with its deposit history")
    void shouldParseCheckoutCallback() {
        String json = """
            {"checkoutId":"f4401bd2-1568-4140-bf2d-eb77d2b2b639","status":"COMPLETED","created":"2020-02-21T17:32:29Z",
             "depositsHistory":[{"depositId":"f4401bd2-1568-4140-bf2d-eb77d2b2b639","status":"COMPLETED",
                                  "created":"2020-02-21T17:32:29Z","providerTransactionId":"ABC123","amount":"15","currency":"ZMW",
                                  "country":"ZMB","customerMessage":"Note of 4 to 22 chars",
                                  "metadata":{"orderId":"ORD-123456789","customerId":"customer@email.com"}}],
             "metadata":{"orderId":"ORD-123456789","customerId":"customer@email.com"},"checkoutCode":"7mVk1x8UbTamQ64xGR",
             "returnUrl":"https://merchant.example.com/checkout-result","countries":["ZMB","CIV"],
             "amounts":[{"country":"ZMB","currency":"ZMW","amount":"15"}],"clientReferenceId":"INV-123456",
             "providerTransactionId":"ABC123","reason":{"en":"GOODS PURCHASE"}}
            """;

        PawapayCheckout checkout = PawapayCallbacks.parseCheckoutCallback(json);

        assertThat(checkout.checkoutId()).isEqualTo("f4401bd2-1568-4140-bf2d-eb77d2b2b639");
        assertThat(checkout.status()).isEqualTo(PawapayCheckoutStatus.COMPLETED);
        assertThat(checkout.isCompleted()).isTrue();
        assertThat(checkout.isFinal()).isTrue();
        assertThat(checkout.depositsHistory()).hasSize(1);
        assertThat(checkout.depositsHistory().get(0).amount()).isEqualTo("15");
        assertThat(checkout.countries()).containsExactly("ZMB", "CIV");
        assertThat(checkout.reason()).containsEntry("en", "GOODS PURCHASE");
        assertThat(checkout.checkoutCode()).isEqualTo("7mVk1x8UbTamQ64xGR");
        assertThat(checkout.createdInstant()).contains(Instant.parse("2020-02-21T17:32:29Z"));

        assertThatThrownBy(() -> PawapayCallbacks.parseCheckoutCallback("{\"status\": \"COMPLETED\"}"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("missing checkoutId");
    }

    @Test
    @DisplayName("Should parse statement callbacks, bare or wrapped in a data envelope")
    void shouldParseStatementCallback() {
        String completed = """
            {
              "statementId": "f4401bd2-1568-4140-bf2d-eb77d2b2b639",
              "status": "COMPLETED",
              "wallet": {"country": "ZMB", "currency": "ZMW", "provider": "MTN_MOMO_ZMB"},
              "created": "2020-02-21T17:32:29Z",
              "startDate": "2025-05-10T10:00:00",
              "endDate": "2025-05-11T10:00:00",
              "fileSize": 1048576,
              "downloadUrl": "https://files.pawapay.io/statement.csv",
              "downloadUrlExpiresAt": "2025-05-13T10:00:00",
              "completedAt": "2025-05-12T10:00:00"
            }
            """;
        String failedWrapped = """
            {"status": "FOUND", "data": {
              "statementId": "8917c345-4791-4285-a416-62f24b6982db", "status": "FAILED",
              "wallet": {"country": "ZMB", "currency": "ZMW"},
              "created": "2020-02-21T17:32:29Z", "startDate": "2025-05-10T10:00:00", "endDate": "2025-05-11T10:00:00",
              "failedAt": "2025-05-12T10:00:00",
              "failureReason": {"failureCode": "UNKNOWN_ERROR", "failureMessage": "Unable to process request due to an unknown problem."}}}
            """;

        PawapayStatement statement = PawapayCallbacks.parseStatementCallback(completed);
        assertThat(statement.statementId()).isEqualTo("f4401bd2-1568-4140-bf2d-eb77d2b2b639");
        assertThat(statement.status()).isEqualTo(PawapayStatementStatus.COMPLETED);
        assertThat(statement.isFinal()).isTrue();
        assertThat(statement.wallet().currency()).isEqualTo("ZMW");
        assertThat(statement.fileSize()).isEqualTo(1048576L);
        assertThat(statement.downloadUrl()).isEqualTo("https://files.pawapay.io/statement.csv");
        assertThat(statement.isDownloadable()).isFalse(); // expiry in the past
        assertThat(statement.downloadUrlExpiresAtInstant()).contains(Instant.parse("2025-05-13T10:00:00Z"));

        PawapayStatement failed = PawapayCallbacks.parseStatementCallback(failedWrapped.getBytes());
        assertThat(failed.statementId()).isEqualTo("8917c345-4791-4285-a416-62f24b6982db");
        assertThat(failed.isFailed()).isTrue();
        assertThat(failed.wallet().provider()).isNull();
        assertThat(failed.failedAtInstant()).contains(Instant.parse("2025-05-12T10:00:00Z"));
        assertThat(failed.describeFailure()).startsWith("UNKNOWN_ERROR");

        assertThatThrownBy(() -> PawapayCallbacks.parseStatementCallback("{\"status\": \"NOT_FOUND\"}"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("missing statementId");
        assertThatThrownBy(() -> PawapayCallbacks.parseStatementCallback("not json"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("statement callback payload");
    }

    @Test
    @DisplayName("Should map unknown statuses to UNKNOWN even with a default ObjectMapper")
    void shouldTolerateUnknownStatusWithDefaultMapper() throws Exception {
        String json = """
            {"depositId": "f4401bd2-1568-4140-bf2d-eb77d2b2b639", "status": "BRAND_NEW_STATUS", "amount": "1",
             "currency": "XAF", "country": "COG", "created": "2020-02-21T17:32:29Z", "unknownField": 42,
             "payer": {"type": "MMO", "accountDetails": {"phoneNumber": "242065551234", "provider": "MTN_MOMO_COG", "extra": 1}}}
            """;

        PawapayDeposit deposit = new ObjectMapper().readValue(json, PawapayDeposit.class);

        assertThat(deposit.getStatus()).isEqualTo(PawapayTransactionStatus.UNKNOWN);
        assertThat(deposit.status()).isEqualTo(MoMoTransferStatus.UNKNOW.name());
        assertThat(deposit.getPayer().provider()).isEqualTo("MTN_MOMO_COG");
        assertThat(PawapayCallbacks.parseDepositCallback(json).getStatus()).isEqualTo(PawapayTransactionStatus.UNKNOWN);
    }

    @Test
    @DisplayName("Should reject malformed or foreign payloads")
    void shouldRejectInvalidPayloads() {
        assertThatThrownBy(() -> PawapayCallbacks.parseDepositCallback("not-json-at-all"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("deposit callback payload");
        assertThatThrownBy(() -> PawapayCallbacks.parseDepositCallback("{\"payoutId\": \"x\", \"status\": \"COMPLETED\"}"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("missing depositId");
        assertThatThrownBy(() -> PawapayCallbacks.parsePayoutCallback("{\"depositId\": \"x\"}"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("missing payoutId");
        assertThatThrownBy(() -> PawapayCallbacks.parseRefundCallback("{}"))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("missing refundId");
        assertThatThrownBy(() -> PawapayCallbacks.parseDepositCallback(new byte[0]))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("empty body");
        assertThatThrownBy(() -> PawapayCallbacks.parseDepositCallback((String) null))
            .isInstanceOf(PaymuxException.class).hasMessageContaining("empty body");
    }
}
