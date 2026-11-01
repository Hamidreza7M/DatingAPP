import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');

test('PWA manifest defines installable standalone Persian RTL experience', () => {
  const manifest = JSON.parse(fs.readFileSync(path.join(root,'apps/pwa/manifest.webmanifest'),'utf8'));
  assert.equal(manifest.display,'standalone');
  assert.equal(manifest.dir,'rtl');
  assert.equal(manifest.lang,'fa');
  assert.ok(manifest.icons.some(icon=>icon.sizes==='192x192'));
  assert.ok(manifest.icons.some(icon=>icon.sizes==='512x512'));
});

test('service worker caches the shared app shell and serves navigation offline', () => {
  const sw = fs.readFileSync(path.join(root,'apps/pwa/sw.js'),'utf8');
  assert.match(sw,/addAll\(APP_SHELL\)/);
  assert.match(sw,/caches\.match\('\/'\)/);
  assert.match(sw,/self\.clients\.claim/);
});

test('PWA registers service worker through shared frontend bootstrap', () => {
  const app = fs.readFileSync(path.join(root,'packages/user-core/src/app.js'),'utf8');
  assert.match(app,/navigator\.serviceWorker\.register\('\/sw\.js'\)/);
});
