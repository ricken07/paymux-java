# Paymux Documentation

Paymux is a Java SDK for Mobile Money integrations. It gives every provider the same shape of
client - configuration, an operation, a typed response - so switching or adding a provider does
not mean rewriting your integration code.

This folder documents each module: what it does, how to configure it, and how to use it.

| Module | Documentation | What it is |
|---|---|---|
| `paymux-java-core` / `paymux-java-http-client` | [Core](core.md) | Shared interfaces, HTTP abstraction, configuration loading |
| `paymux-java-mtn-congo` | [MTN Congo](mtn-congo.md) | Direct MTN Mobile Money client for Congo-Brazzaville |
| `paymux-java-pawapay` | [PawaPay](pawapay.md) | Aggregator client: deposits, payouts, refunds, remittances, checkouts, payment pages, finances |

New to the project? Start with [Core](core.md), then open the page for the provider you need.
The top-level [README](../README.md) covers installation and project structure.
