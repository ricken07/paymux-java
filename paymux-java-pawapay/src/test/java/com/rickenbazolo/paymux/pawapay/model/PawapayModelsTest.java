package com.rickenbazolo.paymux.pawapay.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.rickenbazolo.paymux.core.enums.MoMoTransferStatus;
import com.rickenbazolo.paymux.core.operations.refund.RefundRequest;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutPayer;
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutRequest;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositRequest;
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageRequest;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementRequest;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayWallet;
import com.rickenbazolo.paymux.pawapay.util.PawapayDates;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutRequest;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundRequest;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayGender;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayIdentificationType;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayPurposeOfFunds;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRelationship;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceRecipient;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceRequest;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapayRemittanceSender;
import com.rickenbazolo.paymux.pawapay.remittance.model.PawapaySourceOfFunds;
import com.rickenbazolo.paymux.pawapay.util.PawapayJson;
import com.rickenbazolo.paymux.pawapay.util.PawapayValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.rickenbazolo.paymux.pawapay.PawapayTestSupport.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the PawaPay request models, validation and status mapping.
 */
@DisplayName("PawaPay Models Tests")
class PawapayModelsTest {

    private static final String DEPOSIT_ID = "f4401bd2-1568-4140-bf2d-eb77d2b2b639";

    @Nested
    @DisplayName("Deposit request")
    class DepositRequestTests {

        @Test
        @DisplayName("Should serialize exactly the PawaPay JSON shape")
        void shouldSerializeToPawapayJson() {
            PawapayDepositRequest request = PawapayDepositRequest.builder()
                .depositId(DEPOSIT_ID)
                .amount("15.00")
                .currency("ZMW")
                .phoneNumber("260763456789")
                .provider(PawapayProviders.MTN_MOMO_ZMB)
                .preAuthorisationCode("123456")
                .clientReferenceId("INV-123456")
                .customerMessage("Payment for invoice")
                .addMetadata("orderId", "ORD-123456789")
                .addPiiMetadata("customerId", "customer@email.com")
                .build();

            JsonNode json = parse(PawapayJson.write(request));

            assertThat(json.get("depositId").asText()).isEqualTo(DEPOSIT_ID);
            assertThat(json.get("amount").asText()).isEqualTo("15");
            assertThat(json.get("currency").asText()).isEqualTo("ZMW");
            assertThat(json.at("/payer/type").asText()).isEqualTo("MMO");
            assertThat(json.at("/payer/accountDetails/phoneNumber").asText()).isEqualTo("260763456789");
            assertThat(json.at("/payer/accountDetails/provider").asText()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(json.get("preAuthorisationCode").asText()).isEqualTo("123456");
            assertThat(json.get("clientReferenceId").asText()).isEqualTo("INV-123456");
            assertThat(json.get("customerMessage").asText()).isEqualTo("Payment for invoice");
            assertThat(json.get("metadata").isArray()).isTrue();
            assertThat(json.get("metadata")).hasSize(2);
            assertThat(json.at("/metadata/0/orderId").asText()).isEqualTo("ORD-123456789");
            assertThat(json.at("/metadata/1/customerId").asText()).isEqualTo("customer@email.com");
            assertThat(json.at("/metadata/1/isPII").asBoolean()).isTrue();

            // core interface accessors must not leak into the wire format
            assertThat(json.has("externalId")).isFalse();
            assertThat(json.has("recipientPhoneNumber")).isFalse();
            assertThat(json.has("description")).isFalse();
            assertThat(json.has("reference")).isFalse();
        }

        @Test
        @DisplayName("Should omit null fields and empty metadata")
        void shouldOmitNullAndEmpty() {
            PawapayDepositRequest request = PawapayDepositRequest.builder()
                .amount(new BigDecimal("100"))
                .currency("XAF")
                .phoneNumber("242065551234")
                .provider(PawapayProviders.MTN_MOMO_COG)
                .build();

            JsonNode json = parse(PawapayJson.write(request));

            assertThat(json.has("preAuthorisationCode")).isFalse();
            assertThat(json.has("clientReferenceId")).isFalse();
            assertThat(json.has("customerMessage")).isFalse();
            assertThat(json.has("metadata")).isFalse();
            assertThat(json.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("depositId", "payer", "amount", "currency");
        }

        @Test
        @DisplayName("Should generate a UUID deposit id when absent and expose core accessors")
        void shouldGenerateDepositId() {
            PawapayDepositRequest request = PawapayDepositRequest.builder()
                .amount("1000")
                .currency("XAF")
                .phoneNumber("242065551234")
                .provider(PawapayProviders.MTN_MOMO_COG)
                .customerMessage("Order 12345")
                .clientReferenceId("ORDER-12345")
                .build();

            assertThat(request.getDepositId()).isNotBlank();
            assertThat(UUID.fromString(request.getDepositId())).isNotNull();
            assertThat(request.externalId()).isEqualTo(request.getDepositId());
            assertThat(request.amount()).isEqualTo("1000");
            assertThat(request.currency()).isEqualTo("XAF");
            assertThat(request.recipientPhoneNumber()).isEqualTo("242065551234");
            assertThat(request.description()).isEqualTo("Order 12345");
            assertThat(request.reference()).isEqualTo("ORDER-12345");
        }

        @Test
        @DisplayName("Should normalize amounts")
        void shouldNormalizeAmounts() {
            assertThat(PawapayValidation.normalizeAmount("15.00")).isEqualTo("15");
            assertThat(PawapayValidation.normalizeAmount("10.50")).isEqualTo("10.5");
            assertThat(PawapayValidation.normalizeAmount("100")).isEqualTo("100");
            assertThat(PawapayValidation.normalizeAmount("1000.000")).isEqualTo("1000");
            assertThat(PawapayValidation.normalizeAmount("0.5")).isEqualTo("0.5");
            assertThat(PawapayValidation.normalizeAmount("1E+3")).isEqualTo("1000");
        }

        @Test
        @DisplayName("Should reject invalid fields")
        void shouldRejectInvalidFields() {
            assertThatThrownBy(() -> validDeposit().amount("0").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positive");
            assertThatThrownBy(() -> validDeposit().amount("abc").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid amount");
            assertThatThrownBy(() -> validDeposit().amount("1.23456").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid amount");
            assertThatThrownBy(() -> validDeposit().phoneNumber("+242065551234").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("phone number");
            assertThatThrownBy(() -> validDeposit().phoneNumber("065551234").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("phone number");
            assertThatThrownBy(() -> validDeposit().currency("xaf").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("currency");
            assertThatThrownBy(() -> validDeposit().customerMessage("abc").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("customer message");
            assertThatThrownBy(() -> validDeposit().customerMessage("Order #12345").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("customer message");
            assertThatThrownBy(() -> validDeposit().preAuthorisationCode("12-34").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("pre-authorisation");
            assertThatThrownBy(() -> validDeposit().depositId("not-a-uuid").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("depositId");

            var builder = validDeposit();
            IntStream.range(0, 11).forEach(i -> builder.addMetadata("key" + i, "value"));
            assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("metadata");
        }

        @Test
        @DisplayName("Should require mandatory fields")
        void shouldRequireMandatoryFields() {
            assertThatThrownBy(() -> PawapayDepositRequest.builder().currency("XAF").phoneNumber("242065551234").provider("MTN_MOMO_COG").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Amount is required");
            assertThatThrownBy(() -> PawapayDepositRequest.builder().amount("10").phoneNumber("242065551234").provider("MTN_MOMO_COG").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Currency is required");
            assertThatThrownBy(() -> PawapayDepositRequest.builder().amount("10").currency("XAF").provider("MTN_MOMO_COG").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("phone number is required");
            assertThatThrownBy(() -> PawapayDepositRequest.builder().amount("10").currency("XAF").phoneNumber("242065551234").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("provider is required");
        }

        private PawapayDepositRequest.Builder validDeposit() {
            return PawapayDepositRequest.builder()
                .amount("1000")
                .currency("XAF")
                .phoneNumber("242065551234")
                .provider(PawapayProviders.MTN_MOMO_COG);
        }
    }

    @Nested
    @DisplayName("Payout request")
    class PayoutRequestTests {

        @Test
        @DisplayName("Should serialize exactly the PawaPay JSON shape")
        void shouldSerializeToPawapayJson() {
            PawapayPayoutRequest request = PawapayPayoutRequest.builder()
                .payoutId(UUID.fromString(DEPOSIT_ID))
                .amount("15")
                .currency("ZMW")
                .phoneNumber("260763456789")
                .provider(PawapayProviders.MTN_MOMO_ZMB)
                .customerMessage("Payment note")
                .clientReferenceId("INV-123456")
                .addMetadata("orderId", "ORD-123456789")
                .build();

            JsonNode json = parse(PawapayJson.write(request));

            assertThat(json.get("payoutId").asText()).isEqualTo(DEPOSIT_ID);
            assertThat(json.at("/recipient/type").asText()).isEqualTo("MMO");
            assertThat(json.at("/recipient/accountDetails/phoneNumber").asText()).isEqualTo("260763456789");
            assertThat(json.at("/recipient/accountDetails/provider").asText()).isEqualTo("MTN_MOMO_ZMB");
            assertThat(json.get("amount").asText()).isEqualTo("15");
            assertThat(json.get("customerMessage").asText()).isEqualTo("Payment note");
            assertThat(json.get("clientReferenceId").asText()).isEqualTo("INV-123456");
            assertThat(json.at("/metadata/0/orderId").asText()).isEqualTo("ORD-123456789");
            assertThat(json.has("payer")).isFalse();

            assertThat(request.recipientPhoneNumber()).isEqualTo("260763456789");
            assertThat(request.reference()).isEqualTo("INV-123456");
            assertThat(request.description()).isEqualTo("Payment note");
        }

        @Test
        @DisplayName("Should require recipient")
        void shouldRequireRecipient() {
            assertThatThrownBy(() -> PawapayPayoutRequest.builder().amount("10").currency("XAF").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Recipient phone number is required");
        }
    }

    @Nested
    @DisplayName("Refund request")
    class RefundRequestTests {

        @Test
        @DisplayName("Should serialize exactly the PawaPay JSON shape")
        void shouldSerializeToPawapayJson() {
            PawapayRefundRequest request = PawapayRefundRequest.builder()
                .refundId("11111111-2222-4333-8444-555555555555")
                .depositId(DEPOSIT_ID)
                .amount("15")
                .currency("ZMW")
                .clientReferenceId("INV-123456")
                .addMetadata("orderId", "ORD-123456789")
                .build();

            JsonNode json = parse(PawapayJson.write(request));

            assertThat(json.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("refundId", "depositId", "amount", "currency", "clientReferenceId", "metadata");
            assertThat(json.get("depositId").asText()).isEqualTo(DEPOSIT_ID);
            assertThat(request.externalId()).isEqualTo("11111111-2222-4333-8444-555555555555");
            assertThat(request.originalTransactionId()).isEqualTo(DEPOSIT_ID);
            assertThat(request.reference()).isEqualTo("INV-123456");
            assertThat(request.description()).isNull();
        }

        @Test
        @DisplayName("Should build from a generic refund request")
        void shouldBuildFromGenericRequest() {
            RefundRequest generic = new RefundRequest() {
                @Override
                public String amount() {
                    return "10.50";
                }

                @Override
                public String currency() {
                    return "XAF";
                }

                @Override
                public String externalId() {
                    return "11111111-2222-4333-8444-555555555555";
                }

                @Override
                public String originalTransactionId() {
                    return DEPOSIT_ID;
                }

                @Override
                public String reference() {
                    return "REF-1";
                }
            };

            PawapayRefundRequest request = PawapayRefundRequest.from(generic);

            assertThat(request.getRefundId()).isEqualTo("11111111-2222-4333-8444-555555555555");
            assertThat(request.getDepositId()).isEqualTo(DEPOSIT_ID);
            assertThat(request.getAmount()).isEqualTo("10.5");
            assertThat(request.getCurrency()).isEqualTo("XAF");
            assertThat(request.getClientReferenceId()).isEqualTo("REF-1");
            assertThat(PawapayRefundRequest.from(request)).isSameAs(request);
        }

        @Test
        @DisplayName("Should require and validate deposit id")
        void shouldRequireDepositId() {
            assertThatThrownBy(() -> PawapayRefundRequest.builder().amount("10").currency("XAF").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Deposit ID is required");
            assertThatThrownBy(() -> PawapayRefundRequest.builder().depositId("nope").amount("10").currency("XAF").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("depositId");
        }
    }

    @Nested
    @DisplayName("Checkout request")
    class CheckoutRequestTests {

        @Test
        @DisplayName("Should require allowCustomerToOverride on any payer and validate phone/provider when present")
        void shouldValidatePayer() {
            var payer = PawapayCheckoutPayer.mmo("260763456789", "MTN_MOMO_ZMB", true);
            assertThat(payer.allowCustomerToOverride()).isTrue();
            assertThat(payer.phoneNumber()).isEqualTo("260763456789");

            assertThatThrownBy(() -> PawapayCheckoutRequest.builder()
                .returnUrl("https://merchant.example.com/r")
                .payer("+260763456789", "MTN_MOMO_ZMB", true)
                .build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("phone number");
        }

        @Test
        @DisplayName("Should generate a checkout id and omit unset optional fields")
        void shouldGenerateIdAndOmitOptionalFields() {
            PawapayCheckoutRequest request = PawapayCheckoutRequest.builder()
                .returnUrl("https://merchant.example.com/checkout-result")
                .build();

            assertThat(request.getCheckoutId()).isNotBlank();
            assertThat(java.util.UUID.fromString(request.getCheckoutId())).isNotNull();
            assertThat(request.getCountries()).isEmpty();
            assertThat(request.getAmounts()).isEmpty();
            assertThat(request.getReason()).isEmpty();
            assertThat(request.getPayer()).isNull();
        }
    }

    @Nested
    @DisplayName("Payment page request")
    class PaymentPageRequestTests {

        @Test
        @DisplayName("Should require country when fixing an amount and validate the reason length")
        void shouldValidateConstraints() {
            assertThatThrownBy(() -> PawapayPaymentPageRequest.builder()
                .returnUrl("https://merchant.com/r").amount("100", "GHS").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Country is required");
            assertThatThrownBy(() -> PawapayPaymentPageRequest.builder()
                .returnUrl("https://merchant.com/r").reason("x".repeat(51)).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("50");
            assertThatThrownBy(() -> PawapayPaymentPageRequest.builder().build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Return URL is required");
        }

        @Test
        @DisplayName("Should allow fixing only the phone number, without an amount or country")
        void shouldAllowFixedPhoneNumberOnly() {
            PawapayPaymentPageRequest request = PawapayPaymentPageRequest.builder()
                .returnUrl("https://merchant.com/returnUrl")
                .phoneNumber("233593456789")
                .build();

            assertThat(request.getPhoneNumber()).isEqualTo("233593456789");
            assertThat(request.getAmountDetails()).isNull();
            assertThat(request.getCountry()).isNull();
            assertThat(java.util.UUID.fromString(request.getDepositId())).isNotNull();
        }
    }

    @Nested
    @DisplayName("Remittance request")
    class RemittanceRequestTests {

        private PawapayRemittanceSender.TransactionDetails transaction() {
            return new PawapayRemittanceSender.TransactionDetails("TX-1", "100", "USD", "23.88", "1",
                PawapayPurposeOfFunds.FAMILY_SUPPORT, PawapaySourceOfFunds.SALARY);
        }

        private PawapayRemittanceSender.SenderDetails.Builder senderDetails() {
            return PawapayRemittanceSender.SenderDetails.builder()
                .firstName("Jane").lastName("Doe").nationality("USA").phoneNumber("12124567890")
                .address("1476 Sandhill Rd", "84058", "Orem", "USA")
                .identification(PawapayIdentificationType.PASSPORT, "E00007730");
        }

        private PawapayRemittanceRequest.Builder valid() {
            return PawapayRemittanceRequest.builder()
                .amount("15").currency("ZMW")
                .phoneNumber("260763456789").provider(PawapayProviders.MTN_MOMO_ZMB)
                .recipientName("John", "Doe")
                .sender(transaction(), senderDetails().build());
        }

        @Test
        @DisplayName("Should serialize optional sender fields and the PawaPay spelling of BROTHER_IN_LAW")
        void shouldSerializeSender() {
            PawapayRemittanceRequest request = valid()
                .sender(transaction(), senderDetails()
                    .gender(PawapayGender.FEMALE)
                    .dateOfBirth(LocalDate.of(1977, 12, 31))
                    .placeOfBirth("USA").occupation("Project manager")
                    .relationshipRecipient(PawapayRelationship.BROTHER_IN_LAW)
                    .build())
                .build();

            JsonNode json = parse(PawapayJson.write(request));

            assertThat(UUID.fromString(json.get("remittanceId").asText())).isNotNull();
            assertThat(json.at("/sender/senderDetails/gender").asText()).isEqualTo("FEMALE");
            assertThat(json.at("/sender/senderDetails/dateOfBirth").asText()).isEqualTo("1977-12-31");
            assertThat(json.at("/sender/senderDetails/relationshipRecipient").asText()).isEqualTo("BORTHER_IN_LAW");
            assertThat(json.has("clientReferenceId")).isFalse();
            assertThat(request.reference()).isEqualTo("TX-1");
            assertThat(request.recipientPhoneNumber()).isEqualTo("260763456789");
            assertThat(PawapayRelationship.fromValue("BORTHER_IN_LAW")).isEqualTo(PawapayRelationship.BROTHER_IN_LAW);
            assertThat(PawapayRelationship.fromValue("BROTHER_IN_LAW")).isEqualTo(PawapayRelationship.BROTHER_IN_LAW);
            assertThat(PawapayRelationship.fromValue("nope")).isEqualTo(PawapayRelationship.UNKNOWN);
        }

        @Test
        @DisplayName("Should require sender, recipient name and validate KYC fields")
        void shouldValidateKycFields() {
            assertThatThrownBy(() -> PawapayRemittanceRequest.builder().amount("15").currency("ZMW")
                .phoneNumber("260763456789").provider("MTN_MOMO_ZMB").recipientName("John", "Doe").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Sender is required");
            assertThatThrownBy(() -> PawapayRemittanceRequest.builder().amount("15").currency("ZMW")
                .phoneNumber("260763456789").provider("MTN_MOMO_ZMB").sender(transaction(), senderDetails().build()).build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Recipient first name and last name");
            assertThatThrownBy(() -> valid().sender(transaction(), senderDetails().nationality("US").build()).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("country");
            assertThatThrownBy(() -> valid().sender(transaction(), senderDetails().dateOfBirth("31/12/1977").build()).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("date of birth");
            assertThatThrownBy(() -> valid().sender(transaction(), senderDetails().gender(PawapayGender.UNKNOWN).build()).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Gender UNKNOWN");
            assertThatThrownBy(() -> valid().sender(new PawapayRemittanceSender.TransactionDetails("TX-1", "abc", "USD", "1", "1",
                    PawapayPurposeOfFunds.OTHER, PawapaySourceOfFunds.OTHER), senderDetails().build()).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("original amount");
            assertThatThrownBy(() -> valid().sender(new PawapayRemittanceSender.TransactionDetails("TX-1", "100", "USD", "1", "1",
                    PawapayPurposeOfFunds.UNKNOWN, PawapaySourceOfFunds.OTHER), senderDetails().build()).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Purpose of funds UNKNOWN");
            assertThatThrownBy(() -> valid().recipient(PawapayRemittanceRecipient.mmo("260763456789", "MTN_MOMO_ZMB", "x".repeat(65), "Doe")).build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at most 64");
            assertThatThrownBy(() -> PawapayRemittanceSender.SenderDetails.builder().firstName("Jane").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("last name is required");
        }
    }

    @Nested
    @DisplayName("Statement request")
    class StatementRequestTests {

        @Test
        @DisplayName("Should format dates as UTC zone-less timestamps and serialize the PawaPay shape")
        void shouldSerializeToPawapayJson() {
            PawapayStatementRequest request = PawapayStatementRequest.builder()
                .wallet(PawapayWallet.of("ZMB", "ZMW"))
                .callbackUrl("https://merchant.com/statementCallbacks")
                .startDate(Instant.parse("2025-05-10T10:00:00Z"))
                .endDate(LocalDateTime.of(2025, 5, 11, 10, 0, 0, 123_000_000))
                .build();

            JsonNode json = parse(PawapayJson.write(request));

            assertThat(json.get("startDate").asText()).isEqualTo("2025-05-10T10:00:00");
            assertThat(json.get("endDate").asText()).isEqualTo("2025-05-11T10:00:00");
            assertThat(json.at("/wallet/country").asText()).isEqualTo("ZMB");
            assertThat(json.at("/wallet").has("provider")).isFalse();
            assertThat(json.has("compressed")).isFalse();
            assertThat(request.getCompressed()).isNull();
        }

        @Test
        @DisplayName("Should enforce PawaPay statement constraints locally")
        void shouldEnforceConstraints() {
            assertThatThrownBy(() -> valid().callbackUrl("http://merchant.com/cb").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("HTTPS");
            assertThatThrownBy(() -> valid().callbackUrl("not a url").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Callback URL");
            assertThatThrownBy(() -> valid().endDate("2025-05-01T10:00:00").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("after start date");
            assertThatThrownBy(() -> valid().endDate("2025-06-11T10:00:01").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("31 days");
            assertThatThrownBy(() -> valid().startDate("yesterday").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid start date");
            assertThatThrownBy(() -> valid().country("Zambia").build())
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("country");
            assertThatThrownBy(() -> PawapayStatementRequest.builder().currency("ZMW").callbackUrl("https://m.com").startDate("2025-05-10T10:00:00").endDate("2025-05-11T10:00:00").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("country is required");
            assertThatThrownBy(() -> valid().callbackUrl((String) null).build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Callback URL is required");

            // exactly 31 days is accepted
            assertThat(valid().endDate("2025-06-10T10:00:00").build().getEndDate()).isEqualTo("2025-06-10T10:00:00");
        }

        private PawapayStatementRequest.Builder valid() {
            return PawapayStatementRequest.builder()
                .country("ZMB").currency("ZMW")
                .callbackUrl("https://merchant.com/statementCallbacks")
                .startDate("2025-05-10T10:00:00")
                .endDate("2025-05-11T10:00:00");
        }
    }

    @Nested
    @DisplayName("Common models")
    class CommonModelTests {

        @Test
        @DisplayName("Should parse PawaPay timestamps with or without zone")
        void shouldParseDates() {
            assertThat(PawapayDates.parseInstant("2020-02-21T17:32:29Z")).contains(Instant.parse("2020-02-21T17:32:29Z"));
            assertThat(PawapayDates.parseInstant("2020-02-21T17:32:29+02:00")).contains(Instant.parse("2020-02-21T15:32:29Z"));
            assertThat(PawapayDates.parseInstant("2025-05-10T10:00:00")).contains(Instant.parse("2025-05-10T10:00:00Z"));
            assertThat(PawapayDates.parseInstant("garbage")).isEmpty();
            assertThat(PawapayDates.parseInstant(null)).isEmpty();
            assertThat(PawapayDates.parseUtcLocalDateTime("2020-02-21T17:32:29+02:00")).contains(LocalDateTime.of(2020, 2, 21, 15, 32, 29));
            assertThat(PawapayDates.formatUtcLocalDateTime(Instant.parse("2025-05-10T10:00:00.500Z"))).isEqualTo("2025-05-10T10:00:00");
        }

        @Test
        @DisplayName("Should map PawaPay statuses to generic statuses")
        void shouldMapStatuses() {
            assertThat(PawapayTransactionStatus.COMPLETED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.SUCCESSFUL);
            assertThat(PawapayTransactionStatus.FAILED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.FAILED);
            assertThat(PawapayTransactionStatus.ACCEPTED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(PawapayTransactionStatus.ENQUEUED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(PawapayTransactionStatus.PROCESSING.toMoMoStatus()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(PawapayTransactionStatus.IN_RECONCILIATION.toMoMoStatus()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(PawapayTransactionStatus.UNKNOWN.toMoMoStatus()).isEqualTo(MoMoTransferStatus.UNKNOW);
            assertThat(PawapayTransactionStatus.COMPLETED.isFinal()).isTrue();
            assertThat(PawapayTransactionStatus.PROCESSING.isFinal()).isFalse();

            assertThat(PawapayInitiationStatus.ACCEPTED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(PawapayInitiationStatus.DUPLICATE_IGNORED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.PENDING);
            assertThat(PawapayInitiationStatus.REJECTED.toMoMoStatus()).isEqualTo(MoMoTransferStatus.FAILED);
        }

        @Test
        @DisplayName("Should parse enums leniently")
        void shouldParseEnumsLeniently() {
            assertThat(PawapayTransactionStatus.fromValue("completed")).isEqualTo(PawapayTransactionStatus.COMPLETED);
            assertThat(PawapayTransactionStatus.fromValue(" FAILED ")).isEqualTo(PawapayTransactionStatus.FAILED);
            assertThat(PawapayTransactionStatus.fromValue("SOMETHING_NEW")).isEqualTo(PawapayTransactionStatus.UNKNOWN);
            assertThat(PawapayTransactionStatus.fromValue(null)).isEqualTo(PawapayTransactionStatus.UNKNOWN);
            assertThat(PawapayInitiationStatus.fromValue("nope")).isEqualTo(PawapayInitiationStatus.UNKNOWN);
            assertThat(PawapayActionStatus.fromValue("REJECTED")).isEqualTo(PawapayActionStatus.REJECTED);
            assertThat(PawapayProviderStatus.fromValue("DELAYED")).isEqualTo(PawapayProviderStatus.DELAYED);
            assertThat(PawapayOperationType.fromValue("PAYOUT")).isEqualTo(PawapayOperationType.PAYOUT);
        }

        @Test
        @DisplayName("Should describe failure reasons")
        void shouldDescribeFailureReasons() {
            assertThat(new PawapayFailureReason("INVALID_AMOUNT", "Too many decimals").describe()).isEqualTo("INVALID_AMOUNT: Too many decimals");
            assertThat(new PawapayFailureReason("INVALID_AMOUNT", null).describe()).isEqualTo("INVALID_AMOUNT");
            assertThat(new PawapayFailureReason(null, "Too many decimals").describe()).isEqualTo("Too many decimals");
            assertThat(new PawapayFailureReason(null, null).describe()).isNull();
        }

        @Test
        @DisplayName("Should build metadata entries and reject reserved keys")
        void shouldBuildMetadata() {
            assertThat(PawapayMetadata.of("orderId", "1").toJson()).containsExactly(java.util.Map.entry("orderId", "1"));
            assertThat(PawapayMetadata.pii("email", "a@b.c").isPii()).isTrue();
            assertThat(PawapayMetadata.of("orderId", "1").isPii()).isFalse();
            assertThatThrownBy(() -> PawapayMetadata.of("isPII", true)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> PawapayMetadata.of(" ", "x")).isInstanceOf(IllegalArgumentException.class);
            assertThat(PawapayJson.write(List.of(PawapayMetadata.of("a", "b")))).isEqualTo("[{\"a\":\"b\"}]");
        }

        @Test
        @DisplayName("Should extract the country from a provider code")
        void shouldExtractCountry() {
            assertThat(PawapayProviders.countryOf(PawapayProviders.MTN_MOMO_COG)).isEqualTo("COG");
            assertThat(PawapayProviders.countryOf("AIRTEL_OAPI_ZMB")).isEqualTo("ZMB");
            assertThat(PawapayProviders.countryOf("WEIRD")).isNull();
            assertThat(PawapayProviders.countryOf(null)).isNull();
        }
    }
}
