# FastLove Security Verification Report

This report records checks run while preparing the restored security workflow.

## Executed in the local preparation environment

| Check | Result | Details |
|---|---|---|
| Admin API and security tests | PASS | 5 tests passed, 0 failed |
| Admin static build | PASS | `npm run admin:build` completed |
| Admin server JavaScript syntax | PASS | `node --check apps/admin-web/server.mjs` |
| Admin browser JavaScript syntax | PASS | `node --check apps/admin-web/public/app.js` |
| Android manifest and FileProvider policy | PASS | Backup, cleartext traffic, provider exposure and allowed paths checked |
| GitHub Actions YAML syntax | PASS | All workflow and Dependabot YAML files parsed |

## Configured to run on GitHub Actions after push

- CodeQL SAST for Kotlin/Java and JavaScript/TypeScript.
- Gitleaks full-history secret scan.
- Trivy dependency, secret and infrastructure/configuration scan.
- OWASP ZAP passive baseline scan of the local Admin Web server.
- Dependency Review for pull requests when `ENABLE_DEPENDENCY_REVIEW=true` and Dependency Graph is enabled in repository settings.
- Android Gradle unit tests, lint, APK build, coverage and emulator UI tests in the Android quality workflow.

## Not executed locally

The preparation environment did not have Gradle, Android SDK/sdkmanager, Docker, CodeQL CLI, Gitleaks, or Trivy installed. Therefore the Android Gradle build/tests/lint/emulator checks and the GitHub-hosted scanners have **not** been represented as passing. They are wired into the CI workflows and must run on GitHub after these files are committed and pushed.

CodeQL and Dependency Review availability for a private repository depends on its GitHub plan and Code Security entitlement. Gitleaks may require `GITLEAKS_LICENSE` for organization-owned repositories. See `SECURITY-PIPELINE.md`.

## CI/Dependabot repair validation (2026-11-03)

| Check | Result | Details |
|---|---|---|
| Admin API/security regression tests | PASS | 5 tests passed, 0 failed |
| Admin static build | PASS | `npm run admin:build` completed |
| Admin server/browser JavaScript syntax | PASS | `node --check` passed for both files |
| Android manifest/FileProvider security check | PASS | `security_manifest_check.py` passed |
| Workflow, Dependabot and Trivy config YAML syntax | PASS | All five YAML files parsed successfully |

The updated Trivy Action configuration, Dependabot behavior, and emulator setup have been statically reviewed against their documented inputs/configuration. They have not been executed on GitHub in this environment. Android Gradle build/tests, emulator boot, CodeQL, Gitleaks, Trivy and OWASP ZAP still require a GitHub Actions run after the archive is pushed.
