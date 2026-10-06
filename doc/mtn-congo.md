# MTN Congo

`paymux-java-mtn-congo` is a direct client for MTN Mobile Money Congo-Brazzaville's Collection
API. It implements `TransferOperation`: the customer receives a USSD prompt and approves the
payment on their phone.

## Configuration

Settings are read from classpath properties or YAML, prefixed with `paymux.mtn.congo.`:

- `api-user`, `api-key`, `subscription-key` (required)
- `environment` - defaults to `mtncongo` in production, `sandbox` otherwise
- `production` - `true`/`false`, selects the base URL
- `base-url` - override, rarely needed
- `callback-url` - your webhook for transaction notifications
- `connection-timeout`, `request-timeout` (milliseconds)

```yaml
paymux:
  mtn:
    congo:
      api-user: ${CG_MOMO_API_USER}
      api-key: ${CG_MOMO_API_KEY}
      subscription-key: ${CG_MOMO_SUBSCRIPTION_KEY}
      environment: mtncongo
      production: false
```

```properties
paymux.mtn.congo.api-user=${CG_MOMO_API_USER}
paymux.mtn.congo.api-key=${CG_MOMO_API_KEY}
paymux.mtn.congo.subscription-key=${CG_MOMO_SUBSCRIPTION_KEY}
paymux.mtn.congo.environment=mtncongo
paymux.mtn.congo.production=false
```

## Usage

```java
MtnCongoConfig config = MtnCongoConfig.fromProperties();
// or
MtnCongoConfig config = MtnCongoConfig.fromPropertiesFile("paymux.yml");
```

```java
import com.rickenbazolo.paymux.core.enums.MoMoCurrency;
import com.rickenbazolo.paymux.core.operations.transfer.TransferResponse;
import com.rickenbazolo.paymux.mtn.congo.MtnCongoClient;
import com.rickenbazolo.paymux.mtn.congo.MtnCongoConfig;
import com.rickenbazolo.paymux.mtn.congo.collection.model.MtnRequestToPay;

import java.util.UUID;

MtnCongoConfig config = MtnCongoConfig.fromPropertiesFile("paymux.yml");

try (MtnCongoClient client = new MtnCongoClient(config)) {
    MtnRequestToPay request = MtnRequestToPay.builder()
        .amount("1000")
        .currency(MoMoCurrency.XAF.getValue())
        .externalId(UUID.randomUUID().toString())
        .payerPhone("242065551234")
        .payerMessage("Payment for order #123")
        .payeeNote("Order #123")
        .build();

    TransferResponse response = client.transfer(request);
    var status = client.getTransferStatus(response.transactionId());
}
```

`client.transfer(request)` returns immediately with a `PENDING` status; the customer still has to
approve the prompt. Poll `getTransferStatus` or use your configured `callback-url` to learn the
final outcome.
