package com.rickenbazolo.paymux.pawapay;

import com.fasterxml.jackson.databind.JsonNode;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.exception.CashoutException;
import com.rickenbazolo.paymux.core.exception.RefundException;
import com.rickenbazolo.paymux.core.exception.TransferException;
import com.rickenbazolo.paymux.core.http.PaymuxHttpClient;
import com.rickenbazolo.paymux.core.http.PaymuxHttpRequest;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutRequest;
import com.rickenbazolo.paymux.core.operations.cashout.CashoutResponse;
import com.rickenbazolo.paymux.core.operations.refund.RefundRequest;
import com.rickenbazolo.paymux.core.operations.refund.RefundResponse;
import com.rickenbazolo.paymux.core.operations.transfer.TransferRequest;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponseStatus;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDeposit;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositRequest;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositResponse;
import com.rickenbazolo.paymux.pawapay.exception.PawapayApiException;
import com.rickenbazolo.paymux.pawapay.exception.PawapayNotFoundException;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatement;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementRequest;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementResponse;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementStatus;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayWallet;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayWalletBalance;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayActionStatus;
import com.rickenbazolo.paymux.pawapay.model.PawapayInitiationStatus;
import com.rickenbazolo.paymux.pawapay.model.PawapayOperationType;
import com.rickenbazolo.paymux.pawapay.model.PawapayProviderStatus;
import com.rickenbazolo.paymux.pawapay.model.PawapayProviders;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckout;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutExpiry;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutRequest;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutResponse;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutStatus;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayReturnMethod;
import com.rickenbazolo.paymux.pawapay.model.PawapayTransactionStatus;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayLanguage;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageRequest;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageResponse;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayout;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutRequest;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutResponse;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefund;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundRequest;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayIdentificationType;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayPurposeOfFunds;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRelationship;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittance;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceRequest;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceResponse;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceSender;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapaySourceOfFunds;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayActiveConfiguration;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayAvailability;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayProviderPrediction;
import com.rickenbazolo.paymux.pawapay.toolkit.model.PawapayPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rickenbazolo.paymux.pawapay.PawapayTestSupport.bodyOf;
import static com.rickenbazolo.paymux.pawapay.PawapayTestSupport.emptyResponse;
import static com.rickenbazolo.paymux.pawapay.PawapayTestSupport.jsonResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PawapayClient} against a mocked HTTP client: request shapes and response parsing
 * use the payloads documented in the PawaPay v2 API reference.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PawaPay Client Tests")
class PawapayClientTest {

    private static final String BASE = "https://api.sandbox.pawapay.io/";
    private static final String ID = "f4401bd2-1568-4140-bf2d-eb77d2b2b639";
    private static final String OTHER_ID = "8917c345-4791-4285-a416-62f24b6982db";

    @Mock
    private PaymuxHttpClient httpClient;

    private PawapayClient client;

    @BeforeEach
    void setUp() {
        PawapayConfig config = PawapayConfig.builder()
            .apiToken("test-token")
            .httpClient(httpClient)
            .build();
        client = new PawapayClient(config);
    }

    private PaymuxHttpRequest sentRequest() {
        ArgumentCaptor<PaymuxHttpRequest> captor = ArgumentCaptor.forClass(PaymuxHttpRequest.class);
        verify(httpClient).execute(captor.capture());
        return captor.getValue();
    }

    private static void assertAuthenticated(PaymuxHttpRequest request) {
        assertThat(request.headers()).containsEntry("Authorization", "Bearer test-token");
        assertThat(request.headers()).containsEntry("Accept", "application/json");
    }

    private static PawapayDepositRequest depositRequest() {
        return PawapayDepositRequest.builder()
            .depositId(ID)
            .amount("15")
            .currency("ZMW")
            .phoneNumber("260763456789")
            .provider(PawapayProviders.MTN_MOMO_ZMB)
            .clientReferenceId("INV-123456")
            .customerMessage("Payment for invoice")
            .addMetadata("orderId", "ORD-123456789")
            .addPiiMetadata("customerId", "customer@email.com")
            .build();
    }

    private static PawapayPayoutRequest payoutRequest() {
        return PawapayPayoutRequest.builder()
            .payoutId(ID)
            .amount("15")
            .currency("ZMW")
            .phoneNumber("260763456789")
            .provider(PawapayProviders.MTN_MOMO_ZMB)
            .customerMessage("Payment note")
            .clientReferenceId("INV-123456")
            .build();
    }

    @Nested
    @DisplayName("Deposits")
    class Deposits {

        @Test
        @DisplayName("Should POST /v2/deposits and parse ACCEPTED")
        void shouldInitiateDeposit() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"depositId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            PawapayDepositResponse response = client.initiateDeposit(depositRequest());

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/deposits");
            assertAuthenticated(request);
            assertThat(request.headers()).containsEntry("Content-Type", "application/json");

            JsonNode body = bodyOf(request);
            assertThat(body.get("depositId").asText()).isEqualTo(ID);
            assertThat(body.at("/payer/type").asText()).isEqualTo("MMO");
            assertThat(body.at("/payer/accountDetails/phoneNumber").asText()).isEqualTo("260763456789");
            assertThat(body.at("/payer/accountDetails/provider").asText()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(body.get("amount").asText()).isEqualTo("15");
            assertThat(body.get("currency").asText()).isEqualTo("ZMW");
            assertThat(body.get("clientReferenceId").asText()).isEqualTo("INV-123456");
            assertThat(body.get("customerMessage").asText()).isEqualTo("Payment for invoice");
            assertThat(body.get("metadata")).hasSize(2);
            assertThat(body.at("/metadata/1/isPII").asBoolean()).isTrue();
            assertThat(body.has("preAuthorisationCode")).isFalse();

            assertThat(response.getDepositId()).isEqualTo(ID);
            assertThat(response.getStatus()).isEqualTo(PawapayInitiationStatus.ACCEPTED);
            assertThat(response.isAccepted()).isTrue();
            assertThat(response.createdInstant()).contains(Instant.parse("2020-10-19T11:17:01Z"));
            assertThat(response.transactionId()).isEqualTo(ID);
            assertThat(response.status()).isEqualTo("PENDING");
            assertThat(response.failureReason()).isNull();
            assertThat(response.message()).contains("accepted");
        }

        @Test
        @DisplayName("Should expose deposits through the core TransferOperation")
        void shouldTransferThroughCoreInterface() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"depositId\":\"" + ID + "\",\"status\":\"DUPLICATE_IGNORED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            TransferResponse response = client.transfer(depositRequest());

            assertThat(response).isInstanceOf(PawapayDepositResponse.class);
            assertThat(response.status()).isEqualTo("PENDING");
            assertThat(((PawapayDepositResponse) response).isDuplicateIgnored()).isTrue();
        }

        @Test
        @DisplayName("Should return a 200 REJECTED response instead of throwing")
        void shouldReturnRejectedResponse() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"depositId": "%s", "status": "REJECTED",
                 "failureReason": {"failureCode": "INVALID_PHONE_NUMBER", "failureMessage": "The phone number '2607634' is invalid for provider 'MTN_MOMO_ZMB'."}}
                """.formatted(ID)));

            PawapayDepositResponse response = client.initiateDeposit(depositRequest());

            assertThat(response.isRejected()).isTrue();
            assertThat(response.status()).isEqualTo("FAILED");
            assertThat(response.getFailureReason().failureCode()).isEqualTo("INVALID_PHONE_NUMBER");
            assertThat(response.failureReason()).startsWith("INVALID_PHONE_NUMBER: The phone number");
            assertThat(response.createdInstant()).isEmpty();
        }

        @Test
        @DisplayName("Should reject a non PawaPay transfer request")
        void shouldRejectForeignTransferRequest() {
            TransferRequest foreign = new TransferRequest() {
                @Override
                public String amount() {
                    return "10";
                }

                @Override
                public String currency() {
                    return "XAF";
                }

                @Override
                public String externalId() {
                    return ID;
                }

                @Override
                public String recipientPhoneNumber() {
                    return "242065551234";
                }
            };

            assertThatThrownBy(() -> client.transfer(foreign))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("PawapayDepositRequest");
            assertThatThrownBy(() -> client.transfer(null))
                .isInstanceOf(TransferException.class);
            verify(httpClient, never()).execute(any());
        }

        @Test
        @DisplayName("Should throw PawapayApiException on HTTP 401 and wrap it in TransferException")
        void shouldThrowOnHttpError() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(401, """
                {"status": "REJECTED", "failureReason": {"failureCode": "AUTHENTICATION_ERROR", "failureMessage": "The API token in the request is invalid."}}
                """));

            assertThatThrownBy(() -> client.initiateDeposit(depositRequest()))
                .isInstanceOfSatisfying(PawapayApiException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(401);
                    assertThat(e.getFailureCode()).isEqualTo("AUTHENTICATION_ERROR");
                    assertThat(e.getFailureMessage()).contains("invalid");
                    assertThat(e.getResponseBody()).contains("AUTHENTICATION_ERROR");
                })
                .hasMessageContaining("HTTP 401")
                .hasMessageContaining("AUTHENTICATION_ERROR");

            assertThatThrownBy(() -> client.transfer(depositRequest()))
                .isInstanceOf(TransferException.class)
                .hasCauseInstanceOf(PawapayApiException.class);
        }

        @Test
        @DisplayName("Should handle non JSON error bodies")
        void shouldHandleNonJsonErrorBody() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(502, "<html>Bad gateway</html>"));

            assertThatThrownBy(() -> client.initiateDeposit(depositRequest()))
                .isInstanceOfSatisfying(PawapayApiException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(502);
                    assertThat(e.getFailureCode()).isNull();
                    assertThat(e.getResponseBody()).contains("Bad gateway");
                });
        }

        @Test
        @DisplayName("Should GET /v2/deposits/{id} and parse a FOUND deposit")
        void shouldFindDeposit() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {
                  "depositId": "%s", "status": "COMPLETED", "amount": "123.00", "currency": "ZMW", "country": "ZMB",
                  "payer": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
                  "customerMessage": "To ACME company", "clientReferenceId": "REF-987654321",
                  "created": "2020-10-19T08:17:01Z", "providerTransactionId": "12356789",
                  "metadata": {"orderId": "ORD-123456789", "customerId": "customer@email.com"}}}
                """.formatted(OTHER_ID)));

            Optional<PawapayDeposit> found = client.findDeposit(OTHER_ID);

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("GET");
            assertThat(request.url()).isEqualTo(BASE + "v2/deposits/" + OTHER_ID);
            assertThat(request.body()).isEmpty();
            assertAuthenticated(request);

            assertThat(found).isPresent();
            PawapayDeposit deposit = found.get();
            assertThat(deposit.getDepositId()).isEqualTo(OTHER_ID);
            assertThat(deposit.getStatus()).isEqualTo(PawapayTransactionStatus.COMPLETED);
            assertThat(deposit.getAmount()).isEqualTo("123.00");
            assertThat(deposit.getPayer().phoneNumber()).isEqualTo("260763456789");
            assertThat(deposit.getProviderTransactionId()).isEqualTo("12356789");
            assertThat(deposit.getMetadata()).containsEntry("orderId", "ORD-123456789");
            assertThat(deposit.status()).isEqualTo("SUCCESSFUL");
        }

        @Test
        @DisplayName("Should return empty on NOT_FOUND and raise a typed cause through the core interface")
        void shouldHandleNotFound() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"));

            assertThat(client.findDeposit(ID)).isEmpty();

            assertThatThrownBy(() -> client.getTransferStatus(ID))
                .isInstanceOf(TransferException.class)
                .hasCauseInstanceOf(PawapayNotFoundException.class)
                .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should return the deposit through getTransferStatus")
        void shouldGetTransferStatus() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {"depositId": "%s", "status": "FAILED", "amount": "123.00", "currency": "ZMW", "country": "ZMB",
                  "payer": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
                  "created": "2020-10-19T08:17:01Z",
                  "failureReason": {"failureCode": "PAYMENT_NOT_APPROVED", "failureMessage": "Customer did not approve the authorisation for this payment"}}}
                """.formatted(ID)));

            TransferResponseStatus status = client.getTransferStatus(ID);

            assertThat(status).isInstanceOf(PawapayDeposit.class);
            assertThat(status.transactionId()).isEqualTo(ID);
            assertThat(status.status()).isEqualTo("FAILED");
            assertThat(status.failureReason()).startsWith("PAYMENT_NOT_APPROVED");
        }

        @Test
        @DisplayName("Should validate the deposit id before calling PawaPay")
        void shouldValidateIdLocally() {
            assertThatThrownBy(() -> client.findDeposit("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("depositId");
            verify(httpClient, never()).execute(any());
        }

        @Test
        @DisplayName("Should POST resend-callback without body")
        void shouldResendDepositCallback() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"depositId\": \"" + ID + "\", \"status\": \"ACCEPTED\"}"));

            PawapayActionResponse response = client.resendDepositCallback(ID);

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/deposits/resend-callback/" + ID);
            assertThat(request.body()).isEmpty();
            assertThat(request.headers()).doesNotContainKey("Content-Type");
            assertThat(response.transactionId()).isEqualTo(ID);
            assertThat(response.isAccepted()).isTrue();
        }

        @Test
        @DisplayName("Should fail on an empty 200 body")
        void shouldFailOnEmptyBody() {
            when(httpClient.execute(any())).thenReturn(emptyResponse(200));

            assertThatThrownBy(() -> client.initiateDeposit(depositRequest()))
                .hasMessageContaining("Empty PawaPay response body");
        }
    }

    @Nested
    @DisplayName("Payouts")
    class Payouts {

        @Test
        @DisplayName("Should POST /v2/payouts and map the status to the core CashoutResponse")
        void shouldInitiatePayout() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"payoutId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            CashoutResponse response = client.cashout(payoutRequest());

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/payouts");
            JsonNode body = bodyOf(request);
            assertThat(body.get("payoutId").asText()).isEqualTo(ID);
            assertThat(body.at("/recipient/accountDetails/provider").asText()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(body.get("customerMessage").asText()).isEqualTo("Payment note");
            assertThat(body.has("metadata")).isFalse();

            assertThat(response).isInstanceOf(PawapayPayoutResponse.class);
            assertThat(response.transactionId()).isEqualTo(ID);
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(response.message()).contains("Payout");
        }

        @Test
        @DisplayName("Should reject a non PawaPay cashout request")
        void shouldRejectForeignCashoutRequest() {
            CashoutRequest foreign = new CashoutRequest() {
                @Override
                public String amount() {
                    return "10";
                }

                @Override
                public String currency() {
                    return "XAF";
                }
            };

            assertThatThrownBy(() -> client.cashout(foreign))
                .isInstanceOf(CashoutException.class)
                .hasMessageContaining("PawapayPayoutRequest")
                .hasMessageContaining("PawapayRemittanceRequest");
            verify(httpClient, never()).execute(any());
        }

        @Test
        @DisplayName("Should POST /v2/payouts/bulk and parse every item")
        void shouldInitiateBulkPayouts() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                [
                  {"payoutId": "%s", "status": "ACCEPTED", "created": "2020-10-19T11:17:01Z"},
                  {"payoutId": "%s", "status": "DUPLICATE_IGNORED", "created": "2020-10-19T10:22:49Z"},
                  {"payoutId": "%s", "status": "REJECTED", "failureReason": {"failureCode": "AMOUNT_TOO_LARGE", "failureMessage": "Amount should not be greater than 1000"}}
                ]
                """.formatted(ID, OTHER_ID, ID)));

            List<PawapayPayoutResponse> responses = client.initiateBulkPayouts(List.of(payoutRequest(), payoutRequest()));

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.url()).isEqualTo(BASE + "v2/payouts/bulk");
            assertThat(bodyOf(request).isArray()).isTrue();
            assertThat(bodyOf(request)).hasSize(2);

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).isAccepted()).isTrue();
            assertThat(responses.get(1).isDuplicateIgnored()).isTrue();
            assertThat(responses.get(2).isRejected()).isTrue();
            assertThat(responses.get(2).failureReason()).isEqualTo("AMOUNT_TOO_LARGE: Amount should not be greater than 1000");
        }

        @Test
        @DisplayName("Should validate bulk size locally")
        void shouldValidateBulkSize() {
            assertThatThrownBy(() -> client.initiateBulkPayouts(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client.initiateBulkPayouts(Collections.nCopies(21, payoutRequest())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum 20");
            verify(httpClient, never()).execute(any());
        }

        @Test
        @DisplayName("Should GET /v2/payouts/{id} through getCashoutStatus")
        void shouldGetCashoutStatus() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {"payoutId": "%s", "status": "FAILED", "amount": "123.00", "currency": "ZMW", "country": "ZMB",
                  "recipient": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
                  "clientReferenceId": "REF-987654321", "customerMessage": "From ACME company", "created": "2020-10-19T08:17:01Z",
                  "failureReason": {"failureCode": "RECIPIENT_NOT_FOUND", "failureMessage": "Recipient not found"},
                  "metadata": {"orderId": "ORD-123456789"}}}
                """.formatted(OTHER_ID)));

            CashoutResponse response = client.getCashoutStatus(OTHER_ID);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/payouts/" + OTHER_ID);
            assertThat(response).isInstanceOf(PawapayPayout.class);
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.FAILED);
            assertThat(response.failureReason()).isEqualTo("RECIPIENT_NOT_FOUND: Recipient not found");
            assertThat(((PawapayPayout) response).getRecipient().provider()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(((PawapayPayout) response).getStatus()).isEqualTo(PawapayTransactionStatus.FAILED);
        }

        @Test
        @DisplayName("Should raise CashoutException with a NOT_FOUND cause")
        void shouldHandlePayoutNotFound() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"));

            assertThat(client.findPayout(ID)).isEmpty();
            assertThatThrownBy(() -> client.getCashoutStatus(ID))
                .isInstanceOf(CashoutException.class)
                .hasCauseInstanceOf(PawapayNotFoundException.class);
        }

        @Test
        @DisplayName("Should call fail-enqueued and resend-callback endpoints")
        void shouldCallPayoutActions() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"payoutId": "%s", "status": "REJECTED", "failureReason": {"failureCode": "INVALID_STATE", "failureMessage": "Payout is not enqueued"}}
                """.formatted(ID)));

            PawapayActionResponse cancel = client.cancelEnqueuedPayout(ID);
            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/payouts/fail-enqueued/" + ID);
            assertThat(cancel.transactionId()).isEqualTo(ID);
            assertThat(cancel.status()).isEqualTo(PawapayActionStatus.REJECTED);
            assertThat(cancel.isAccepted()).isFalse();
            assertThat(cancel.describeFailure()).isEqualTo("INVALID_STATE: Payout is not enqueued");
        }

        @Test
        @DisplayName("Should call resend-callback endpoint for payouts")
        void shouldResendPayoutCallback() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"payoutId\": \"" + ID + "\", \"status\": \"ACCEPTED\"}"));

            assertThat(client.resendPayoutCallback(ID).isAccepted()).isTrue();
            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/payouts/resend-callback/" + ID);
        }
    }

    private static PawapayRemittanceRequest remittanceRequest() {
        return PawapayRemittanceRequest.builder()
            .remittanceId(ID)
            .amount("15")
            .currency("ZMW")
            .phoneNumber("260763456789")
            .provider(PawapayProviders.MTN_MOMO_ZMB)
            .recipientName("John", "Doe")
            .sender(new PawapayRemittanceSender.TransactionDetails("de83150a-5916-48a2-b048-bd85e022cb55", "100", "USD", "23.88", "1",
                    PawapayPurposeOfFunds.FAMILY_SUPPORT, PawapaySourceOfFunds.SALARY),
                PawapayRemittanceSender.SenderDetails.builder()
                    .firstName("Jane").lastName("Doe").nationality("USA").phoneNumber("12124567890")
                    .address("1476 Sandhill Rd", "84058", "Orem", "USA")
                    .identification(PawapayIdentificationType.PASSPORT, "E00007730")
                    .relationshipRecipient(PawapayRelationship.PARTNER)
                    .build())
            .customerMessage("Note of 4 to 22 chars")
            .addMetadata("orderId", "ORD-123456789")
            .build();
    }

    @Nested
    @DisplayName("Remittances")
    class Remittances {

        @Test
        @DisplayName("Should POST /v2/remittances with recipient and sender details")
        void shouldInitiateRemittance() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"remittanceId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            PawapayRemittanceResponse response = client.initiateRemittance(remittanceRequest());

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/remittances");
            JsonNode body = bodyOf(request);
            assertThat(body.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("remittanceId", "recipient", "sender", "amount", "currency", "customerMessage", "metadata");
            assertThat(body.get("remittanceId").asText()).isEqualTo(ID);
            assertThat(body.at("/recipient/type").asText()).isEqualTo("MMO");
            assertThat(body.at("/recipient/accountDetails/phoneNumber").asText()).isEqualTo("260763456789");
            assertThat(body.at("/recipient/accountDetails/provider").asText()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(body.at("/recipient/recipientDetails/firstName").asText()).isEqualTo("John");
            assertThat(body.at("/recipient/recipientDetails/lastName").asText()).isEqualTo("Doe");
            assertThat(body.at("/sender/transactionDetails/transactionReference").asText()).isEqualTo("de83150a-5916-48a2-b048-bd85e022cb55");
            assertThat(body.at("/sender/transactionDetails/originalCurrency").asText()).isEqualTo("USD");
            assertThat(body.at("/sender/transactionDetails/buyFxRate").asText()).isEqualTo("23.88");
            assertThat(body.at("/sender/transactionDetails/purposeOfFunds").asText()).isEqualTo("FAMILY_SUPPORT");
            assertThat(body.at("/sender/transactionDetails/sourceOfFunds").asText()).isEqualTo("SALARY");
            assertThat(body.at("/sender/senderDetails/nationality").asText()).isEqualTo("USA");
            assertThat(body.at("/sender/senderDetails/address/country").asText()).isEqualTo("USA");
            assertThat(body.at("/sender/senderDetails/identification/type").asText()).isEqualTo("PASSPORT");
            assertThat(body.at("/sender/senderDetails/relationshipRecipient").asText()).isEqualTo("PARTNER");
            assertThat(body.at("/sender/senderDetails").has("gender")).isFalse();
            assertThat(body.at("/sender/senderDetails").has("dateOfBirth")).isFalse();
            assertThat(body.get("amount").asText()).isEqualTo("15");

            assertThat(response.getRemittanceId()).isEqualTo(ID);
            assertThat(response.isAccepted()).isTrue();
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(response.message()).contains("Remittance");
        }

        @Test
        @DisplayName("Should route a remittance request through the core cashout operation")
        void shouldCashoutRemittance() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"remittanceId": "%s", "status": "REJECTED", "failureReason": {"failureCode": "REMITTANCES_NOT_ALLOWED", "failureMessage": "Remittances are not enabled"}}
                """.formatted(ID)));

            CashoutResponse response = client.cashout(remittanceRequest());

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/remittances");
            assertThat(response).isInstanceOf(PawapayRemittanceResponse.class);
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.FAILED);
            assertThat(response.failureReason()).startsWith("REMITTANCES_NOT_ALLOWED");
        }

        @Test
        @DisplayName("Should GET /v2/remittances/{id} and parse recipient and sender")
        void shouldFindRemittance() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {
                  "remittanceId": "%s", "status": "COMPLETED", "amount": "123.00", "currency": "ZMW", "country": "ZMB",
                  "recipient": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"},
                                "recipientDetails": {"firstName": "John", "lastName": "Doe"}},
                  "sender": {"transactionDetails": {"transactionReference": "TX-1", "originalAmount": "100", "originalCurrency": "USD",
                                                    "buyFxRate": "23.88", "senderFees": "1", "purposeOfFunds": "FAMILY_SUPPORT", "sourceOfFunds": "SALARY"},
                             "senderDetails": {"firstName": "Jane", "lastName": "Doe", "nationality": "USA", "phoneNumber": "12124567890",
                                               "address": {"addressLine": "1476 Sandhill Rd", "postalCode": "84058", "city": "Orem", "country": "USA"},
                                               "identification": {"type": "PASSPORT", "number": "E00007730"},
                                               "gender": "FEMALE", "dateOfBirth": "1977-12-31", "relationshipRecipient": "BORTHER_IN_LAW"}},
                  "customerMessage": "From ACME company", "created": "2020-10-19T08:17:01Z", "providerTransactionId": "12356789",
                  "metadata": {"orderId": "ORD-123456789"}}}
                """.formatted(OTHER_ID)));

            Optional<PawapayRemittance> found = client.findRemittance(OTHER_ID);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/remittances/" + OTHER_ID);
            assertThat(found).isPresent();
            PawapayRemittance remittance = found.get();
            assertThat(remittance.getRemittanceId()).isEqualTo(OTHER_ID);
            assertThat(remittance.getStatus()).isEqualTo(PawapayTransactionStatus.COMPLETED);
            assertThat(remittance.status()).isEqualTo(MoMoTransferStatus.SUCCESSFUL);
            assertThat(remittance.getRecipient().recipientDetails().firstName()).isEqualTo("John");
            assertThat(remittance.getRecipient().provider()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(remittance.getSender().transactionDetails().purposeOfFunds()).isEqualTo(PawapayPurposeOfFunds.FAMILY_SUPPORT);
            assertThat(remittance.getSender().senderDetails().identification().type()).isEqualTo(PawapayIdentificationType.PASSPORT);
            assertThat(remittance.getSender().senderDetails().relationshipRecipient()).isEqualTo(PawapayRelationship.BROTHER_IN_LAW);
            assertThat(remittance.getProviderTransactionId()).isEqualTo("12356789");
            assertThat(remittance.getMetadata()).containsEntry("orderId", "ORD-123456789");
        }

        @Test
        @DisplayName("Should return empty on NOT_FOUND and call remittance actions")
        void shouldHandleNotFoundAndActions() {
            when(httpClient.execute(any()))
                .thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"))
                .thenReturn(jsonResponse(200, "{\"remittanceId\": \"" + ID + "\", \"status\": \"ACCEPTED\"}"))
                .thenReturn(jsonResponse(200, """
                    {"remittanceId": "%s", "status": "REJECTED", "failureReason": {"failureCode": "INVALID_STATE", "failureMessage": "Remittance is not enqueued"}}
                    """.formatted(ID)));

            assertThat(client.findRemittance(ID)).isEmpty();
            PawapayActionResponse resend = client.resendRemittanceCallback(ID);
            PawapayActionResponse cancel = client.cancelEnqueuedRemittance(ID);

            assertThat(resend.isAccepted()).isTrue();
            assertThat(resend.transactionId()).isEqualTo(ID);
            assertThat(cancel.status()).isEqualTo(PawapayActionStatus.REJECTED);
            assertThat(cancel.describeFailure()).startsWith("INVALID_STATE");

            ArgumentCaptor<PaymuxHttpRequest> captor = ArgumentCaptor.forClass(PaymuxHttpRequest.class);
            verify(httpClient, org.mockito.Mockito.times(3)).execute(captor.capture());
            assertThat(captor.getAllValues().get(1).url()).isEqualTo(BASE + "v2/remittances/resend-callback/" + ID);
            assertThat(captor.getAllValues().get(2).url()).isEqualTo(BASE + "v2/remittances/fail-enqueued/" + ID);
            assertThat(captor.getAllValues().get(2).body()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Refunds")
    class Refunds {

        @Test
        @DisplayName("Should accept a generic RefundRequest and POST /v2/refunds")
        void shouldRefundGenericRequest() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"refundId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            RefundRequest generic = new RefundRequest() {
                @Override
                public String amount() {
                    return "15.00";
                }

                @Override
                public String currency() {
                    return "ZMW";
                }

                @Override
                public String externalId() {
                    return ID;
                }

                @Override
                public String originalTransactionId() {
                    return OTHER_ID;
                }

                @Override
                public String reference() {
                    return "INV-123456";
                }
            };

            RefundResponse response = client.refund(generic);

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/refunds");
            JsonNode body = bodyOf(request);
            assertThat(body.get("refundId").asText()).isEqualTo(ID);
            assertThat(body.get("depositId").asText()).isEqualTo(OTHER_ID);
            assertThat(body.get("amount").asText()).isEqualTo("15");
            assertThat(body.get("currency").asText()).isEqualTo("ZMW");
            assertThat(body.get("clientReferenceId").asText()).isEqualTo("INV-123456");

            assertThat(response.transactionId()).isEqualTo(ID);
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.PENDING);
        }

        @Test
        @DisplayName("Should wrap invalid generic refund requests in RefundException")
        void shouldWrapInvalidRefundRequest() {
            RefundRequest invalid = new RefundRequest() {
                @Override
                public String amount() {
                    return "10";
                }

                @Override
                public String currency() {
                    return "ZMW";
                }

                @Override
                public String externalId() {
                    return ID;
                }

                @Override
                public String originalTransactionId() {
                    return null;
                }
            };

            assertThatThrownBy(() -> client.refund(invalid))
                .isInstanceOf(RefundException.class)
                .hasMessageContaining("Deposit ID is required");
            assertThatThrownBy(() -> client.refund(null)).isInstanceOf(RefundException.class);
            verify(httpClient, never()).execute(any());
        }

        @Test
        @DisplayName("Should initiate a typed refund with metadata")
        void shouldInitiateTypedRefund() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"refundId": "%s", "status": "REJECTED", "failureReason": {"failureCode": "NOT_FOUND", "failureMessage": "Deposit not found"}}
                """.formatted(ID)));

            var response = client.initiateRefund(PawapayRefundRequest.builder()
                .refundId(ID).depositId(OTHER_ID).amount("15").currency("ZMW").addMetadata("orderId", "ORD-1").build());

            assertThat(bodyOf(sentRequest()).at("/metadata/0/orderId").asText()).isEqualTo("ORD-1");
            assertThat(response.isRejected()).isTrue();
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.FAILED);
            assertThat(response.failureReason()).isEqualTo("NOT_FOUND: Deposit not found");
        }

        @Test
        @DisplayName("Should GET /v2/refunds/{id} through getRefundStatus")
        void shouldGetRefundStatus() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {"refundId": "%s", "status": "COMPLETED", "amount": "123.00", "currency": "ZMW", "country": "ZMB",
                  "recipient": {"type": "MMO", "accountDetails": {"phoneNumber": "260763456789", "provider": "MTN_MOMO_ZMB"}},
                  "created": "2020-10-19T08:17:01Z", "providerTransactionId": "12356789"}}
                """.formatted(ID)));

            RefundResponse response = client.getRefundStatus(ID);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/refunds/" + ID);
            assertThat(response).isInstanceOf(PawapayRefund.class);
            assertThat(response.status()).isEqualTo(MoMoTransferStatus.SUCCESSFUL);
            assertThat(((PawapayRefund) response).getProviderTransactionId()).isEqualTo("12356789");
        }

        @Test
        @DisplayName("Should raise RefundException with a NOT_FOUND cause and call refund actions")
        void shouldHandleRefundNotFoundAndActions() {
            when(httpClient.execute(any()))
                .thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"))
                .thenReturn(jsonResponse(200, "{\"refundId\": \"" + ID + "\", \"status\": \"ACCEPTED\"}"))
                .thenReturn(jsonResponse(200, "{\"refundId\": \"" + ID + "\", \"status\": \"ACCEPTED\"}"));

            assertThatThrownBy(() -> client.getRefundStatus(ID))
                .isInstanceOf(RefundException.class)
                .hasCauseInstanceOf(PawapayNotFoundException.class);
            assertThat(client.cancelEnqueuedRefund(ID).isAccepted()).isTrue();
            assertThat(client.resendRefundCallback(ID).isAccepted()).isTrue();

            ArgumentCaptor<PaymuxHttpRequest> captor = ArgumentCaptor.forClass(PaymuxHttpRequest.class);
            verify(httpClient, org.mockito.Mockito.times(3)).execute(captor.capture());
            assertThat(captor.getAllValues().get(1).url()).isEqualTo(BASE + "v2/refunds/fail-enqueued/" + ID);
            assertThat(captor.getAllValues().get(2).url()).isEqualTo(BASE + "v2/refunds/resend-callback/" + ID);
        }
    }

    @Nested
    @DisplayName("Checkouts")
    class Checkouts {

        @Test
        @DisplayName("Should POST /v2/checkouts and parse an ACCEPTED response")
        void shouldInitiateCheckout() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"checkoutId": "%s", "status": "ACCEPTED", "redirectUrl": "https://checkout.sandbox.pawapay.io/7mVk1x8UbTamQ64xGR",
                 "created": "2026-03-27T10:30:00Z", "expiresAt": "2026-03-27T11:30:00Z", "checkoutCode": "7mVk1x8UbTamQ64xGR"}
                """.formatted(ID)));

            PawapayCheckoutResponse response = client.initiateCheckout(PawapayCheckoutRequest.builder()
                .checkoutId(ID)
                .returnUrl("https://merchant.example.com/checkout-result")
                .returnMethod(PawapayReturnMethod.INSTANT)
                .addCountry("ZMB")
                .expiresAfter(60)
                .addAmount("ZMB", "ZMW", "100")
                .payer("260973024434", "MTN_MOMO_ZMB", true)
                .clientReferenceId("ORDER-123")
                .reason("en", "GOODS PURCHASE")
                .addMetadata("orderId", "ORDER-123")
                .build());

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/checkouts");
            JsonNode body = bodyOf(request);
            assertThat(body.get("checkoutId").asText()).isEqualTo(ID);
            assertThat(body.get("returnUrl").asText()).isEqualTo("https://merchant.example.com/checkout-result");
            assertThat(body.get("returnMethod").asText()).isEqualTo("INSTANT");
            assertThat(body.at("/countries/0").asText()).isEqualTo("ZMB");
            assertThat(body.get("expiresAfter").asInt()).isEqualTo(60);
            assertThat(body.at("/amounts/0/country").asText()).isEqualTo("ZMB");
            assertThat(body.at("/amounts/0/amount").asText()).isEqualTo("100");
            assertThat(body.at("/payer/type").asText()).isEqualTo("MMO");
            assertThat(body.at("/payer/accountDetails/allowCustomerToOverride").asBoolean()).isTrue();
            assertThat(body.at("/reason/en").asText()).isEqualTo("GOODS PURCHASE");
            assertThat(body.has("defaultLanguage")).isFalse();

            assertThat(response.getCheckoutId()).isEqualTo(ID);
            assertThat(response.isAccepted()).isTrue();
            assertThat(response.getRedirectUrl()).isEqualTo("https://checkout.sandbox.pawapay.io/7mVk1x8UbTamQ64xGR");
            assertThat(response.getCheckoutCode()).isEqualTo("7mVk1x8UbTamQ64xGR");
            assertThat(response.expiresAtInstant()).contains(Instant.parse("2026-03-27T11:30:00Z"));
        }

        @Test
        @DisplayName("Should accept the minimal checkout request")
        void shouldAcceptMinimalRequest() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"checkoutId\":\"" + ID + "\",\"status\":\"DUPLICATE_IGNORED\",\"created\":\"2026-03-27T10:30:00Z\"}"));

            PawapayCheckoutResponse response = client.initiateCheckout(PawapayCheckoutRequest.builder()
                .checkoutId(ID)
                .returnUrl("https://merchant.example.com/checkout-result")
                .build());

            JsonNode body = bodyOf(sentRequest());
            assertThat(body.fieldNames()).toIterable().containsExactlyInAnyOrder("checkoutId", "returnUrl");
            assertThat(response.isDuplicateIgnored()).isTrue();
            assertThat(response.getRedirectUrl()).isNull();
        }

        @Test
        @DisplayName("Should reject an invalid return URL, an out of range expiry and a payer without allowCustomerToOverride semantics")
        void shouldValidateLocally() {
            assertThatThrownBy(() -> PawapayCheckoutRequest.builder().returnUrl("not a url").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Return URL");
            assertThatThrownBy(() -> PawapayCheckoutRequest.builder().returnUrl("https://merchant.example.com/r").expiresAfter(1).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("expiresAfter");
            assertThatThrownBy(() -> PawapayCheckoutRequest.builder().returnUrl("https://merchant.example.com/r").expiresAfter(61).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("expiresAfter");
            assertThatThrownBy(() -> PawapayCheckoutRequest.builder().build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Return URL is required");
            assertThatThrownBy(() -> PawapayCheckoutRequest.builder()
                .returnUrl("https://merchant.example.com/r").addAmount("ZM", "ZMW", "15").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("country");
            assertThatThrownBy(() -> PawapayCheckoutRequest.builder()
                .returnUrl("https://merchant.example.com/r").reason("en", "ab").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("customer message");
            verify(httpClient, never()).execute(any());
        }

        @Test
        @DisplayName("Should GET /v2/checkouts/{id} and parse the attempt history")
        void shouldFindCheckout() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {
                  "checkoutId": "%s", "status": "COMPLETED", "redirectUrl": "https://checkout.sandbox.pawapay.io/7mVk1x8UbTamQ64xGR",
                  "returnUrl": "https://merchant.example.com/checkout-result", "returnMethod": "INSTANT", "defaultLanguage": "en",
                  "countries": ["ZMB"], "expiresAfter": 60, "amounts": [{"country": "ZMB", "currency": "ZMW", "amount": "100"}],
                  "payer": {"type": "MMO", "accountDetails": {"phoneNumber": "260973024434", "provider": "MTN_MOMO_ZMB", "allowCustomerToOverride": true}},
                  "clientReferenceId": "ORDER-123", "created": "2026-03-27T10:30:00Z", "providerTransactionId": "PROVIDER-TXN-123",
                  "depositStatus": "COMPLETED",
                  "deposit": {"depositId": "%s", "status": "COMPLETED", "created": "2026-03-27T10:35:00Z", "providerTransactionId": "PROVIDER-TXN-123",
                              "amount": "100", "currency": "ZMW", "country": "ZMB",
                              "payer": {"type": "MMO", "accountDetails": {"phoneNumber": "260973024434", "provider": "MTN_MOMO_ZMB"}},
                              "customerMessage": "To ACME company"},
                  "depositsHistory": [{"depositId": "%s", "status": "COMPLETED", "created": "2026-03-27T10:35:00Z"}],
                  "metadata": {"orderId": "ORDER-123"}, "reason": {"en": "GOODS PURCHASE"}, "checkoutCode": "7mVk1x8UbTamQ64xGR"}}
                """.formatted(OTHER_ID, ID, ID)));

            Optional<PawapayCheckout> found = client.findCheckout(OTHER_ID);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/checkouts/" + OTHER_ID);
            assertThat(found).isPresent();
            PawapayCheckout checkout = found.get();
            assertThat(checkout.status()).isEqualTo(PawapayCheckoutStatus.COMPLETED);
            assertThat(checkout.isCompleted()).isTrue();
            assertThat(checkout.isFinal()).isTrue();
            assertThat(checkout.depositStatus()).isEqualTo(PawapayTransactionStatus.COMPLETED);
            assertThat(checkout.deposit()).isNotNull();
            assertThat(checkout.deposit().depositId()).isEqualTo(ID);
            assertThat(checkout.deposit().payer().provider()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(checkout.depositsHistory()).hasSize(1);
            assertThat(checkout.payer().allowCustomerToOverride()).isTrue();
            assertThat(checkout.amounts().get(0).amount()).isEqualTo("100");
            assertThat(checkout.reason()).containsEntry("en", "GOODS PURCHASE");
            assertThat(checkout.checkoutCode()).isEqualTo("7mVk1x8UbTamQ64xGR");
        }

        @Test
        @DisplayName("Should return empty for an unknown checkout")
        void shouldHandleNotFound() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"));

            assertThat(client.findCheckout(ID)).isEmpty();
        }

        @Test
        @DisplayName("Should POST /v2/checkouts/{id}/expire")
        void shouldExpireCheckout() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"checkoutId": "%s", "status": "EXPIRED", "expiredAt": "2026-03-27T10:45:00Z", "reason": "MANUAL_EXPIRY", "expiredBy": "API"}
                """.formatted(ID)));

            PawapayCheckoutExpiry expiry = client.expireCheckout(ID);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/checkouts/" + ID + "/expire");
            assertThat(expiry.status()).isEqualTo(PawapayCheckoutStatus.EXPIRED);
            assertThat(expiry.reason()).isEqualTo("MANUAL_EXPIRY");
            assertThat(expiry.expiredBy()).isEqualTo("API");
            assertThat(expiry.expiredAtInstant()).contains(Instant.parse("2026-03-27T10:45:00Z"));
        }

        @Test
        @DisplayName("Should throw PawapayApiException with NOT_FOUND on a 404 expire response")
        void shouldThrowOnExpireNotFound() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(404, """
                {"status": "REJECTED", "failureReason": {"failureCode": "NOT_FOUND", "failureMessage": "Checkout with ID %s not found."}}
                """.formatted(ID)));

            assertThatThrownBy(() -> client.expireCheckout(ID))
                .isInstanceOfSatisfying(PawapayApiException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(404);
                    assertThat(e.getFailureCode()).isEqualTo("NOT_FOUND");
                });
        }
    }

    @Nested
    @DisplayName("Payment page")
    class PaymentPage {

        @Test
        @DisplayName("Should POST /v2/paymentpage with a fixed amount and country")
        void shouldCreatePaymentPage() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"redirectUrl\": \"https://paywith.pawapay.io/?token=abc\"}"));

            PawapayPaymentPageResponse response = client.initiatePaymentPage(PawapayPaymentPageRequest.builder()
                .depositId(ID)
                .returnUrl("https://merchant.com/returnUrl")
                .amount("100", "GHS")
                .country("GHA")
                .language(PawapayLanguage.EN)
                .reason("Demo payment")
                .addMetadata("orderId", "ORD-1")
                .build());

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/paymentpage");
            JsonNode body = bodyOf(request);
            assertThat(body.get("depositId").asText()).isEqualTo(ID);
            assertThat(body.at("/amountDetails/amount").asText()).isEqualTo("100");
            assertThat(body.at("/amountDetails/currency").asText()).isEqualTo("GHS");
            assertThat(body.get("country").asText()).isEqualTo("GHA");
            assertThat(body.get("language").asText()).isEqualTo("EN");
            assertThat(body.get("reason").asText()).isEqualTo("Demo payment");
            assertThat(body.has("phoneNumber")).isFalse();

            assertThat(response.isAccepted()).isTrue();
            assertThat(response.getRedirectUrl()).contains("paywith.pawapay.io");
            assertThat(response.isRejected()).isFalse();
        }

        @Test
        @DisplayName("Should reject fixing an amount without a country")
        void shouldRequireCountryForFixedAmount() {
            assertThatThrownBy(() -> PawapayPaymentPageRequest.builder()
                .returnUrl("https://merchant.com/r").amount("100", "GHS").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Country is required");
        }

        @Test
        @DisplayName("Should parse a business rejection")
        void shouldParseRejection() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"depositId": "%s", "status": "REJECTED", "failureReason": {"failureCode": "AMOUNT_OUT_OF_BOUNDS",
                 "failureMessage": "The amount needs to be more than '1' and less than '20000' for provider 'MTN_MOMO_ZMB'."}}
                """.formatted(ID)));

            PawapayPaymentPageResponse response = client.initiatePaymentPage(PawapayPaymentPageRequest.builder()
                .depositId(ID).returnUrl("https://merchant.com/returnUrl").phoneNumber("233593456789").build());

            assertThat(response.isAccepted()).isFalse();
            assertThat(response.isRejected()).isTrue();
            assertThat(response.getDepositId()).isEqualTo(ID);
            assertThat(response.describeFailure()).startsWith("AMOUNT_OUT_OF_BOUNDS");
        }
    }

    @Nested
    @DisplayName("Toolkit")
    class Toolkit {

        @Test
        @DisplayName("Should GET /v2/active-conf with filters and parse the configuration")
        void shouldGetActiveConfiguration() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"companyName": "Merchant Inc.",
                 "signatureConfiguration": {"signedRequestsOnly": true, "signedCallbacks": true},
                 "countries": [{"country": "BEN", "displayName": {"en": "Benin", "fr": "Le Benin"}, "prefix": "229", "flag": "https://cdn.com/ben_flag.png",
                   "providers": [{"provider": "MTN_MOMO_BEN", "displayName": "MTN", "logo": "https://cdn.com/mtn_logo.png", "nameDisplayedToCustomer": "Merchant Inc.",
                     "currencies": [{"currency": "XOF", "displayName": "CFA",
                       "operationTypes": {"DEPOSIT": {"authType": "PROVIDER_AUTH", "pinPrompt": "AUTOMATIC", "pinPromptRevivable": true, "minAmount": "1", "maxAmount": "1000",
                                                      "decimalsInAmount": "NONE", "status": "OPERATIONAL", "callbackUrl": "https://merchant.com/depositCallback"},
                                          "PAYOUT": {"minAmount": "1", "maxAmount": "5000", "decimalsInAmount": "NONE", "status": "CLOSED"}}}]}]}]}
                """));

            PawapayActiveConfiguration configuration = client.getActiveConfiguration("BEN", PawapayOperationType.DEPOSIT);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/active-conf?country=BEN&operationType=DEPOSIT");
            assertThat(configuration.companyName()).isEqualTo("Merchant Inc.");
            assertThat(configuration.signatureConfiguration().signedCallbacks()).isTrue();
            assertThat(configuration.country("BEN")).isPresent();
            assertThat(configuration.country("BEN").get().displayName()).containsEntry("fr", "Le Benin");
            assertThat(configuration.provider("MTN_MOMO_BEN")).isPresent();
            assertThat(configuration.isOperational("MTN_MOMO_BEN", "XOF", PawapayOperationType.DEPOSIT)).isTrue();
            assertThat(configuration.isOperational("MTN_MOMO_BEN", "XOF", PawapayOperationType.PAYOUT)).isFalse();
            assertThat(configuration.isOperational("MTN_MOMO_BEN", "XOF", PawapayOperationType.REFUND)).isFalse();
            assertThat(configuration.isOperational("UNKNOWN", "XOF", PawapayOperationType.DEPOSIT)).isFalse();

            var deposit = configuration.provider("MTN_MOMO_BEN").get().currency("XOF").get().operationType(PawapayOperationType.DEPOSIT).get();
            assertThat(deposit.authType()).isEqualTo("PROVIDER_AUTH");
            assertThat(deposit.maxAmount()).isEqualTo("1000");
            assertThat(deposit.status()).isEqualTo(PawapayProviderStatus.OPERATIONAL);
        }

        @Test
        @DisplayName("Should GET /v2/active-conf without query when unfiltered and reject UNKNOWN filter")
        void shouldGetUnfilteredActiveConfiguration() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"companyName\": \"Merchant Inc.\"}"));

            PawapayActiveConfiguration configuration = client.getActiveConfiguration();

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/active-conf");
            assertThat(configuration.countries()).isEmpty();
            assertThatThrownBy(() -> client.getActiveConfiguration(null, PawapayOperationType.UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client.getActiveConfiguration("Benin", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("country");
        }

        @Test
        @DisplayName("Should GET /v2/availability and expose provider statuses")
        void shouldGetAvailability() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                [{"country": "GHA", "providers": [{"provider": "VODAFONE_GHA", "operationTypes": [
                   {"operationType": "DEPOSIT", "status": "OPERATIONAL"},
                   {"operationType": "PAYOUT", "status": "DELAYED"},
                   {"operationType": "REMITTANCE", "status": "OPERATIONAL"}]}]}]
                """));

            List<PawapayAvailability> availability = client.getAvailability("GHA", null);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/availability?country=GHA");
            assertThat(availability).hasSize(1);
            assertThat(availability.get(0).statusOf("VODAFONE_GHA", PawapayOperationType.PAYOUT)).contains(PawapayProviderStatus.DELAYED);
            assertThat(availability.get(0).statusOf("VODAFONE_GHA", PawapayOperationType.DEPOSIT)).contains(PawapayProviderStatus.OPERATIONAL);
            assertThat(availability.get(0).statusOf("VODAFONE_GHA", PawapayOperationType.REFUND)).isEmpty();
        }

        @Test
        @DisplayName("Should POST /v2/predict-provider")
        void shouldPredictProvider() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"country\": \"ZMB\", \"provider\": \"MTN_MOMO_ZMB\", \"phoneNumber\": \"260763456789\"}"));

            PawapayProviderPrediction prediction = client.predictProvider("+260 763-456789");

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.url()).isEqualTo(BASE + "v2/predict-provider");
            assertThat(bodyOf(request).get("phoneNumber").asText()).isEqualTo("+260 763-456789");
            assertThat(prediction.country()).isEqualTo("ZMB");
            assertThat(prediction.provider()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(prediction.phoneNumber()).isEqualTo("260763456789");
        }

        @Test
        @DisplayName("Should GET /v2/public-key/http")
        void shouldGetPublicKeys() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                [{"id": "HTTP_EC_P256_KEY:1", "key": "-----BEGIN PUBLIC KEY-----\\nMFkw...\\n-----END PUBLIC KEY-----"}]
                """));

            List<PawapayPublicKey> keys = client.getPublicKeys();

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/public-key/http");
            assertThat(keys).hasSize(1);
            assertThat(keys.get(0).id()).isEqualTo("HTTP_EC_P256_KEY:1");
            assertThat(keys.get(0).key()).startsWith("-----BEGIN PUBLIC KEY-----");
        }

    }

    @Nested
    @DisplayName("Finances")
    class Finances {

        @Test
        @DisplayName("Should GET /v2/wallet-balances")
        void shouldGetWalletBalances() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"balances": [{"country": "ZMB", "balance": "21798.03", "currency": "ZMW", "provider": ""},
                              {"country": "UGA", "balance": "10798.03", "currency": "UGX", "provider": ""}]}
                """));

            List<PawapayWalletBalance> balances = client.getWalletBalances("ZMB");

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/wallet-balances?country=ZMB");
            assertThat(balances).hasSize(2);
            assertThat(balances.get(0).balance()).isEqualTo("21798.03");
            assertThat(balances.get(1).currency()).isEqualTo("UGX");
        }

        @Test
        @DisplayName("Should POST /v2/statements and parse ACCEPTED")
        void shouldInitiateStatement() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"statementId\": \"" + ID + "\", \"status\": \"ACCEPTED\", \"created\": \"2025-10-19T11:17:01Z\"}"));

            PawapayStatementResponse response = client.initiateStatement(PawapayStatementRequest.builder()
                .wallet(PawapayWallet.of("ZMB", "ZMW", "MTN_MOMO_ZMB"))
                .callbackUrl("https://merchant.com/statementCallbacks")
                .startDate(LocalDateTime.of(2025, 5, 10, 10, 0))
                .endDate(LocalDateTime.of(2025, 5, 11, 10, 0))
                .compressed(true)
                .build());

            PaymuxHttpRequest request = sentRequest();
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.url()).isEqualTo(BASE + "v2/statements");
            JsonNode body = bodyOf(request);
            assertThat(body.at("/wallet/country").asText()).isEqualTo("ZMB");
            assertThat(body.at("/wallet/currency").asText()).isEqualTo("ZMW");
            assertThat(body.at("/wallet/provider").asText()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(body.get("callbackUrl").asText()).isEqualTo("https://merchant.com/statementCallbacks");
            assertThat(body.get("startDate").asText()).isEqualTo("2025-05-10T10:00:00");
            assertThat(body.get("endDate").asText()).isEqualTo("2025-05-11T10:00:00");
            assertThat(body.get("compressed").asBoolean()).isTrue();
            assertThat(body.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("wallet", "callbackUrl", "startDate", "endDate", "compressed");

            assertThat(response.isAccepted()).isTrue();
            assertThat(response.statementId()).isEqualTo(ID);
            assertThat(response.createdInstant()).contains(Instant.parse("2025-10-19T11:17:01Z"));
            assertThat(response.describeFailure()).isNull();
        }

        @Test
        @DisplayName("Should return a REJECTED statement initiation")
        void shouldReturnRejectedStatement() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "REJECTED", "failureReason": {"failureCode": "WALLET_NOT_FOUND", "failureMessage": "Wallet combination does not exist."}}
                """));

            PawapayStatementResponse response = client.initiateStatement(PawapayStatementRequest.builder()
                .country("COG").currency("XAF")
                .callbackUrl("https://merchant.com/statementCallbacks")
                .startDate("2025-05-10T10:00:00")
                .endDate("2025-05-11T10:00:00")
                .build());

            assertThat(bodyOf(sentRequest()).at("/wallet").has("provider")).isFalse();
            assertThat(bodyOf(sentRequest()).has("compressed")).isFalse();
            assertThat(response.isRejected()).isTrue();
            assertThat(response.statementId()).isNull();
            assertThat(response.describeFailure()).isEqualTo("WALLET_NOT_FOUND: Wallet combination does not exist.");
        }

        @Test
        @DisplayName("Should GET /v2/statements/{id} and parse a completed statement")
        void shouldFindStatement() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, """
                {"status": "FOUND", "data": {
                  "statementId": "%s", "status": "COMPLETED",
                  "wallet": {"country": "ZMB", "currency": "ZMW", "provider": "MTN_MOMO_ZMB"},
                  "created": "2025-05-12T17:32:29Z", "startDate": "2025-05-10T10:00:00", "endDate": "2025-05-11T10:00:00",
                  "fileSize": 1048576, "downloadUrl": "https://files.pawapay.io/statement.csv.gz",
                  "downloadUrlExpiresAt": "2099-05-13T10:00:00", "completedAt": "2025-05-12T17:35:00Z"}}
                """.formatted(ID)));

            Optional<PawapayStatement> found = client.findStatement(ID);

            assertThat(sentRequest().url()).isEqualTo(BASE + "v2/statements/" + ID);
            assertThat(found).isPresent();
            PawapayStatement statement = found.get();
            assertThat(statement.status()).isEqualTo(PawapayStatementStatus.COMPLETED);
            assertThat(statement.isCompleted()).isTrue();
            assertThat(statement.isDownloadable()).isTrue();
            assertThat(statement.wallet().provider()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(statement.fileSize()).isEqualTo(1048576L);
            assertThat(statement.downloadUrl()).endsWith(".csv.gz");
            assertThat(statement.startDateTime()).contains(LocalDateTime.of(2025, 5, 10, 10, 0));
            assertThat(statement.endDateTime()).contains(LocalDateTime.of(2025, 5, 11, 10, 0));
            assertThat(statement.completedAtInstant()).contains(Instant.parse("2025-05-12T17:35:00Z"));
        }

        @Test
        @DisplayName("Should return empty for an unknown statement and validate the id locally")
        void shouldHandleStatementNotFound() {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"));

            assertThat(client.findStatement(ID)).isEmpty();
            assertThatThrownBy(() -> client.findStatement("nope")).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Outbound request signing")
    class RequestSigning {

        private PawapayClient signedClient(boolean enabled) throws Exception {
            var generator = java.security.KeyPairGenerator.getInstance("EC");
            generator.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
            var keyPair = generator.generateKeyPair();

            PawapayConfig config = PawapayConfig.builder()
                .apiToken("test-token")
                .httpClient(httpClient)
                .requestSigningEnabled(enabled)
                .signingKeyId("CUSTOMER_TEST_KEY")
                .signingAlgorithm(com.rickenbazolo.paymux.pawapay.signing.PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
                .signingPrivateKey(keyPair.getPrivate())
                .build();
            return new PawapayClient(config);
        }

        @Test
        @DisplayName("Should add the 4 signature headers to POST /v2/deposits when enabled")
        void shouldSignDeposit() throws Exception {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"depositId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            signedClient(true).initiateDeposit(depositRequest());

            assertSigned(sentRequest());
        }

        @Test
        @DisplayName("Should add the 4 signature headers to POST /v2/payouts and /v2/payouts/bulk when enabled")
        void shouldSignPayoutAndBulkPayout() throws Exception {
            when(httpClient.execute(any()))
                .thenReturn(jsonResponse(200, "{\"payoutId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"))
                .thenReturn(jsonResponse(200, "[{\"payoutId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}]"));
            var client = signedClient(true);

            client.initiatePayout(payoutRequest());
            client.initiateBulkPayouts(List.of(payoutRequest()));

            ArgumentCaptor<PaymuxHttpRequest> captor = ArgumentCaptor.forClass(PaymuxHttpRequest.class);
            verify(httpClient, org.mockito.Mockito.times(2)).execute(captor.capture());
            assertSigned(captor.getAllValues().get(0));
            assertSigned(captor.getAllValues().get(1));
        }

        @Test
        @DisplayName("Should add the 4 signature headers to POST /v2/refunds when enabled")
        void shouldSignRefund() throws Exception {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"refundId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            signedClient(true).initiateRefund(com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundRequest.builder()
                .refundId(ID).depositId(OTHER_ID).amount("15").currency("ZMW").build());

            assertSigned(sentRequest());
        }

        @Test
        @DisplayName("Should never sign a request when disabled, even for financial endpoints")
        void shouldNotSignWhenDisabled() throws Exception {
            when(httpClient.execute(any())).thenReturn(jsonResponse(200,
                "{\"depositId\":\"" + ID + "\",\"status\":\"ACCEPTED\",\"created\":\"2020-10-19T11:17:01Z\"}"));

            signedClient(false).initiateDeposit(depositRequest());

            assertNotSigned(sentRequest());
        }

        @Test
        @DisplayName("Should never sign non-financial endpoints, even when signing is enabled")
        void shouldNotSignOtherEndpoints() throws Exception {
            when(httpClient.execute(any()))
                .thenReturn(jsonResponse(200, "{\"status\": \"NOT_FOUND\"}"))
                .thenReturn(jsonResponse(200, "{\"depositId\": \"" + ID + "\", \"status\": \"ACCEPTED\"}"));
            var client = signedClient(true);

            client.findDeposit(ID);
            client.resendDepositCallback(ID);

            ArgumentCaptor<PaymuxHttpRequest> captor = ArgumentCaptor.forClass(PaymuxHttpRequest.class);
            verify(httpClient, org.mockito.Mockito.times(2)).execute(captor.capture());
            assertNotSigned(captor.getAllValues().get(0));
            assertNotSigned(captor.getAllValues().get(1));
        }

        private void assertSigned(PaymuxHttpRequest request) {
            assertThat(request.headers()).containsKeys("Content-Digest", "Signature-Date", "Signature-Input", "Signature");
            assertThat(request.headers().get("Signature-Input"))
                .contains("keyid=\"CUSTOMER_TEST_KEY\"")
                .contains("alg=\"ecdsa-p256-sha256\"")
                .contains("\"@method\" \"@authority\" \"@path\" \"signature-date\" \"content-digest\" \"content-type\"");
            assertThat(request.headers().get("Signature")).startsWith("sig-pp=:");
            assertThat(request.headers().get("Content-Digest")).startsWith("sha-256=:");
        }

        private void assertNotSigned(PaymuxHttpRequest request) {
            assertThat(request.headers()).doesNotContainKeys("Content-Digest", "Signature-Date", "Signature-Input", "Signature");
        }
    }

    @Test
    @DisplayName("Should expose config, http client and a shared signature verifier, and close the http client")
    void shouldExposeInfrastructure() {
        assertThat(client.getConfig().getApiToken()).isEqualTo("test-token");
        assertThat(client.getHttpClient()).isSameAs(httpClient);
        assertThat(client.callbackSignatureVerifier()).isSameAs(client.callbackSignatureVerifier());

        client.close();

        verify(httpClient).close();
    }
}
