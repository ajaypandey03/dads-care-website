# dadscare-mobile

Godown operator app for Dad's Care's customers — Android and iOS, one React Native codebase. Talks only to `dadscare-backend` over HTTPS; there is no direct connection to lock hardware from this app (no Bluetooth/local pairing — lock control is fully server-mediated, see the Godown Operational Workflow page in Confluence).

**Phase 4 is built.** Stack: Expo (managed workflow) + Expo Router (file-based routing), TanStack Query v5, NativeWind (Tailwind for RN), react-hook-form + Zod, axios with SecureStore token storage — mirroring `velosyss-mobile`'s conventions (see the Implementation Tracker for what was actually verified there).

## What's built

- **Auth**: login screen, JWT stored via `expo-secure-store` (falls back to `localStorage` on the web target only — see `src/utils/secureStorage.ts`), session-expiry (401) handling. No refresh-token flow — `dadscare-backend` issues a single fixed-TTL token, unlike `velosyss-mobile`.
- **Sites → Shutters**: browse godowns and their shutter units, see live device online/battery status.
- **Opening/Closing form**: dynamic stock lines and truck entries (pulled from `dadscare-backend`'s product/transporter masters), labor count, remarks, up to 10 custom fields — submits alongside the lock/unlock command in one call.
- **Offline drafting**: a failed submission (real network error, not a validation/auth error) is queued in AsyncStorage and retried on reconnect, mirroring `velosyss-mobile-driver`'s settlement-queue pattern. Important difference from that pattern: queuing here does **not** mean the shutter unlocks immediately — the physical action only happens once the queued request actually reaches the backend, and the UI says so explicitly.
- **Push notifications**: registers an Expo push token with `dadscare-backend` (`PUT /api/v1/me/push-token`) — the backend's `ExpoPushSender` (Phase 4 backend work) delivers real pushes, not a stub.
- **Alerts feed + feedback**: lists alerts, lets an operator mark an `UNEXPLAINED_*` alert "Correct"/"Not correct" (`POST /api/v1/alerts/{id}/feedback`).

## Running locally

```bash
cd dadscare-mobile
npm install
npm run ios      # or: npm run android / npm run web
```

Points at `dadscare-backend` on `http://localhost:8090` by default (auto-detected from the Expo dev server host — see `src/api/apiClient.ts`). Override with `EXPO_PUBLIC_API_URL` if the backend's running elsewhere. The backend also needs `dadscare-mobile`'s dev server origin in its `CORS_ALLOWED_ORIGINS` if testing the **web** target (native iOS/Android don't hit CORS at all).

```bash
npm run typecheck
npm run lint
```

**Verified against a real, running `dadscare-backend`** (not just typechecked) via the Expo web target: login issuing a real JWT, the product-master picker showing real seeded data, and a full form submission (stock line + labor + remarks) correctly persisting an `UnlockRequest` + `GodownForm` + `StockLine` in the database. No iOS Simulator/Android emulator was available on this machine (Xcode not fully installed) — the actual native builds haven't been exercised on-device yet.

## Docs

- [Godown Operational Workflow](https://ajaytoybox.atlassian.net/wiki/spaces/DC/overview) — the form spec, master data, offline behavior, and lock-control flow this app implements
- Implementation Tracker (Confluence) — Phase 4 checklist
