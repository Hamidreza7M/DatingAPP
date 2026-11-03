const $ = (s, root = document) => root.querySelector(s);
const $$ = (s, root = document) => [...root.querySelectorAll(s)];
const state = { page: 'dashboard', admin: null, userId: null, userTab: 'overview', socialTab: 'likes', ticketId: null, chatId: null, chatsQuery: '', ticketStatus: 'all', usersQuery: '', paymentQuery: '', paymentStatus: 'all', ticketQuery: '', referralQuery: '', paymentModal: null, usersPlan: 'all', usersStatus: 'all', usersOnline: 'all' };

const titles = {
  dashboard: 'داشبورد BI', users: 'مدیریت کاربران', referrals: 'گزارش کدهای معرف', userDetail: 'جزئیات کاربر', deletedUsers: 'بایگانی کاربران حذف‌شده', chats: 'گفت‌وگوی کاربران', help: 'راهنمای پنل', payments: 'پرداخت‌ها',
  reports: 'گزارش تخلف', verification: 'احراز هویت ویدئویی', engagement: 'لایک، مچ و دیت', tickets: 'تیکت‌های پشتیبانی', packages: 'بسته‌ها و قیمت‌گذاری'
};
const iconForActivity = { login:'↪', like:'♡', payment:'▤', ticket:'▣', verification:'◉', match:'✦', date:'⌖', report:'⚑' };

function esc(value = '') {
  return String(value ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
}
function safeHttpsUrl(value = '') {
  try {
    const url = new URL(String(value || '').trim());
    return url.protocol === 'https:' ? url.href : '';
  } catch {
    return '';
  }
}
function money(value) { return `${Number(value || 0).toLocaleString('fa-IR')} تومان`; }
function number(value) { return Number(value || 0).toLocaleString('fa-IR'); }
function date(value, withTime = false) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '—';
  return new Intl.DateTimeFormat('fa-IR', withTime ? { year:'numeric', month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' } : { year:'numeric', month:'short', day:'numeric' }).format(d);
}
function initials(name = '') { return name.trim().split(/\s+/).slice(0,2).map(x => x[0] || '').join('') || '؟'; }
function avatar(name, gender = '') { return `<span class="person-avatar ${gender === 'زن' ? 'female' : ''}">${esc(initials(name))}</span>`; }
function badge(status, label) {
  const map = {
    active:['success','فعال'], success:['success','موفق'], verified:['success','تأیید شده'], approved:['success','تأیید شده'], open:['info','باز'], pending:['warning','در انتظار'], rejected:['danger','رد شده'], failed:['danger','ناموفق'], closed:['neutral','بسته'], suspended:['danger','تعلیق'], deleted:['neutral','حذف شده'], Free:['neutral','رایگان'], Pro:['info','Pro'], VIP:['purple','VIP'], accepted:['success','تأیید شده'], declined:['danger','رد شده'], cancelled:['neutral','لغو شده'], completed:['success','انجام شده'], ended:['neutral','پایان یافته'], not_verified:['neutral','تأیید نشده']
  };
  const [cls, text] = map[status] || ['neutral', label || status || '—'];
  return `<span class="badge ${cls}">${esc(label || text)}</span>`;
}
function toast(message, type = 'success') {
  const root = $('#toastRoot'); const el = document.createElement('div');
  el.className = `toast ${type === 'error' ? 'error' : ''}`; el.textContent = message; root.append(el);
  setTimeout(() => el.remove(), 3600);
}
async function api(path, options = {}) {
  const config = { method: options.method || 'GET', credentials: 'same-origin', headers: { ...(options.body ? { 'Content-Type':'application/json' } : {}), ...(options.headers || {}) } };
  if (options.body) config.body = JSON.stringify(options.body);
  let response;
  try {
    response = await fetch(path, config);
  } catch {
    const error = new Error('ارتباط با سرور پنل برقرار نشد. مطمئن شو سرور را با فایل START-ADMIN.bat یا دستور npm run admin:dev اجرا کرده‌ای و آدرس http://localhost:4177 را باز کرده‌ای.');
    error.network = true;
    throw error;
  }

  const contentType = response.headers.get('content-type') || '';
  if (!contentType.toLowerCase().includes('application/json')) {
    const message = response.status === 404
      ? 'خطای 404: سرور API پنل پیدا نشد. احتمالاً پنل را با Live Server یا npx serve اجرا کرده‌ای. آن سرور را متوقف کن و از پوشه اصلی پروژه فایل START-ADMIN.bat را اجرا کن؛ سپس http://localhost:4177 را باز کن.'
      : `سرور پنل پاسخ JSON معتبر نداد (HTTP ${response.status}). پنل را با START-ADMIN.bat اجرا کن.`;
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(data.error || `خطا در ارتباط با سرور (${response.status})`);
    error.status = response.status; throw error;
  }
  return data;
}
function showLogin() {
  $('#appView').hidden = true; $('#loginView').hidden = false;
  $('#username').focus();
}
function showApp(admin) {
  state.admin = admin; $('#loginView').hidden = true; $('#appView').hidden = false;
  $('#adminName').textContent = admin.username; $('#topAdminName').textContent = admin.username;
  $('#todayLabel').textContent = date(new Date().toISOString());
  navigate('dashboard');
}
function pageHeader(title, subtitle, actions = '') {
  return `<div class="page-heading"><div><h1>${title}</h1><p>${subtitle}</p></div><div class="heading-actions">${actions}</div></div>`;
}
function statCard(title, value, icon, foot, colors = {}) {
  return `<article class="stat-card" style="--glow:${colors.glow || 'rgba(255,113,138,.08)'};--icon-bg:${colors.bg || 'rgba(255,113,138,.13)'};--icon-color:${colors.color || '#ff91a7'}"><div class="stat-head"><span>${title}</span><span class="stat-icon">${icon}</span></div><div class="stat-value">${value}</div><div class="stat-foot">${foot}</div></article>`;
}
function personCell(name, phoneOrMeta, gender = '') {
  return `<div class="person-cell">${avatar(name, gender)}<div class="person-meta"><b>${esc(name)}</b><small>${esc(phoneOrMeta || '')}</small></div></div>`;
}
function tableEmpty(cols, msg = 'موردی برای نمایش وجود ندارد.') { return `<tr><td colspan="${cols}" class="empty-state"><strong>داده‌ای پیدا نشد</strong>${msg}</td></tr>`; }
function shellPage(content) { $('#pageContainer').innerHTML = content; $('#pageTitle').textContent = titles[state.page] || 'جزئیات کاربر'; $$('.nav-link').forEach(x => x.classList.toggle('active', x.dataset.page === (state.page === 'userDetail' ? 'users' : state.page))); }
function navigate(page) { state.page = page; state.userId = page === 'userDetail' ? state.userId : null; $('#sidebar').classList.remove('open'); render(); }
async function render() {
  try {
    if (state.page === 'dashboard') await renderDashboard();
    else if (state.page === 'users') await renderUsers();
    else if (state.page === 'referrals') await renderReferrals();
    else if (state.page === 'deletedUsers') await renderDeletedUsers();
    else if (state.page === 'chats') await renderChats();
    else if (state.page === 'help') await renderHelp();
    else if (state.page === 'userDetail') await renderUserDetail();
    else if (state.page === 'payments') await renderPayments();
    else if (state.page === 'reports') await renderReports();
    else if (state.page === 'verification') await renderVerifications();
    else if (state.page === 'engagement') await renderEngagement();
    else if (state.page === 'tickets') await renderTickets();
    else if (state.page === 'packages') await renderPackages();
  } catch (error) {
    if (error.status === 401) { showLogin(); toast('نشست شما پایان یافته است.', 'error'); }
    else { shellPage(`<div class="empty-state"><strong>بارگذاری اطلاعات انجام نشد</strong>${esc(error.message)}<br/><button class="button" data-action="refresh">تلاش دوباره</button></div>`); }
  }
}

async function renderDashboard() {
  const data = await api('/api/dashboard'); const t = data.totals;
  const maxRevenue = Math.max(1, ...data.revenueTrend.map(x => x.revenue));
  const bars = data.revenueTrend.map(x => `<div class="chart-col"><div class="bar-stack"><div class="bar" style="height:${Math.max(4, x.revenue / maxRevenue * 145)}px" title="${money(x.revenue)}"></div><div class="bar secondary" style="height:${Math.max(4, x.payments / Math.max(1,...data.revenueTrend.map(y=>y.payments)) * 78)}px"></div></div><span class="chart-label">${esc(x.label)}</span></div>`).join('');
  const planTotal = data.usersByPlan.reduce((s,x)=>s+x.value,0) || 1;
  let cursor = 0; const stops = data.usersByPlan.map((x,i)=>{ const colors=['#9d76f4','#ff829b','#3ebf9e']; const start=cursor; cursor += x.value / planTotal * 100; return `${colors[i]} ${start}% ${cursor}%`; }).join(',');
  const plans = data.usersByPlan.map((x,i)=>`<div class="donut-item"><span class="legend-dot" style="background:${['#9d76f4','#ff829b','#3ebf9e'][i]}"></span>${esc(x.label)}<b>${number(x.value)}</b></div>`).join('');
  const funnel = data.funnel.map((x,i)=>`<div class="progress-row"><label>${esc(x.label)}</label><div class="progress-track"><span style="width:${Math.max(0,Math.min(100,x.value))}%"></span></div><b>${number(x.value)}٪</b></div>`).join('');
  const payments = data.recentPayments.map(p=>`<tr><td><span class="mono">${esc(p.id)}</span></td><td>${personCell(p.userName,p.userId)}</td><td>${esc(p.packageTitle)}</td><td>${money(p.amount)}</td><td>${badge(p.status)}</td></tr>`).join('') || tableEmpty(5);
  const activities = data.recentActivity.map(a=>`<div class="activity-row"><div class="activity-dot">${iconForActivity[a.type] || '·'}</div><div><b>${esc(a.title)}</b><p>${esc(a.detail)}</p></div><time>${date(a.createdAt,true)}</time></div>`).join('');
  shellPage(`${pageHeader('داشبورد مدیریتی','نمای کلی عملکرد FastLove، وضعیت کاربران و شاخص‌های کلیدی کسب‌وکار.',`<button class="button" data-action="refresh">↻ بروزرسانی داده‌ها</button><span class="badge warning">داده نمونه</span>`)}
    <div class="grid stats-grid">
      ${statCard('کل کاربران',number(t.users),'♙',`<span class="trend-up">↑ ۱۲.۸٪</span> نسبت به ماه قبل`,{bg:'rgba(104,167,255,.12)',color:'#7eb4ff',glow:'rgba(104,167,255,.1)'})}
      ${statCard('کاربران فعال',number(t.activeUsers),'◉',`<span class="trend-up">↑ ۸.۲٪</span> فعالیت ماهانه`,{bg:'rgba(57,201,154,.12)',color:'#53d6aa',glow:'rgba(57,201,154,.1)'})}
      ${statCard('درآمد ثبت‌شده',money(t.revenue),'▤',`<span class="trend-up">↑ ۱۸.۶٪</span> نسبت به ماه قبل`,{bg:'rgba(178,108,255,.13)',color:'#c29cff',glow:'rgba(178,108,255,.1)'})}
      ${statCard('نرخ تبدیل به پرداخت',`${number(t.conversionRate)}٪`,'↗',`<span class="trend-up">↑ ۱.۴٪</span> از ثبت‌نام تا خرید`,{bg:'rgba(243,189,101,.12)',color:'#f3c77f',glow:'rgba(243,189,101,.1)'})}
    </div>
    <div class="grid dashboard-layout">
      <section class="panel"><div class="panel-head"><div><h3>روند درآمد و تراکنش</h3><p>گزارش ۶ ماه اخیر · مبلغ به تومان</p></div><div class="chart-legend"><span><i class="legend-dot" style="background:#a77af2"></i>درآمد</span><span><i class="legend-dot" style="background:#f17d98"></i>تعداد پرداخت</span></div></div><div class="revenue-chart">${bars}</div><div class="stat-foot" style="margin-top:12px">مقادیر نمودار بر اساس نمونه داده موجود محاسبه شده‌اند.</div></section>
      <section class="panel"><div class="panel-head"><div><h3>ترکیب کاربران بر اساس پلن</h3><p>توزیع کاربران نمونه</p></div><button class="panel-link" data-page="users">مشاهده کاربران ←</button></div><div class="donut-wrap"><div class="donut" style="background:conic-gradient(${stops})"><div class="donut-center"><b>${number(t.users)}</b><small>کل کاربران</small></div></div><div class="donut-legend">${plans}</div></div></section>
    </div>
    <div class="grid lower-grid">
      <section class="panel table-panel"><div class="panel-head"><div><h3>آخرین پرداخت‌ها</h3><p>جدیدترین تراکنش‌های ثبت‌شده</p></div><button class="panel-link" data-page="payments">همه پرداخت‌ها ←</button></div><div class="table-wrap"><table class="data-table"><thead><tr><th>شناسه</th><th>کاربر</th><th>بسته</th><th>مبلغ</th><th>وضعیت</th></tr></thead><tbody>${payments}</tbody></table></div></section>
      <section class="panel"><div class="panel-head"><div><h3>فعالیت‌های اخیر</h3><p>رویدادهای مهم محصول</p></div><span class="badge info">Live sample</span></div><div class="activity-list">${activities}</div></section>
    </div>
    <div class="grid dashboard-layout" style="margin-top:16px"><section class="panel"><div class="panel-head"><div><h3>قیف تبدیل محصول</h3><p>درصد عبور کاربران از مراحل کلیدی تجربه</p></div><span class="badge purple">Product Analytics</span></div>${funnel}<div class="stat-foot">نرخ‌ها نمایشی هستند و باید پس از اتصال Event Tracking از داده واقعی محاسبه شوند.</div></section><section class="panel"><div class="panel-head"><div><h3>شاخص‌های تعامل</h3><p>خروجی‌های کلیدی محصول</p></div></div><div class="info-grid" style="grid-template-columns:repeat(2,minmax(0,1fr))"><div class="info-item"><small>لایک ثبت‌شده</small><b>${number(t.likes)}</b></div><div class="info-item"><small>مچ ایجادشده</small><b>${number(t.matches)}</b></div><div class="info-item"><small>دیت برنامه‌ریزی‌شده</small><b>${number(t.dates)}</b></div><div class="info-item"><small>پرداخت موفق</small><b>${number(t.successfulPayments)}</b></div></div></section></div>
    <div class="grid stats-grid" style="margin-top:15px;grid-template-columns:repeat(4,minmax(0,1fr))">
      ${statCard('گزارش‌های در انتظار',number(t.pendingReports),'⚑','نیازمند بررسی مدیر',{bg:'rgba(243,189,101,.12)',color:'#f3c77f'})}
      ${statCard('احراز هویت در انتظار',number(t.pendingVerifications),'◉','ویدئوهای بررسی‌نشده',{bg:'rgba(104,167,255,.12)',color:'#7eb4ff'})}
      ${statCard('تیکت‌های باز',number(t.openTickets),'▣','درخواست‌های پشتیبانی',{bg:'rgba(57,201,154,.12)',color:'#53d6aa'})}
      ${statCard('مچ‌های ایجادشده',number(t.matches),'♡',`${number(t.likes)} لایک ثبت‌شده · ${number(t.dates)} دیت`,{bg:'rgba(255,113,138,.12)',color:'#ff91a7'})}
    </div>
    <div class="grid stats-grid product-metrics">
      ${statCard('تکمیل پروفایل',`${number(t.profileCompletionRate)}٪`,'▣','کاربران دارای معرفی و تصویر')}
      ${statCard('نرخ کاربران پرداخت‌کننده',`${number(t.payerRate)}٪`,'↗','کاربران با حداقل یک پرداخت موفق')}
      ${statCard('پوشش تصویر پروفایل',`${number(t.photoCoverageRate)}٪`,'▧','کاربران دارای تصویر ثبت‌شده')}
      ${statCard('نرخ احراز هویت موفق',`${number(t.verificationRate)}٪`,'◉','از کل کاربران')}
      ${statCard('گفت‌وگوهای ثبت‌شده',number(t.totalChats),'▤',`${number(t.chatMessages)} پیام ثبت‌شده`)}
      ${statCard('میانگین پیام هر گفتگو',number(t.avgMessagesPerChat),'☷','شاخص عمق تعامل')}
      ${statCard('کاربران حذف‌شده',number(t.deletedUsers),'▧','سوابق حفظ‌شده در بایگانی')}
      ${statCard('تیکت به ازای ۱۰۰ کاربر',number(t.openTicketsPer100Users),'▣','شاخص بار پشتیبانی')}
    </div>`);
}

async function renderUsers() {
  const params = new URLSearchParams();
  if (state.usersQuery) params.set('q', state.usersQuery);
  if (state.usersPlan !== 'all') params.set('plan', state.usersPlan);
  if (state.usersStatus !== 'all') params.set('status', state.usersStatus);
  if (state.usersOnline !== 'all') params.set('online', state.usersOnline);
  const data = await api(`/api/users?${params}`);
  const users = data.users;
  const onlineCount = users.filter(u => u.online === true).length;
  const rows = users.map(u=>`<tr><td>${personCell(u.name,u.phone,u.gender)}</td><td>${esc(u.city)}</td><td>${u.age} سال</td><td>${badge(u.plan)}</td><td>${badge(u.verification)}</td><td>${badge(u.status)}</td><td>${u.online ? '<span class="online-indicator"><i></i> آنلاین</span>' : '<span class="offline-indicator">آفلاین</span>'}</td><td>${date(u.joinedAt)}</td><td><button class="button small-btn" data-action="open-user" data-id="${esc(u.id)}">مشاهده جزئیات ↗</button></td></tr>`).join('') || tableEmpty(9,'با فیلترهای انتخاب‌شده کاربری پیدا نشد.');
  shellPage(`${pageHeader('مدیریت کاربران','جست‌وجو، مشاهده جزئیات و مدیریت حساب کاربران ثبت‌نام‌شده.',`<span class="badge info">${number(users.length)} نتیجه</span><span class="badge success">${number(onlineCount)} آنلاین</span>`)}
    <section class="panel table-panel"><div class="toolbar" style="padding:16px 17px 0"><form class="toolbar-start" data-form="search-users"><label class="search-box"><span>⌕</span><input name="q" value="${esc(state.usersQuery)}" placeholder="نام، شماره موبایل، شناسه یا شهر..." aria-label="جست‌وجوی کاربر"/><button type="submit" class="button small-btn">جست‌وجو</button></label></form><div class="toolbar-end"><select class="select" id="userOnlineFilter" aria-label="فیلتر وضعیت آنلاین"><option value="all">آنلاین و آفلاین</option><option value="online">فقط کاربران آنلاین</option><option value="offline">فقط کاربران آفلاین</option></select><select class="select" id="userPlanFilter"><option value="all">همه پلن‌ها</option><option value="Free">Free</option><option value="Pro">Pro</option><option value="VIP">VIP</option></select><select class="select" id="userStatusFilter"><option value="all">همه وضعیت‌ها</option><option value="active">فعال</option><option value="suspended">تعلیق</option><option value="deleted">حذف‌شده</option></select></div></div>
    <div class="table-wrap"><table class="data-table"><thead><tr><th>کاربر</th><th>شهر</th><th>سن</th><th>پلن</th><th>احراز هویت</th><th>وضعیت حساب</th><th>وضعیت آنلاین</th><th>تاریخ عضویت</th><th>عملیات</th></tr></thead><tbody>${rows}</tbody></table></div><div class="table-footer"><span>نمایش ${number(users.length)} کاربر نمونه</span><span>${number(onlineCount)} کاربر از نتایج فعلی آنلاین هستند.</span></div></section>`);
  $('#userPlanFilter').value = state.usersPlan;
  $('#userStatusFilter').value = state.usersStatus;
  $('#userOnlineFilter').value = state.usersOnline;
  $('#userPlanFilter').addEventListener('change', e=>{ state.usersPlan=e.target.value; renderUsers(); });
  $('#userStatusFilter').addEventListener('change', e=>{ state.usersStatus=e.target.value; renderUsers(); });
  $('#userOnlineFilter').addEventListener('change', e=>{ state.usersOnline=e.target.value; renderUsers(); });
}

async function renderReferrals() {
  const query = state.referralQuery ? `?q=${encodeURIComponent(state.referralQuery)}` : '';
  const data = await api(`/api/referrals${query}`);
  const rows = data.groups.flatMap(group => {
    if (!group.referredUsers.length) return [`<tr><td><span class="mono">${esc(group.code)}</span></td><td>${personCell(group.owner.name, `${group.owner.id} · صاحب کد معرف`)}</td><td><span class="muted">هنوز عضوی با این کد ثبت‌نام نکرده است.</span></td><td>${badge(group.owner.status)}</td><td>۰</td></tr>`];
    return group.referredUsers.map(member => `<tr><td><span class="mono">${esc(group.code)}</span></td><td>${personCell(group.owner.name, `${group.owner.id} · صاحب کد معرف`)}</td><td>${personCell(member.name, `${member.id} · عضو معرفی‌شده`)}</td><td>${badge(member.status)}</td><td>${date(member.joinedAt)}</td></tr>`);
  }).join('') || tableEmpty(5,'برای عبارت جست‌وجو نتیجه‌ای وجود ندارد.');
  shellPage(`${pageHeader('گزارش کدهای معرف','مشاهده شفاف صاحب هر کد معرف و تمام کاربرانی که با آن کد ثبت‌نام کرده‌اند.',`<span class="badge info">${number(data.totalReferred)} عضو معرفی‌شده</span>`)}
    <div class="grid stats-grid" style="grid-template-columns:repeat(2,minmax(0,1fr))">${statCard('کدهای معرف پیدا شده',number(data.totalReferrers),'⌁','کدهای متعلق به کاربران')}${statCard('ثبت‌نام از طریق معرف',number(data.totalReferred),'♙','مجموع اعضای معرفی‌شده')}</div>
    <section class="panel table-panel"><div class="toolbar" style="padding:16px 17px 0"><form class="toolbar-start" data-form="search-referrals"><label class="search-box"><span>⌕</span><input name="q" value="${esc(state.referralQuery)}" placeholder="کد معرف، نام صاحب کد یا عضو معرفی‌شده..."/><button class="button small-btn" type="submit">جست‌وجو</button></label></form></div><div class="table-wrap"><table class="data-table"><thead><tr><th>کد معرف</th><th>صاحب کد معرف</th><th>کاربر عضو‌شده با کد</th><th>وضعیت عضو</th><th>تاریخ عضویت</th></tr></thead><tbody>${rows}</tbody></table></div><div class="table-footer"><span>صاحب کد معرف و عضو ثبت‌نام‌شده در ستون‌های جداگانه مشخص شده‌اند.</span><span>داده‌های دمو</span></div></section>`);
}

async function renderDeletedUsers() {
  const data = await api('/api/users?status=deleted');
  const rows = data.users.map(u=>`<tr><td>${personCell(u.name,u.phone,u.gender)}</td><td>${esc(u.id)}</td><td>${esc(u.deletedBy==='admin'?'مدیر پنل':u.deletedBy==='user'?'خود کاربر':'نامشخص')}</td><td>${date(u.deletedAt,true)}</td><td>${esc(u.deletionReason||'—')}</td><td><button class="button small-btn" data-action="open-user" data-id="${esc(u.id)}">مشاهده سوابق</button></td></tr>`).join('') || tableEmpty(6,'کاربر حذف‌شده‌ای وجود ندارد.');
  shellPage(`${pageHeader('بایگانی کاربران حذف‌شده','حذف نرم؛ اطلاعات پروفایل و سوابق مرتبط برای بررسی مدیریتی نگهداری می‌شوند.',`<span class="badge info">${number(data.total)} پرونده</span>`)}<section class="panel table-panel"><div class="table-wrap"><table class="data-table"><thead><tr><th>کاربر</th><th>شناسه</th><th>حذف‌کننده</th><th>تاریخ حذف</th><th>علت</th><th>عملیات</th></tr></thead><tbody>${rows}</tbody></table></div></section>`);
}
async function renderChats() {
  const data = await api(`/api/chats${state.chatsQuery ? `?q=${encodeURIComponent(state.chatsQuery)}` : ''}`);
  const chats = data.chats;
  const selected = chats.find(c=>c.id===state.chatId) || chats[0];
  const list = chats.map(c=>`<button class="chat-admin-item ${selected?.id===c.id?'active':''}" data-action="select-chat" data-id="${esc(c.id)}"><b>${esc(c.userAName)} ↔ ${esc(c.userBName)}</b><small>${number((c.messages||[]).length)} پیام · ${date(c.updatedAt,true)}</small><p>${esc((c.messages||[]).at(-1)?.text||'بدون پیام')}</p></button>`).join('') || '<div class="empty-state">گفت‌وگویی ثبت نشده است.</div>';
  const messages = (selected?.messages||[]).map(m=>`<article class="message"><div class="message-head"><b>${esc(m.senderName||m.senderId)}</b><time>${date(m.createdAt,true)}</time></div><p>${esc(m.text)}</p></article>`).join('') || '<div class="empty-state">پیامی در این گفتگو وجود ندارد.</div>';
  shellPage(`${pageHeader('گفت‌وگوی کاربران','نمای فقط‌خواندنی مکالمات کاربران برای بررسی ایمنی و رسیدگی به گزارش‌ها.',`<span class="badge warning">دسترسی محرمانه مدیر</span>`)}<form class="chat-search-form" data-form="search-chats"><input name="q" value="${esc(state.chatsQuery)}" placeholder="جست‌وجو بر اساس نام، شناسه یا متن پیام..."/><button class="button small-btn" type="submit">جست‌وجو</button></form><div class="privacy-notice">مشاهده پیام‌ها صرفاً برای رسیدگی مجاز و مطابق سیاست حریم خصوصی و قوانین مربوط انجام شود. این نسخه نمایشی شامل داده نمونه است.</div><div class="chat-admin-layout"><section class="panel chat-admin-list">${list}</section><section class="panel chat-admin-thread"><div class="ticket-chat-head"><div><h3>${selected?`${esc(selected.userAName)} و ${esc(selected.userBName)}`:'گفتگو انتخاب نشده'}</h3><p>${selected?esc(selected.id):'—'} · ${number(selected?.messages?.length||0)} پیام</p></div></div><div class="ticket-messages">${messages}</div></section></div>`);
}
function renderHelp() {
  shellPage(`${pageHeader('راهنمای پنل','راهنمای استفاده از بخش‌های مدیریتی FastLove.') }<div class="grid help-grid"><article class="panel"><h3>مدیریت کاربران</h3><p>جست‌وجو، فیلتر، مشاهده تصاویر، فعالیت‌ها، پرداخت‌ها و سوابق کاربران. حذف حساب به‌صورت نرم انجام می‌شود.</p></article><article class="panel"><h3>گفت‌وگوی کاربران</h3><p>مشاهده مکالمات برای بررسی گزارش‌های سوءاستفاده و ایمنی؛ دسترسی باید محدود و قابل ممیزی باشد.</p></article><article class="panel"><h3>داشبورد BI</h3><p>متریک‌های محصول شامل تکمیل پروفایل، نرخ پرداخت، تعامل، پیام‌رسانی و احراز هویت.</p></article><article class="panel"><h3>بایگانی کاربران حذف‌شده</h3><p>پرونده کاربران حذف‌شده و علت/عامل حذف، بدون پاک‌کردن سوابق مالی و عملیاتی نگهداری می‌شود.</p></article><article class="panel"><h3>مستندات فنی</h3><p><a href="/guide.html" target="_blank" rel="noopener">باز کردن مستندات پنل و API ↗</a></p></article></div>`);
}

async function renderUserDetail() {
  const id = state.userId; const data = await api(`/api/users/${encodeURIComponent(id)}`); const u=data.user;
  const tabs = [['overview','اطلاعات پایه'],['activity','فعالیت‌ها'],['payments','پرداخت‌ها'],['social','لایک و مچ'],['dates','دیت‌ها'],['tickets','تیکت‌ها'],['verification','احراز هویت'],['reports','گزارش‌ها']];
  let content = '';
  if(state.userTab==='overview') {
    content=`<section class="profile-photos"><div class="section-title"><h3>تصاویر کاربر</h3><span>${number((u.photoUrls||[]).length)} تصویر</span></div><div class="photo-gallery">${(u.photoUrls||[]).map((src,i)=>{const safeUrl=safeHttpsUrl(src);return safeUrl?`<a href="${esc(safeUrl)}" target="_blank" rel="noopener noreferrer"><img src="${esc(safeUrl)}" alt="تصویر ${i+1} کاربر ${esc(u.name)}" loading="lazy"/></a>`:'';}).filter(Boolean).join('')||'<div class="empty-state">تصویری برای این کاربر ثبت نشده است.</div>'}</div></section><div class="info-grid">${[['شناسه کاربری',u.id],['شماره موبایل',u.phone],['ایمیل',u.email],['شهر',u.city],['سن و جنسیت',`${u.age} سال · ${u.gender}`],['شغل',u.job],['هدف ارتباط',u.goal],['پلن',u.plan],['وضعیت حساب',u.status],['وضعیت احراز هویت',u.verification],['تاریخ عضویت',date(u.joinedAt)],['آخرین بازدید',date(u.lastSeenAt,true)],['کد معرف',u.referralCode],['پایان اشتراک',date(u.subscriptionExpiresAt)],['حذف‌کننده',u.deletedBy==='admin'?'مدیر پنل':u.deletedBy==='user'?'خود کاربر':'—'],['تاریخ حذف',date(u.deletedAt,true)],['علت حذف',u.deletionReason]].map(([k,v])=>`<div class="info-item"><small>${k}</small><b>${esc(v || '—')}</b></div>`).join('')}</div><div class="profile-bio"><b>درباره کاربر</b><div style="margin-top:7px">${esc(u.bio || 'توضیحی ثبت نشده است.')}</div><div class="tag-list">${(u.interests||[]).map(x=>`<span class="tag">${esc(x)}</span>`).join('')||'—'}</div></div><div class="grid stats-grid" style="grid-template-columns:repeat(4,minmax(0,1fr));margin-top:13px">${statCard('پرداخت‌ها',number(data.payments.length),'▤','تراکنش ثبت‌شده')}${statCard('لایک‌ها',number(data.likes.length),'♡','دریافتی و ارسالی')}${statCard('مچ‌ها',number(data.matches.length),'✦','ارتباط دوطرفه')}${statCard('تیکت‌ها',number(data.tickets.length),'▣','درخواست پشتیبانی')}</div>`;
  } else if(state.userTab==='activity') {
    content=`<div class="activity-list">${data.activity.sort((a,b)=>b.createdAt.localeCompare(a.createdAt)).map(a=>`<div class="activity-row"><div class="activity-dot">${iconForActivity[a.type]||'·'}</div><div><b>${esc(a.title)}</b><p>${esc(a.detail)}</p></div><time>${date(a.createdAt,true)}</time></div>`).join('')||'<div class="empty-state">فعالیتی ثبت نشده است.</div>'}</div>`;
  } else if(state.userTab==='payments') {
    content=`<div class="table-wrap"><table class="data-table"><thead><tr><th>شناسه</th><th>بسته</th><th>مبلغ</th><th>وضعیت</th><th>تاریخ</th><th>جزئیات</th></tr></thead><tbody>${data.payments.map(p=>`<tr><td class="mono">${esc(p.id)}</td><td>${esc(p.packageTitle)}</td><td>${money(p.amount)}</td><td>${badge(p.status)}</td><td>${date(p.createdAt,true)}</td><td><button class="button small-btn" data-action="payment-detail" data-id="${esc(p.id)}">جزئیات</button></td></tr>`).join('')||tableEmpty(6)}</tbody></table></div>`;
  } else if(state.userTab==='social') {
    content=`<div class="grid list-cards">${data.likes.map(l=>`<article class="record-card"><h4>♡ ${esc(l.fromName)} <span class="muted">←</span> ${esc(l.toName)}</h4><p>${l.type==='incoming'?'لایک دریافتی':'لایک ارسالی'}</p><div class="record-meta"><span>${esc(l.id)}</span><span>${date(l.createdAt,true)}</span></div></article>`).join('')}${data.matches.map(m=>`<article class="record-card"><h4>✦ ${esc(m.userAName)} و ${esc(m.userBName)}</h4><p>وضعیت ارتباط: ${esc(m.status==='active'?'فعال':'پایان‌یافته')}</p><div class="record-meta"><span>${esc(m.id)}</span><span>${date(m.createdAt,true)}</span></div></article>`).join('')}${data.likes.length+data.matches.length===0?'<div class="empty-state">لایک یا مچی ثبت نشده است.</div>':''}</div>`;
  } else if(state.userTab==='dates') {
    content=`<div class="list-cards">${data.dates.map(d=>`<article class="record-card"><h4>⌖ ${esc(d.place)}</h4><p>${esc(d.userAName)} · ${esc(d.userBName)}</p><div class="record-meta"><span>${badge(d.status)}</span><span>${date(d.scheduledAt,true)}</span></div></article>`).join('')||'<div class="empty-state">دیتی برای این کاربر ثبت نشده است.</div>'}</div>`;
  } else if(state.userTab==='tickets') {
    content=`<div class="list-cards">${data.tickets.map(t=>`<article class="record-card"><h4>${esc(t.subject)}</h4><p>${esc(t.category)}</p><div class="record-meta"><span>${badge(t.status)}</span><button class="panel-link" data-action="open-ticket" data-id="${esc(t.id)}">مشاهده تیکت ←</button></div></article>`).join('')||'<div class="empty-state">تیکتی ثبت نشده است.</div>'}</div>`;
  } else if(state.userTab==='verification') {
    content=`<div class="list-cards">${data.verifications.map(v=>`<article class="record-card"><h4>${esc(v.id)} · درخواست ویدئویی</h4><p>${esc(v.videoLabel || 'ویدیو') } · ${number(v.durationSeconds)} ثانیه</p><p>${esc(v.reviewReason||v.prompt||'')}</p><div class="record-meta"><span>${badge(v.status)}</span><span>${date(v.submittedAt,true)}</span></div></article>`).join('')||'<div class="empty-state">درخواستی ثبت نشده است.</div>'}</div>`;
  } else {
    content=`<div class="list-cards">${data.reports.map(r=>`<article class="record-card"><h4>${esc(r.reason)}</h4><p>${esc(r.description)}</p><p>${r.reporterId===u.id?'این کاربر گزارش را ثبت کرده است.':'این کاربر گزارش شده است.'}</p><div class="record-meta"><span>${badge(r.status)}</span><span>${date(r.createdAt,true)}</span></div></article>`).join('')||'<div class="empty-state">گزارشی مرتبط با این کاربر وجود ندارد.</div>'}</div>`;
  }
  const actions=`<button class="button danger" data-action="delete-user" data-id="${esc(u.id)}">حذف حساب کاربری</button>`;
  shellPage(`<button class="back-link" data-action="back-users">← بازگشت به فهرست کاربران</button><div class="detail-head"><div class="detail-profile">${avatar(u.name,u.gender)}<div><h2>${esc(u.name)} ${u.online?'<span class="badge success">آنلاین</span>':''}</h2><p>${esc(u.id)} · ${esc(u.phone)} · عضویت ${date(u.joinedAt)}</p></div></div><div class="heading-actions">${badge(u.status)} ${actions}</div></div><section class="panel"><div class="tabs">${tabs.map(([k,l])=>`<button class="tab-button ${state.userTab===k?'active':''}" data-action="user-tab" data-tab="${k}">${l}</button>`).join('')}</div>${content}</section>`);
}

async function renderPayments() {
  const params=new URLSearchParams();if(state.paymentQuery)params.set('q',state.paymentQuery);if(state.paymentStatus!=='all')params.set('status',state.paymentStatus);
  const {payments}=await api(`/api/payments?${params}`);
  const rows=payments.map(p=>`<tr><td class="mono">${esc(p.id)}</td><td>${personCell(p.userName,p.userId)}</td><td>${esc(p.packageTitle)}</td><td>${money(p.amount)}</td><td>${badge(p.status)}</td><td>${esc(p.gateway)}</td><td>${date(p.createdAt,true)}</td><td><button class="button small-btn" data-action="payment-detail" data-id="${esc(p.id)}">جزئیات ↗</button></td></tr>`).join('')||tableEmpty(8);
  shellPage(`${pageHeader('پرداخت‌ها','مشاهده تراکنش‌ها، وضعیت درگاه و جزئیات مالی هر پرداخت.',`<span class="badge info">${number(payments.length)} تراکنش</span>`)}<section class="panel table-panel"><div class="toolbar" style="padding:16px 17px 0"><form class="toolbar-start" data-form="search-payments"><label class="search-box"><span>⌕</span><input name="q" value="${esc(state.paymentQuery)}" placeholder="شناسه پرداخت، کاربر یا کد تراکنش..."/><button type="submit" class="button small-btn">جست‌وجو</button></label></form><div class="toolbar-end"><select class="select" id="paymentStatus"><option value="all">همه وضعیت‌ها</option><option value="success">موفق</option><option value="failed">ناموفق</option><option value="pending">در انتظار</option></select></div></div><div class="table-wrap"><table class="data-table"><thead><tr><th>شناسه</th><th>کاربر</th><th>محصول</th><th>مبلغ</th><th>وضعیت</th><th>درگاه</th><th>تاریخ</th><th>عملیات</th></tr></thead><tbody>${rows}</tbody></table></div><div class="table-footer"><span>نمایش ${number(payments.length)} تراکنش نمونه</span><span>تمام مبلغ‌ها به تومان هستند.</span></div></section>`);
  $('#paymentStatus').value=state.paymentStatus;$('#paymentStatus').addEventListener('change',e=>{state.paymentStatus=e.target.value;renderPayments();});
}
async function openPayment(id) {
  const {payment:p,user:u}=await api(`/api/payments/${encodeURIComponent(id)}`);
  openModal(`جزئیات پرداخت ${p.id}`,`<div class="detail-list">${[['شناسه پرداخت',p.id],['شناسه درگاه',p.transactionId],['کاربر',p.userName],['شناسه کاربر',p.userId],['محصول',p.packageTitle],['مبلغ',money(p.amount)],['وضعیت',badge(p.status)],['درگاه پرداخت',p.gateway],['تاریخ تراکنش',date(p.createdAt,true)],['شرح',p.description],['ایمیل کاربر',u?.email||'—'],['شماره موبایل',u?.phone||'—']].map(([k,v])=>`<div class="detail-box"><small>${k}</small><b>${v}</b></div>`).join('')}</div>`,`<button class="button" data-action="close-modal">بستن</button>`,'wide');
}

async function renderReports() {
  const {reports}=await api('/api/reports'); const pending=reports.filter(x=>x.status==='pending').length;
  const list=reports.map(r=>`<article class="report-item"><div class="report-symbol">⚑</div><div class="report-content"><div class="ticket-item-top"><h4>${esc(r.reason)}</h4>${badge(r.status)}</div><p>${esc(r.description)}</p><div class="report-meta"><span>گزارش‌دهنده: <b>${esc(r.reporterName)}</b></span><span>کاربر گزارش‌شده: <b>${esc(r.targetName)}</b></span><span>${date(r.createdAt,true)}</span></div><div class="report-actions"><button class="button small-btn" data-action="report-detail" data-id="${esc(r.id)}">مشاهده جزئیات</button>${r.status==='pending'?`<button class="button success small-btn" data-action="report-decision" data-id="${esc(r.id)}" data-status="approved">تأیید گزارش</button><button class="button danger small-btn" data-action="report-decision" data-id="${esc(r.id)}" data-status="rejected">رد گزارش</button>`:''}</div></div></article>`).join('')||'<div class="empty-state">گزارشی ثبت نشده است.</div>';
  shellPage(`${pageHeader('گزارش تخلف','بررسی گزارش‌های کاربران و تصمیم‌گیری درباره محتوای نامناسب یا رفتارهای خلاف قوانین.',`<span class="badge warning">${number(pending)} مورد در انتظار</span>`)}<div class="report-layout"><section class="panel" style="padding:0">${list}</section><aside class="panel side-summary report-summary"><div><span class="subtle small">کل گزارش‌ها</span><div class="summary-number">${number(reports.length)}</div></div><div><span class="subtle small">در انتظار بررسی</span><div class="summary-number" style="color:var(--yellow)">${number(pending)}</div></div><div><span class="subtle small">تأیید / رد</span><div class="summary-number" style="color:var(--green)">${number(reports.filter(x=>x.status==='approved').length)} / ${number(reports.filter(x=>x.status==='rejected').length)}</div></div></aside></div>`);
}
async function reviewReport(id,status) {
  const label=status==='approved'?'تأیید':'رد';
  openModal(`${label} گزارش تخلف`,`<p>آیا از ${label} این گزارش اطمینان داری؟ ${status==='approved'?'پس از تأیید، گزارش به‌عنوان تخلف تأییدشده ثبت می‌شود.':'گزارش با وضعیت رد شده بایگانی می‌شود.'}</p><label class="field-label" for="reviewNote">یادداشت مدیر (اختیاری)</label><textarea id="reviewNote" class="reply-form" style="width:100%;background:#202941;color:#fff;border:1px solid var(--line);border-radius:9px;padding:10px;min-height:75px" placeholder="توضیح تصمیم..."></textarea>`,`<button class="button" data-action="close-modal">انصراف</button><button class="button ${status==='approved'?'success':'danger'}" data-action="confirm-report" data-id="${esc(id)}" data-status="${status}">ثبت تصمیم</button>`);
}
async function renderVerifications() {
  const {verifications}=await api('/api/verifications');const pending=verifications.filter(v=>v.status==='pending').length;
  const cards=verifications.map(v=>{const videoUrl=safeHttpsUrl(v.videoUrl);const safeVideo=videoUrl?`<video class="verification-video" controls preload="metadata" src="${esc(videoUrl)}">مرورگر شما پخش ویدئو را پشتیبانی نمی‌کند.</video>`:`<div class="video-placeholder"><span class="play-button">▶</span><b>${esc(v.videoLabel||'ویدیوی کاربر')}</b><small>${v.durationSeconds?`${number(v.durationSeconds)} ثانیه`:'فایل نمونه'}</small></div>`;return `<article class="verification-card">${safeVideo}<div class="verification-info"><div class="ticket-item-top"><h4>${esc(v.userName)} <span class="muted mono">${esc(v.id)}</span></h4>${badge(v.status)}</div><p>درخواست احراز هویت ویدئویی برای تأیید هویت و اصالت پروفایل.</p><p><b>عبارت درخواستی:</b> ${esc(v.prompt||'—')}</p>${v.reviewReason?`<p><b>علت رد:</b> ${esc(v.reviewReason)}</p>`:''}<div class="record-meta"><span>کاربر: ${esc(v.userId)}</span><span>ثبت: ${date(v.submittedAt,true)}</span></div>${v.status==='pending'?`<div class="report-actions"><button class="button success small-btn" data-action="verification-decision" data-id="${esc(v.id)}" data-status="verified">تأیید هویت</button><button class="button danger small-btn" data-action="verification-decision" data-id="${esc(v.id)}" data-status="rejected">رد درخواست</button><button class="button small-btn" data-action="open-user" data-id="${esc(v.userId)}">پروفایل کاربر</button></div>`:`<div class="report-actions"><button class="button small-btn" data-action="open-user" data-id="${esc(v.userId)}">مشاهده کاربر</button></div>`}</div></article>`;}).join('')||'<div class="empty-state">درخواست احراز هویتی وجود ندارد.</div>';
  shellPage(`${pageHeader('احراز هویت ویدئویی','صف درخواست‌های تأیید ویدئو؛ تصمیم مدیر در پروفایل کاربر نیز ثبت می‌شود.',`<span class="badge warning">${number(pending)} درخواست جدید</span>`)}<div class="panel" style="padding:15px"><div class="table-note">توجه: این نسخه دمو فایل ویدئویی واقعی در دیتاست ندارد و فقط فراداده درخواست را نمایش می‌دهد. در نسخه عملیاتی باید ویدئو از فضای ذخیره‌سازی خصوصی و با لینک موقت امن نمایش داده شود.</div>${cards}</div>`);
}
async function verificationDecision(id,status) {
  if(status==='verified') {
    const {verification}=await api(`/api/verifications/${encodeURIComponent(id)}`,{method:'PATCH',body:{status}});toast(`درخواست ${verification.id} تأیید شد.`);renderVerifications();return;
  }
  openModal('رد درخواست احراز هویت','<p>علت رد درخواست را وارد کن تا در سوابق بررسی ذخیره شود.</p><label class="field-label" for="verificationReason">علت رد</label><textarea id="verificationReason" style="width:100%;min-height:85px;background:#202941;color:#fff;border:1px solid var(--line);border-radius:9px;padding:10px" placeholder="مثلاً کیفیت پایین ویدئو یا عدم تطابق اطلاعات..." required></textarea>',`<button class="button" data-action="close-modal">انصراف</button><button class="button danger" data-action="confirm-verification" data-id="${esc(id)}">ثبت رد درخواست</button>`);
}

async function renderEngagement() {
  const [likes,matches,datesData]=await Promise.all([api('/api/likes'),api('/api/matches'),api('/api/dates')]);
  const data={likes:likes.likes,matches:matches.matches,dates:datesData.dates};const tabs=[['likes','لایک‌ها',data.likes.length],['matches','مچ‌ها',data.matches.length],['dates','دیت‌های برنامه‌ریزی‌شده',data.dates.length]];
  let content='';
  if(state.socialTab==='likes') content=`<table class="data-table"><thead><tr><th>از طرف</th><th>به طرف</th><th>نوع</th><th>تاریخ</th><th>شناسه</th></tr></thead><tbody>${data.likes.map(l=>`<tr><td>${personCell(l.fromName,l.fromId)}</td><td>${personCell(l.toName,l.toId)}</td><td>${badge(l.type==='incoming'?'info':'purple',l.type==='incoming'?'دریافتی':'ارسالی')}</td><td>${date(l.createdAt,true)}</td><td class="mono">${esc(l.id)}</td></tr>`).join('')||tableEmpty(5)}</tbody></table>`;
  else if(state.socialTab==='matches') content=`<table class="data-table"><thead><tr><th>کاربر اول</th><th>کاربر دوم</th><th>وضعیت</th><th>تاریخ مچ</th><th>شناسه</th></tr></thead><tbody>${data.matches.map(m=>`<tr><td>${personCell(m.userAName,m.userAId)}</td><td>${personCell(m.userBName,m.userBId)}</td><td>${badge(m.status)}</td><td>${date(m.createdAt,true)}</td><td class="mono">${esc(m.id)}</td></tr>`).join('')||tableEmpty(5)}</tbody></table>`;
  else content=`<table class="data-table"><thead><tr><th>کاربر اول</th><th>کاربر دوم</th><th>مکان قرار</th><th>زمان قرار</th><th>وضعیت</th><th>جزئیات</th></tr></thead><tbody>${data.dates.map(d=>`<tr><td>${personCell(d.userAName,d.userAId)}</td><td>${personCell(d.userBName,d.userBId)}</td><td>${esc(d.location?.name || d.place)}</td><td>${date(d.scheduledAt,true)}</td><td>${badge(d.status)}</td><td><button class="button small-btn" data-action="date-detail" data-id="${esc(d.id)}">مشاهده دیت ↗</button></td></tr>`).join('')||tableEmpty(6)}</tbody></table>`;
  shellPage(`${pageHeader('تعاملات محصول','پایش لایک‌های ارسالی و دریافتی، ارتباط‌های دوطرفه و قرارهای برنامه‌ریزی‌شده.')}
    <section class="panel table-panel"><div class="tabs" style="padding:0 14px;margin:0">${tabs.map(([id,label,count])=>`<button class="tab-button ${state.socialTab===id?'active':''}" data-action="social-tab" data-tab="${id}">${label} <span class="badge neutral">${number(count)}</span></button>`).join('')}</div><div class="table-wrap">${content}</div><div class="table-footer"><span>نمایش داده‌های نمونه تعاملات</span><span>برای مشاهده تاریخچه کاربر از صفحه جزئیات او استفاده کن.</span></div></section>`);
}

async function renderTickets() {
  const params=new URLSearchParams();if(state.ticketQuery)params.set('q',state.ticketQuery);if(state.ticketStatus!=='all')params.set('status',state.ticketStatus);
  const {tickets}=await api(`/api/tickets?${params}`);
  if(state.ticketId && !tickets.some(t=>t.id===state.ticketId)) state.ticketId=tickets[0]?.id||null;
  if(!state.ticketId && tickets.length) state.ticketId=tickets[0].id;
  const selected=tickets.find(t=>t.id===state.ticketId);
  const list=tickets.map(t=>`<button class="ticket-item ${t.id===state.ticketId?'active':''}" data-action="open-ticket" data-id="${esc(t.id)}"><div class="ticket-item-top"><b>${esc(t.subject)}</b>${badge(t.status)}</div><p>${esc(t.messages.at(-1)?.text||'بدون پیام')}</p><small>${esc(t.userName)} · ${date(t.updatedAt,true)}</small></button>`).join('')||'<div class="empty-state">تیکتی پیدا نشد.</div>';
  const messages=selected?selected.messages.map(m=>{const attachmentUrl=safeHttpsUrl(m.attachmentUrl);const safeAttachment=attachmentUrl?`<p><a class="panel-link" href="${esc(attachmentUrl)}" target="_blank" rel="noopener noreferrer">مشاهده پیوست ↗</a></p>`:'';return `<div class="message ${m.author==='admin'?'admin':''}"><div class="message-head"><b>${esc(m.authorName)}</b><time>${date(m.createdAt,true)}</time></div><p>${esc(m.text)}</p>${safeAttachment}</div>`;}).join(''):'<div class="empty-state">یک تیکت را از فهرست انتخاب کن.</div>';
  const history=selected?(selected.history||[]).map(h=>`<div class="activity-row"><div class="activity-dot">↻</div><div><b>${esc(h.action)}</b><p>${esc(h.actor||'سیستم')}</p></div><time>${date(h.createdAt||selected.updatedAt,true)}</time></div>`).join(''):' ';
  const detail=selected?`<div class="ticket-chat-head"><div><h3>${esc(selected.subject)}</h3><p>${esc(selected.id)} · ${esc(selected.userName)} · ${esc(selected.category)}</p></div><div class="heading-actions">${badge(selected.status)}<button class="button small-btn ${selected.status==='open'?'danger':'success'}" data-action="ticket-status" data-id="${esc(selected.id)}" data-status="${selected.status==='open'?'closed':'open'}">${selected.status==='open'?'بستن تیکت':'بازکردن مجدد'}</button><button class="button small-btn" data-action="open-user" data-id="${esc(selected.userId)}">پروفایل</button></div></div><div class="ticket-messages">${messages}</div><details class="ticket-history"><summary>مشاهده تاریخچه تغییرات تیکت</summary><div class="activity-list">${history||'<div class="empty-state">تاریخچه‌ای ثبت نشده است.</div>'}</div></details><div class="reply-form">${selected.status==='open'?`<form data-form="ticket-reply"><input type="hidden" name="id" value="${esc(selected.id)}"/><textarea name="text" required maxlength="5000" placeholder="پاسخ خود را برای کاربر بنویس..."></textarea><div class="reply-form-actions"><small>پاسخ شما با نام «پشتیبانی FastLove» ثبت می‌شود.</small><button class="button primary" type="submit">ارسال پاسخ ←</button></div></form>`:'<div class="empty-state" style="padding:15px">این تیکت بسته شده است. برای پاسخ‌گویی ابتدا آن را باز کنید.</div>'}</div>`:'<div class="empty-state">از فهرست سمت راست یک تیکت را انتخاب کن.</div>';
  shellPage(`${pageHeader('تیکت‌های پشتیبانی','مشاهده تاریخچه گفتگو، پاسخ‌گویی به کاربران و مدیریت وضعیت درخواست‌ها.',`<span class="badge info">${number(tickets.length)} تیکت</span>`)}<div class="ticket-layout"><section class="panel ticket-chat">${detail}</section><section class="panel ticket-list"><div class="ticket-list-head"><h3>صندوق ورودی</h3><form data-form="search-tickets" class="search-box" style="min-width:0;width:100%"><span>⌕</span><input name="q" value="${esc(state.ticketQuery)}" placeholder="جست‌وجوی تیکت یا کاربر..."/><button type="submit" class="button small-btn">بگرد</button></form><div class="toolbar-start" style="margin-top:10px"><button class="filter-chip ${state.ticketStatus==='all'?'active':''}" data-action="ticket-filter" data-status="all">همه</button><button class="filter-chip ${state.ticketStatus==='open'?'active':''}" data-action="ticket-filter" data-status="open">باز</button><button class="filter-chip ${state.ticketStatus==='closed'?'active':''}" data-action="ticket-filter" data-status="closed">بسته</button></div></div>${list}</section></div>`);
}
async function renderPackages() {
  const {packages}=await api('/api/packages');
  const cards=packages.map(p=>`<article class="panel package-card"><div class="package-top"><h3>${esc(p.name)}</h3>${badge(p.active?'active':'closed',p.active?'فعال':'غیرفعال')}</div><div class="package-meta"><span>${p.type==='subscription'?'اشتراک ویژه':'پیام مستقیم'}</span>${p.durationDays?`<span>${number(p.durationDays)} روز اعتبار</span>`:''}${p.messageCount?`<span>${number(p.messageCount)} پیام</span>`:''}<span class="mono">${esc(p.id)}</span></div><div class="package-price">${money(p.price)}</div><p>${esc(p.description || '')}</p><form data-form="save-package"><input type="hidden" name="id" value="${esc(p.id)}"/><div class="field-label">قیمت جدید (تومان)</div><div class="package-edit"><input type="number" min="1" max="100000000000" step="1000" name="price" required value="${Number(p.price)}"/><button class="button primary" type="submit">ذخیره قیمت</button></div></form><div class="package-actions"><button class="button small-btn ${p.active?'':'success'}" data-action="toggle-package" data-id="${esc(p.id)}" data-active="${p.active?'false':'true'}">${p.active?'غیرفعال‌کردن':'فعال‌کردن'}</button><button class="button danger small-btn" data-action="delete-package" data-id="${esc(p.id)}" data-name="${esc(p.name)}">حذف بسته</button></div></article>`).join('') || '<div class="empty-state">هنوز بسته‌ای تعریف نشده است.</div>';
  shellPage(`${pageHeader('بسته‌ها و قیمت‌گذاری','تعریف و مدیریت پویای بسته‌های اشتراک ویژه و پیام مستقیم. قیمت‌ها برحسب تومان ثبت می‌شوند.',`<span class="badge info">${number(packages.length)} بسته</span>`)}
    <section class="panel package-create-panel"><div class="panel-head"><div><h3>تعریف بسته جدید</h3><p>نوع بسته را انتخاب و عنوان، اعتبار یا تعداد پیام و قیمت را مشخص کنید.</p></div></div><form class="package-create-form" data-form="create-package"><div class="package-form-grid"><label><span class="field-label">نوع بسته</span><select class="select" name="type" id="newPackageType" required><option value="subscription">اشتراک ویژه</option><option value="direct_message">پیام مستقیم</option></select></label><label><span class="field-label">عنوان بسته</span><input name="name" maxlength="100" required placeholder="مثلاً اشتراک ویژه سه‌ماهه"/></label><label id="packageDurationField"><span class="field-label">بازه اعتبار (روز)</span><input type="number" name="durationDays" min="1" max="3650" value="30"/></label><label id="packageMessagesField" hidden><span class="field-label">تعداد پیام مستقیم</span><input type="number" name="messageCount" min="1" max="100000" value="10"/></label><label><span class="field-label">قیمت (تومان)</span><input type="number" name="price" min="1" max="100000000000" step="1000" required placeholder="مثلاً ۳۹۰۰۰۰"/></label></div><div class="package-create-footer"><small>بسته جدید پس از ذخیره به‌صورت فعال در فهرست قرار می‌گیرد.</small><button class="button primary" type="submit">＋ ایجاد بسته</button></div></form></section>
    <div class="package-section-title"><h3>بسته‌های تعریف‌شده</h3><span>${number(packages.length)} مورد</span></div><div class="grid package-grid">${cards}</div><div class="panel" style="margin-top:15px;padding:14px"><div class="table-note">در محیط واقعی، سرویس پرداخت باید نوع، اعتبار و قیمت فعال هر بسته را از بک‌اند بخواند و مبلغ ارسالی کلاینت را معتبر نداند.</div></div>`);
  syncPackageTypeFields();
}
function syncPackageTypeFields() {
  const select = $('#newPackageType'); if (!select) return;
  const subscription = select.value === 'subscription';
  $('#packageDurationField').hidden = !subscription;
  $('#packageMessagesField').hidden = subscription;
  $('#packageDurationField input').required = subscription;
  $('#packageMessagesField input').required = !subscription;
}
async function showDateDetail(id) {
  const {date: item,userA,userB}=await api(`/api/dates/${encodeURIComponent(id)}`);
  const loc=item.location || {};
  const mapUrl=Number.isFinite(Number(loc.latitude)) && Number.isFinite(Number(loc.longitude)) ? `https://www.google.com/maps?q=${encodeURIComponent(`${loc.latitude},${loc.longitude}`)}` : '';
  const details=[['شناسه دیت',item.id],['کاربر اول',userA?.name || item.userAName],['کاربر دوم',userB?.name || item.userBName],['زمان برنامه‌ریزی‌شده',date(item.scheduledAt,true)],['وضعیت',badge(item.status)],['مکان انتخاب‌شده',loc.name || item.place],['آدرس کامل',loc.address || 'برای این دیت ثبت نشده است.'],['نقطه ملاقات',loc.meetPoint || '—'],['مختصات',loc.latitude != null && loc.longitude != null ? `${loc.latitude}, ${loc.longitude}` : 'ثبت نشده'],['توضیحات مکان',loc.notes || '—']];
  openModal(`جزئیات دیت ${esc(item.id)}`,`<div class="detail-list">${details.map(([label,value],index)=>`<div class="detail-box"><small>${label}</small><b>${index===4?value:esc(value)}</b></div>`).join('')}</div>${mapUrl?`<p class="date-map-link"><a class="button" href="${mapUrl}" target="_blank" rel="noopener noreferrer">نمایش موقعیت در نقشه ↗</a></p>`:''}`,`<button class="button" data-action="close-modal">بستن</button>`,'wide');
}

function openModal(title, body, actions = '', size = '') {
  $('#modalRoot').innerHTML = `<div class="modal-backdrop" data-action="backdrop-close"><section class="modal ${size}" role="dialog" aria-modal="true" aria-labelledby="modalTitle"><div class="modal-head"><h3 id="modalTitle">${title}</h3><button class="modal-close" data-action="close-modal" aria-label="بستن">×</button></div><div class="modal-body">${body}</div><div class="modal-actions">${actions}</div></section></div>`;
  $('.modal', $('#modalRoot')).addEventListener('click',e=>e.stopPropagation());
}
function closeModal(){ $('#modalRoot').innerHTML=''; }

// Delegated interactions keep dynamically rendered views keyboard- and mouse-accessible.
document.addEventListener('click', async (event) => {
  const button = event.target.closest('[data-action], [data-page]'); if(!button)return;
  if(button.dataset.page){ navigate(button.dataset.page); return; }
  const action=button.dataset.action; const id=button.dataset.id;
  try {
    if(action==='toggle-password'){ const input=$('#password');input.type=input.type==='password'?'text':'password'; }
    else if(action==='toggle-sidebar'){ $('#sidebar').classList.toggle('open'); }
    else if(action==='logout'){ await api('/api/auth/logout',{method:'POST'}); showLogin(); toast('با موفقیت خارج شدی.'); }
    else if(action==='refresh'){ await render(); toast('اطلاعات بروزرسانی شد.'); }
    else if(action==='select-chat'){state.chatId=id;await renderChats();}
    else if(action==='date-detail'){await showDateDetail(id);}
    else if(action==='toggle-package'){await api(`/api/packages/${encodeURIComponent(id)}`,{method:'PATCH',body:{active:button.dataset.active==='true'}});toast(button.dataset.active==='true'?'بسته فعال شد.':'بسته غیرفعال شد.');await renderPackages();}
    else if(action==='delete-package'){openModal('حذف بسته',`<p>بسته «${esc(button.dataset.name || id)}» به‌طور کامل از فهرست بسته‌ها حذف می‌شود.</p><p><b>آیا از حذف این بسته مطمئن هستید؟</b></p>`,`<button class="button" data-action="close-modal">انصراف</button><button class="button danger" data-action="confirm-delete-package" data-id="${esc(id)}">حذف بسته</button>`);}
    else if(action==='confirm-delete-package'){await api(`/api/packages/${encodeURIComponent(id)}`,{method:'DELETE'});closeModal();toast('بسته حذف شد.');await renderPackages();}
    else if(action==='open-user'){ state.userId=id;state.userTab='overview';state.page='userDetail';await render(); }
    else if(action==='back-users'){ state.userId=null;state.page='users';await render(); }
    else if(action==='user-tab'){state.userTab=button.dataset.tab;await renderUserDetail();}
    else if(action==='delete-user'){
      openModal('حذف حساب کاربری','<p>این عملیات دسترسی کاربر را غیرفعال کرده و وضعیت حساب را به «حذف شده» تغییر می‌دهد. برای حفظ یکپارچگی سوابق مالی و گزارش‌ها، داده‌ها به‌صورت نرم حذف می‌شوند.</p><p><b>آیا از حذف حساب اطمینان داری؟</b></p>',`<button class="button" data-action="close-modal">انصراف</button><button class="button danger" data-action="confirm-delete-user" data-id="${esc(id)}">حذف حساب</button>`);
    }
    else if(action==='confirm-delete-user'){await api(`/api/users/${encodeURIComponent(id)}`,{method:'DELETE'});closeModal();toast('حساب کاربر حذف نرم شد.');state.page='users';state.userId=null;await render();}
    else if(action==='payment-detail'){await openPayment(id);}
    else if(action==='report-decision'){await reviewReport(id,button.dataset.status);}
    else if(action==='report-detail'){
      const {reports}=await api('/api/reports');const r=reports.find(x=>x.id===id);if(r)openModal(`جزئیات گزارش ${r.id}`,`<div class="detail-list">${[['گزارش‌دهنده',r.reporterName],['شناسه گزارش‌دهنده',r.reporterId],['کاربر گزارش‌شده',r.targetName],['شناسه کاربر',r.targetId],['دسته‌بندی',r.reason],['وضعیت',badge(r.status)],['تاریخ ثبت',date(r.createdAt,true)],['مدارک پیوست',number(r.evidenceCount)]].map(([k,v])=>`<div class="detail-box"><small>${k}</small><b>${v}</b></div>`).join('')}</div><p style="margin-top:14px"><b>شرح گزارش</b><br/>${esc(r.description)}</p>${r.reviewNote?`<p><b>یادداشت مدیر</b><br/>${esc(r.reviewNote)}</p>`:''}`,`<button class="button" data-action="close-modal">بستن</button>`,'wide');
    }
    else if(action==='confirm-report'){const note=$('#reviewNote')?.value||'';await api(`/api/reports/${encodeURIComponent(id)}`,{method:'PATCH',body:{status:button.dataset.status,note}});closeModal();toast('نتیجه بررسی گزارش ذخیره شد.');await renderReports();}
    else if(action==='verification-decision'){await verificationDecision(id,button.dataset.status);}
    else if(action==='confirm-verification'){const reason=$('#verificationReason')?.value.trim()||'';if(!reason){toast('لطفاً علت رد را وارد کن.','error');return;}await api(`/api/verifications/${encodeURIComponent(id)}`,{method:'PATCH',body:{status:'rejected',reason}});closeModal();toast('درخواست احراز هویت رد شد.');await renderVerifications();}
    else if(action==='social-tab'){state.socialTab=button.dataset.tab;await renderEngagement();}
    else if(action==='open-ticket'){state.ticketId=id;state.page='tickets';await renderTickets();}
    else if(action==='ticket-filter'){state.ticketStatus=button.dataset.status;state.ticketId=null;await renderTickets();}
    else if(action==='ticket-status'){await api(`/api/tickets/${encodeURIComponent(id)}/status`,{method:'PATCH',body:{status:button.dataset.status}});toast(button.dataset.status==='closed'?'تیکت بسته شد.':'تیکت دوباره باز شد.');await renderTickets();}
    else if(action==='close-modal'){closeModal();}
    else if(action==='backdrop-close' && event.target===button){closeModal();}
  } catch(error){toast(error.message,'error');}
});

document.addEventListener('change', event=>{if(event.target?.id==='newPackageType')syncPackageTypeFields();});

document.addEventListener('submit', async (event)=>{
  const form=event.target.closest('form[data-form], #loginForm'); if(!form)return;event.preventDefault();
  const action=form.dataset.form;
  try{
    if(form.id==='loginForm'){
      $('#loginError').textContent='';const fd=new FormData(form);const result=await api('/api/auth/login',{method:'POST',body:{username:fd.get('username'),password:fd.get('password')}});showApp(result.admin);
    }
    else if(action==='search-users'){state.usersQuery=String(new FormData(form).get('q')||'').trim();await renderUsers();}
    else if(action==='search-referrals'){state.referralQuery=String(new FormData(form).get('q')||'').trim();await renderReferrals();}
    else if(action==='search-payments'){state.paymentQuery=String(new FormData(form).get('q')||'').trim();await renderPayments();}
    else if(action==='search-chats'){state.chatsQuery=String(new FormData(form).get('q')||'').trim();state.chatId=null;await renderChats();}
    else if(action==='search-tickets'){state.ticketQuery=String(new FormData(form).get('q')||'').trim();state.ticketId=null;await renderTickets();}
    else if(action==='ticket-reply'){const fd=new FormData(form);const text=String(fd.get('text')||'').trim();if(!text){toast('متن پاسخ اجباری است.','error');return;}await api(`/api/tickets/${encodeURIComponent(fd.get('id'))}/reply`,{method:'POST',body:{text}});toast('پاسخ برای کاربر ارسال شد.');await renderTickets();}
    else if(action==='save-package'){const fd=new FormData(form);const payload={price:Number(fd.get('price'))};await api(`/api/packages/${encodeURIComponent(fd.get('id'))}`,{method:'PATCH',body:payload});toast('قیمت بسته ذخیره شد.');await renderPackages();}
    else if(action==='create-package'){const fd=new FormData(form);const payload={type:fd.get('type'),name:String(fd.get('name')||'').trim(),price:Number(fd.get('price'))};if(payload.type==='subscription')payload.durationDays=Number(fd.get('durationDays'));else payload.messageCount=Number(fd.get('messageCount'));await api('/api/packages',{method:'POST',body:payload});toast('بسته جدید ایجاد شد.');await renderPackages();}
  }catch(error){if(form.id==='loginForm')$('#loginError').textContent=error.message;else toast(error.message,'error');}
});

(async function init(){
  showLogin();
  try {
    const health = await api('/api/health');
    if (health.mode === 'demo') $('#demoCredentials').hidden = false;
  } catch (error) {
    $('#loginError').textContent = error.message;
    return;
  }

  try {
    const auth = await api('/api/auth/me');
    showApp(auth.admin);
  } catch (error) {
    if (error.status !== 401) $('#loginError').textContent = error.message;
  }
})();
