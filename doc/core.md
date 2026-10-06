# Core

`paymux-java-core` defines the contracts every provider module implements. `paymux-java-http-client`
gives them a default HTTP implementation. Neither module talks to a real provider on its own -
use [MTN Congo](mtn-congo.md) or [PawaPay](pawapay.md) for that.

## Client and configuration

Every provider client implements `MobileMoneyClient<T extends MobileMoneyConfig>`:

- `getConfig()` - the client's configuration
- `getHttpClient()` - the underlying `PaymuxHttpClient`
- `close()` - releases resources (clients are `AutoCloseable`)

`MobileMoneyConfig` holds the base URL, credentials, timeouts and an optional custom
`PaymuxHttpClient`. Each provider module extends it with its own fields (see its own doc page)
and can load it from classpath properties or YAML through `ConfigurationLoader`.

## Operations

Providers implement the operation interfaces that match what they actually offer:

| Interface | Represents |
|---|---|
| `TransferOperation` | Request to pay / collection - the customer pays the merchant |
| `CashinOperation` | Deposit into a mobile money account |
| `CashoutOperation` | Payout - the merchant pays a customer |
| `RefundOperation` | Refund of a completed transfer |

Each operation has a request type, a response type, and a matching exception
(`TransferException`, `CashinException`, `CashoutException`, `RefundException`), all extending
`PaymuxException`. Responses expose a `MoMoTransferStatus` (`PENDING`, `SUCCESSFUL`, `FAILED`,
`UNKNOW`) so application code can branch on outcome without knowing which provider answered.

## HTTP abstraction

`PaymuxHttpClient` is the interface providers call against; `PaymuxHttpRequest` /
`PaymuxHttpResponse` are its request and response types. `paymux-java-http-client` ships
`DefaultPaymuxHttpClient`, a `java.net.http`-based implementation with configurable connect and
request timeouts. Pass your own `PaymuxHttpClient` through a provider's config if you need a
different transport.

```java
var httpClient = DefaultPaymuxHttpClient.builder()
    .connectTimeout(5000)
    .requestTimeout(30000)
    .build();
```

## Configuration loading

`ConfigurationLoader` reads `paymux.yml` / `paymux.properties` (or `application.yml` /
`application.properties`) from the classpath, and resolves `${ENV_VAR}` / `${ENV_VAR:default}`
placeholders in values. Each provider config exposes `fromProperties()` and
`fromPropertiesFile(String)` built on top of it - see the provider pages for the exact keys.
