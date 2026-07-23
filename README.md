# paymux-java — Java SDK for Mobile Money APIs

[![Maven Central](https://img.shields.io/maven-central/v/com.rickenbazolo/paymux-java-bom.svg)](https://central.sonatype.com/artifact/com.rickenbazolo/paymux-java-bom)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Paymux-java** is a unified Java SDK that simplifies the integration of Mobile Money APIs into your applications.
Build once, integrate multiple operators seamlessly.

## Why Paymux-java?

- **Modular Architecture**: Add only the operators you need
- **Pluggable HTTP Client**: Use java.net.http (default) or bring your own
- **Unified API**: Consistent interface across all operators
- **Type-Safe**: Leverage Java's type system for safer code
- **Zero Dependencies**: No Spring, Jakarta EE, or framework required
- **Production Ready**: Support for both sandbox and production environments
- **Async Support**: Synchronous and asynchronous operations

## Supported Operators

### Republic of Congo (Congo-Brazzaville)
- **MTN Mobile Money** (`paymux-java-mtn-congo`) — Active development

### Coming Soon
- Airtel Money Congo
- Orange Money RDC
- Other African operators

## Installation

### Using BOM (recommended)

**Maven:**
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
  <!-- Pick the operator module(s) you need -->
  <dependency>
    <groupId>com.rickenbazolo</groupId>
    <artifactId>paymux-java-mtn-congo</artifactId>
  </dependency>
</dependencies>
```

**Gradle:**
```groovy
implementation platform('com.rickenbazolo:paymux-java-bom:VERSION')
implementation 'com.rickenbazolo:paymux-java-mtn-congo'
```

## Configuration

Create a `paymux.yml` (or `paymux.properties`) on your classpath:

```yaml
paymux:
  mtn:
    congo:
      api-user: ${MTN_API_USER}
      api-key: ${MTN_API_KEY}
      subscription-key: ${MTN_SUBSCRIPTION_KEY}
      environment: mtncongo
      production: false
      connection-timeout: 30000
      request-timeout: 60000
```

Or using properties format:

```properties
paymux.mtn.congo.api-user=${MTN_API_USER}
paymux.mtn.congo.api-key=${MTN_API_KEY}
paymux.mtn.congo.subscription-key=${MTN_SUBSCRIPTION_KEY}
paymux.mtn.congo.environment=mtncongo
paymux.mtn.congo.production=false
```

## Usage

### MTN Mobile Money — Congo-Brazzaville

```java
// Load config from classpath (paymux.yml or paymux.properties)
MtnCongoConfig config = MtnCongoConfig.fromClasspath();

// Or from a specific file
MtnCongoConfig config = MtnCongoConfig.fromPropertiesFile("paymux.yml");

// Create the client
MtnCongoClient client = new MtnCongoClient(config);

// Request to Pay (Cash-in)
CashinRequest request = CashinRequest.builder()
    .amount("1000")
    .currency(MoMoCurrency.XAF)
    .phoneNumber("242XXXXXXXX")
    .payerMessage("Payment for order #123")
    .payeeNote("Order #123")
    .build();

CashinResponse response = client.cashin(request);
```

## Module Structure

| Module | Description |
|---|---|
| `paymux-java-bom` | Bill of Materials — version management |
| `paymux-java-core` | Core interfaces and abstractions |
| `paymux-java-http-client` | Default HTTP client (`java.net.http`) |
| `paymux-java-mtn-congo` | MTN Mobile Money — Congo-Brazzaville |

## License

[MIT License](LICENSE) © Ricken Bazolo
