# Changelog

## 1.1.0 — 2026-11-01

### User Web and PWA clients
- Implemented Responsive Web user client in `apps/web-user`.
- Implemented installable PWA client in `apps/pwa` with manifest, icons, service worker and offline app shell.
- Added shared user product core in `packages/user-core`; both browser clients now mount the same application code.
- Added user feature/API contract in `contracts/openapi/user-api.yaml` and monorepo architecture documentation.
- Added independent Web/PWA build, preview and test scripts plus GitHub Actions workflow.
- Browser demo includes OTP flow, profile setup/edit, discovery filters, likes/matches, map, text/image/voice chat, date proposals/details, subscription and direct-message packages, payment history, privacy, blocking, video verification, support tickets and account deletion.

### Important integration boundary
- Browser user flows currently use a local demo repository. Central API implementation and Android migration are still required for real cross-device synchronization and Production payments/chat/authentication/media.

## 1.0.4
- Admin referral reports, scheduled-date location details, dynamic packages and expanded demo seed data.

## 1.0.3
- Improved admin navigation, BI metrics, user photo detail, chat moderation view and deleted-user archive.

## 1.0.2
- Fixed admin login state and navigation rendering.
