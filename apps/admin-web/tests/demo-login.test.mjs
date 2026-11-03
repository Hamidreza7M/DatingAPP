import test from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { mkdtemp, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

test('demo credentials admin / 123456 work without environment overrides', async () => {
  const dataDir = await mkdtemp(path.join(os.tmpdir(), 'fastlove-demo-login-'));
  const port = 44000 + Math.floor(Math.random() * 1000);
  const child = spawn(process.execPath, ['server.mjs'], {
    cwd: ROOT,
    env: {
      ...process.env,
      PORT: String(port),
      DATA_DIR: dataDir,
      NODE_ENV: 'development',
      ADMIN_USERNAME: '',
      ADMIN_PASSWORD: '',
      SESSION_SECRET: ''
    },
    stdio: 'ignore'
  });
  const base = `http://127.0.0.1:${port}`;
  const pause = ms => new Promise(resolve => setTimeout(resolve, ms));

  try {
    let healthy = false;
    for (let i = 0; i < 60; i++) {
      try {
        const response = await fetch(`${base}/api/health`);
        if (response.ok) { healthy = true; break; }
      } catch {}
      await pause(100);
    }
    assert.equal(healthy, true, 'demo API should start');

    const login = await fetch(`${base}/api/auth/login`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ username: 'admin', password: '123456' })
    });
    assert.equal(login.status, 200, 'default demo credentials must authenticate');
    assert.match(login.headers.get('set-cookie') || '', /fl_admin_session=/);

    const me = await fetch(`${base}/api/auth/me`, {
      headers: { Cookie: login.headers.get('set-cookie').split(';')[0] }
    });
    assert.equal(me.status, 200);
    assert.equal((await me.json()).admin.username, 'admin');
  } finally {
    child.kill('SIGTERM');
    await pause(100);
    await rm(dataDir, { recursive: true, force: true });
  }
});
