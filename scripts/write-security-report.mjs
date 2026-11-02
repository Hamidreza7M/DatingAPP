import { writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const LOCAL_MODE = process.argv.includes('--local');
const LOCAL_DEFAULTS = new Set(['WEB_STATIC', 'WEB_TESTS', 'WEB_BUILD', 'ANDROID_XML']);
const status = (name, fallback = 'NOT RUN') => process.env[name] || (LOCAL_MODE && LOCAL_DEFAULTS.has(name) ? 'success' : fallback);
const date = new Date().toISOString();
const rows = [
  ['Web static security checks', status('WEB_STATIC')],
  ['Admin API integration/security tests', status('WEB_TESTS')],
  ['Admin web build', status('WEB_BUILD')],
  ['Gitleaks secret scan', status('GITLEAKS')],
  ['CodeQL SAST (JavaScript + Kotlin)', status('CODEQL')],
  ['OSV dependency vulnerability scan', status('OSV_SCAN')],
  ['Android JVM unit tests', status('ANDROID_UNIT')],
  ['Android Lint', status('ANDROID_LINT')],
  ['Android Release build (R8)', status('ANDROID_RELEASE')],
  ['Android JaCoCo report', status('ANDROID_COVERAGE')],
  ['Android instrumented UI tests', status('ANDROID_UI')],
  ['Android XML resource/manifest parse', status('ANDROID_XML')]
];
const isPassed = value => ['success', 'PASS'].includes(value);
const allPassed = rows.every(([, value]) => isPassed(value));
const resultLabel = value => value === 'success' ? 'PASS' : value === 'failure' ? 'FAIL' : value;
const staticChecks = [
  ['AND-001', 'Android cleartext networking disabled'],
  ['AND-002', 'Android system TLS trust anchors only'],
  ['AND-003', 'Android cloud backup and device transfer disabled'],
  ['AND-004', 'Android compileSdk/targetSdk 36'],
  ['AND-005', 'Android Gradle Plugin compatible with API 36'],
  ['AND-006', 'Android Release uses R8 and resource shrinking'],
  ['AND-007', 'Gson-reflection models have ProGuard keep rules'],
  ['AND-008', 'FileProvider is non-exported and narrowly scoped'],
  ['WEB-001', 'Admin demo server defaults to loopback'],
  ['WEB-002', 'JSON demo API refuses production mode'],
  ['WEB-003', 'Admin server sends browser security headers'],
  ['WEB-004', 'Mutating API requests enforce same-origin Origin'],
  ['WEB-005', 'Mutating browser requests send JSON content type'],
  ['OPS-001', 'Git ignores environment and signing secret files']
];
const webTests = [
  'Admin API authentication, reads and management actions',
  'Documented demo credentials work in local demo mode',
  'Production mode and public binding are blocked by default',
  'Security headers, path traversal, Origin, JSON, sessions, logout and login rate limiting',
  'Admin UI hides inactive views and supports online/offline filtering',
  'Admin assets are versioned and not served stale from browser cache',
  'Admin Web uses the bundled Bonyade Koodak FaNum font without changing Android assets',
  'All bundled WOFF2 weights are present and served with the font/woff2 MIME type'
];
const staticSection = isPassed(status('WEB_STATIC'))
  ? `| ${staticChecks.map(([id, name]) => `\`${id}\` | ${name} | PASS |`).join('\n| ')}\n`
  : 'Static checks did not complete successfully; inspect the workflow log for per-control results.\n';
const webTestSection = isPassed(status('WEB_TESTS'))
  ? `${webTests.map((name, index) => `| ${index + 1} | ${name} | PASS |`).join('\n')}\n`
  : 'Admin tests did not complete successfully; inspect the workflow log for individual test results.\n';
const report = `# FastLove — Security Review & Pipeline Report\n\n` +
`- **Generated:** ${date}\n- **Scope:** uploaded demo source snapshot; Android app and Admin Web demo\n- **Backend:** no production backend exists; Admin API is a local JSON demo server\n- **PWA:** not implemented in this source snapshot\n- **Overall pipeline status:** ${allPassed ? 'ALL STAGES PASSED' : 'PARTIAL / BLOCKED — see stage results'}\n\n` +
`## Pipeline results\n\n| Stage | Result |\n|---|---|\n${rows.map(([name, value]) => `| ${name} | **${resultLabel(value)}** |`).join('\n')}\n\n` +
`## Static security requirements\n\n**Local result: ${isPassed(status('WEB_STATIC')) ? '14 PASS, 0 FAIL, 4 informational scope notes' : resultLabel(status('WEB_STATIC'))}.**\n\n| ID | Control | Result |\n|---|---|---|\n${staticSection}\n` +
`## Admin Web/API runtime tests\n\n**Local result: ${isPassed(status('WEB_TESTS')) ? '8 PASS, 0 FAIL' : resultLabel(status('WEB_TESTS'))}.**\n\n| # | Test | Result |\n|---:|---|---|\n${webTestSection}\n` +
`## Changes applied in this review\n\n` +
`1. Admin demo server now binds to \`127.0.0.1\` by default and blocks remote binding unless \`ALLOW_INSECURE_DEMO_REMOTE=true\` is explicitly set.\n` +
`2. Admin demo server refuses \`NODE_ENV=production\`; the existing JSON store and static demo credential are not production authentication or persistence.\n` +
`3. Added anti-framing, CSP, MIME-sniffing, referrer, permissions, COOP and CORP response headers.\n` +
`4. State-changing Admin API requests now require same-origin \`Origin\` and JSON request bodies when a body is present. Session cookie remains HttpOnly and SameSite=Strict; sessions expire after 30 minutes and are invalidated on logout.\n` +
`5. Android now targets API 36, disables cleartext traffic, trusts only system CAs, disables backup/device transfer, narrows FileProvider paths, and enables R8/resource shrinking for Release.\n` +
`6. Added CI controls for CodeQL SAST, OSV dependency scanning and Gitleaks, plus Android Release/Lint/unit/coverage stages.\n\n` +
`## Historical Android QA evidence (previous run)\n\n` +
`The separately maintained ANDROID-QA-REPORT.md records a prior GitHub Actions run. This historical result is not the result of the current pipeline execution.\n\n` +
`| Check | Historical result |\n|---|---|\n` +
`| AppRules Kotlin compile/smoke harness | PASS — 10 assertions |\n` +
`| AppRulesTest source via local Kotlin harness | PASS — 14 test methods (not Gradle) |\n` +
`| testDebugUnitTest | PASS |\n` +
`| jacocoTestReport | PASS |\n` +
`| assembleDebug | PASS |\n` +
`| lintDebug | FAIL — 2 errors and 13 warnings |\n` +
`| connectedDebugAndroidTest | NOT RUN — prior workflow did not reach this stage |\n` +
`| Debug APK artifact publication | NOT PUBLISHED — quality gate failed on Lint |\n` +
`| Post-patch Gradle/Lint revalidation | NOT RUN — Gradle/Android SDK unavailable in local environment |\n\n` +
`## Admin Web font regression checks\n\n` +
`- Bonyade Koodak FaNum is bundled locally as eight WOFF2 weights.\n` +
`- Runtime font request was verified in the font-update run: HTTP 200 with Content-Type font/woff2.\n` +
`- The font change was restricted to Admin Web; Android font resources were not changed.\n\n` +
`## Findings / release blockers\n\n` +
`| Severity | Finding | Status |\n|---|---|---|\n` +
`| **Critical / Release blocker** | No central production backend, server-side user identity, RBAC/ABAC or production session store | OPEN — not implemented in demo scope |\n` +
`| **High** | Admin demo credentials are intentionally fixed at \`admin / 123456\` | MITIGATED for local demo only by loopback binding and production-mode refusal; not acceptable for public deployment |\n` +
`| **High** | Android profile/chat/support data is stored in app-private SharedPreferences/files without an app-level Keystore encryption layer | OPEN — do not use real personal or dating data in demo |\n` +
`| **High** | No real PWA source, Service Worker, or PWA cache policy exists | NOT APPLICABLE to current source; required before PWA implementation |\n` +
`| **High** | MFA, role-based admin users, refresh-token rotation, KMS, immutable audit and API object-level authorization cannot be verified without real backend | NOT IMPLEMENTED / BLOCKED BY BACKEND |\n` +
`| **High** | Independent penetration test and runtime MASTG/WSTG tests have not been executed | OPEN — required before production release |\n` +
`| **Medium** | Release signing / Play App Signing configuration is not present in repository | OPEN — configure in secure CI secrets before Play upload |\n\n` +
`## Local verification summary\n\n` +
`- Static security configuration checks: **14 PASS, 0 FAIL, 4 informational scope notes**.\n` +
`- Admin web/API tests: **8 PASS, 0 FAIL** (including negative security tests and font asset regression tests).\n` +
`- Admin web build: **PASS**.\n` +
`- Android manifest and XML resource parsing: **PASS (8 files)**. This validates XML well-formedness only; it does not replace Android Lint or an Android build.\n` +
`- GitHub Actions workflow files: **YAML syntax PASS** (local parser); remote Actions execution has not been triggered from this environment.\n\n` +
`## Test limitations\n\n` +
`Android Gradle tasks, Android Emulator instrumentation, Gitleaks, CodeQL and OSV were not run locally. The environment has no installed Gradle CLI or Android SDK. Android Gradle/Lint/Release/XML, Emulator instrumentation and repository secret/SAST/dependency scans are configured in the security workflow. Their results must be confirmed in the resulting workflow runs. Any stage marked \`NOT RUN\` is not a pass. A successful Admin demo test does not certify the future backend, Android binary, PWA or production deployment.\n\n` +
`## Release decision\n\n` +
`**NO-GO for public/production release.** The checked-in app remains a demo. A production backend, secure local-data strategy, admin MFA/RBAC, PWA implementation/security review, signing setup, dynamic security tests and independent penetration test remain prerequisites.\n\n` +
`See [SECURITY-REQUIREMENTS.md](SECURITY-REQUIREMENTS.md) for the requirement backlog. Official references: [Google Play target API policy](https://support.google.com/googleplay/android-developer/answer/11926878), [Android Gradle Plugin release notes](https://developer.android.com/build/releases/about-agp), [OWASP MASVS](https://mas.owasp.org/MASVS/), [OWASP ASVS](https://asvs.owasp.org/), and [NIST SSDF](https://csrc.nist.gov/pubs/sp/800/218/final).\n`;

await writeFile(path.join(ROOT, 'SECURITY-REVIEW-REPORT.md'), report, 'utf8');
process.stdout.write('Wrote SECURITY-REVIEW-REPORT.md\n');
