# Admin Web Font Change Report

## Scope
- Replaced the global font in the Admin Web UI with **Bonyade Koodak FaNum** from the user-provided font archive.
- Added local WOFF2 font files for weights 100, 200, 300, 400, 500, 700, 800 and 900.
- Added `font-display: swap`, the correct `font/woff2` HTTP MIME type, and bumped the Admin Web stylesheet cache key to `1.0.5`.
- The separate `.mono` code font remains Consolas.

## Android integrity
- No files under `apps/android/` were edited.
- Android font resources remain the original Vazirmatn TTF files.
- An SHA-256 before/after comparison is performed as part of this delivery and is required to report unchanged.

## Test scope
- Admin Web asset regression test checks the font-family declaration, bundled font references, font-display behavior and stylesheet version.
- Admin Web test suite and build are executed as part of final verification.

## Note
This change affects only the Admin Web interface. It does not add or replace fonts in Android resources. Verify that the supplied font license permits embedding and distribution of its webfont files with the deployed website.

## Verification results (2026-11-02)
- `npm run security:verify`: PASS.
- Security static checks: 14 PASS, 0 FAIL, 4 informational scope notes.
- Admin Web test suite: 8/8 PASS, 0 FAIL.
- Admin Web build: PASS (`apps/admin-web/dist` generated successfully).
- Runtime font request: HTTP 200, `Content-Type: font/woff2`, 53,652 bytes for the Regular weight.
- Android integrity: SHA-256 comparison across all 26 files under `apps/android/` before and after the change matched exactly.

## Changed files
- `apps/admin-web/public/fonts/` — eight WOFF2 weights.
- `apps/admin-web/public/styles.css` — Admin Web font family and `@font-face` declarations.
- `apps/admin-web/public/index.html` — asset cache-busting and UI version 1.0.5.
- `apps/admin-web/server.mjs` — `font/woff2` MIME type.
- `apps/admin-web/tests/ui-assets.test.mjs` — font regression and asset checks.
- Root and Admin Web `package.json` — version 1.0.5.
- `CHANGELOG.md` — font update entry.

## Scope confirmation
No file under `apps/android/` was edited. Android font files remain unchanged.
