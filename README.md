# Dad's Care Platform (Monorepo)

This repo hosts the whole Dad's Care × Velosyss Digital Lock platform. Full design docs live in Confluence — space **Dads Care** (key `DC`): https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview

## Layout

| Folder | Purpose | Status |
|---|---|---|
| [`website/`](./website) | Public marketing pages **and** the authenticated customer dashboard — one Next.js app, one domain | Live (marketing pages); dashboard section not built yet |
| [`dadscare-backend/`](./dadscare-backend) | Spring Boot API: lock-event ingestion, multi-tenant data model, Rules & Alerts Engine, device-command relay to Velosyss, reporting, notifications | Not started |
| [`dadscare-mobile/`](./dadscare-mobile) | Godown operator app (Android + iOS), React Native/Expo | Not started |
| [`dadscare-chatbot-service/`](./dadscare-chatbot-service) | Static Q&A chatbot service (template-based, no LLM yet) | Not started |
| [`dadscare-infra/`](./dadscare-infra) | IaC, CI/CD, environment config | Not started |

## Where to start

See the **[Implementation Tracker](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview)** in Confluence for the phased build-out checklist, and the **Dad's Care Platform Design** / **Godown Operational Workflow** pages for the detailed spec each folder above should implement.

## Context

Dad's Care fits smart locks to warehouse ("godown") shutters. Velosyss supplies the lock hardware and telemetry (a new "Digital Lock" product, alongside SafeTrack/SwiftTrack) and bridges lock events to this platform. This platform turns that into reports, alerts, and notifications for Dad's Care's own customers — multi-tenant, so many customers share one app — and specifically fixes the "lock opened but shutter wasn't" false-positive problem that Dad's Care's previous vendor (QTS) couldn't solve. See the Confluence space for the full architecture.
