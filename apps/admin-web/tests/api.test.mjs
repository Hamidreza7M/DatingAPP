import test from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { mkdtemp, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

test('admin API: auth, read routes and management actions', async () => {
  const dataDir = await mkdtemp(path.join(os.tmpdir(), 'fastlove-admin-test-'));
  const port = 43000 + Math.floor(Math.random() * 1000);
  const child = spawn(process.execPath, ['server.mjs'], {
    cwd: ROOT,
    env: { ...process.env, PORT: String(port), DATA_DIR: dataDir, NODE_ENV: 'test', ADMIN_USERNAME: 'test-admin', ADMIN_PASSWORD: 'test-password', SESSION_SECRET: 'test-session-secret' },
    stdio: 'ignore'
  });
  const base = `http://127.0.0.1:${port}`;
  const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
  async function waitForServer() {
    for (let i = 0; i < 50; i++) {
      try { const r = await fetch(`${base}/api/health`); if (r.ok) return; } catch {}
      await pause(100);
    }
    throw new Error('Test server did not start');
  }
  try {
    await waitForServer();
    const unauth = await fetch(`${base}/api/users`);
    assert.equal(unauth.status, 401, 'private routes must require a session');

    const invalid = await fetch(`${base}/api/auth/login`, { method:'POST', headers:{'content-type':'application/json'}, body:JSON.stringify({username:'test-admin',password:'wrong'}) });
    assert.equal(invalid.status, 401, 'invalid credentials must be rejected');

    const login = await fetch(`${base}/api/auth/login`, { method:'POST', headers:{'content-type':'application/json'}, body:JSON.stringify({username:'test-admin',password:'test-password'}) });
    assert.equal(login.status, 200);
    const cookie = login.headers.get('set-cookie').split(';')[0];
    const request = (route, options={}) => fetch(`${base}${route}`, { ...options, headers:{ ...(options.body?{'content-type':'application/json'}:{}), Cookie:cookie, ...(options.headers||{}) } });

    for (const route of ['/api/auth/me','/api/dashboard','/api/users','/api/users/USR-1001','/api/payments','/api/payments/PAY-8001','/api/reports','/api/verifications','/api/tickets','/api/likes','/api/matches','/api/dates','/api/packages','/api/audit']) {
      const response = await request(route);
      assert.equal(response.status, 200, `${route} should return 200`);
    }

    const report = await request('/api/reports/RPT-5001', { method:'PATCH', body:JSON.stringify({status:'approved',note:'test review'}) });
    assert.equal(report.status, 200); assert.equal((await report.json()).report.status, 'approved');
    const reply = await request('/api/tickets/TKT-2001/reply', { method:'POST', body:JSON.stringify({text:'test admin reply'}) });
    assert.equal(reply.status, 200); assert.equal((await reply.json()).ticket.messages.length, 4);
    const close = await request('/api/tickets/TKT-2001/status', { method:'PATCH', body:JSON.stringify({status:'closed'}) });
    assert.equal(close.status, 200); assert.equal((await close.json()).ticket.status, 'closed');
    const price = await request('/api/packages/pro-monthly', { method:'PATCH', body:JSON.stringify({price:420000,active:true}) });
    assert.equal(price.status, 200); assert.equal((await price.json()).package.price, 420000);
    const verify = await request('/api/verifications/VER-3001', { method:'PATCH', body:JSON.stringify({status:'verified'}) });
    assert.equal(verify.status, 200); assert.equal((await verify.json()).verification.status, 'verified');
    const remove = await request('/api/users/USR-1008', { method:'DELETE' });
    assert.equal(remove.status, 200); assert.equal((await remove.json()).user.status, 'deleted');
  } finally {
    child.kill('SIGTERM');
    await pause(100);
    await rm(dataDir, { recursive:true, force:true });
  }
});
