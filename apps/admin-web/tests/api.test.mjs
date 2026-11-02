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

    const invalid = await fetch(`${base}/api/auth/login`, { method:'POST', headers:{'content-type':'application/json', Origin:base}, body:JSON.stringify({username:'test-admin',password:'wrong'}) });
    assert.equal(invalid.status, 401, 'invalid credentials must be rejected');

    const login = await fetch(`${base}/api/auth/login`, { method:'POST', headers:{'content-type':'application/json', Origin:base}, body:JSON.stringify({username:'test-admin',password:'test-password'}) });
    assert.equal(login.status, 200);
    const cookie = login.headers.get('set-cookie').split(';')[0];
    const request = (route, options={}) => fetch(`${base}${route}`, { ...options, headers:{ ...(options.body?{'content-type':'application/json'}:{}), ...(!['GET','HEAD','OPTIONS'].includes((options.method||'GET').toUpperCase())?{Origin:base}:{}), Cookie:cookie, ...(options.headers||{}) } });

    for (const route of ['/api/auth/me','/api/dashboard','/api/users','/api/referrals','/api/users/USR-1001','/api/payments','/api/payments/PAY-8001','/api/reports','/api/verifications','/api/tickets','/api/likes','/api/matches','/api/dates','/api/chats','/api/chats/CHAT-9001','/api/packages','/api/audit']) {
      const response = await request(route);
      assert.equal(response.status, 200, `${route} should return 200`);
    }

    for (const [route, key] of [['/api/users','users'],['/api/payments','payments'],['/api/reports','reports'],['/api/verifications','verifications'],['/api/tickets','tickets'],['/api/likes','likes'],['/api/matches','matches'],['/api/dates','dates'],['/api/chats','chats'],['/api/packages','packages']]) {
      const response = await request(route);
      const payload = await response.json();
      assert.ok(payload[key].length >= 20, `${route} should contain at least twenty demo records`);
    }

    const referralData = await request('/api/referrals');
    const referralPayload = await referralData.json();
    assert.ok(referralPayload.totalReferred >= 20, 'referral reports include at least twenty attributed demo members');
    assert.ok(referralPayload.groups.some(group => group.owner.name && group.code && group.referredUsers.length), 'referral owner and referred members are explicit');
    const dateDetail = await request('/api/dates/DAT-1');
    const datePayload = await dateDetail.json();
    assert.ok(datePayload.date.location?.address && datePayload.date.location?.latitude, 'date detail includes a meeting address and coordinates');

    const chatDetail = await request('/api/chats/CHAT-9001');
    assert.equal(chatDetail.status, 200);
    assert.ok((await chatDetail.json()).chat.messages.length > 0, 'admin can read chat messages');
    const dashboard = await request('/api/dashboard');
    const dashboardData = await dashboard.json();
    assert.ok('profileCompletionRate' in dashboardData.totals && 'photoCoverageRate' in dashboardData.totals, 'product BI metrics are available');
    const userDetail = await request('/api/users/USR-1001');
    assert.ok((await userDetail.json()).user.photoUrls.length > 0, 'user photo URLs are available in detail');

    const onlineUsers = await request('/api/users?online=online');
    const onlinePayload = await onlineUsers.json();
    assert.ok(onlinePayload.users.length > 0, 'online filter should return demo users');
    assert.ok(onlinePayload.users.every(user => user.online === true), 'online filter must only return online users');
    const offlineUsers = await request('/api/users?online=offline');
    const offlinePayload = await offlineUsers.json();
    assert.ok(offlinePayload.users.length > 0, 'offline filter should return demo users');
    assert.ok(offlinePayload.users.every(user => user.online !== true), 'offline filter must only return offline users');

    const report = await request('/api/reports/RPT-5001', { method:'PATCH', body:JSON.stringify({status:'approved',note:'test review'}) });
    assert.equal(report.status, 200); assert.equal((await report.json()).report.status, 'approved');
    const reply = await request('/api/tickets/TKT-2001/reply', { method:'POST', body:JSON.stringify({text:'test admin reply'}) });
    assert.equal(reply.status, 200); assert.equal((await reply.json()).ticket.messages.length, 4);
    const close = await request('/api/tickets/TKT-2001/status', { method:'PATCH', body:JSON.stringify({status:'closed'}) });
    assert.equal(close.status, 200); assert.equal((await close.json()).ticket.status, 'closed');
    const price = await request('/api/packages/pro-monthly', { method:'PATCH', body:JSON.stringify({price:420000,active:true}) });
    assert.equal(price.status, 200); assert.equal((await price.json()).package.price, 420000);
    const createdPackage = await request('/api/packages', { method:'POST', body:JSON.stringify({type:'direct_message',name:'بسته پیام آزمایشی API',messageCount:25,price:180000}) });
    assert.equal(createdPackage.status, 201);
    const createdPayload = await createdPackage.json();
    assert.equal(createdPayload.package.messageCount, 25); assert.equal(createdPayload.package.active, true); assert.equal(createdPayload.package.currency, 'TOMAN');
    const toggledPackage = await request(`/api/packages/${createdPayload.package.id}`, { method:'PATCH', body:JSON.stringify({active:false}) });
    assert.equal((await toggledPackage.json()).package.active, false);
    const deletedPackage = await request(`/api/packages/${createdPayload.package.id}`, { method:'DELETE' });
    assert.equal(deletedPackage.status, 200);
    const verify = await request('/api/verifications/VER-3001', { method:'PATCH', body:JSON.stringify({status:'verified'}) });
    assert.equal(verify.status, 200); assert.equal((await verify.json()).verification.status, 'verified');
    const remove = await request('/api/users/USR-1008', { method:'DELETE' });
    assert.equal(remove.status, 200); assert.equal((await remove.json()).user.status, 'deleted');
    const deletedArchive = await request('/api/users?status=deleted');
    assert.ok((await deletedArchive.json()).users.some(user => user.id === 'USR-1008'), 'soft-deleted users remain available in archive');
  } finally {
    child.kill('SIGTERM');
    await pause(100);
    await rm(dataDir, { recursive:true, force:true });
  }
});
