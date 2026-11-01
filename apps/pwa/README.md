# FastLove PWA — User Client

Installable Progressive Web App. It uses the same `packages/user-core` application code as Responsive Web and adds a web app manifest, service worker, standalone display and cached application shell.

## Run

```bash
npm run pwa:dev
```

Open `http://localhost:4179`. Install with the browser's Install action (Chrome/Edge may show an in-app install affordance when eligible). On iOS Safari use Share → Add to Home Screen.

## Build / Preview

```bash
npm run pwa:build
npm run pwa:preview
```

Static output: `apps/pwa/dist/`.

The service worker caches app shell files only. It intentionally does not cache authenticated API responses or private media. Third-party map tiles and remote profile photos may not be available offline until cached by the browser. User state in the current demo is stored in localStorage.

Real cross-client synchronization requires the central API described in `../../contracts/openapi/user-api.yaml`.
