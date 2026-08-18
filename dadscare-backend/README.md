# dadscare-backend

Spring Boot backend for the Dad's Care platform: receives lock/tamper telemetry pushed from Velosyss, relays lock/unlock commands back to Velosyss's device API, runs the Rules & Alerts Engine (including the false-positive fix), and serves the REST API consumed by `website/` (dashboard section), `dadscare-mobile/`, and `dadscare-chatbot-service/`.

**Not started yet — this is a placeholder.** Planned stack: Java 21, Spring Boot 3.3.5, JPA, MySQL, Redis, JWT (JJWT), Springdoc OpenAPI — matching Velosyss's own conventions for team familiarity.

## Docs

- [Dad's Care Platform Design](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — multi-tenant data model, auth, notifications, chatbot
- [Lock vs Shutter — False-Positive Problem & Proposed Solution](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the Authorized-Open Correlation logic this service implements
- [Integration Contract](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the webhook payload and Device Command API this service calls into (owned by Velosyss, not this repo)
- [Godown Operational Workflow](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — forms, master data, and sequential reference codes this service manages
- Implementation Tracker (Confluence) — Phases 1 & 2 cover this service
