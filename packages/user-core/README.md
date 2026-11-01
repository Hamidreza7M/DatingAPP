# @fastlove/user-core

Shared browser user product core consumed by both `apps/web-user` and `apps/pwa`.

- `src/app.js`: shared UI, routes, and interaction workflows.
- `src/data.js`: shared feature inventory, demo profiles, packages and seed state.
- `src/repository.js`: demo state persistence adapter (browser localStorage).
- `src/api-client.js`: shared API transport boundary for the future central API.
- `src/styles.css`: shared RTL responsive design system.

Keep product rules in this package rather than duplicating them between browser clients. Client-specific code should be limited to app shell concerns (e.g., PWA installation and service-worker registration).

**Important:** `api-client.js` currently provides transport methods and optional health check only; user workflows still use the local demo repository. Complete API wiring and Android migration are prerequisites for production data parity.
