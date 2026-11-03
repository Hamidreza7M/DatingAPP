# FastLove Security Pipeline

The workflow `.github/workflows/security.yml` runs on pushes, pull requests, a weekly schedule, and manual dispatch. It adds these automated checks:

- **Admin API security tests:** runs the Node.js API/authentication/security regression suite and verifies the production static build.
- **CodeQL SAST:** Kotlin/Android and JavaScript/TypeScript. Kotlin is built with JDK 17, Android SDK 35, and Gradle 8.9 before CodeQL analysis. A dedicated policy check also verifies Android backup, cleartext-traffic, and FileProvider restrictions.
- **Gitleaks:** scans the full Git history for exposed credentials and tokens.
- **Trivy:** scans Gradle-resolved and locked Android dependencies (including test dependencies), secrets, and repository configuration for HIGH/CRITICAL findings; its SARIF report is retained as an artifact.
- **Dependency Review:** checks dependency changes on pull requests and fails for HIGH or CRITICAL severity.
- **OWASP ZAP baseline:** passively scans the locally started production-configured Admin Web server and stores JSON/HTML reports. The committed `.zap/baseline.conf` makes missing secure-cookie flags and core response headers (Content-Type, anti-framing, nosniff, CSP, SameSite) blocking findings; other baseline warnings remain visible in the report but do not block the run. This is not an active penetration test.
- **Dependabot:** weekly update PRs for GitHub Actions, Gradle, and npm dependencies.

## Required repository configuration

The Android dependency scan resolves and locks configurations during CI before Trivy reads the generated Gradle lockfile. The Android `FileProvider` is restricted to camera capture, app pictures, and support attachments. The Admin Web checks external media URLs are HTTPS before placing them into DOM URL attributes. The ZAP job generates temporary random credentials during each run. It does not require repository secrets and does not use production credentials or production services; it scans an isolated local server process started inside the GitHub runner.

CodeQL result upload requires GitHub code scanning to be available for the repository. For private repositories, availability depends on the repository's GitHub plan and Code Security entitlement. Gitleaks organizations may also require a Gitleaks license secret.

## Scope limits

These checks do not replace a manual OWASP MASVS/MASTG assessment, a mobile runtime test on real devices, a penetration test, or a DAST scan of a deployed production API. The current source archive contains no live production API endpoint or backend deployment configuration, so this workflow only runs DAST against the Admin Web server available in this repository.
