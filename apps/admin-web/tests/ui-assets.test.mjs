import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

test('admin UI hides inactive views and provides online/offline user filtering', async () => {
  const [css, html, js] = await Promise.all([
    readFile(path.join(ROOT, 'public/styles.css'), 'utf8'),
    readFile(path.join(ROOT, 'public/index.html'), 'utf8'),
    readFile(path.join(ROOT, 'public/app.js'), 'utf8')
  ]);
  assert.match(css, /\[hidden\]\s*\{\s*display\s*:\s*none\s*!important\s*\}/, 'hidden attribute must override component display styles');
  assert.match(html, /id="loginView"[^>]*hidden/, 'login view starts hidden until app initialization reveals it');
  assert.match(js, /function showApp\(admin\)[\s\S]*?\$\('#loginView'\)\.hidden\s*=\s*true[\s\S]*?\$\('#appView'\)\.hidden\s*=\s*false/, 'successful login must hide login view and reveal app');
  assert.match(js, /id="userOnlineFilter"/, 'user list must expose online filter');
  assert.match(js, /state\.usersOnline\s*=\s*e\.target\.value/, 'online filter must update UI state');
  assert.match(html, /data-page="referrals"/, 'referral report must be reachable from the sidebar');
  assert.match(js, /data-form=\"create-package\"/, 'admin can create dynamic packages from the UI');
  assert.match(js, /date-detail/, 'planned dates expose a detail action');
});


test('admin web assets are versioned and cannot be served stale from browser cache', async () => {
  const [html, server] = await Promise.all([
    readFile(path.join(ROOT, 'public/index.html'), 'utf8'),
    readFile(path.join(ROOT, 'server.mjs'), 'utf8')
  ]);
  assert.match(html, /styles\.css\?v=1\.0\.4/);
  assert.match(html, /app\.js\?v=1\.0\.4/);
  assert.match(server, /no-store, no-cache, must-revalidate/);
});
