# FastLove — Security Requirements (Demo Baseline)

**Scope:** Android demo app, Admin Web demo, future PWA, and future central backend/API.

This file is a product/security backlog, not a claim of MASVS/ASVS certification. Requirements are selected from OWASP MASVS/MASTG, OWASP ASVS 5.x, OWASP API Security Top 10, and NIST SSDF. Verify the applicable version and requirement IDs when the real backend and PWA are implemented.

## Implemented in this demo snapshot

| ID | Requirement | Verification |
|---|---|---|
| AND-01 | Disable cleartext network traffic | Manifest + Network Security Config static check |
| AND-02 | Trust system TLS certificates only | Network Security Config static check |
| AND-03 | Disable cloud backup and device transfer of app-local data | Manifest + backup XML checks |
| AND-04 | Narrow FileProvider access and keep it non-exported | Manifest + paths XML checks |
| AND-05 | Target API 36 for Google Play submission | Gradle config static check |
| AND-06 | Minify and shrink resources in Release | Gradle build config + R8 rules |
| WEB-01 | Bind demo server to loopback by default | Runtime security test |
| WEB-02 | Refuse production mode for the JSON demo API | Runtime security test |
| WEB-03 | Apply browser security headers | Runtime HTTP test |
| WEB-04 | Require same-origin Origin on state-changing requests | Runtime HTTP test |
| WEB-05 | Require JSON request bodies on state-changing endpoints | Runtime HTTP test |
| WEB-06 | Use HttpOnly + SameSite=Strict server-side session cookie | Runtime HTTP test |
| WEB-07 | Invalidate server-side session on logout | Runtime HTTP test |
| WEB-08 | Keep unauthenticated access to private admin API blocked | Existing API integration test |

## Mandatory before a public/production release

| ID | Requirement | Owner | Acceptance evidence |
|---|---|---|---|
| BE-01 | Implement a real central backend; replace local JSON file store | Backend | Architecture, API contract, integration tests |
| BE-02 | Implement OIDC/OAuth 2.0 Authorization Code + PKCE for native clients | Backend + Android | Authentication flow test and threat model |
| BE-03 | Enforce RBAC/ABAC in backend for every object, function and property | Backend | BOLA/BFLA negative API tests |
| BE-04 | Enforce MFA/passkeys and step-up authentication for admin | Backend + Admin | MFA and recovery tests |
| BE-05 | Use short-lived access tokens, refresh-token rotation/revocation | Backend | Session lifecycle tests |
| BE-06 | Use password hashing (Argon2id or suitable approved KDF); never plaintext | Backend | Config review and password-storage test |
| BE-07 | Use PostgreSQL or equivalent; encrypt sensitive fields/backups; least-privilege DB roles | Backend/DevOps | DB configuration and restore test |
| BE-08 | Centralized KMS/Secret Manager; rotate secrets; no secrets in client binaries | DevOps | Secret scan and rotation evidence |
| BE-09 | Rate limit authentication and sensitive endpoints; abuse and resource quotas | Backend | Load/abuse test evidence |
| BE-10 | Audit log with actor, object, timestamp, outcome; tamper resistance and retention | Backend | Audit integrity and retention test |
| BE-11 | Define data retention, deletion, export, consent and privacy workflows | Product/Legal/Backend | Data inventory, DPIA where applicable, deletion test |
| AND-10 | Classify mobile data; encrypt sensitive local records with Keystore-backed keys if offline storage is needed | Android | MASVS-STORAGE test on device |
| AND-11 | Complete MASVS-selected profile and MASTG dynamic tests (including backup, logs, root/tamper as risk-appropriate) | Android/Security | MASTG test report |
| WEB-10 | Implement PWA only with HTTPS, strict CSP, safe Service Worker scope, no sensitive cache by default | Web | ASVS test + browser storage/cache inspection |
| WEB-11 | Use HttpOnly Secure SameSite cookies or a reviewed BFF pattern; deploy only behind HTTPS/HSTS | Web/DevOps | ASVS session and TLS tests |
| WEB-12 | Add CSRF token strategy for cookie-authenticated state changes; retain Origin validation | Web/Backend | Cross-site request regression tests |
| API-10 | Address all OWASP API Security Top 10 2023 risks (BOLA, auth, property/function auth, resource consumption, SSRF, misconfiguration, inventory, unsafe API consumption) | Backend | API security test report |
| SDLC-10 | Run SAST, SCA/SBOM, secret scan, IaC/container scan, DAST and dependency review in CI | DevOps | CI artifacts and policy gate |
| SEC-10 | Independent penetration test; no unresolved Critical/High findings before release | Security | Signed penetration-test report and remediation retest |
| IR-10 | Incident response, monitoring, alerting, backup recovery and breach workflow | Security/DevOps | Tabletop and restore exercise |

## Release policy

- No public deployment of the current Admin JSON demo server.
- No real user, identity, payment, location, profile-photo, chat, or verification-video data in this demo.
- Release is blocked on unresolved Critical/High security findings.
- Production approval requires evidence for every mandatory item applicable to the launched features, plus a documented risk acceptance for any exception.
