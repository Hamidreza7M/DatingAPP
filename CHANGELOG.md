# FastLove Admin changelog

## 1.0.4 — 2026-11-01

- Added a referral report that groups registered members under the owner of each referral code.
- Expanded demo records to at least 20 entries for every operational list, with 40 demo users split between 20 non-deleted and 20 deleted accounts.
- Added a local-demo upgrade migration that preserves current records and fills missing demo records from the expanded seed.
- Added planned-date detail view with meeting address, meeting point, latitude/longitude and a map link. DAT-1 includes a sample Tehran location.
- Added dynamic subscription and direct-message package creation; package prices are explicitly stored/displayed in Toman.
- Added package price editing, activate/deactivate controls, permanent catalog deletion and audit events.
- Updated the admin web assets, API contract and documentation to version 1.0.4.

> Demo records and sample meeting locations are synthetic. Production use still requires connecting the admin API to the central application backend and receiving the real referral attribution and date location from that backend.
