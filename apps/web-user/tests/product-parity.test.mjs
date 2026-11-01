import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { PRODUCT_FEATURES, PEOPLE, DEFAULT_PACKAGES, createInitialState } from '../../../packages/user-core/src/data.js';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');

test('shared product core includes all Android user journeys', () => {
  const ids = new Set(PRODUCT_FEATURES.map(feature => feature.id));
  for (const id of ['discover','likes','map','chats','profile','dates','subscription','payments','blocked','privacy','verification','support']) assert.ok(ids.has(id), `missing feature ${id}`);
});

test('demo data and dynamic purchase packages are initialized', () => {
  assert.ok(PEOPLE.length >= 20);
  assert.ok(DEFAULT_PACKAGES.some(p => p.type === 'subscription'));
  assert.ok(DEFAULT_PACKAGES.some(p => p.type === 'direct'));
  const state = createInitialState();
  assert.ok(state.dates.length >= 1);
  assert.ok(state.chats.length >= 1);
  assert.ok(state.tickets.length >= 1);
});

test('responsive web and PWA import the exact same shared app entry', () => {
  const web = fs.readFileSync(path.join(root,'apps/web-user/main.js'),'utf8');
  const pwa = fs.readFileSync(path.join(root,'apps/pwa/main.js'),'utf8');
  assert.match(web,/mountFastLove\(root, \{ variant: 'web' \}\)/);
  assert.match(pwa,/mountFastLove\(root, \{ variant: 'pwa' \}\)/);
});

test('responsive shell declares RTL viewport and accessible app root', () => {
  const html = fs.readFileSync(path.join(root,'apps/web-user/index.html'),'utf8');
  assert.match(html,/name="viewport"/);
  assert.match(html,/lang="fa" dir="rtl"/);
  assert.match(html,/id="app"/);
});
