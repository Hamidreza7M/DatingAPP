import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const PUBLIC = path.join(ROOT, 'public');
const DATA_DIR = process.env.DATA_DIR || path.join(ROOT, 'data');
const DB_FILE = path.join(DATA_DIR, 'db.json');
const SEED_FILE = path.join(ROOT, 'data', 'seed.json');
const PORT = Number(process.env.PORT || 4177);
const HOST = process.env.HOST || '127.0.0.1';
const IS_PROD = process.env.NODE_ENV === 'production';
const IS_LOOPBACK_HOST = ['127.0.0.1', '::1', 'localhost'].includes(HOST.toLowerCase());
const ADMIN_USERNAME = process.env.ADMIN_USERNAME || 'admin';
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || '123456';
const SESSION_TTL = 30 * 60 * 1000;
const sessions = new Map();
const loginAttempts = new Map();

// This server is a local demo API backed by a JSON file, not a production backend.
// Fail closed instead of accidentally exposing demo credentials/data on the Internet.
if (IS_PROD) {
  console.error('FastLove demo server cannot run with NODE_ENV=production. Replace this JSON demo API with the production backend before deployment.');
  process.exit(1);
}
if (!IS_LOOPBACK_HOST && process.env.ALLOW_INSECURE_DEMO_REMOTE !== 'true') {
  console.error('Remote demo binding is disabled. Use HOST=127.0.0.1 or explicitly set ALLOW_INSECURE_DEMO_REMOTE=true on an isolated trusted network.');
  process.exit(1);
}

await fs.mkdir(DATA_DIR, { recursive: true });
try { await fs.access(DB_FILE); }
catch { await fs.copyFile(SEED_FILE, DB_FILE); }

let db = JSON.parse(await fs.readFile(DB_FILE, 'utf8'));
// Backward-compatible schema migration for existing local databases.
const seedData = JSON.parse(await fs.readFile(SEED_FILE, 'utf8'));
if (!Array.isArray(db.chats)) db.chats = seedData.chats || [];
// Local demo upgrades: preserve existing records and append missing synthetic records
// from the expanded seed so every admin area remains useful after upgrading v1.0.3.
if (!IS_PROD) {
  for (const [key, seedRows] of Object.entries(seedData)) {
    if (!Array.isArray(seedRows)) continue;
    if (!Array.isArray(db[key])) db[key] = [];
    const known = new Set(db[key].map(item => item?.id).filter(Boolean));
    for (const row of seedRows) {
      if (row?.id && !known.has(row.id)) { db[key].push(structuredClone(row)); known.add(row.id); }
    }
  }
}
for (const user of db.users || []) {
  if (!Array.isArray(user.photoUrls)) {
    const seedUser = seedData.users.find(item => item.id === user.id);
    user.photoUrls = !IS_PROD && seedUser?.photoUrls ? [...seedUser.photoUrls] : [];
  }
  if (user.status === 'deleted' && !user.deletedBy) user.deletedBy = 'unknown';
  if (!Object.hasOwn(user, 'referredByCode')) user.referredByCode = null;
}
for (const dateItem of db.dates || []) {
  const seededDate = seedData.dates.find(item => item.id === dateItem.id);
  if (!dateItem.location && seededDate?.location) dateItem.location = structuredClone(seededDate.location);
}
for (const item of db.packages || []) {
  if (item.type === 'credit') item.type = 'direct_message';
  item.currency = 'TOMAN';
  if (item.type === 'direct_message' && !Number.isInteger(item.messageCount)) {
    const count = String(item.id || '').match(/^credit-(\d+)$/) || String(item.name || '').match(/\d+/); item.messageCount = count ? Number(count[1] || count[0]) : 10;
  }
}
let writeQueue = Promise.resolve();
function persist() {
  const snapshot = JSON.stringify(db, null, 2);
  writeQueue = writeQueue.then(async () => {
    const temp = `${DB_FILE}.tmp`;
    await fs.writeFile(temp, snapshot, 'utf8');
    await fs.rename(temp, DB_FILE);
  });
  return writeQueue;
}
function now() { return new Date().toISOString(); }
function randomId(prefix) { return `${prefix}-${crypto.randomBytes(4).toString('hex').toUpperCase()}`; }
function safeEqual(a, b) {
  const ah = crypto.createHash('sha256').update(String(a)).digest();
  const bh = crypto.createHash('sha256').update(String(b)).digest();
  return crypto.timingSafeEqual(ah, bh);
}
function parseCookies(header = '') {
  const cookies = {};
  for (const pair of header.split(';').map(value => value.trim()).filter(Boolean)) {
    const i = pair.indexOf('=');
    if (i <= 0) continue;
    try {
      cookies[decodeURIComponent(pair.slice(0, i))] = decodeURIComponent(pair.slice(i + 1));
    } catch {
      // Ignore malformed cookie pairs instead of turning them into a server error.
    }
  }
  return cookies;
}
const SECURITY_HEADERS = {
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
  'Referrer-Policy': 'no-referrer',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=()',
  'Cross-Origin-Opener-Policy': 'same-origin',
  'Cross-Origin-Resource-Policy': 'same-origin',
  'Content-Security-Policy': "default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; form-action 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; media-src 'self' https:; connect-src 'self'"
};
function send(res, status, payload, headers = {}) {
  res.writeHead(status, { ...SECURITY_HEADERS, 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store', ...headers });
  res.end(JSON.stringify(payload));
}
function getSession(req) {
  const cookies = parseCookies(req.headers.cookie);
  const token = cookies.fl_admin_session || '';
  const session = sessions.get(token);
  if (!session || session.expiresAt < Date.now()) {
    if (token) sessions.delete(token);
    return null;
  }
  return { token, ...session };
}
function requireAdmin(req, res) {
  const session = getSession(req);
  if (!session) { send(res, 401, { error: 'برای ادامه وارد پنل مدیریت شوید.' }); return null; }
  session.expiresAt = Date.now() + SESSION_TTL;
  sessions.set(session.token, { username: session.username, expiresAt: session.expiresAt });
  return session;
}
async function readBody(req, maxBytes = 1024 * 1024) {
  const chunks = []; let size = 0;
  for await (const chunk of req) {
    size += chunk.length;
    if (size > maxBytes) throw Object.assign(new Error('حجم درخواست بیش از حد مجاز است.'), { status: 413 });
    chunks.push(chunk);
  }
  if (!chunks.length) return {};
  try { return JSON.parse(Buffer.concat(chunks).toString('utf8')); }
  catch { throw Object.assign(new Error('ساختار درخواست معتبر نیست.'), { status: 400 }); }
}
function audit(action, entity, entityId, details = '') {
  db.audit.unshift({ id: randomId('AUD'), action, entity, entityId, details, actor: 'admin', createdAt: now() });
  db.audit = db.audit.slice(0, 500);
}
function userById(id) { return db.users.find(u => u.id === id); }
function fmtMonth(date) { return date.toISOString().slice(0, 7); }
function dashboard() {
  const successfulPayments = db.payments.filter(p => p.status === 'success');
  const revenue = successfulPayments.reduce((sum, p) => sum + Number(p.amount || 0), 0);
  const activeUsers = db.users.filter(u => u.status === 'active').length;
  const pendingReports = db.reports.filter(r => r.status === 'pending').length;
  const pendingVerifications = db.verifications.filter(v => v.status === 'pending').length;
  const openTickets = db.tickets.filter(t => t.status === 'open').length;
  const totalUsers = db.users.length;
  const deletedUsers = db.users.filter(u => u.status === 'deleted').length;
  const completedProfiles = db.users.filter(u => u.bio && (u.photoUrls?.length || u.photos)).length;
  const payingUsers = new Set(successfulPayments.map(p => p.userId)).size;
  const chatMessages = (db.chats || []).reduce((sum, chat) => sum + (chat.messages || []).length, 0);
  const avgMessagesPerChat = db.chats?.length ? Math.round(chatMessages / db.chats.length * 10) / 10 : 0;
  const months = [];
  for (let i = 5; i >= 0; i--) {
    const d = new Date(); d.setMonth(d.getMonth() - i); d.setDate(1);
    const key = fmtMonth(d);
    const title = new Intl.DateTimeFormat('fa-IR', { month: 'short' }).format(d);
    const monthPayments = successfulPayments.filter(p => fmtMonth(new Date(p.createdAt)) === key);
    months.push({ label: title, revenue: monthPayments.reduce((s, p) => s + Number(p.amount || 0), 0), payments: monthPayments.length });
  }
  const usersByPlan = ['Free', 'Pro', 'VIP'].map(plan => ({ label: plan, value: db.users.filter(u => u.plan === plan && u.status !== 'deleted').length }));
  return {
    totals: {
      users: 12486 + db.users.length - 8,
      activeUsers: 8214 + activeUsers - 7,
      newUsers: 372,
      revenue: 124800000 + revenue,
      conversionRate: 8.4,
      successfulPayments: successfulPayments.length,
      pendingReports, pendingVerifications, openTickets,
      likes: 928,
      matches: 214,
      dates: 86,
      newUsers: db.users.filter(u => Date.now() - new Date(u.joinedAt).getTime() <= 30 * 86400000).length,
      deletedUsers,
      profileCompletionRate: totalUsers ? Math.round(completedProfiles / totalUsers * 100) : 0,
      payerRate: totalUsers ? Math.round(payingUsers / totalUsers * 1000) / 10 : 0,
      avgMessagesPerChat,
      totalChats: db.chats?.length || 0,
      chatMessages,
      photoCoverageRate: totalUsers ? Math.round(db.users.filter(u => (u.photoUrls || []).length > 0).length / totalUsers * 100) : 0,
      verificationRate: totalUsers ? Math.round(db.users.filter(u => u.verification === 'verified').length / totalUsers * 100) : 0,
      openTicketsPer100Users: totalUsers ? Math.round(openTickets / totalUsers * 1000) / 10 : 0
    },
    revenueTrend: months,
    usersByPlan,
    funnel: [{ label: 'بازدید پروفایل', value: 100 }, { label: 'ثبت‌نام', value: 62 }, { label: 'تکمیل پروفایل', value: 44 }, { label: 'اولین لایک', value: 31 }, { label: 'خرید اشتراک', value: 8.4 }],
    recentPayments: [...db.payments].sort((a,b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 5),
    recentActivity: [...db.activity].sort((a,b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 6)
  };
}

const MIME = { '.html':'text/html; charset=utf-8', '.css':'text/css; charset=utf-8', '.js':'text/javascript; charset=utf-8', '.json':'application/json; charset=utf-8', '.woff2':'font/woff2', '.svg':'image/svg+xml', '.png':'image/png', '.ico':'image/x-icon' };
async function staticFile(req, res, pathname) {
  const clean = decodeURIComponent(pathname.split('?')[0]);
  const relative = clean === '/' ? 'index.html' : clean.replace(/^\/+/, '');
  let target = path.resolve(PUBLIC, relative);
  if (!target.startsWith(PUBLIC + path.sep) && target !== path.join(PUBLIC, 'index.html')) return send(res, 403, { error: 'Forbidden' });
  try {
    const stat = await fs.stat(target);
    if (stat.isDirectory()) target = path.join(target, 'index.html');
    const content = await fs.readFile(target);
    // Admin assets must not remain stale after a deployment: an old app.js can keep the login screen visible even after a successful server-side fix.
    const assetType = path.extname(target);
    const cachePolicy = ['.html', '.js', '.css'].includes(assetType) ? 'no-store, no-cache, must-revalidate' : 'public, max-age=3600';
    res.writeHead(200, { ...SECURITY_HEADERS, 'Content-Type': MIME[assetType] || 'application/octet-stream', 'Cache-Control': cachePolicy, 'Pragma': 'no-cache' });
    res.end(content);
  } catch {
    if (!path.extname(clean)) return staticFile(req, res, '/index.html');
    send(res, 404, { error: 'فایل پیدا نشد.' });
  }
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = url.pathname;
    const method = req.method || 'GET';
    if (!pathname.startsWith('/api/')) return staticFile(req, res, pathname);
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const expectedOrigin = process.env.PUBLIC_ORIGIN || `http://${req.headers.host}`;
      if (!req.headers.origin || req.headers.origin !== expectedOrigin) {
        return send(res, 403, { error: 'درخواست تغییردهنده وضعیت باید از مبدأ معتبر پنل ارسال شود.' });
      }
      const hasRequestBody = Number(req.headers['content-length'] || 0) > 0 || Boolean(req.headers['transfer-encoding']);
      if (hasRequestBody && (req.headers['content-type'] || '').split(';')[0].trim().toLowerCase() !== 'application/json') {
        return send(res, 415, { error: 'نوع محتوای درخواست باید application/json باشد.' });
      }
    }

    if (pathname === '/api/health' && method === 'GET') return send(res, 200, { ok: true, mode: IS_PROD ? 'production' : 'demo' });
    if (pathname === '/api/auth/login' && method === 'POST') {
      const ip = req.socket.remoteAddress || 'unknown';
      const attempts = loginAttempts.get(ip) || { count: 0, since: Date.now() };
      if (Date.now() - attempts.since > 10 * 60 * 1000) { attempts.count = 0; attempts.since = Date.now(); }
      if (attempts.count >= 8) return send(res, 429, { error: 'تعداد تلاش‌های ورود زیاد است. ۱۰ دقیقه دیگر دوباره تلاش کنید.' });
      const body = await readBody(req);
      if (!safeEqual(body.username || '', ADMIN_USERNAME) || !safeEqual(body.password || '', ADMIN_PASSWORD)) {
        attempts.count++; loginAttempts.set(ip, attempts);
        return send(res, 401, { error: 'نام کاربری یا رمز عبور صحیح نیست.' });
      }
      loginAttempts.delete(ip);
      const token = crypto.randomBytes(32).toString('base64url');
      sessions.set(token, { username: ADMIN_USERNAME, expiresAt: Date.now() + SESSION_TTL });
      const cookie = `fl_admin_session=${encodeURIComponent(token)}; HttpOnly; SameSite=Strict; Path=/; Max-Age=${SESSION_TTL / 1000}${IS_PROD ? '; Secure' : ''}`;
      return send(res, 200, { admin: { username: ADMIN_USERNAME } }, { 'Set-Cookie': cookie });
    }
    if (pathname === '/api/auth/logout' && method === 'POST') {
      const session = getSession(req); if (session) sessions.delete(session.token);
      return send(res, 200, { ok: true }, { 'Set-Cookie': `fl_admin_session=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0${IS_PROD ? '; Secure' : ''}` });
    }
    if (pathname === '/api/auth/me' && method === 'GET') {
      const session = getSession(req);
      return session ? send(res, 200, { admin: { username: session.username } }) : send(res, 401, { error: 'نیاز به ورود' });
    }

    const session = requireAdmin(req, res); if (!session) return;
    if (pathname === '/api/dashboard' && method === 'GET') return send(res, 200, dashboard());

    if (pathname === '/api/referrals' && method === 'GET') {
      const q = (url.searchParams.get('q') || '').trim().toLowerCase();
      const groups = (db.users || []).filter(u => u.referralCode).map(owner => {
        const referredUsers = (db.users || []).filter(member => member.referredByCode && member.referredByCode.toLowerCase() === String(owner.referralCode).toLowerCase()).sort((a,b) => b.joinedAt.localeCompare(a.joinedAt));
        return { code: owner.referralCode, owner: { id: owner.id, name: owner.name, phone: owner.phone, status: owner.status, joinedAt: owner.joinedAt }, total: referredUsers.length, referredUsers };
      }).filter(group => !q || [group.code,group.owner.name,group.owner.id,...group.referredUsers.flatMap(u => [u.name,u.id,u.phone])].some(value => String(value || '').toLowerCase().includes(q)))
        .sort((a,b) => b.total - a.total || a.code.localeCompare(b.code));
      return send(res, 200, { groups, totalReferrers: groups.length, totalReferred: groups.reduce((sum, group) => sum + group.total, 0) });
    }

    if (pathname === '/api/users' && method === 'GET') {
      const q = (url.searchParams.get('q') || '').trim().toLowerCase();
      const status = url.searchParams.get('status') || 'all';
      const plan = url.searchParams.get('plan') || 'all';
      const online = url.searchParams.get('online') || 'all';
      let users = [...db.users];
      if (q) users = users.filter(u => [u.name,u.phone,u.email,u.id,u.city].some(v => String(v || '').toLowerCase().includes(q)));
      if (status !== 'all') users = users.filter(u => u.status === status);
      if (plan !== 'all') users = users.filter(u => u.plan === plan);
      if (online === 'online') users = users.filter(u => u.online === true);
      else if (online === 'offline') users = users.filter(u => u.online !== true);
      return send(res, 200, { users: users.sort((a,b) => b.joinedAt.localeCompare(a.joinedAt)), total: users.length, onlineCount: users.filter(u => u.online === true).length });
    }
    let match = pathname.match(/^\/api\/users\/([^/]+)$/);
    if (match && method === 'GET') {
      const user = userById(match[1]); if (!user) return send(res, 404, { error: 'کاربر پیدا نشد.' });
      const userId = user.id;
      return send(res, 200, {
        user,
        payments: db.payments.filter(x => x.userId === userId),
        reports: db.reports.filter(x => x.reporterId === userId || x.targetId === userId),
        tickets: db.tickets.filter(x => x.userId === userId),
        likes: db.likes.filter(x => x.fromId === userId || x.toId === userId),
        matches: db.matches.filter(x => x.userAId === userId || x.userBId === userId),
        dates: db.dates.filter(x => x.userAId === userId || x.userBId === userId),
        activity: db.activity.filter(x => x.userId === userId),
        verifications: db.verifications.filter(x => x.userId === userId),
        chats: (db.chats || []).filter(x => x.userAId === userId || x.userBId === userId)
      });
    }
    if (match && method === 'DELETE') {
      const user = userById(match[1]); if (!user) return send(res, 404, { error: 'کاربر پیدا نشد.' });
      user.status = 'deleted'; user.deletedAt = now(); user.deletedBy = 'admin'; user.deletionReason = 'حذف توسط مدیر پنل'; user.online = false;
      audit('حذف نرم حساب کاربری', 'user', user.id, user.name);
      await persist(); return send(res, 200, { user });
    }

    if (pathname === '/api/payments' && method === 'GET') {
      const q = (url.searchParams.get('q') || '').toLowerCase();
      const status = url.searchParams.get('status') || 'all';
      let payments = [...db.payments];
      if (q) payments = payments.filter(p => [p.id,p.userName,p.userId,p.transactionId,p.packageTitle].some(v => String(v || '').toLowerCase().includes(q)));
      if (status !== 'all') payments = payments.filter(p => p.status === status);
      return send(res, 200, { payments: payments.sort((a,b) => b.createdAt.localeCompare(a.createdAt)) });
    }
    match = pathname.match(/^\/api\/payments\/([^/]+)$/);
    if (match && method === 'GET') {
      const payment = db.payments.find(p => p.id === match[1]);
      return payment ? send(res, 200, { payment, user: userById(payment.userId) || null }) : send(res, 404, { error: 'پرداخت پیدا نشد.' });
    }

    if (pathname === '/api/reports' && method === 'GET') return send(res, 200, { reports: [...db.reports].sort((a,b) => b.createdAt.localeCompare(a.createdAt)) });
    match = pathname.match(/^\/api\/reports\/([^/]+)$/);
    if (match && method === 'PATCH') {
      const body = await readBody(req); const item = db.reports.find(x => x.id === match[1]);
      if (!item) return send(res, 404, { error: 'گزارش پیدا نشد.' });
      if (!['approved','rejected','pending'].includes(body.status)) return send(res, 400, { error: 'وضعیت نامعتبر است.' });
      item.status = body.status; item.reviewedAt = now(); item.reviewNote = String(body.note || '').slice(0, 1000);
      audit('بررسی گزارش تخلف', 'report', item.id, body.status);
      await persist(); return send(res, 200, { report: item });
    }

    if (pathname === '/api/verifications' && method === 'GET') return send(res, 200, { verifications: [...db.verifications].sort((a,b) => b.submittedAt.localeCompare(a.submittedAt)) });
    match = pathname.match(/^\/api\/verifications\/([^/]+)$/);
    if (match && method === 'PATCH') {
      const body = await readBody(req); const item = db.verifications.find(x => x.id === match[1]);
      if (!item) return send(res, 404, { error: 'درخواست احراز هویت پیدا نشد.' });
      if (!['verified','rejected','pending'].includes(body.status)) return send(res, 400, { error: 'وضعیت نامعتبر است.' });
      item.status = body.status; item.reviewedAt = now(); item.reviewReason = String(body.reason || '').slice(0, 1000);
      const user = userById(item.userId); if (user) user.verification = body.status;
      audit('بررسی احراز هویت ویدئویی', 'verification', item.id, body.status);
      await persist(); return send(res, 200, { verification: item });
    }

    if (pathname === '/api/tickets' && method === 'GET') {
      const q = (url.searchParams.get('q') || '').toLowerCase(); const status = url.searchParams.get('status') || 'all';
      let tickets = [...db.tickets];
      if (q) tickets = tickets.filter(t => [t.id,t.userName,t.subject,t.category].some(v => String(v || '').toLowerCase().includes(q)));
      if (status !== 'all') tickets = tickets.filter(t => t.status === status);
      return send(res, 200, { tickets: tickets.sort((a,b) => b.updatedAt.localeCompare(a.updatedAt)) });
    }
    match = pathname.match(/^\/api\/tickets\/([^/]+)\/reply$/);
    if (match && method === 'POST') {
      const body = await readBody(req); const ticket = db.tickets.find(t => t.id === match[1]);
      if (!ticket) return send(res, 404, { error: 'تیکت پیدا نشد.' });
      if (ticket.status === 'closed') return send(res, 409, { error: 'تیکت بسته است؛ ابتدا آن را باز کنید.' });
      const message = String(body.text || '').trim();
      if (!message) return send(res, 400, { error: 'متن پاسخ اجباری است.' });
      const createdAt = now();
      ticket.messages.push({ id: randomId('MSG'), author: 'admin', authorName: 'پشتیبانی FastLove', text: message.slice(0, 5000), createdAt });
      ticket.updatedAt = createdAt; ticket.history.push({ action: 'پاسخ مدیر ثبت شد', actor: 'پشتیبانی', createdAt });
      audit('پاسخ به تیکت', 'ticket', ticket.id);
      await persist(); return send(res, 200, { ticket });
    }
    match = pathname.match(/^\/api\/tickets\/([^/]+)\/status$/);
    if (match && method === 'PATCH') {
      const body = await readBody(req); const ticket = db.tickets.find(t => t.id === match[1]);
      if (!ticket) return send(res, 404, { error: 'تیکت پیدا نشد.' });
      if (!['open','closed'].includes(body.status)) return send(res, 400, { error: 'وضعیت نامعتبر است.' });
      ticket.status = body.status; ticket.updatedAt = now(); ticket.history.push({ action: body.status === 'closed' ? 'تیکت بسته شد' : 'تیکت دوباره باز شد', actor: 'پشتیبانی', createdAt: ticket.updatedAt });
      audit(body.status === 'closed' ? 'بستن تیکت' : 'بازکردن مجدد تیکت', 'ticket', ticket.id);
      await persist(); return send(res, 200, { ticket });
    }

    if (pathname === '/api/chats' && method === 'GET') {
      const q = (url.searchParams.get('q') || '').trim().toLowerCase();
      let chats = [...(db.chats || [])];
      if (q) chats = chats.filter(c => [c.id,c.userAName,c.userBName,c.userAId,c.userBId,...(c.messages||[]).map(m=>m.text)].some(v => String(v||'').toLowerCase().includes(q)));
      return send(res, 200, { chats: chats.sort((a,b) => String(b.updatedAt||'').localeCompare(String(a.updatedAt||''))), total: chats.length, messages: chats.reduce((n,c)=>n+(c.messages||[]).length,0) });
    }
    match = pathname.match(/^\/api\/chats\/([^/]+)$/);
    if (match && method === 'GET') {
      const chat = (db.chats || []).find(c => c.id === match[1]);
      return chat ? send(res, 200, { chat }) : send(res, 404, { error: 'گفت‌وگو پیدا نشد.' });
    }
    if (pathname === '/api/likes' && method === 'GET') return send(res, 200, { likes: [...db.likes].sort((a,b) => b.createdAt.localeCompare(a.createdAt)) });
    if (pathname === '/api/matches' && method === 'GET') return send(res, 200, { matches: [...db.matches].sort((a,b) => b.createdAt.localeCompare(a.createdAt)) });
    if (pathname === '/api/dates' && method === 'GET') return send(res, 200, { dates: [...db.dates].sort((a,b) => b.scheduledAt.localeCompare(a.scheduledAt)) });
    match = pathname.match(/^\/api\/dates\/([^/]+)$/);
    if (match && method === 'GET') {
      const item = db.dates.find(date => date.id === match[1]);
      return item ? send(res, 200, { date: item, userA: userById(item.userAId) || null, userB: userById(item.userBId) || null }) : send(res, 404, { error: 'دیت پیدا نشد.' });
    }

    if (pathname === '/api/packages' && method === 'GET') return send(res, 200, { packages: [...db.packages].sort((a,b) => Number(b.active) - Number(a.active)) });
    if (pathname === '/api/packages' && method === 'POST') {
      const body = await readBody(req);
      const type = String(body.type || '');
      const name = String(body.name || body.title || '').trim().slice(0, 100);
      const price = Number(body.price);
      if (!['subscription','direct_message'].includes(type)) return send(res, 400, { error: 'نوع بسته را انتخاب کنید.' });
      if (!name) return send(res, 400, { error: 'عنوان بسته الزامی است.' });
      if (!Number.isSafeInteger(price) || price < 1 || price > 100000000000) return send(res, 400, { error: 'قیمت باید عدد صحیح مثبت به تومان باشد.' });
      const durationDays = type === 'subscription' ? Number(body.durationDays) : 0;
      const messageCount = type === 'direct_message' ? Number(body.messageCount) : 0;
      if (type === 'subscription' && (!Number.isSafeInteger(durationDays) || durationDays < 1 || durationDays > 3650)) return send(res, 400, { error: 'مدت اعتبار اشتراک باید بین ۱ تا ۳۶۵۰ روز باشد.' });
      if (type === 'direct_message' && (!Number.isSafeInteger(messageCount) || messageCount < 1 || messageCount > 100000)) return send(res, 400, { error: 'تعداد پیام باید عدد صحیح مثبت باشد.' });
      const item = { id: randomId('PKG'), name, type, durationDays, messageCount, price, currency: 'TOMAN', active: true, description: type === 'subscription' ? `اعتبار ${durationDays} روزه اشتراک` : `امکان ارسال ${messageCount} پیام مستقیم`, createdAt: now(), updatedAt: now() };
      db.packages.unshift(item); audit('ایجاد بسته', 'package', item.id, `${name} · ${price} تومان`);
      await persist(); return send(res, 201, { package: item });
    }
    match = pathname.match(/^\/api\/packages\/([^/]+)$/);
    if (match && method === 'PATCH') {
      const body = await readBody(req); const item = db.packages.find(p => p.id === match[1]);
      if (!item) return send(res, 404, { error: 'بسته پیدا نشد.' });
      if (body.price !== undefined) {
        const price = Number(body.price);
        if (!Number.isSafeInteger(price) || price < 1 || price > 100000000000) return send(res, 400, { error: 'قیمت باید عدد صحیح مثبت به تومان باشد.' });
        item.price = price;
      }
      if (typeof body.active === 'boolean') item.active = body.active;
      if (body.name !== undefined || body.title !== undefined) {
        const name = String(body.name ?? body.title ?? '').trim().slice(0,100);
        if (!name) return send(res, 400, { error: 'عنوان بسته الزامی است.' });
        item.name = name;
      }
      if (body.durationDays !== undefined && item.type === 'subscription') {
        const days = Number(body.durationDays); if (!Number.isSafeInteger(days) || days < 1 || days > 3650) return send(res, 400, { error: 'مدت اشتراک نامعتبر است.' }); item.durationDays = days;
      }
      if (body.messageCount !== undefined && item.type === 'direct_message') {
        const count = Number(body.messageCount); if (!Number.isSafeInteger(count) || count < 1 || count > 100000) return send(res, 400, { error: 'تعداد پیام نامعتبر است.' }); item.messageCount = count;
      }
      item.description = item.type === 'subscription' ? `اعتبار ${item.durationDays} روزه اشتراک` : `امکان ارسال ${item.messageCount || 0} پیام مستقیم`;
      item.updatedAt = now(); audit('ویرایش بسته', 'package', item.id, `${item.active ? 'فعال' : 'غیرفعال'} · ${item.price} تومان`);
      await persist(); return send(res, 200, { package: item });
    }
    if (match && method === 'DELETE') {
      const index = db.packages.findIndex(p => p.id === match[1]);
      if (index < 0) return send(res, 404, { error: 'بسته پیدا نشد.' });
      const [item] = db.packages.splice(index, 1);
      audit('حذف بسته', 'package', item.id, item.name);
      await persist(); return send(res, 200, { ok: true, package: item });
    }
    if (pathname === '/api/audit' && method === 'GET') return send(res, 200, { audit: db.audit.slice(0, 100) });
    return send(res, 404, { error: 'مسیر API پیدا نشد.' });
  } catch (error) {
      console.error('[demo-api] request failed:', error?.message || 'unknown error');
    send(res, error.status || 500, { error: error.status ? error.message : 'خطای داخلی سرور.' });
  }
});

server.listen(PORT, HOST, () => {
  console.log(`FastLove Admin demo is running at http://${HOST}:${PORT}`);
  console.log(`DEMO credentials: ${ADMIN_USERNAME} / ${ADMIN_PASSWORD} (local demo only; never use with real data)`);
});
