# CI / Dependabot repair notes

This patch is a CI configuration repair. It does not upgrade Android or Kotlin dependencies and does not intentionally change app behavior.

## Changes

- Removed the unsupported `trivy-flags` input from `aquasecurity/trivy-action`. The supported `trivy-config` input points to `.github/trivy.yaml`, which enables Gradle development/test dependency reporting through `pkg.include-dev-deps: true`.
- Kept the Gradle dependency-resolution and lockfile-generation step so Trivy can inspect the actual resolved Android dependency graph.
- Made Dependency Review opt-in through the Actions repository variable `ENABLE_DEPENDENCY_REVIEW=true`. This avoids a failed job when GitHub Dependency Graph is disabled. Enable Dependency Graph first in repository Settings → Security → Code security and analysis.
- Changed Dependabot so routine Gradle version-update PRs are not opened (`open-pull-requests-limit: 0`). Dependabot security-update PRs are separately grouped and can still be opened when security updates are enabled in repository settings.
- Grouped routine GitHub Actions and npm minor/patch updates to reduce separate PRs; major updates are held for manual review.
- Improved Android emulator setup by enabling KVM and allowing a 600-second boot timeout. This reduces avoidable emulator startup timeouts but cannot guarantee a hosted runner will always boot successfully.

## Android dependency baseline intentionally preserved

The project versions remain unchanged in this patch:

- Android Gradle Plugin: `8.7.3`
- Kotlin Android: `2.0.21`
- Kotlin Compose Compiler plugin: `2.0.21`
- Gradle used by CI: `8.9`
- `compileSdk`: `35`
- `androidx.core:core-ktx`: `1.15.0`

No Dependabot bump from the open PRs is merged by this patch.

## After replacing the repository files

1. Push the updated files to the default branch.
2. In Settings → Security and analysis, enable Dependency Graph. If you want the Dependency Review job, add Actions variable `ENABLE_DEPENDENCY_REVIEW` with value `true`.
3. Confirm Dependabot security updates are enabled if you want vulnerability-driven Gradle PRs.
4. Close existing routine Gradle bump PRs that you have decided not to merge; changing `dependabot.yml` does not guarantee already-open PRs disappear immediately.
5. Re-run `Android Quality Gate and APK Build` and `FastLove Security Pipeline` on GitHub. The Android Gradle build, Trivy findings, CodeQL analysis and emulator boot must be confirmed by those hosted runs; they have not been executed in this file-preparation environment.

## Important Trivy interpretation

The unsupported `trivy-flags` input is removed, but its presence alone does not prove why the previous Trivy scan returned `failure`; GitHub Action inputs can be ignored with a warning. The repaired workflow preserves a non-zero exit for configured HIGH/CRITICAL findings. Review the Trivy log and SARIF artifact after the next run to determine whether a failure is caused by real findings or by a scanner/setup error.
