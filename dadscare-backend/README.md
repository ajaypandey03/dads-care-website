# dadscare-backend

Spring Boot backend for the Dad's Care platform: receives lock/tamper telemetry pushed from Velosyss, relays lock/unlock commands to Velosyss's device API, runs the Rules & Alerts Engine (the false-positive fix), and serves the REST API consumed by `website/` (dashboard section), `dadscare-mobile/`, and `dadscare-chatbot-service/`.

**Phases 1 & 2 are built.** Stack: Java 21, Spring Boot 3.3.5, JPA, MySQL, Redis, JWT (JJWT 0.12.5), Flyway, Springdoc OpenAPI, Lombok + MapStruct — matching Velosyss's own conventions for team familiarity.

## What's built so far

**Phase 1 — foundation:**
- Core entities: `Organization` (tenant), `Site`, `ShutterUnit`, `Device`, `User`, `Role`, `UserSiteAccess`
- JWT auth (`POST /api/v1/auth/login`) with tenant claims; every tenant-scoped repository method takes an explicit `organizationId` — see `TenantContext` javadoc for why there's no global Hibernate filter
- Webhook receiver (`POST /api/v1/webhooks/velosyss/lock-events`) — one `ALARM`/`SEAL_STATE`/`COMMAND_RESULT` event per call, HMAC-SHA256 verified (`X-Velosyss-Signature`), de-duplicated by `eventId`, persists immutable `RawEvent` rows. See `com.dadscare.backend.velosyss` for the client-side pieces (`VelosyssCommandClient`, `VelosyssReadClient`, `VelosyssPollingService`) and `com.dadscare.backend.telemetry` for ingestion.
- `SequenceCounterService` — atomic, per-org sequential WhatsApp reference codes (e.g. `DC-000482`)
- `ProductMaster` / `TransporterMaster` CRUD

**Phase 2 — Rules & Alerts Engine:**
- `UnlockRequest` + `VelosyssCommandClient` (`POST /locks/{id}/commands`, `SEAL`/`UNSEAL`) — relays Dad's Care's `LOCK`/`UNLOCK` to Velosyss's real Device Command API. Async: the initial response only gives a `requestId` + in-flight status; the actual outcome lands later via the `COMMAND_RESULT` webhook (or `GET /unlock-requests/{id}` polling) — see `UnlockRequestStatus` for the full lifecycle (`QUEUED → DISPATCHED → {DEVICE_OFFLINE, RESPONDED, EXPIRED}`, plus our own `PENDING`/`FAILED`).
- `RulesEngineService` — the Authorized-Open Correlation fix: matches every `LOCK_OPEN`/`LOCK_CLOSE` telemetry event (derived from a `SEAL_STATE` webhook transition) against an `UnlockRequest` this backend itself relayed *and Velosyss confirmed succeeded*. Match → `CONFIRMED`. No match → scored via a per-device-tunable (`DeviceCalibration`) weight + quick-reclose heuristic → `UNEXPLAINED_HIGH` / `UNEXPLAINED_VERIFY` / `SUPPRESSED`. A Velosyss `ALARM` event always escalates straight to `UNEXPLAINED_HIGH`, unscored (`evaluateAlarm`).
- `VelosyssPollingService` — the reconciliation safety net the Integration Guide recommends: polls `GET /locks/positions` (device online/battery/live-position cache) and `GET /locks/events` (replayed through the same ingestion path as the webhook, safe via `eventId` dedup) — real-time state still comes from the webhook, this only catches what it might have missed.
- `Alert` (`GET /api/v1/alerts`) + `FeedbackEntry` (`POST /api/v1/alerts/{id}/feedback`) — the "was this correct?" loop
- `NotificationDispatcher` — sends the tabulated alert message (with sequence code) to every `ORG_ADMIN`/`SITE_MANAGER` with a phone number on file. Currently a logging stub per channel (`LoggingNotificationSender`) — swap in a real WhatsApp/SMS/push provider once one's chosen (Open Decisions in Confluence)

## Running locally

```bash
docker compose up -d          # MySQL on :3308, Redis on :6381 (non-default — see docker-compose.yml)
cp .env.example .env          # fill in JWT_SECRET (openssl rand -hex 64) and WEBHOOK_VELOSYSS_SECRET
mvn spring-boot:run
```

API docs: `http://localhost:8090/swagger-ui.html`. Flyway runs the schema automatically on startup (`src/main/resources/db/migration`).

```bash
mvn test    # unit tests: webhook signature verification, sequence-code formatting, auth, rules engine classification
```

## Docs

- [Dad's Care Platform Design](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — multi-tenant data model, auth, notifications, chatbot
- [Lock vs Shutter — False-Positive Problem & Proposed Solution](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the Authorized-Open Correlation logic `RulesEngineService` implements
- [Velosyss Lock Integration Guide](https://ajaytoybox.atlassian.net/wiki/spaces/DC/pages/75366402/Velosyss+Lock+Integration+Guide) — the authoritative webhook payload, signature scheme, and REST (command + read) contract this service implements (owned by Velosyss, not this repo)
- [Godown Operational Workflow](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — forms, master data, and sequential reference codes this service manages
- Implementation Tracker (Confluence) — Phase 3+ (website dashboard, mobile app, chatbot) build on top of this
