# Paymux Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.rickenbazolo/paymux-java-bom.svg)](https://central.sonatype.com/artifact/com.rickenbazolo/paymux-java-bom)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Paymux (Payment Multiplexer) is a Java SDK for Mobile Money integrations. It gives every
provider - MTN, PawaPay, and whatever comes next - the same shape of client, so your application
code doesn't change when you add or swap a provider.

It exists to keep that provider-specific plumbing out of your codebase: no framework dependency,
type-safe requests and responses, and modules you pull in one at a time.

## Current Scope

- Core interfaces shared by every provider (see [Core](doc/core.md))
- MTN Mobile Money Congo-Brazzaville, direct integration (see [MTN Congo](doc/mtn-congo.md))
- PawaPay, one API token across many African providers (see [PawaPay](doc/pawapay.md))

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
  <!-- or -->
  <dependency>
    <groupId>com.rickenbazolo</groupId>
    <artifactId>paymux-java-pawapay</artifactId>
  </dependency>
</dependencies>
```

### Gradle

```groovy
implementation platform('com.rickenbazolo:paymux-java-bom:VERSION')
implementation 'com.rickenbazolo:paymux-java-mtn-congo'
// or
implementation 'com.rickenbazolo:paymux-java-pawapay'
```

## Documentation

Configuration, client usage and examples for each module live under [`doc/`](doc/index.md):

- [Core](doc/core.md) - shared interfaces, HTTP abstraction, configuration loading
- [MTN Congo](doc/mtn-congo.md) - direct MTN Mobile Money client
- [PawaPay](doc/pawapay.md) - deposits, payouts, refunds, remittances, checkouts, payment pages, finances

## Project Structure

| Module | Description |
|---|---|
| `paymux-java-bom` | Bill of Materials for version alignment |
| `paymux-java-core` | Core contracts, HTTP abstraction, and shared models |
| `paymux-java-http-client` | Default HTTP client implementation |
| `paymux-java-mtn-congo` | MTN Congo provider implementation |
| `paymux-java-pawapay` | PawaPay aggregator implementation |

## Design Principles

- Keep the public API small and consistent
- Push provider-specific logic into provider modules
- Keep the core usable without Spring or Jakarta EE
- Favor explicit configuration and type-safe models

## Roadmap

The repository currently ships MTN Congo and PawaPay. Additional operator modules will be added as the shared core stabilizes and provider integrations are implemented.

## License

[MIT License](LICENSE) © Ricken Bazolo
