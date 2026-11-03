import test from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { mkdtemp, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

test('production security controls: headers, session cookie, origin, auth, rate limit and body cap', async () => {
  const dataDir = await mkdtemp(path.join(os.tmpdir(), 'fastlove-security-test-'));
  const port = 45000 + Math.floor(Math.random() * 1000);
  const base = `http://127.0.0.1:${port}`;
  const expectedOrigin = base;
  const child = spawn(process.execPath, ['server.mjs'], {
    cwd: ROOT,
    env: {
      ...process.env,
      PORT: String(port),
      DATA_DIR: dataDir,
      NODE_ENV: 'production',
      PUBLIC_ORIGIN: expectedOrigin,
      ADMIN_USERNAME: 'security-admin',
      ADMIN_PASSWORD: 'scan-only-strong-password',
      SESSION_SECRET: 'test-only-session-secret-at-least-32-bytes-long'
    },
    stdio: 'ignore'
  });
  const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
  async function waitForServer() {
    for (let i = 0; i < 50; i++) {
      try {
        const response = await fetch(`${base}/api/health`);
        if (response.ok) return;
      } catch {}
      await pause(100);
    }
    throw new Error('Security test server did not start');
  }

  try {
    await waitForServer();

    const health = await fetch(`${base}/api/health`);
    assert.equal(health.status, 200);
    assert.equal(health.headers.get('x-content-type-options'), 'nosniff');
    assert.equal(health.headers.get('x-frame-options'), 'DENY');
    assert.equal(health.headers.get('referrer-policy'), 'strict-origin-when-cross-origin');
    assert.match(health.headers.get('content-security-policy') || '', /default-src 'self'/);
    assert.match(health.headers.get('content-security-policy') || '', /media-src 'self' https:/);
    assert.equal(health.headers.get('permissions-policy'), 'camera=(), microphone=(), geolocation=()');
    assert.match(health.headers.get('strict-transport-security') || '', /max-age=31536000/);

    const unauthenticated = await fetch(`${base}/api/users`);
    assert.equal(unauthenticated.status, 401, 'private API routes must require a session');

    const crossOrigin = await fetch(`${base}/api/auth/login`, {
      method: 'POST',
      headers: { 'content-type': 'application/json', origin: 'https://attacker.invalid' },
      body: JSON.stringify({ username: 'security-admin', password: 'scan-only-strong-password' })
    });
    assert.equal(crossOrigin.status, 403, 'cross-origin state-changing requests must be rejected');

    const login = await fetch(`${base}/api/auth/login`, {
      method: 'POST',
      headers: { 'content-type': 'application/json', origin: expectedOrigin },
      body: JSON.stringify({ username: 'security-admin', password: 'scan-only-strong-password' })
    });
    assert.equal(login.status, 200);
    const cookieHeader = login.headers.get('set-cookie') || '';
    assert.match(cookieHeader, /HttpOnly/i);
    assert.match(cookieHeader, /SameSite=Strict/i);
    assert.match(cookieHeader, /Secure/i);
    const cookie = cookieHeader.split(';')[0];

    const authorized = await fetch(`${base}/api/users`, { headers: { cookie } });
    assert.equal(authorized.status, 200, 'valid session should access private API routes');

    const forbiddenMutation = await fetch(`${base}/api/packages/pro-monthly`, {
      method: 'PATCH',
      headers: { cookie, origin: 'https://attacker.invalid', 'content-type': 'application/json' },
      body: JSON.stringify({ price: 1 })
    });
    assert.equal(forbiddenMutation.status, 403);

    const oversized = await fetch(`${base}/api/auth/login`, {
      method: 'POST',
      headers: { 'content-type': 'application/json', origin: expectedOrigin },
      body: JSON.stringify({ username: 'x'.repeat(1_100_000), password: 'irrelevant' })
    });
    assert.equal(oversized.status, 413, 'request bodies above 1 MiB must be rejected');

    for (let i = 0; i < 8; i++) {
      const invalidLogin = await fetch(`${base}/api/auth/login`, {
        method: 'POST',
        headers: { 'content-type': 'application/json', origin: expectedOrigin },
        body: JSON.stringify({ username: 'security-admin', password: `wrong-${i}` })
      });
      assert.equal(invalidLogin.status, 401);
    }
    const lockedLogin = await fetch(`${base}/api/auth/login`, {
      method: 'POST',
      headers: { 'content-type': 'application/json', origin: expectedOrigin },
      body: JSON.stringify({ username: 'security-admin', password: 'scan-only-strong-password' })
    });
    assert.equal(lockedLogin.status, 429, 'login attempts must be throttled after the configured limit');
  } finally {
    child.kill('SIGTERM');
    await pause(100);
    await rm(dataDir, { recursive: true, force: true });
  }
});
