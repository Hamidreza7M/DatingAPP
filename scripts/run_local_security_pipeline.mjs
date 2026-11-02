import { spawn } from 'node:child_process';
import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const WEB_EVIDENCE = path.join(ROOT, '.ci-evidence', 'web');
const ANDROID_EVIDENCE = path.join(ROOT, '.ci-evidence', 'android');
const npmCommand = process.platform === 'win32' ? 'npm.cmd' : 'npm';
const pythonCommand = process.env.PYTHON || (process.platform === 'win32' ? 'python' : 'python3');

await mkdir(WEB_EVIDENCE, { recursive: true });
await mkdir(path.join(ANDROID_EVIDENCE, 'logs'), { recursive: true });

const results = {};
const evidence = [];

function run(name, command, args, logPath, env = {}) {
  return new Promise(resolve => {
    const child = spawn(command, args, {
      cwd: ROOT,
      env: { ...process.env, ...env },
      shell: process.platform === 'win32',
      windowsHide: true,
      stdio: ['ignore', 'pipe', 'pipe']
    });
    let output = '';
    child.stdout.on('data', chunk => { const value = chunk.toString(); output += value; process.stdout.write(value); });
    child.stderr.on('data', chunk => { const value = chunk.toString(); output += value; process.stderr.write(value); });
    child.on('error', error => {
      output += `\nProcess launch error: ${error.message}\n`;
      resolve({ name, ok: false, output, code: null });
    });
    child.on('close', code => resolve({ name, ok: code === 0, output, code }));
  }).then(async result => {
    const heading = `===== ${name} | ${result.ok ? 'PASS' : 'FAIL'} | exit=${result.code ?? 'launch-error'} =====\n`;
    await writeFile(logPath, heading + result.output, 'utf8');
    evidence.push(heading + result.output);
    results[name] = result.ok ? 'success' : 'failure';
    return result;
  });
}

const stages = [
  ['Static security checks', process.execPath, ['scripts/security-check.mjs'], path.join(WEB_EVIDENCE, 'security-check.log')],
  ['Admin Web/API tests', npmCommand, ['--prefix', 'apps/admin-web', 'test'], path.join(WEB_EVIDENCE, 'admin-tests.tap')],
  ['Admin Web build', npmCommand, ['run', 'admin:build'], path.join(WEB_EVIDENCE, 'admin-build.log')],
  ['Android XML validation', pythonCommand, ['scripts/validate_android_xml.py'], path.join(ANDROID_EVIDENCE, 'logs', 'android-xml.log')]
];

for (const [name, command, args, logPath] of stages) {
  console.log(`\n--- Running ${name} ---`);
  await run(name, command, args, logPath);
}

const combined = [
  'FastLove local security verification log',
  `Generated: ${new Date().toISOString()}`,
  '',
  ...evidence
].join('\n');
await writeFile(path.join(ROOT, 'SECURITY-TEST-LOG.txt'), combined, 'utf8');

const reportEnv = {
  WEB_STATIC: results['Static security checks'],
  WEB_TESTS: results['Admin Web/API tests'],
  WEB_BUILD: results['Admin Web build'],
  ANDROID_XML: results['Android XML validation'],
  REPORT_MODE: 'local'
};

console.log('\n--- Generating consolidated Markdown report ---');
const mdReport = await run('Markdown security report', process.execPath, ['scripts/write-security-report.mjs'], path.join(WEB_EVIDENCE, 'markdown-report.log'), reportEnv);
console.log('\n--- Generating consolidated PDF report ---');
const pdfReport = await run('Comprehensive PDF report', pythonCommand, ['scripts/generate_comprehensive_report.py', '--local'], path.join(WEB_EVIDENCE, 'pdf-report.log'), reportEnv);

const failed = Object.entries(results).filter(([, value]) => value !== 'success');
console.log('\n=== Local verification summary ===');
for (const [name, value] of Object.entries(results)) console.log(`${value.toUpperCase()}: ${name}`);
console.log(`PDF report: reports/FastLove-Comprehensive-Test-Security-Report.pdf`);
if (!mdReport.ok || !pdfReport.ok) console.error('Report generation failed; inspect .ci-evidence/web logs.');
if (failed.length || !mdReport.ok || !pdfReport.ok) process.exitCode = 1;
