# dadscare-backend

Spring Boot backend for the Dad's Care platform: receives lock/tamper telemetry pushed from Velosyss, relays lock/unlock commands back to Velosyss's device API (Phase 2), runs the Rules & Alerts Engine (Phase 2), and serves the REST API consumed by `website/` (dashboard section), `dadscare-mobile/`, and `dadscare-chatbot-service/`.

**Phase 1 (foundation) is scaffolded.** Stack: Java 21, Spring Boot 3.3.5, JPA, MySQL, Redis, JWT (JJWT 0.12.5), Flyway, Springdoc OpenAPI, Lombok + MapStruct — matching Velosyss's own conventions for team familiarity.

## What's built so far

- Core entities: `Organization` (tenant), `Site`, `ShutterUnit`, `Device`, `User`, `Role`, `UserSiteAccess`
- JWT auth (`POST /api/v1/auth/login`) with tenant claims; every tenant-scoped repository method takes an explicit `organizationId` — see `TenantContext` javadoc for why there's no global Hibernate filter
- Webhook receiver (`POST /api/v1/webhooks/velosyss/lock-events`) — HMAC-SHA256 verified, de-duplicated by `eventId`, persists immutable `RawEvent` rows
- `SequenceCounterService` — atomic, per-org sequential WhatsApp reference codes (e.g. `DC-000482`)
- `ProductMaster` / `TransporterMaster` CRUD

**Not yet built (Phase 2):** `UnlockRequest`, the Authorized-Open Correlation rules engine, `Alert`/`Notification` dispatch. See the Implementation Tracker in Confluence.

## Running locally

```bash
docker compose up -d          # MySQL on :3306, Redis on :6379
cp .env.example .env          # fill in JWT_SECRET (openssl rand -hex 64) and WEBHOOK_VELOSYSS_SECRET
mvn spring-boot:run
```

API docs: `http://localhost:8090/swagger-ui.html`. Flyway runs the schema automatically on startup (`src/main/resources/db/migration`).

```bash
mvn test    # unit tests only (webhook signature verification, sequence-code formatting, auth service)
```

## Docs

- [Dad's Care Platform Design](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — multi-tenant data model, auth, notifications, chatbot
- [Lock vs Shutter — False-Positive Problem & Proposed Solution](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the Authorized-Open Correlation logic Phase 2 implements
- [Integration Contract](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the webhook payload and Device Command API this service calls into (owned by Velosyss, not this repo)
- [Godown Operational Workflow](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — forms, master data, and sequential reference codes this service manages
- Implementation Tracker (Confluence) — Phases 1 & 2 cover this service
