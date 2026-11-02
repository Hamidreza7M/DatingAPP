# FastLove Admin changelog

## Engineering pipeline update — 2026-11-02

- Added a unified, Persian-language PDF report combining static security controls, Admin Web/API tests, Android validation/build/test outcomes, code-quality checks and security scanners.
- The security workflow now collects Web and Android evidence and generates the PDF after all build/test/scan jobs complete.
- `npm run security:verify` now retains detailed logs, refreshes the Markdown security report and generates `reports/FastLove-Comprehensive-Test-Security-Report.pdf` after local checks and the Admin Web build.
- Added a reusable Android XML validation script and documentation for PDF report dependencies/output.

## 1.0.5 — 2026-11-02

- Replaced the Admin Web interface font with the supplied Bonyade Koodak FaNum family, bundled locally as WOFF2 weights 100–900.
- Added the `font/woff2` response MIME type and versioned Admin Web asset URLs.
- Android source files and Android font resources were not changed.

## 1.0.4 — 2026-11-01

- Added a referral report that groups registered members under the owner of each referral code.
- Expanded demo records to at least 20 entries for every operational list, with 40 demo users split between 20 non-deleted and 20 deleted accounts.
- Added a local-demo upgrade migration that preserves current records and fills missing demo records from the expanded seed.
- Added planned-date detail view with meeting address, meeting point, latitude/longitude and a map link. DAT-1 includes a sample Tehran location.
- Added dynamic subscription and direct-message package creation; package prices are explicitly stored/displayed in Toman.
- Added package price editing, activate/deactivate controls, permanent catalog deletion and audit events.
- Updated the admin web assets, API contract and documentation to version 1.0.4.

> Demo records and sample meeting locations are synthetic. Production use still requires connecting the admin API to the central application backend and receiving the real referral attribution and date location from that backend.
