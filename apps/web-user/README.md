# FastLove Responsive Web — User Client

Responsive web client for FastLove. It mounts `@fastlove/user-core` from `packages/user-core`, so the user product screens and business interaction flows are shared with the PWA.

## Run

```bash
npm run web:dev
```

Open `http://localhost:4178`. Node.js 20+ is required. In demo mode choose **ورود به حساب نمایشی**, or complete the local OTP flow with code `123456`.

## Build / Preview

```bash
npm run web:build
npm run web:preview
```

Static output: `apps/web-user/dist/`.

## Product scope

Discovery and filters, likes/matches, nearby map, chat (text/image/voice), profile setup/edit, dates and their location details, subscription/direct-message packages, payment history, privacy, blocked users, video verification, referral profile code, support tickets and account deletion are implemented in the shared UI core.

**Demo mode:** state is local to the browser. Real OTP, cross-device synchronization, chat realtime delivery, payment gateway, secure media storage and verification review require the central API. See `../../docs/MONOREPO-ARCHITECTURE.md` and `../../contracts/openapi/user-api.yaml`.
