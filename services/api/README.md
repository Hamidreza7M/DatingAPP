# FastLove Central API — implementation boundary

The central API service is the shared system of record for Android, Responsive Web, PWA and Admin Web. It is not yet an operational backend in this repository.

## Contract

User-facing endpoints are defined in `contracts/openapi/user-api.yaml`; admin endpoints remain in `contracts/openapi/admin-api.yaml`. All clients must use versioned HTTPS endpoints and must never connect directly to PostgreSQL or object storage.

## Required production implementation

- OTP authentication, session lifecycle, account recovery, rate limiting and abuse prevention.
- User profile, photo and voice-introduction management.
- Discovery ranking, filters, like/pass, block, matches and referral attribution.
- Consent-based coarse location search and visibility.
- Conversation authorization, realtime message delivery, image and voice media upload.
- Date proposals, acceptance state and optional coordinates.
- Active package catalog, price resolution on server, payment gateway checkout and verified purchase callbacks.
- Profile video verification workflow and moderation.
- Support ticket lifecycle, replies and private attachments.
- Privacy controls, soft-delete/retention policy and audit events.

Recommended production infrastructure: PostgreSQL, private S3-compatible object storage, a realtime gateway for chat/events, a payment gateway adapter, and a background worker for OTP, media processing and notifications.

## Current demo status

`apps/android` currently persists most app data locally using SharedPreferences/files. `apps/web-user` and `apps/pwa` use `packages/user-core/src/repository.js` and browser localStorage. `apps/admin-web` uses a separate JSON demo store. These demo stores are not synchronized. The `FastLoveApiClient` is an integration boundary, not a substitute for a deployed API. See `docs/MONOREPO-ARCHITECTURE.md`.
