# FastLove Monorepo Architecture

## Product clients

```text
FastLove/
├── apps/
│   ├── android/              # Existing Android app (Kotlin + Jetpack Compose)
│   ├── web-user/             # Responsive user website
│   ├── pwa/                  # Installable Progressive Web App
│   └── admin-web/            # Admin portal (kept independent)
├── packages/
│   └── user-core/            # Shared user product core: screens, models, demo repository, API adapter and design system
├── services/
│   └── api/                  # Central API service boundary (production implementation still required)
├── contracts/openapi/
│   ├── admin-api.yaml        # Existing admin contract
│   └── user-api.yaml         # User API shared contract
├── scripts/                  # Monorepo development and build scripts
└── docs/
```

## Single product implementation for Web and PWA

`apps/web-user/main.js` and `apps/pwa/main.js` both mount the exact same `packages/user-core/src/app.js` application. They only provide a shell variant:

- Responsive Web: desktop right sidebar, mobile bottom navigation, responsive cards and layouts.
- PWA: same screens and interactions, plus install manifest, service worker, standalone display and cached application shell.

All product entities, demo seed data, local repository semantics, shared API adapter and design tokens live under `packages/user-core`. This avoids feature drift between the two browser products. Android remains a native Kotlin client and should integrate with the same versioned API contract rather than importing browser code.

## User feature parity scope

| Product capability | Android app | Responsive Web | PWA |
|---|---|---|---|
| OTP login / registration and profile setup | Native | Shared user-core | Shared user-core |
| Profile, photos, interests, voice introduction | Native | Shared user-core | Shared user-core |
| Discovery, filters, like / pass / block | Native | Shared user-core | Shared user-core |
| Likes sent / received / matches | Native | Shared user-core | Shared user-core |
| Nearby map and map visibility privacy | Native / OSM | Shared user-core / OSM | Shared user-core / OSM |
| Chat text, image and voice | Native | Shared user-core | Shared user-core |
| Date proposal, date status and location details | Native | Shared user-core | Shared user-core |
| Subscription and direct message credits | Native | Shared user-core | Shared user-core |
| Purchase history | Native | Shared user-core | Shared user-core |
| Video profile verification | Native | Shared user-core | Shared user-core |
| Privacy, blocked users, account deletion | Native | Shared user-core | Shared user-core |
| Support tickets, replies and attachments | Native | Shared user-core | Shared user-core |

## Data and API architecture

No client should connect directly to PostgreSQL or object storage. The target data flow is:

```text
Android ───────┐
Responsive Web ├── HTTPS + versioned OpenAPI ── Central API ── PostgreSQL
PWA ───────────┤                                  ├────────── Object Storage (private media)
Admin Web ─────┘                                  ├────────── Realtime gateway (chat / events)
                                                  └────────── Payment gateway
```

The browser clients include a common `FastLoveApiClient`. Set `window.FASTLOVE_API_BASE` before loading the app to target a central API. The OpenAPI contract is in `contracts/openapi/user-api.yaml`.

**Current repository limitation:** `services/api` is still a planned service and the Android source describes itself as a local demo using on-device persistence. The new Web/PWA clients therefore run against a persistent local demo repository by default. The UI and API boundary are implemented, but cross-device account sync, real OTP delivery, live chat delivery, real payment settlement, server-side verification, moderation, and shared production records will not work until the central API and database are implemented and the Android app is migrated to it. The PWA's service worker caches the shell; it does not turn remote server operations into offline writes or cache third-party map tiles.

## Browser security and production requirements

- Configure a real API base URL over HTTPS and use secure, HttpOnly, SameSite session cookies.
- OTP delivery, rate limits, account recovery and anti-abuse controls must be server-side.
- Validate every profile interaction and authorization on the API; localStorage is only for demo state.
- Store photos, voice notes, support attachments and verification videos in private object storage using short-lived signed URLs. Do not persist large media data URLs in localStorage in production.
- Never treat the client-submitted package price as authoritative. The API must resolve active package price and initiate the trusted payment gateway flow.
- Map visibility must be opt-in and return only coarse location for consenting users. Never expose exact user coordinates to other users.
- Account deletion should run through a server retention policy, with auditability and legally required retention handled centrally.
- PWA offline caching is restricted to static app resources; authenticated API responses and private user media must not be cached by the service worker.

## Development and builds

Node.js 20+ is required for Web/PWA development.

```bash
# Responsive Web
npm run web:dev       # http://localhost:4178
npm run web:build
npm run web:preview

# PWA
npm run pwa:dev       # http://localhost:4179
npm run pwa:build
npm run pwa:preview

# Tests and all user-client builds
npm run user:test
npm run user:build

# Existing products
npm run admin:dev
npm run admin:build
npm run admin:test
npm run android:build
```

Demo login:
- Use the **ورود به حساب نمایشی** action on either browser client.
- For the OTP flow, any test phone can be used and the local demo OTP is `123456`.

Each browser client builds to its own `apps/<client>/dist` directory and can be deployed independently. Deploy the contents of each `dist` directory at the origin root so the shared `/packages/user-core/` assets resolve correctly. For a sub-path deployment, change the base asset paths and PWA manifest/service-worker scope together.
