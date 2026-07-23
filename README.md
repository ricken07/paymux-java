# Paymux Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.rickenbazolo/paymux-java-bom.svg)](https://central.sonatype.com/artifact/com.rickenbazolo/paymux-java-bom)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Paymux stands for Payment Multiplexer.

Paymux is a modular Java SDK for Mobile Money integrations.

The project is designed around one core idea: keep the API surface consistent while isolating provider-specific behavior in dedicated modules. Today, the repository ships the shared core abstractions, a default HTTP client, and a first provider implementation for MTN Mobile Money Congo-Brazzaville.

## Current Scope

- Shared core interfaces and abstractions
- Default HTTP client based on `java.net.http`
- MTN Mobile Money Congo-Brazzaville support
- Classpath-based configuration loading
- Synchronous and asynchronous HTTP support at the client layer

## Why This Project Exists

- Reduce the cost of integrating Mobile Money APIs
- Keep operator-specific logic out of application code
- Avoid framework lock-in
- Provide type-safe request and response models
- Let consumers use only the modules they need

## What Is Implemented Today

### Available modules

- `paymux-java-bom` - dependency management
- `paymux-java-core` - core interfaces, HTTP abstraction, shared models
- `paymux-java-http-client` - default `java.net.http` implementation
- `paymux-java-mtn-congo` - MTN Mobile Money Congo-Brazzaville client

### Supported provider

- MTN Mobile Money Congo-Brazzaville

### Planned providers

- Airtel Money Congo
- Orange Money RDC
- Other African operators

## Installation

Use the BOM to keep module versions aligned.

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.rickenbazolo</groupId>
      <artifactId>paymux-java-bom</artifactId>
      <version>VERSION</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.rickenbazolo</groupId>
    <artifactId>paymux-java-mtn-congo</artifactId>
  </dependency>
</dependencies>
```

### Gradle

```groovy
implementation platform('com.rickenbazolo:paymux-java-bom:VERSION')
implementation 'com.rickenbazolo:paymux-java-mtn-congo'
```

## Configuration

The MTN Congo module reads its settings from classpath properties or YAML.

### Supported keys

Prefix all MTN Congo settings with `paymux.mtn.congo.`

- `api-user`
- `api-key`
- `subscription-key`
- `environment`
- `production`
- `base-url`
- `callback-url`
- `connection-timeout`
- `request-timeout`

### Example `paymux.yml`

```yaml
paymux:
  mtn:
    congo:
      api-user: ${CG_MOMO_API_USER}
      api-key: ${CG_MOMO_API_KEY}
      subscription-key: ${CG_MOMO_SUBSCRIPTION_KEY}
      environment: mtncongo
      production: false
      connection-timeout: 30000
      request-timeout: 60000
```

### Example `paymux.properties`

```properties
paymux.mtn.congo.api-user=${CG_MOMO_API_USER}
paymux.mtn.congo.api-key=${CG_MOMO_API_KEY}
paymux.mtn.congo.subscription-key=${CG_MOMO_SUBSCRIPTION_KEY}
paymux.mtn.congo.environment=mtncongo
paymux.mtn.congo.production=false
paymux.mtn.congo.connection-timeout=30000
paymux.mtn.congo.request-timeout=60000
```

## Usage

### Load configuration

```java
MtnCongoConfig config = MtnCongoConfig.fromProperties();
// or
MtnCongoConfig config = MtnCongoConfig.fromPropertiesFile("paymux.yml");
```

### Create a transfer request

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

## Project Structure

| Module | Description |
|---|---|
| `paymux-java-bom` | Bill of Materials for version alignment |
| `paymux-java-core` | Core contracts, HTTP abstraction, and shared models |
| `paymux-java-http-client` | Default HTTP client implementation |
| `paymux-java-mtn-congo` | MTN Congo provider implementation |

## Design Principles

- Keep the public API small and consistent
- Push provider-specific logic into provider modules
- Keep the core usable without Spring or Jakarta EE
- Favor explicit configuration and type-safe models

## Roadmap

The repository currently focuses on MTN Congo. Additional operator modules will be added as the shared core stabilizes and provider integrations are implemented.

## License

[MIT License](LICENSE) © Ricken Bazolo
