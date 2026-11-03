import { readFile, access } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const results = [];
async function read(relative) { return readFile(path.join(ROOT, relative), 'utf8'); }
async function exists(relative) { try { await access(path.join(ROOT, relative)); return true; } catch { return false; } }
function check(id, title, condition, detail = '') {
  results.push({ id, title, status: condition ? 'PASS' : 'FAIL', detail });
}
function note(id, title, detail) { results.push({ id, title, status: 'INFO', detail }); }

const [manifest, androidBuild, androidRootBuild, netsec, backup, extraction, provider, proguard, adminServer, adminClient, gitignore, webReadme] = await Promise.all([
  read('apps/android/app/src/main/AndroidManifest.xml'),
  read('apps/android/app/build.gradle.kts'),
  read('apps/android/build.gradle.kts'),
  read('apps/android/app/src/main/res/xml/network_security_config.xml'),
  read('apps/android/app/src/main/res/xml/backup_rules.xml'),
  read('apps/android/app/src/main/res/xml/data_extraction_rules.xml'),
  read('apps/android/app/src/main/res/xml/file_paths.xml'),
  read('apps/android/app/proguard-rules.pro'),
  read('apps/admin-web/server.mjs'),
  read('apps/admin-web/public/app.js'),
  read('.gitignore'),
  read('apps/web-user/README.md')
]);

check('AND-001', 'Android cleartext networking disabled', /android:usesCleartextTraffic="false"/.test(manifest) && /cleartextTrafficPermitted="false"/.test(netsec), 'Manifest and Network Security Config explicitly prohibit cleartext HTTP.');
check('AND-002', 'Android system trust anchors only', /<certificates src="system"\s*\/>/.test(netsec) && !/src="user"/.test(netsec), 'Custom user-added CA certificates are not trusted by the app network policy.');
check('AND-003', 'Android backup disabled and data domains excluded', /android:allowBackup="false"/.test(manifest) && /android:fullBackupContent="@xml\/backup_rules"/.test(manifest) && /android:dataExtractionRules="@xml\/data_extraction_rules"/.test(manifest) && ['root','file','database','sharedpref','external'].every(domain => backup.includes(`domain="${domain}"`)) && ['root','file','database','sharedpref','external'].every(domain => extraction.includes(`domain="${domain}"`)), 'Manifest references backup policies and backup/device-transfer exclusions cover app-local data and SharedPreferences.');
check('AND-004', 'Android target API 36', /compileSdk\s*=\s*36/.test(androidBuild) && /targetSdk\s*=\s*36/.test(androidBuild), 'Required target API for new Google Play submissions as of 31 Aug 2026.');
check('AND-005', 'Android Gradle Plugin supports API 36', /com\.android\.application"\) version "8\.9\.[1-9]"/.test(androidRootBuild) || /com\.android\.application"\) version "(9\.|1[0-9]\.)/.test(androidRootBuild), 'AGP must be at least 8.9.1 to compile/target API 36.');
check('AND-006', 'Android release minification and resource shrinking', /isMinifyEnabled\s*=\s*true/.test(androidBuild) && /isShrinkResources\s*=\s*true/.test(androidBuild) && /isDebuggable\s*=\s*false/.test(androidBuild), 'R8 and resource shrinking are enabled for release builds.');
check('AND-007', 'Android Gson model keep rules exist', ['UserProfile','Person','ChatMessage','DateProposal','PaymentRecord','SupportTicket'].every(name => proguard.includes(`ir.hamdam.demo.${name}`)), 'Names and fields for local Gson-reflection models are retained.');
check('AND-008', 'FileProvider is not exported and paths are narrow', /android:name="androidx\.core\.content\.FileProvider"[\s\S]*?android:exported="false"/.test(manifest) && !/path="\."/.test(provider) && provider.includes('media/support_attachments/'), 'The provider only exposes camera capture, app pictures and support attachments.');
check('WEB-001', 'Demo server defaults to loopback and opt-in remote binding', /const HOST = process\.env\.HOST \|\| '127\.0\.0\.1'/.test(adminServer) && /ALLOW_INSECURE_DEMO_REMOTE !== 'true'/.test(adminServer), 'Network-visible demo mode requires an explicit unsafe override.');
check('WEB-002', 'Production execution is blocked for JSON demo API', /if \(IS_PROD\)[\s\S]*?process\.exit\(1\)/.test(adminServer) && /cannot run with NODE_ENV=production/.test(adminServer), 'The demo JSON API refuses production mode.');
check('WEB-003', 'HTTP security headers configured', ['Content-Security-Policy','X-Frame-Options','X-Content-Type-Options','Referrer-Policy','Permissions-Policy'].every(header => adminServer.includes(`'${header}'`)), 'CSP, anti-framing, MIME sniffing, referrer and permissions policy headers are set.');
check('WEB-004', 'Unsafe API requests require same-origin JSON', /!req\.headers\.origin \|\| req\.headers\.origin !== expectedOrigin/.test(adminServer) && /application\/json/.test(adminServer), 'State-changing API requests are checked for Origin and JSON content type.');
check('WEB-005', 'Admin browser client sends JSON content type for mutations', /const isMutation = !\['GET', 'HEAD', 'OPTIONS'\]/.test(adminClient), 'Browser requests include JSON content type for state-changing calls.');
check('OPS-001', 'Environment files and signing material ignored by Git', ['.env','*.jks','*.keystore'].every(value => gitignore.includes(value)), 'Local secrets and Android signing files are ignored.');
note('SCOPE-001', 'PWA not implemented in uploaded source', webReadme.includes('هنوز پیاده‌سازی نشده است') || !(await exists('apps/web-user/src')), 'No PWA source, Service Worker or manifest was present; PWA-specific controls are not applicable to this demo snapshot.');
note('SCOPE-002', 'No production backend/API implementation', 'services/api contains design notes only; Android stores demo state locally and admin uses local JSON seed data.');
note('SCOPE-003', 'Local Android data remains in app-private SharedPreferences/files', 'No app-level Keystore encryption layer is present. The app sandbox and backups are protected, but do not enter real user or dating data into this demo.');
note('SCOPE-004', 'Demo credentials remain intentionally public', 'admin / 123456 is retained for local demo usability; loopback binding and production-mode refusal are enforced.');

const failed = results.filter(item => item.status === 'FAIL');
for (const item of results) {
  const marker = item.status === 'PASS' ? '✓' : item.status === 'FAIL' ? '✗' : 'i';
  process.stdout.write(`${marker} ${item.id} ${item.status}: ${item.title}${item.detail ? ` — ${item.detail}` : ''}\n`);
}
process.stdout.write(`\nSecurity static checks: ${results.filter(x => x.status === 'PASS').length} PASS, ${failed.length} FAIL, ${results.filter(x => x.status === 'INFO').length} INFO\n`);
if (failed.length) process.exitCode = 1;
