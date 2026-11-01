import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const appName = process.argv[2] || 'web-user';
if (!['web-user', 'pwa'].includes(appName)) throw new Error('Usage: node scripts/serve-user.mjs web-user|pwa');
const useDist = process.env.FASTLOVE_USE_DIST === '1';
const appRoot = path.join(repoRoot, 'apps', appName, ...(useDist ? ['dist'] : []));
const packageRoot = useDist ? path.join(appRoot, 'packages') : path.join(repoRoot, 'packages');
const defaults = { 'web-user': 4178, pwa: 4179 };
const port = Number(process.env.PORT || process.env.FASTLOVE_PORT || defaults[appName]);
const mime = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.json': 'application/json; charset=utf-8', '.webmanifest': 'application/manifest+json; charset=utf-8',
  '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.webp': 'image/webp',
};

function resolveFile(urlPath) {
  const decoded = decodeURIComponent(urlPath.split('?')[0]);
  if (decoded === '/' || decoded === '/index.html') return path.join(appRoot, 'index.html');
  if (decoded.startsWith('/packages/')) return path.join(packageRoot, decoded.slice('/packages/'.length));
  const clean = path.normalize(decoded.replace(/^\/+/, ''));
  const result = path.resolve(appRoot, clean);
  if (!result.startsWith(path.resolve(appRoot) + path.sep) && result !== path.resolve(appRoot)) return null;
  return result;
}

const server = http.createServer((req, res) => {
  if (req.url === '/healthz') { res.writeHead(200, {'Content-Type':'application/json'}); res.end(JSON.stringify({ok:true,app:appName,mode:useDist?'dist':'development'})); return; }
  const file = resolveFile(req.url || '/');
  if (!file) { res.writeHead(403); res.end('Forbidden'); return; }
  fs.readFile(file, (error, data) => {
    if (error) { res.writeHead(error.code === 'ENOENT' ? 404 : 500, {'Content-Type':'text/plain; charset=utf-8'}); res.end(error.code === 'ENOENT' ? 'Not found' : 'Server error'); return; }
    const ext = path.extname(file).toLowerCase();
    res.writeHead(200, {
      'Content-Type': mime[ext] || 'application/octet-stream',
      'X-Content-Type-Options': 'nosniff',
      'Referrer-Policy': 'strict-origin-when-cross-origin',
      'Cache-Control': ext === '.html' ? 'no-cache' : 'public, max-age=300',
    });
    res.end(data);
  });
});

server.listen(port, '0.0.0.0', () => {
  console.log(`FastLove ${appName} (${useDist?'dist':'development'}) at http://localhost:${port}`);
});
