# FastLove — Security Review & Pipeline Report

- **Generated:** 2026-11-02T08:10:25.873Z
- **Scope:** uploaded demo source snapshot; Android app and Admin Web demo
- **Backend:** no production backend exists; Admin API is a local JSON demo server
- **PWA:** not implemented in this source snapshot
- **Overall pipeline status:** PARTIAL / BLOCKED — see stage results

## Pipeline results

| Stage | Result |
|---|---|
| Web static security checks | **PASS** |
| Admin API integration/security tests | **PASS** |
| Admin web build | **PASS** |
| Gitleaks secret scan | **NOT RUN** |
| CodeQL SAST (JavaScript + Kotlin) | **NOT RUN** |
| OSV dependency vulnerability scan | **NOT RUN** |
| Android JVM unit tests | **NOT RUN** |
| Android Lint | **NOT RUN** |
| Android Release build (R8) | **NOT RUN** |
| Android JaCoCo report | **NOT RUN** |
| Android instrumented UI tests | **NOT RUN** |
| Android XML resource/manifest parse | **PASS** |

## Static security requirements

**Local result: 14 PASS, 0 FAIL, 4 informational scope notes.**

| ID | Control | Result |
|---|---|---|
| `AND-001` | Android cleartext networking disabled | PASS |
| `AND-002` | Android system TLS trust anchors only | PASS |
| `AND-003` | Android cloud backup and device transfer disabled | PASS |
| `AND-004` | Android compileSdk/targetSdk 36 | PASS |
| `AND-005` | Android Gradle Plugin compatible with API 36 | PASS |
| `AND-006` | Android Release uses R8 and resource shrinking | PASS |
| `AND-007` | Gson-reflection models have ProGuard keep rules | PASS |
| `AND-008` | FileProvider is non-exported and narrowly scoped | PASS |
| `WEB-001` | Admin demo server defaults to loopback | PASS |
| `WEB-002` | JSON demo API refuses production mode | PASS |
| `WEB-003` | Admin server sends browser security headers | PASS |
| `WEB-004` | Mutating API requests enforce same-origin Origin | PASS |
| `WEB-005` | Mutating browser requests send JSON content type | PASS |
| `OPS-001` | Git ignores environment and signing secret files | PASS |

## Admin Web/API runtime tests

**Local result: 8 PASS, 0 FAIL.**

| # | Test | Result |
|---:|---|---|
| 1 | Admin API authentication, reads and management actions | PASS |
| 2 | Documented demo credentials work in local demo mode | PASS |
| 3 | Production mode and public binding are blocked by default | PASS |
| 4 | Security headers, path traversal, Origin, JSON, sessions, logout and login rate limiting | PASS |
| 5 | Admin UI hides inactive views and supports online/offline filtering | PASS |
| 6 | Admin assets are versioned and not served stale from browser cache | PASS |
| 7 | Admin Web uses the bundled Bonyade Koodak FaNum font without changing Android assets | PASS |
| 8 | All bundled WOFF2 weights are present and served with the font/woff2 MIME type | PASS |

## Changes applied in this review

1. Admin demo server now binds to `127.0.0.1` by default and blocks remote binding unless `ALLOW_INSECURE_DEMO_REMOTE=true` is explicitly set.
2. Admin demo server refuses `NODE_ENV=production`; the existing JSON store and static demo credential are not production authentication or persistence.
3. Added anti-framing, CSP, MIME-sniffing, referrer, permissions, COOP and CORP response headers.
4. State-changing Admin API requests now require same-origin `Origin` and JSON request bodies when a body is present. Session cookie remains HttpOnly and SameSite=Strict; sessions expire after 30 minutes and are invalidated on logout.
5. Android now targets API 36, disables cleartext traffic, trusts only system CAs, disables backup/device transfer, narrows FileProvider paths, and enables R8/resource shrinking for Release.
6. Added CI controls for CodeQL SAST, OSV dependency scanning and Gitleaks, plus Android Release/Lint/unit/coverage stages.

## Historical Android QA evidence (previous run)

The separately maintained ANDROID-QA-REPORT.md records a prior GitHub Actions run. This historical result is not the result of the current pipeline execution.

| Check | Historical result |
|---|---|
| AppRules Kotlin compile/smoke harness | PASS — 10 assertions |
| AppRulesTest source via local Kotlin harness | PASS — 14 test methods (not Gradle) |
| testDebugUnitTest | PASS |
| jacocoTestReport | PASS |
| assembleDebug | PASS |
| lintDebug | FAIL — 2 errors and 13 warnings |
| connectedDebugAndroidTest | NOT RUN — prior workflow did not reach this stage |
| Debug APK artifact publication | NOT PUBLISHED — quality gate failed on Lint |
| Post-patch Gradle/Lint revalidation | NOT RUN — Gradle/Android SDK unavailable in local environment |

## Admin Web font regression checks

- Bonyade Koodak FaNum is bundled locally as eight WOFF2 weights.
- Runtime font request was verified in the font-update run: HTTP 200 with Content-Type font/woff2.
- The font change was restricted to Admin Web; Android font resources were not changed.

## Findings / release blockers

| Severity | Finding | Status |
|---|---|---|
| **Critical / Release blocker** | No central production backend, server-side user identity, RBAC/ABAC or production session store | OPEN — not implemented in demo scope |
| **High** | Admin demo credentials are intentionally fixed at `admin / 123456` | MITIGATED for local demo only by loopback binding and production-mode refusal; not acceptable for public deployment |
| **High** | Android profile/chat/support data is stored in app-private SharedPreferences/files without an app-level Keystore encryption layer | OPEN — do not use real personal or dating data in demo |
| **High** | No real PWA source, Service Worker, or PWA cache policy exists | NOT APPLICABLE to current source; required before PWA implementation |
| **High** | MFA, role-based admin users, refresh-token rotation, KMS, immutable audit and API object-level authorization cannot be verified without real backend | NOT IMPLEMENTED / BLOCKED BY BACKEND |
| **High** | Independent penetration test and runtime MASTG/WSTG tests have not been executed | OPEN — required before production release |
| **Medium** | Release signing / Play App Signing configuration is not present in repository | OPEN — configure in secure CI secrets before Play upload |

## Local verification summary

- Static security configuration checks: **14 PASS, 0 FAIL, 4 informational scope notes**.
- Admin web/API tests: **8 PASS, 0 FAIL** (including negative security tests and font asset regression tests).
- Admin web build: **PASS**.
- Android manifest and XML resource parsing: **PASS (8 files)**. This validates XML well-formedness only; it does not replace Android Lint or an Android build.
- GitHub Actions workflow files: **YAML syntax PASS** (local parser); remote Actions execution has not been triggered from this environment.

## Test limitations

Android Gradle tasks, Android Emulator instrumentation, Gitleaks, CodeQL and OSV were not run locally. The environment has no installed Gradle CLI or Android SDK. Android Gradle/Lint/Release/XML, Emulator instrumentation and repository secret/SAST/dependency scans are configured in the security workflow. Their results must be confirmed in the resulting workflow runs. Any stage marked `NOT RUN` is not a pass. A successful Admin demo test does not certify the future backend, Android binary, PWA or production deployment.

## Release decision

**NO-GO for public/production release.** The checked-in app remains a demo. A production backend, secure local-data strategy, admin MFA/RBAC, PWA implementation/security review, signing setup, dynamic security tests and independent penetration test remain prerequisites.

See [SECURITY-REQUIREMENTS.md](SECURITY-REQUIREMENTS.md) for the requirement backlog. Official references: [Google Play target API policy](https://support.google.com/googleplay/android-developer/answer/11926878), [Android Gradle Plugin release notes](https://developer.android.com/build/releases/about-agp), [OWASP MASVS](https://mas.owasp.org/MASVS/), [OWASP ASVS](https://asvs.owasp.org/), and [NIST SSDF](https://csrc.nist.gov/pubs/sp/800/218/final).
