# FastLove Security Pipeline

The workflow `.github/workflows/security.yml` runs on pushes, pull requests, a weekly schedule, and manual dispatch. It adds these automated checks:

- **Admin API security tests:** runs the Node.js API/authentication/security regression suite and verifies the production static build.
- **CodeQL SAST:** Kotlin/Android and JavaScript/TypeScript. Kotlin is built with JDK 17, Android SDK 35, and Gradle 8.9 before CodeQL analysis. A dedicated policy check also verifies Android backup, cleartext-traffic, and FileProvider restrictions.
- **Gitleaks:** scans the full Git history for exposed credentials and tokens.
- **Trivy:** scans Gradle-resolved and locked Android dependencies (including test dependencies), secrets, and repository configuration for HIGH/CRITICAL findings; `.github/trivy.yaml` enables development/test dependency reporting, and the SARIF report is retained as an artifact.
- **Dependency Review:** runs on pull requests only when the repository variable `ENABLE_DEPENDENCY_REVIEW` is set to `true`; enable Dependency Graph first. This avoids a false pipeline failure when GitHub Dependency Graph is unavailable or disabled.
- **OWASP ZAP baseline:** passively scans the locally started production-configured Admin Web server and stores JSON/HTML reports. The committed `.zap/baseline.conf` makes missing secure-cookie flags and core response headers (Content-Type, anti-framing, nosniff, CSP, SameSite) blocking findings; other baseline warnings remain visible in the report but do not block the run. This is not an active penetration test.
- **Dependabot:** weekly grouped updates for GitHub Actions and Admin Web npm dependencies. Routine Gradle version-update PRs are disabled to prevent incompatible Android plugin/library bumps; Dependabot security-update PRs remain available when enabled in repository settings and are grouped separately.

## Required repository configuration

The Android dependency scan resolves and locks configurations during CI before Trivy reads the generated Gradle lockfile. The Trivy configuration includes development/test dependencies. The Android `FileProvider` is restricted to camera capture, app pictures, and support attachments. The Android emulator job enables KVM and allows up to 10 minutes for emulator boot; emulator startup can still depend on GitHub runner capacity. The Admin Web checks external media URLs are HTTPS before placing them into DOM URL attributes. The ZAP job generates temporary random credentials during each run. It does not require repository secrets and does not use production credentials or production services; it scans an isolated local server process started inside the GitHub runner.

To enable Dependency Review, turn on **Dependency Graph** under repository Settings → Security → Code security and analysis, enable Dependabot security updates if desired, and add the repository Actions variable `ENABLE_DEPENDENCY_REVIEW=true`. Without that variable the Dependency Review job is intentionally skipped; Trivy still performs the configured dependency scan.

CodeQL result upload requires GitHub code scanning to be available for the repository. For private repositories, availability depends on the repository's GitHub plan and Code Security entitlement. Gitleaks organizations may also require a Gitleaks license secret.

## Scope limits

These checks do not replace a manual OWASP MASVS/MASTG assessment, a mobile runtime test on real devices, a penetration test, or a DAST scan of a deployed production API. The current source archive contains no live production API endpoint or backend deployment configuration, so this workflow only runs DAST against the Admin Web server available in this repository.
