# FastLove Android CI test reports

The `Android Quality Gate and APK Build` GitHub Actions workflow creates a report on every run, including failed runs. It deliberately continues through independent stages after a failure so the report can show all available test and build outcomes.

## Where to find the report

1. Open the repository on GitHub and select **Actions**.
2. Open **Android Quality Gate and APK Build**.
3. Open the relevant workflow run.
4. Read **Summary** for the stage outcomes, JVM and instrumented test totals, line coverage and Lint totals.
5. Download `fastlove-android-detailed-report-*` under **Artifacts** for the full Markdown and JSON reports, per-test results, raw JUnit XML, Gradle HTML reports, JaCoCo, Lint findings and console logs.
6. Download `fastlove-debug-apk-*` only when the workflow is green. The APK is withheld if any required quality-gate stage fails.

## Report contents

- Per-stage outcome: JVM unit tests, Android Lint, debug APK build, JaCoCo and Emulator UI tests.
- Per-suite totals: passed, failed, errors, skipped and execution duration.
- Per-test details: test class, case name, result, duration and failure detail where available.
- JaCoCo coverage: line, branch, instruction, method and class counters when generated.
- Android Lint: parsed errors and warnings with rule IDs and source locations when available.
- Compiler/deprecation warnings captured from Gradle console logs.

The report describes the actual outputs found in that workflow run. Missing reports are shown as unavailable rather than being interpreted as zero failures or 100% coverage.
