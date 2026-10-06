# PawaPay

`paymux-java-pawapay` is a client for the PawaPay Merchant API v2
(`https://api.sandbox.pawapay.io/` or `https://api.pawapay.io/`), a single bearer token giving
access to many Mobile Money providers across many African countries. The provider (`MTN_MOMO_COG`,
`AIRTEL_COG`, ...) is chosen per transaction, not per client.

| PawaPay operation | Core abstraction | Client methods |
|---|---|---|
| Deposit (customer pays the merchant) | `TransferOperation` | `transfer`, `getTransferStatus`, `initiateDeposit`, `findDeposit`, `resendDepositCallback` |
| Payout (merchant pays a customer) | `CashoutOperation` | `cashout`, `getCashoutStatus`, `initiatePayout`, `initiateBulkPayouts`, `findPayout`, `resendPayoutCallback`, `cancelEnqueuedPayout` |
| Remittance (international transfer with sender KYC) | `CashoutOperation` (`cashout` accepts `PawapayRemittanceRequest`) | `initiateRemittance`, `findRemittance`, `resendRemittanceCallback`, `cancelEnqueuedRemittance` |
| Refund of a completed deposit | `RefundOperation` | `refund`, `getRefundStatus`, `initiateRefund`, `findRefund`, `resendRefundCallback`, `cancelEnqueuedRefund` |
| Checkout (hosted, redirect-based payment page with its own lifecycle and retries) | - | `initiateCheckout`, `findCheckout`, `expireCheckout` |
| Payment page (simpler, fixed 15-minute hosted deposit session) | - | `initiatePaymentPage` (tracked as a regular deposit) |
| Toolkit | - | `getActiveConfiguration`, `getAvailability`, `predictProvider`, `getPublicKeys` |
| Finances (wallet balances, statements) | - | `getWalletBalances`, `initiateStatement`, `findStatement` |

Reconciliation, retries and persistence stay in your application: the library only gives typed
access to the PawaPay API.

## Configuration

Settings are read from classpath properties or YAML, prefixed with `paymux.pawapay.`:

- `api-token` (required)
- `production` - `true`/`false`, defaults to `false`
- `base-url` - override, rarely needed
- `connection-timeout`, `request-timeout` (milliseconds)
- `public-key-cache-ttl`, `signature-clock-skew` (seconds, for callback signature verification)

Callback URLs are configured per merchant account in the PawaPay Dashboard, not in the library
(the exception is statements, whose callback URL is passed per request - see below).

```yaml
paymux:
  pawapay:
    api-token: ${PAWAPAY_API_TOKEN}
    production: false
```

## Deposits, payouts and refunds

```java
import com.rickenbazolo.paymux.pawapay.PawapayClient;
import com.rickenbazolo.paymux.pawapay.PawapayConfig;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositRequest;
import com.rickenbazolo.paymux.pawapay.deposit.model.PawapayDepositResponse;
import com.rickenbazolo.paymux.pawapay.model.PawapayProviders;
import com.rickenbazolo.paymux.pawapay.payout.model.PawapayPayoutRequest;
import com.rickenbazolo.paymux.pawapay.refund.model.PawapayRefundRequest;

PawapayConfig config = PawapayConfig.fromPropertiesFile("paymux.yml");

try (PawapayClient client = new PawapayClient(config)) {
    // Deposit: the customer receives a payment prompt
    PawapayDepositResponse deposit = client.initiateDeposit(PawapayDepositRequest.builder()
        .amount("1000")
        .currency("XAF")
        .phoneNumber("242065551234")
        .provider(PawapayProviders.MTN_MOMO_COG)
        .customerMessage("Order 12345")
        .clientReferenceId("ORDER-12345")
        .addMetadata("orderId", "12345")
        .build());

    if (deposit.isAccepted()) {
        // Final status comes through the callback, or by polling
        client.findDeposit(deposit.getDepositId())
            .ifPresent(d -> System.out.println(d.getStatus() + " " + d.getProviderTransactionId()));
    }

    // Payout
    var payout = client.initiatePayout(PawapayPayoutRequest.builder()
        .amount("15000")
        .currency("XAF")
        .phoneNumber("242065551234")
        .provider(PawapayProviders.MTN_MOMO_COG)
        .customerMessage("Salary 2024 01")
        .build());

    // Refund of a completed deposit
    var refund = client.initiateRefund(PawapayRefundRequest.builder()
        .depositId(deposit.getDepositId())
        .amount("1000")
        .currency("XAF")
        .build());
}
```

Initiations answer `ACCEPTED`, `REJECTED` (returned, not thrown, with a `failureReason`) or
`DUPLICATE_IGNORED`. A non-2xx HTTP answer raises `PawapayApiException` (with the PawaPay
`failureCode`); an unknown id on a status lookup raises `PawapayNotFoundException`. Through the
core interfaces these are wrapped in `TransferException`, `CashoutException` or `RefundException`
and available via `getCause()`.

## Remittances

```java
import com.rickenbazolo.paymux.pawapay.remittance.model.*;

var sender = new PawapayRemittanceSender(
    new PawapayRemittanceSender.TransactionDetails("TX-123", "100", "USD", "23.88", "1",
        PawapayPurposeOfFunds.FAMILY_SUPPORT, PawapaySourceOfFunds.SALARY),
    PawapayRemittanceSender.SenderDetails.builder()
        .firstName("Jane").lastName("Doe").nationality("USA").phoneNumber("12124567890")
        .address("1476 Sandhill Rd", "84058", "Orem", "USA")
        .identification(PawapayIdentificationType.PASSPORT, "E00007730")
        .relationshipRecipient(PawapayRelationship.PARTNER)
        .build());

var remittance = client.initiateRemittance(PawapayRemittanceRequest.builder()
    .amount("15000")
    .currency("XAF")
    .phoneNumber("242065551234")
    .provider(PawapayProviders.MTN_MOMO_COG)
    .recipientName("John", "Doe")
    .sender(sender)
    .build());
// Final status: remittance callback (PawapayCallbacks.parseRemittanceCallback) or client.findRemittance(id)
```

## Checkouts

A checkout forwards the customer to a hosted, redirect-based payment page and tracks the whole
payment - including any retries - under a single reference.

```java
import com.rickenbazolo.paymux.pawapay.checkout.model.PawapayCheckoutRequest;

var checkout = client.initiateCheckout(PawapayCheckoutRequest.builder()
    .returnUrl("https://merchant.example.com/checkout-result")
    .addAmount("COG", "XAF", "1000")
    .reason("en", "Order 12345")
    .clientReferenceId("ORDER-12345")
    .build());

if (checkout.isAccepted()) {
    // Redirect the customer to checkout.getRedirectUrl().
    // Store checkout.getCheckoutId() and checkout.getCheckoutCode(): the code is appended to
    // returnUrl as a query parameter when the customer comes back, and identifies the checkout.
}

// Final status: checkout callback (PawapayCallbacks.parseCheckoutCallback) or client.findCheckout(id)
var status = client.findCheckout(checkout.getCheckoutId());
```

## Payment page

A simpler, single-attempt alternative to a checkout: a fixed 15-minute hosted session with no
retries and no lifecycle of its own. Once the customer presses "Pay", a regular deposit is
registered under the `depositId` you supplied - track it like any other deposit.

```java
import com.rickenbazolo.paymux.pawapay.paymentpage.model.PawapayPaymentPageRequest;

var page = client.initiatePaymentPage(PawapayPaymentPageRequest.builder()
    .returnUrl("https://merchant.com/returnUrl")
    .amount("100", "XAF")
    .country("COG")
    .reason("Demo payment")
    .build());

if (page.isAccepted()) {
    // Redirect the customer to page.getRedirectUrl() (valid for 15 minutes)
}
```

## Finances

```java
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayStatementRequest;
import com.rickenbazolo.paymux.pawapay.finance.model.PawapayWallet;

var balances = client.getWalletBalances("COG");

// Statement generation is asynchronous: PawaPay posts the download link to callbackUrl
var statement = client.initiateStatement(PawapayStatementRequest.builder()
    .wallet(PawapayWallet.of("COG", "XAF"))
    .callbackUrl("https://merchant.com/callbacks/pawapay/statements")
    .startDate(LocalDateTime.of(2025, 5, 1, 0, 0))
    .endDate(LocalDateTime.of(2025, 5, 31, 23, 59, 59))   // at most 31 days
    .compressed(true)
    .build());

client.findStatement(statement.statementId())
    .filter(s -> s.isCompleted())
    .ifPresent(s -> System.out.println(s.downloadUrl() + " until " + s.downloadUrlExpiresAt()));
// In the statement callback endpoint: PawapayCallbacks.parseStatementCallback(body)
```

## Callbacks

```java
import com.rickenbazolo.paymux.pawapay.callback.PawapayCallbackRequest;
import com.rickenbazolo.paymux.pawapay.callback.PawapayCallbacks;

// In your HTTP endpoint (any framework): verify the signature, then parse
byte[] body = request.getInputStream().readAllBytes();
var callback = PawapayCallbackRequest.builder()
    .method(request.getMethod())
    .authority(request.getHeader("Host"))
    .path(request.getRequestURI())
    .headers(allHeadersOf(request))
    .body(body)
    .build();

client.callbackSignatureVerifier().verify(callback); // throws PawapaySignatureException
var deposit = PawapayCallbacks.parseDepositCallback(body);
```

Each payment kind has a matching parser: `parseDepositCallback`, `parsePayoutCallback`,
`parseRefundCallback`, `parseRemittanceCallback`, `parseCheckoutCallback`,
`parseStatementCallback`. A payment page session has no callback of its own - it reuses
`parseDepositCallback`.

## Outbound request signing

A second layer of security on top of the API token: PawaPay can be configured to only accept
signed financial requests - `initiateDeposit`, `initiatePayout`, `initiateBulkPayouts` and
`initiateRefund` - so a leaked API token alone is not enough to move funds. This uses RFC 9421
HTTP Message Signatures, the same standard as callback signatures, but in the outbound direction.

The library only signs with a private key you supply; it never generates keys or touches the
PawaPay Dashboard. Before enabling this, in the PawaPay Dashboard under
**System configuration > API tokens > Signed requests**:

1. Generate a key pair (EC or RSA) yourself.
2. Upload the **public** key and give it a name - that name is `signing.key-id`.
3. Enable "Signed requests".

Then configure the **private** key on the client, toggled with `signing.enabled`:

```yaml
paymux:
  pawapay:
    api-token: ${PAWAPAY_API_TOKEN}
    signing:
      enabled: true
      key-id: CUSTOMER_TEST_KEY
      algorithm: ecdsa-p256-sha256
      private-key-path: ${PAWAPAY_SIGNING_KEY_PATH}
```

Supply exactly one private key source: `signing.private-key-path` (a file), `signing.private-key`
(inline PEM or base64 - literal `\n` sequences are unescaped, so it can live in a single-line
environment variable), or, programmatically, an already-loaded `java.security.PrivateKey`:

```java
PawapayConfig config = PawapayConfig.builder()
    .apiToken(System.getenv("PAWAPAY_API_TOKEN"))
    .requestSigningEnabled(true)
    .signingKeyId("CUSTOMER_TEST_KEY")
    .signingAlgorithm(PawapaySignatureAlgorithms.ECDSA_P256_SHA256)
    .signingPrivateKey(myPrivateKey) // or .signingPrivateKeyPath("/path/to/key.pem")
    .build();
```

Supported algorithms (`PawapaySignatureAlgorithms`): `ecdsa-p256-sha256`, `ecdsa-p384-sha384`,
`rsa-v1_5-sha256`, `rsa-pss-sha512`. The private key must be PKCS8 (`-----BEGIN PRIVATE KEY-----`);
convert a PKCS1/legacy key first with `openssl pkcs8 -topk8 -nocrypt`.

Signing is disabled by default and applies only to the four financial endpoints above; every
other call (status checks, resend callback, remittances, checkouts, payment page, finances,
toolkit) is unaffected whether signing is enabled or not. Never commit a private key to version
control - use a file path or an environment variable, as in the examples above.
