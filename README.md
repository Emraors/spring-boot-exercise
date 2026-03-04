# Payment Service — Spring Boot Exercise

A deliberately small application used to practise every layer of the Spring Boot testing pyramid, from pure unit tests all the way to full `@SpringBootTest` integration tests with real infrastructure.
Note that this is JUST AN EXERCISE, the domain is purely fictional and was mostly vibe-coded.
---

## What it does

The service exposes two REST endpoints:

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/payments` | Create a payment, call an external provider, persist the result |
| `GET` | `/payments/{id}` | Retrieve a payment by ID |

Domain rules:
- **Amount must be > 0**
- **Idempotency** — sending the same `idempotencyKey` twice always returns the same payment
- A payment starts as `CREATED`, then moves to `AUTHORIZED` or `DECLINED` based on the provider response

---

## Architecture — Ports & Adapters (Hexagonal)

The codebase is split into six Maven modules that enforce strict dependency boundaries:

```
┌─────────────────────────────────────────────────────────┐
│                     payment-app                         │  ← Spring Boot entry point, wires everything
└────────────────────────┬────────────────────────────────┘
                         │ depends on
        ┌────────────────┼─────────────────────┐
        ▼                ▼                     ▼
┌──────────────┐  ┌─────────────────┐  ┌──────────────────────────┐
│payment-      │  │payment-adapters-│  │payment-adapters-         │
│adapters-web  │  │persistence-jpa  │  │provider-http             │
│(REST in)     │  │(DB out)         │  │(external provider out)   │
└──────────────┘  └─────────────────┘  └──────────────────────────┘
        │                │                     │
        └────────────────┼─────────────────────┘
                         │ all depend on
                ┌────────┴────────┐
                │ payment-        │
                │ application     │  ← use cases + port interfaces (no Spring)
                └────────┬────────┘
                         │ depends on
                ┌────────┴────────┐
                │ payment-domain  │  ← entities, value objects, domain rules (plain Java)
                └─────────────────┘
```

### Modules

| Module | Role |
|--------|------|
| `payment-domain` | `Payment` aggregate, `PaymentId`, `Money`, `PaymentStatus` — zero dependencies |
| `payment-application` | `CreatePaymentUseCase`, `GetPaymentUseCase`, port interfaces (`PaymentRepository`, `PaymentProvider`, `PaymentEvents`) — no Spring |
| `payment-adapters-web` | `PaymentController`, DTOs, Bean Validation, exception mapping |
| `payment-adapters-persistence-jpa` | JPA entities, Spring Data repositories, Flyway migrations |
| `payment-adapters-provider-http` | `RestTemplate`-based HTTP client to the external payment provider |
| `payment-app` | `@SpringBootApplication`, configuration, wires all adapters |

---

## Testing strategy

The project covers every tier of the testing pyramid:

### 1 · Unit tests — `payment-domain`, `payment-application`
No Spring context at all. Validate domain invariants and use-case behaviour using plain Mockito mocks/fakes.

```
payment-domain      → PaymentTest                    (10 tests)
payment-application → CreatePaymentServiceTest        (4 tests)
                    → GetPaymentServiceTest            (2 tests)
```

### 2 · Minimal-context Spring tests — `payment-application`
`@ContextConfiguration` with only the beans under test. Learn exactly which features need a Spring proxy.

```
→ CreatePaymentValidationContextTest  – @Validated method validation  (2 tests)
→ GetPaymentCachingContextTest        – @Cacheable behaviour           (1 test)
```

### 3 · Slice tests — `payment-adapters-web`, `payment-adapters-persistence-jpa`
Only the slice of the context needed for each adapter.

```
@WebMvcTest  → PaymentControllerTest          – validation 400, JSON shape, error mapping  (5 tests)
@DataJpaTest → PaymentRepositoryAdapterTest   – save/find, status update, idempotency      (5 tests)
```

WireMock stubs the external provider HTTP calls:

```
payment-adapters-provider-http → PaymentProviderAdapterTest  (2 tests)
```

### 4 · Integration tests — `payment-app`
Full `@SpringBootTest` with a real Postgres database via **Testcontainers** and a stubbed provider via **WireMock**.

```
→ PaymentIntegrationTest  (5 tests)
   ✔ POST /payments → provider approved → status AUTHORIZED
   ✔ POST /payments → provider declined → status DECLINED
   ✔ GET  /payments/{id} → returns correct payment
   ✔ GET  /payments/{unknown-id} → 404
   ✔ Same idempotencyKey twice → same payment returned
```

---

## CI/CD

Three GitHub Actions workflows live in `.github/workflows/`:

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| `ci.yml` | push / PR to `main`, `master`, `develop` | Unit & slice tests → Integration tests (Testcontainers) → Build & upload fat jar |
| `release.yml` | push of a `v*.*.*` tag | Full verify → Docker image pushed to `ghcr.io` → GitHub Release created with jar attached |
| `dependency-review.yml` | PR to `main`, `master`, `develop` | CVE scan via GitHub Advisory Database, fails on HIGH/CRITICAL |

### Running locally

```bash
# Unit & slice tests only (fast, no Docker)
mvn test -pl payment-domain,payment-application,payment-adapters-web,payment-adapters-persistence-jpa,payment-adapters-provider-http -am

# Full build including integration tests (requires Docker)
mvn verify -pl payment-app -am
```

### Creating a release

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## Tech stack

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 3.2 |
| Persistence | Spring Data JPA + Hibernate + Flyway |
| Database | PostgreSQL (runtime) / H2 (slice tests) |
| HTTP client | RestTemplate |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| Unit testing | JUnit 5 + Mockito |
| Slice testing | `@WebMvcTest`, `@DataJpaTest` |
| Integration testing | `@SpringBootTest` + Testcontainers + WireMock |
| Build | Maven multi-module |
| Container | Docker (multi-stage, layered Spring Boot image) |
| CI/CD | GitHub Actions |
</content>
</invoke>
