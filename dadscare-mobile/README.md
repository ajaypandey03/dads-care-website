# dadscare-mobile

Godown operator app for Dad's Care's customers — Android and iOS, one React Native codebase. Talks only to `dadscare-backend` over HTTPS; there is no direct connection to lock hardware from this app (no Bluetooth/local pairing — lock control is fully server-mediated).

**Not started yet — this is a placeholder.** Planned stack: Expo (managed workflow) + Expo Router, TanStack Query v5, NativeWind, react-hook-form + Zod, axios with `expo-secure-store` token storage — mirroring `velosyss-mobile`'s conventions.

## Docs

- [Godown Operational Workflow](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the Opening/Closing form spec, master data, offline behavior, and lock-control flow this app implements
- Implementation Tracker (Confluence) — Phase 4 covers this app
