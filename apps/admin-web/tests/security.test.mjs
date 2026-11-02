import test from 'node:test';
import assert from 'node:assert/strict';
import { spawn, spawnSync } from 'node:child_process';
import { mkdtemp, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

function blockedStartup(extraEnv = {}) {
  return spawnSync(process.execPath, ['server.mjs'], {
    cwd: ROOT,
    env: { ...process.env, NODE_ENV: 'test', DATA_DIR: path.join(os.tmpdir(), `fastlove-blocked-${process.pid}`), ...extraEnv },
    encoding: 'utf8',
    timeout: 5000
  });
}

test('demo server refuses production mode and non-loopback binding by default', () => {
  const production = blockedStartup({ NODE_ENV: 'production' });
  assert.equal(production.status, 1);
  assert.match(production.stderr, /cannot run with NODE_ENV=production/i);

  const remote = blockedStartup({ HOST: '0.0.0.0' });
  assert.equal(remote.status, 1);
  assert.match(remote.stderr, /Remote demo binding is disabled/i);
});

test('demo HTTP responses have security headers and unsafe requests enforce same-origin JSON', async () => {
  const dataDir = await mkdtemp(path.join(os.tmpdir(), 'fastlove-security-test-'));
  const port = 45000 + Math.floor(Math.random() * 1000);
  const base = `http://127.0.0.1:${port}`;
  const child = spawn(process.execPath, ['server.mjs'], {
    cwd: ROOT,
    env: { ...process.env, PORT: String(port), HOST: '127.0.0.1', DATA_DIR: dataDir, NODE_ENV: 'test', ADMIN_USERNAME: 'security-admin', ADMIN_PASSWORD: 'security-test-password' },
    stdio: 'ignore'
  });

  try {
    let ready = false;
    for (let i = 0; i < 60; i++) {
      try { const response = await fetch(`${base}/api/health`); if (response.ok) { ready = true; break; } } catch {}
      await wait(100);
    }
    assert.equal(ready, true, 'local demo API should start');

    const page = await fetch(base);
    assert.equal(page.status, 200);
    assert.equal(page.headers.get('x-content-type-options'), 'nosniff');
    assert.equal(page.headers.get('x-frame-options'), 'DENY');
    assert.equal(page.headers.get('referrer-policy'), 'no-referrer');
    assert.match(page.headers.get('content-security-policy') || '', /frame-ancestors 'none'/);

    const traversal = await fetch(`${base}/%2e%2e%2fserver.mjs`);
    assert.equal(traversal.status, 403, 'encoded path traversal must not expose server source');

    const health = await fetch(`${base}/api/health`);
    assert.equal(health.headers.get('cache-control'), 'no-store');
    assert.equal(health.headers.get('x-content-type-options'), 'nosniff');

    const noOrigin = await fetch(`${base}/api/auth/login`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ username: 'security-admin', password: 'security-test-password' })
    });
    assert.equal(noOrigin.status, 403, 'unsafe requests without Origin must be rejected');

    const badOrigin = await fetch(`${base}/api/auth/login`, {
      method: 'POST', headers: { 'content-type': 'application/json', Origin: 'https://attacker.example' },
      body: JSON.stringify({ username: 'security-admin', password: 'security-test-password' })
    });
    assert.equal(badOrigin.status, 403, 'cross-origin unsafe requests must be rejected');

    const badContentType = await fetch(`${base}/api/auth/login`, {
      method: 'POST', headers: { 'content-type': 'text/plain', Origin: base }, body: 'username=admin&password=123'
    });
    assert.equal(badContentType.status, 415, 'non-JSON request bodies must be rejected');

    const login = await fetch(`${base}/api/auth/login`, {
      method: 'POST', headers: { 'content-type': 'application/json', Origin: base },
      body: JSON.stringify({ username: 'security-admin', password: 'security-test-password' })
    });
    assert.equal(login.status, 200);
    const cookieHeader = login.headers.get('set-cookie') || '';
    assert.match(cookieHeader, /HttpOnly/i);
    assert.match(cookieHeader, /SameSite=Strict/i);
    assert.match(cookieHeader, /Path=\//i);
    const cookie = cookieHeader.split(';')[0];

    const logout = await fetch(`${base}/api/auth/logout`, {
      method: 'POST', headers: { Origin: base, Cookie: cookie }
    });
    assert.equal(logout.status, 200);
    const me = await fetch(`${base}/api/auth/me`, { headers: { Cookie: cookie } });
    assert.equal(me.status, 401, 'logout must invalidate the server-side session');

    const malformedCookie = await fetch(`${base}/api/auth/me`, { headers: { Cookie: 'fl_admin_session=%E0%A4%A' } });
    assert.equal(malformedCookie.status, 401, 'malformed cookies should be ignored safely');

    for (let attempt = 0; attempt < 8; attempt++) {
      const rejected = await fetch(`${base}/api/auth/login`, {
        method: 'POST', headers: { 'content-type': 'application/json', Origin: base },
        body: JSON.stringify({ username: 'security-admin', password: 'wrong-password' })
      });
      assert.equal(rejected.status, 401);
    }
    const throttled = await fetch(`${base}/api/auth/login`, {
      method: 'POST', headers: { 'content-type': 'application/json', Origin: base },
      body: JSON.stringify({ username: 'security-admin', password: 'wrong-password' })
    });
    assert.equal(throttled.status, 429, 'login rate limit must throttle repeated failed attempts');
  } finally {
    child.kill('SIGTERM');
    await wait(100);
    await rm(dataDir, { recursive: true, force: true });
  }
});
