import { PRODUCT_FEATURES, PEOPLE, DEMO_PROFILE, createInitialState } from './data.js';
import { Repository } from './repository.js';
import { FastLoveApiClient } from './api-client.js';

const api = new FastLoveApiClient();
const faNum = value => String(value ?? '').replace(/\d/g, d => '۰۱۲۳۴۵۶۷۸۹'[Number(d)]);
const money = value => `${Number(value || 0).toLocaleString('fa-IR')} تومان`;
const dateFa = value => value ? new Intl.DateTimeFormat('fa-IR', { dateStyle: 'medium' }).format(new Date(value)) : '—';
const timeFa = value => value ? new Intl.DateTimeFormat('fa-IR', { hour: '2-digit', minute: '2-digit' }).format(new Date(value)) : '';
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const person = id => PEOPLE.find(p => p.id === Number(id));
const uid = prefix => `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2,6)}`;

export function mountFastLove(root, { variant = 'web' } = {}) {
  let state = Repository.load();
  const todayKey = new Date().toISOString().slice(0, 10);
  if (state.swipeDay !== todayKey) { state.dailySwipes = 0; state.swipeDay = todayKey; Repository.save(state); }
  let authStep = 1;
  let authPhone = '';
  let modal = null;
  let activePersonId = null;
  let activeTicketId = null;
  let chatAttachment = null;
  let voiceRecorder = null;
  let voiceChunks = [];
  let selectedPhotoData = [];
  let selectedVoiceData = null;
  let selectedTicketAttachment = null;
  let videoObjectUrl = null;
  let swipeCount = 0;
  let installPrompt = null;
  let toastTimer;
  const appVariant = variant === 'pwa' ? 'pwa' : 'web';

  function persist() { Repository.save(state); }
  function update(patch) { state = { ...state, ...patch }; persist(); render(); }
  function toast(message) {
    const el = root.querySelector('.toast');
    if (!el) return;
    el.textContent = message;
    el.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 2600);
  }
  function getChats() { return state.chats || []; }
  function getChat(id) { return getChats().find(c => Number(c.personId) === Number(id)); }
  function getMessages(id) { return getChat(id)?.messages || []; }
  function setMessages(id, messages) {
    const chats = getChats().filter(c => Number(c.personId) !== Number(id));
    chats.unshift({ personId: Number(id), messages });
    state.chats = chats;
    persist();
  }
  function activePerson() { return person(activePersonId); }
  function userAge() { return state.profile?.birthYear ? Math.max(18, 1406 - Number(state.profile.birthYear)) : 29; }
  function isMatched(id) { return state.matchedIds.includes(Number(id)); }
  function canChat(id) { return isMatched(id) || state.paidChatIds.includes(Number(id)); }
  function currentCredits() { return Number(state.messageCredits || 0); }
  function currentPlan() { return state.subscription?.plan || 'رایگان'; }
  function navigate(screen) {
    state.currentScreen = screen;
    activePersonId = null;
    activeTicketId = null;
    modal = null;
    persist();
    render();
  }
  function openPerson(id) { activePersonId = Number(id); modal = 'person'; render(); }
  function like(id) {
    id = Number(id);
    if (state.likedIds.includes(id)) { toast('این پروفایل قبلاً لایک شده است.'); return; }
    const swipeLimit = currentPlan()==='رایگان' ? 10 : currentPlan()==='پرو' ? 50 : null;
    if (swipeLimit !== null && state.dailySwipes >= swipeLimit) { modal = 'limit'; render(); return; }
    state.likedIds = [...state.likedIds, id];
    state.dailySwipes += 1;
    swipeCount += 1;
    if (person(id)?.likedYou) {
      state.matchedIds = [...new Set([...state.matchedIds, id])];
      toast(`با ${person(id).name} مچ شدی! 💗`);
    } else toast('لایک با موفقیت ثبت شد.');
    persist();
    modal = null;
    render();
    maybeRate();
  }
  function pass(id) {
    id = Number(id);
    state.passedIds = [...new Set([...state.passedIds, id])];
    state.likedIds = state.likedIds.filter(x => x !== id);
    state.matchedIds = state.matchedIds.filter(x => x !== id);
    state.dailySwipes += 1;
    swipeCount += 1;
    persist(); modal = null; render(); maybeRate();
  }
  function maybeRate() {
    if (swipeCount >= 8 && !(state.ratings || []).length) { modal = 'rating'; render(); }
  }
  function filteredPeople() {
    const f = state.filters || {};
    return PEOPLE.filter(p => !state.blockedIds.includes(p.id) && !state.passedIds.includes(p.id) &&
      p.age >= Number(f.minAge || 18) && p.age <= Number(f.maxAge || 45) && p.distanceKm <= Number(f.maxDistance || 50) &&
      (f.city === 'همه شهرها' || !f.city || p.city === f.city) &&
      (f.goal === 'همه' || !f.goal || p.goal === f.goal) && (!f.jobs?.length || f.jobs.includes(p.job)) && (!f.interests?.length || p.interests.some(i=>f.interests.includes(i))) && (!f.verifiedOnly || p.verified));
  }
  function profileCard(p, compact = false) {
    if (!p) return '';
    return `<article class="person-card ${compact ? 'compact' : ''}" data-action="person" data-id="${p.id}">
      <div class="person-cover"><img src="${esc(p.photo)}" alt="تصویر ${esc(p.name)}" loading="lazy"><span class="online-dot ${p.online ? 'on' : ''}"></span>
        ${p.verified ? '<span class="verified-tag">✓ تأییدشده</span>' : ''}<span class="distance-tag">${faNum(p.distanceKm)} کیلومتر</span></div>
      <div class="person-info"><div class="person-name">${esc(p.name)} <small>${faNum(p.age)}</small></div><div class="muted">${esc(p.job)} · ${esc(p.city)}</div>
      <p>${esc(p.bio)}</p><div class="chips">${p.interests.slice(0,3).map(x=>`<span>${esc(x)}</span>`).join('')}</div></div></article>`;
  }
  function renderLogin() {
    if (state.session && !state.profile) return renderProfileSetup();
    return `<div class="auth-layout"><div class="auth-art"><div class="brand-mark">♥</div><div class="auth-copy"><span class="eyebrow">ارتباط واقعی، تجربه‌ای تازه</span><h1>آشنایی از یک<br><em>گفت‌وگوی خوب</em> شروع می‌شود.</h1><p>آدم‌های هم‌مسیرت را کشف کن، گفتگو بساز و اولین قرار را برنامه‌ریزی کن.</p><div class="auth-stats"><b>کشف</b><span>·</span><b>گفتگو</b><span>·</span><b>قرار</b></div></div></div>
      <section class="auth-panel"><div class="auth-brand"><span class="brand-symbol">♥</span><b>FastLove</b></div>
      ${authStep === 1 ? `<span class="eyebrow">به فست‌لاو خوش آمدی</span><h2>ورود یا ساخت حساب</h2><p class="muted">شماره موبایلت را وارد کن تا کد تأیید برایت ارسال شود.</p><form data-form="send-otp" class="form-stack"><label>شماره موبایل</label><input name="phone" type="tel" inputmode="tel" placeholder="مثلاً ۰۹۱۲۱۲۳۴۵۶۷" required minlength="10" value="${esc(authPhone)}"><button class="btn primary wide">دریافت کد تأیید</button></form><div class="demo-divider"><span>یا برای پیش‌نمایش</span></div><button class="btn ghost wide" data-action="demo-login">ورود به حساب نمایشی</button><small class="muted demo-note">در حالت دمو، کد تأیید ثابت ۱۲۳۴۵۶ است.</small>` : `<span class="eyebrow">تأیید شماره موبایل</span><h2>کد ۶ رقمی را وارد کن</h2><p class="muted">کد آزمایشی برای ${esc(authPhone)}: <b dir="ltr">123456</b></p><form data-form="verify-otp" class="form-stack"><label>کد تأیید</label><input name="code" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="۱۲۳۴۵۶" required><button class="btn primary wide">تأیید و ادامه</button></form><button class="text-btn" data-action="back-auth">تغییر شماره موبایل</button>`}
      <div class="auth-foot">با ادامه، <a href="#terms">شرایط استفاده</a> و <a href="#privacy">حریم خصوصی</a> را می‌پذیری.</div></section></div>`;
  }
  function renderProfileSetup() {
    return `<div class="setup-wrap"><div class="setup-card"><div class="auth-brand"><span class="brand-symbol">♥</span><b>FastLove</b></div><span class="eyebrow">قدم اول آشنایی</span><h2>پروفایلت را بساز</h2><p class="muted">این اطلاعات به دیگران کمک می‌کند تو را بهتر بشناسند.</p>
    <form data-form="setup-profile" class="form-grid"><label>نام نمایشی<input name="name" required placeholder="نام و نام خانوادگی" maxlength="48"></label><label>سال تولد (شمسی)<input name="birthYear" type="number" min="1350" max="1388" value="1375" required></label><label>ماه تولد<select name="birthMonth">${Array.from({length:12},(_,i)=>`<option value="${i+1}">${['فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'][i]}</option>`).join('')}</select></label><label>جنسیت<select name="gender"><option>زن</option><option selected>مرد</option><option>سایر</option></select></label><label>دنبال آشنایی با<select name="lookingFor"><option>زن</option><option>مرد</option><option>هر دو</option></select></label><label>هدف آشنایی<select name="goal"><option>رابطه جدی</option><option>دوستی ساده</option><option>ازدواج</option><option>قرار عادی</option></select></label><label>شهر<input name="city" value="تهران" required></label><label>شغل<input name="job" placeholder="مثلاً طراح محصول"></label><label class="full">درباره من<textarea name="about" rows="3" placeholder="کمی از خودت بگو..."></textarea></label><label class="full">علایق (با ویرگول جدا کن)<input name="interests" value="سفر، کافه، موسیقی"></label><label class="full">عکس‌های پروفایل<input name="photos" type="file" accept="image/*" multiple></label><button class="btn primary wide full">ساخت پروفایل و ورود</button></form></div></div>`;
  }
  function renderHeader() {
    return `<header class="topbar"><button class="mobile-menu" data-action="toggle-menu" aria-label="منو">☰</button><a class="brand" href="#" data-action="navigate" data-screen="discover"><span class="brand-symbol">♥</span><b>FastLove</b><small>ارتباط‌های معنادار</small></a><div class="topbar-actions">${appVariant==='pwa'&&installPrompt?'<button class="btn ghost install-btn" data-action="install-app">نصب اپلیکیشن</button>':''}<span class="credit-pill">♢ ${faNum(currentCredits())} اعتبار پیام</span><button class="icon-btn" data-action="navigate" data-screen="support" title="پشتیبانی">؟</button><button class="user-chip" data-action="navigate" data-screen="profile"><span class="mini-avatar">${esc(state.profile?.name?.slice(0,1) || 'ف')}</span><span>${esc(state.profile?.name || 'حساب من')}</span></button></div></header>`;
  }
  const primaryNav = ['discover','likes','map','chats','profile'];
  function navButton(id, label, icon) { return `<button class="nav-item ${state.currentScreen===id?'active':''}" data-action="navigate" data-screen="${id}"><span>${icon}</span><b>${label}</b>${id==='likes'&&state.likedIds.length?`<i>${faNum(state.likedIds.length)}</i>`:''}${id==='chats'&&state.matchedIds.length?`<i>${faNum(state.matchedIds.length)}</i>`:''}</button>`; }
  function renderSidebar() {
    const items = [
      ['discover','کشف','✦'],['likes','لایک‌ها','♡'],['map','نقشه','⌖'],['chats','گفتگوها','▤'],['dates','دیت‌های من','◷'],
      ['subscription','اشتراک و اعتبار','♢'],['payments','تاریخچه پرداخت','▧'],['verification','تأیید پروفایل','✓'],['support','پشتیبانی','?'],['profile','پروفایل من','◉'],
    ];
    return `<aside class="sidebar"><div class="sidebar-label">فضای آشنایی</div>${items.map(([id,l,i])=>navButton(id,l,i)).join('')}<div class="sidebar-bottom"><div class="plan-mini"><small>پلن فعلی</small><b>${esc(currentPlan())}</b><button data-action="navigate" data-screen="subscription">ارتقای اشتراک ←</button></div><button class="nav-item" data-action="navigate" data-screen="privacy"><span>⌑</span><b>حریم خصوصی</b></button><button class="nav-item" data-action="navigate" data-screen="blocked"><span>⊘</span><b>مسدودشده‌ها</b></button></div></aside>`;
  }
  function renderBottomNav() { return `<nav class="bottom-nav">${primaryNav.map(id=>{const f=PRODUCT_FEATURES.find(x=>x.id===id);return navButton(id,f.label,f.icon)}).join('')}</nav>`; }
  function renderDiscover() {
    const list = filteredPeople();
    const candidate = list[0];
    return `<section class="page-head"><div><span class="eyebrow">آدم‌های تازه، ارتباط‌های تازه</span><h1>کشف ارتباط‌های جدید <span class="pink-dot">✦</span></h1><p class="muted">پیشنهادهایی بر اساس علایق و ترجیحات تو</p></div><button class="btn secondary" data-action="filters">☷ فیلترها</button></section><div class="discover-layout"><div class="discover-main">${candidate ? `<div class="discovery-card"><div class="discovery-photo" data-action="person" data-id="${candidate.id}"><img src="${esc(candidate.photo)}" alt="${esc(candidate.name)}"><div class="photo-shade"></div><div class="photo-top"><span class="online-label ${candidate.online?'online':''}">${candidate.online?'● آنلاین':'آخرین بازدید'}</span>${candidate.verified?'<span class="verified-tag">✓ تأییدشده</span>':''}</div><div class="photo-bottom"><h2>${esc(candidate.name)}، ${faNum(candidate.age)}</h2><p>${esc(candidate.job)} · ${esc(candidate.city)}</p><p class="discover-bio">${esc(candidate.bio)}</p><div class="chips">${candidate.interests.map(i=>`<span>${esc(i)}</span>`).join('')}</div></div></div><div class="discover-actions"><button class="round-action pass" data-action="pass" data-id="${candidate.id}" title="رد کردن">×</button><button class="round-action message" data-action="quick-chat" data-id="${candidate.id}" title="پیام">✉</button><button class="round-action like" data-action="like" data-id="${candidate.id}" title="لایک">♥</button></div><div class="swipe-meta"><span>سهمیه امروز: ${currentPlan()==='وی‌آی‌پی'?'نامحدود':faNum(Math.max(0,(currentPlan()==='رایگان'?10:50)-state.dailySwipes))+' لایک'}</span><button class="text-btn" data-action="navigate" data-screen="subscription">افزایش سهمیه</button></div></div>` : `<div class="empty-state"><div>♡</div><h3>فعلاً پیشنهاد تازه‌ای نداریم</h3><p>فیلترها را کمی بازتر کن یا بعداً دوباره سر بزن.</p><button class="btn primary" data-action="filters">تغییر فیلترها</button></div>`}</div><aside class="discover-aside"><div class="surface-card"><div class="card-heading"><h3>برای تو</h3><span class="soft-tag">شخصی‌سازی‌شده</span></div><div class="mini-stat"><b>${faNum(list.length)}</b><span>پروفایل سازگار</span></div><div class="mini-stat"><b>${faNum(state.likedIds.length)}</b><span>لایک ارسالی</span></div><div class="mini-stat"><b>${faNum(state.matchedIds.length)}</b><span>ارتباط دوطرفه</span></div><div class="divider"></div><p class="muted small">علاقه‌ها و هدف آشنایی‌ات را به‌روز کن تا پیشنهادهای دقیق‌تری ببینی.</p><button class="btn ghost wide" data-action="edit-profile">ویرایش پروفایل</button></div><div class="surface-card soft-gradient"><div class="sparkle">✧</div><h3>ارتباط بهتر با فست‌لاو پلاس</h3><p class="muted small">لایک‌های دریافتی را ببین و فیلترهای بیشتری داشته باش.</p><button class="btn primary wide" data-action="navigate" data-screen="subscription">مشاهده پلن‌ها</button></div></aside></div>`;
  }
  function renderLikes() {
    const tab = state.likesTab || 'received';
    const received = PEOPLE.filter(p=>p.likedYou&&!state.blockedIds.includes(p.id)&&!state.passedIds.includes(p.id));
    const sent = state.likedIds.map(person).filter(Boolean);
    const matches = state.matchedIds.map(person).filter(Boolean);
    const list = tab==='sent'?sent:tab==='matches'?matches:received;
    const listMarkup = tab==='received' && currentPlan()==='رایگان' ? received.map(p=>`<article class="person-card compact locked-like"><div class="person-cover"><img src="${esc(p.photo)}" style="filter:blur(16px)" alt=""><span class="locked-heart">♥</span></div><div class="person-info"><b>یک نفر به تو علاقه‌مند شده</b><p>برای مشاهده پروفایل و شروع آشنایی، اشتراکت را ارتقا بده.</p></div></article>`).join('') : list.map(p=>profileCard(p,true)).join('');
    return `<section class="page-head"><div><span class="eyebrow">تعامل‌های تو</span><h1>لایک‌ها و ارتباط‌ها</h1><p class="muted">ارتباط‌های یک‌طرفه را به آشنایی دوطرفه تبدیل کن.</p></div></section><div class="tabs"><button data-action="likes-tab" data-tab="received" class="${tab==='received'?'active':''}">دریافتی <b>${faNum(received.length)}</b></button><button data-action="likes-tab" data-tab="sent" class="${tab==='sent'?'active':''}">ارسالی <b>${faNum(sent.length)}</b></button><button data-action="likes-tab" data-tab="matches" class="${tab==='matches'?'active':''}">مچ‌ها <b>${faNum(matches.length)}</b></button></div>${tab==='received'&&currentPlan()==='رایگان'?`<div class="upgrade-strip"><div><b>علاقه‌مندها منتظر آشنایی با تو هستند</b><p>با اشتراک ویژه، هویت تمام لایک‌های دریافتی را ببین.</p></div><button class="btn primary" data-action="navigate" data-screen="subscription">مشاهده لایک‌ها</button></div>`:''}<div class="people-grid">${list.length?listMarkup:'<div class="empty-state"><div>♡</div><h3>هنوز موردی اینجا نیست</h3><p>با ادامه کشف، این بخش کامل‌تر می‌شود.</p><button class="btn secondary" data-action="navigate" data-screen="discover">رفتن به کشف</button></div>'}</div>`;
  }
  function renderMap() {
    const visible = PEOPLE.filter(p=>!state.blockedIds.includes(p.id)&&p.distanceKm<=50);
    const markers = visible.map(p=>`<button class="map-person" data-action="person" data-id="${p.id}"><img src="${esc(p.photo)}" alt=""><span><b>${esc(p.name)}</b><small>${faNum(p.distanceKm)} کیلومتر · ${esc(p.city)}</small></span></button>`).join('');
    const mapPins = visible.slice(0,18).map(p=>{const right=Math.max(4,Math.min(93,((p.longitude-51.20)/.40)*100));const top=Math.max(7,Math.min(88,100-((p.latitude-35.58)/.24)*100));return `<button class="map-pin" style="right:${right}%;top:${top}%" data-action="person" data-id="${p.id}" title="${esc(p.name)}"><img src="${esc(p.photo)}" alt="${esc(p.name)}"></button>`;}).join('');
    const myPin = state.showOnMap && state.userCoords ? `<div class="my-map-pin" style="right:${Math.max(4,Math.min(93,((state.userCoords.longitude-51.20)/.40)*100))}%;top:${Math.max(7,Math.min(88,100-((state.userCoords.latitude-35.58)/.24)*100))}%">من</div>` : '';
    const bbox = '51.20%2C35.58%2C51.60%2C35.82';
    return `<section class="page-head"><div><span class="eyebrow">آشنایی در نزدیکی تو</span><h1>نقشه ارتباط‌ها</h1><p class="muted">پروفایل‌های اطراف را ببین؛ نمایش موقعیت خودت کاملاً در اختیار توست.</p></div><label class="switch-row"><input type="checkbox" data-setting="showOnMap" ${state.showOnMap?'checked':''}><span class="switch"></span><span>نمایش من روی نقشه</span></label></section><div class="map-layout"><div class="map-frame"><iframe title="نقشه تهران" src="https://www.openstreetmap.org/export/embed.html?bbox=${bbox}&layer=mapnik" loading="lazy"></iframe><div class="map-markers">${mapPins}${myPin}</div><div class="map-overlay"><span class="map-legend"><i></i> کاربران نمونه نزدیک</span><button class="btn secondary small" data-action="locate">◎ موقعیت من</button></div></div><aside class="surface-card map-list"><div class="card-heading"><h3>افراد نزدیک</h3><span class="soft-tag">${faNum(visible.length)} نفر</span></div>${markers}</aside></div><p class="muted small map-note">موقعیت کاربران نمونه و تقریبی است. در نسخه عملیاتی فقط موقعیت تقریبی کاربرانِ رضایت‌داده از API مرکزی دریافت می‌شود. نقشه: © OpenStreetMap contributors.</p>`;
  }
  function renderChats() {
    const ids = [...new Set([...state.matchedIds,...state.paidChatIds])];
    const rows = ids.map(id=>person(id)).filter(Boolean).map(p=>{
      const msgs=getMessages(p.id), last=msgs[msgs.length-1];
      return `<button class="chat-row" data-action="open-chat" data-id="${p.id}"><img src="${esc(p.photo)}" alt=""><span class="chat-row-main"><b>${esc(p.name)} ${p.verified?'✓':''}</b><small>${esc(last?.text || 'گفتگو را شروع کن')}</small></span><span class="chat-row-meta">${last?timeFa(last.at):''}${state.matchedIds.includes(p.id)?'<i class="match-indicator">مچ</i>':'<i class="paid-indicator">مستقیم</i>'}</span></button>`;
    }).join('');
    return `<section class="page-head"><div><span class="eyebrow">ارتباط‌های دوطرفه</span><h1>گفتگوها</h1><p class="muted">گفتگو را ادامه بده یا برای یک دیدار برنامه‌ریزی کن.</p></div><button class="btn secondary" data-action="navigate" data-screen="dates">◷ دیت‌های من</button></section><div class="chat-layout"><div class="surface-card chat-list"><div class="searchbox">⌕ <input id="chat-search" placeholder="جستجوی گفتگوها"></div>${rows||'<div class="empty-state compact-empty"><div>▤</div><h3>هنوز گفتگویی نداری</h3><p>از بخش کشف، یک ارتباط تازه بساز.</p><button class="btn primary" data-action="navigate" data-screen="discover">شروع کشف</button></div>'}</div><div class="chat-welcome surface-card"><div class="welcome-icon">♡</div><h2>گفتگوهای معنادار از اینجا شروع می‌شوند</h2><p class="muted">یک گفتگو را انتخاب کن تا پیام‌هایت را ببینی.</p></div></div>`;
  }
  function renderChatThread(id) {
    const p=person(id); if(!p) return '';
    const msgs=getMessages(id).map(m=>`<div class="message ${m.from==='me'?'mine':'theirs'}">${m.type==='image'?`<img class="message-image" src="${esc(m.url)}" alt="تصویر ارسال‌شده">`:m.type==='voice'?`<div class="voice-message">${m.url?`<audio controls src="${esc(m.url)}"></audio>`:'▶ پیام صوتی'}</div>`:esc(m.text)}<small>${timeFa(m.at)}</small></div>`).join('');
    return `<div class="thread-head"><button class="icon-btn" data-action="close-chat">→</button><img src="${esc(p.photo)}"><div><b>${esc(p.name)}</b><small>${p.online?'● آنلاین':'آخرین بازدید'}</small></div><div class="thread-actions"><button class="icon-btn" data-action="person" data-id="${p.id}">ⓘ</button><button class="icon-btn" data-action="propose-date" data-id="${p.id}">◷ قرار</button><button class="icon-btn" data-action="unmatch" data-id="${p.id}" title="پایان ارتباط">⋯</button></div></div><div class="thread-messages" id="thread-messages">${msgs||'<div class="empty-state"><div>♡</div><h3>شروع یک گفتگوی تازه</h3><p>یک سلام ساده می‌تواند شروع خوبی باشد.</p></div>'}</div><form data-form="send-message" data-id="${p.id}" class="message-composer"><label class="attach-btn" title="ارسال تصویر">＋<input type="file" name="image" accept="image/*" hidden></label><input name="message" placeholder="پیامت را بنویس..." autocomplete="off"><button type="button" class="icon-btn voice-btn" data-action="voice-demo" title="پیام صوتی">◉</button><button class="btn primary send-btn">ارسال</button></form><div class="thread-bottom"><span>پیام‌های تو خصوصی هستند.</span><button class="text-btn" data-action="report" data-id="${p.id}">گزارش تخلف</button></div>`;
  }
  function renderProfile() {
    const p=state.profile||DEMO_PROFILE;
    const status=state.verification.status;
    return `<section class="page-head"><div><span class="eyebrow">فضای شخصی تو</span><h1>پروفایل من</h1><p class="muted">مدیریت اطلاعات، امنیت و تجربه فست‌لاو</p></div><button class="btn secondary" data-action="edit-profile">✎ ویرایش پروفایل</button></section><div class="profile-layout"><div class="profile-main"><div class="surface-card profile-hero"><div class="profile-photo"><img src="${esc(p.photos?.[p.primaryPhotoIndex||0]||p.photos?.[0]||'')}" alt="تصویر پروفایل"></div><div class="profile-summary"><span class="soft-tag">${esc(status)}</span><h2>${esc(p.name)} <small>${faNum(userAge())} ساله</small></h2><p class="muted">${esc(p.job||'') } · ${esc(p.city||'')}</p><p>${esc(p.about||'')}</p><div class="chips">${(p.interests||[]).map(i=>`<span>${esc(i)}</span>`).join('')}</div>${p.voiceIntro?`<div class="voice-intro"><small>معرفی صوتی</small><audio controls src="${esc(p.voiceIntro)}"></audio></div>`:''}</div><button class="icon-btn edit-icon" data-action="edit-profile">✎</button></div><button class="btn ghost wide" data-action="public-preview">مشاهده پیش‌نمایش پروفایل عمومی</button><div class="profile-stats"><div class="surface-card"><b>${faNum(state.likedIds.length)}</b><small>لایک ارسالی</small></div><div class="surface-card"><b>${faNum(state.matchedIds.length)}</b><small>مچ‌ها</small></div><div class="surface-card"><b>${faNum(state.dates.length)}</b><small>دیت برنامه‌ریزی‌شده</small></div></div><div class="surface-card referral-card"><div class="referral-icon">↗</div><div><b>دعوت دوستان با کد معرف</b><p class="muted small">کدت را برای دوستانت بفرست و آشنایی را گسترش بده.</p><strong dir="ltr">${esc(p.referralCode||'FAST-LOVE')}</strong></div><button class="btn ghost" data-action="copy-referral">کپی کد</button></div><div class="surface-card profile-actions"><h3>تنظیمات و برنامه‌ها</h3><div class="action-grid">${[['dates','◷','دیت‌های برنامه‌ریزی‌شده'],['subscription','♢','اشتراک و اعتبار'],['payments','▧','تاریخچه پرداخت'],['verification','✓','تأیید پروفایل'],['privacy','⌑','حریم خصوصی'],['blocked','⊘','کاربران مسدودشده'],['support','?','پشتیبانی و تیکت‌ها']].map(([id,icon,label])=>`<button data-action="navigate" data-screen="${id}"><span>${icon}</span><b>${label}</b><i>←</i></button>`).join('')}</div></div><button class="btn danger-outline wide" data-action="logout">خروج از حساب کاربری</button></div><aside class="profile-side"><div class="surface-card plan-card"><span class="eyebrow">اشتراک فعلی</span><h2>${esc(currentPlan())}</h2><p class="muted small">${state.subscription.expiresAt?`فعال تا ${dateFa(state.subscription.expiresAt)}`:'با ارتقای اشتراک از امکانات بیشتر استفاده کن.'}</p><button class="btn primary wide" data-action="navigate" data-screen="subscription">مدیریت اشتراک</button></div><div class="surface-card verify-card"><div class="verify-badge">✓</div><h3>پروفایل تأییدشده</h3><p class="muted small">با تأیید ویدئویی، اعتماد بیشتری در آشنایی‌ها ایجاد کن.</p><button class="btn ghost wide" data-action="navigate" data-screen="verification">${status==='تأییدشده'?'مشاهده وضعیت':'شروع تأیید پروفایل'}</button></div></aside></div>`;
  }
  function renderDates() {
    const rows=state.dates.map(d=>{const p=person(d.personId);return `<button class="date-row" data-action="date-detail" data-id="${esc(d.id)}"><span class="date-calendar"><b>${faNum((d.date||'').split('-')[2]||'')}</b><small>${esc((d.date||'').split('-')[1]||'')}</small></span><span class="date-main"><b>${esc(p?.name||'کاربر')}</b><small>${esc(d.place)} · ${esc(d.address||'')}</small><small>${faNum(d.date)} · ${faNum(d.time)}</small></span><span class="status-pill ${d.status==='تأییدشده'?'success':''}">${esc(d.status)}</span><i>←</i></button>`;}).join('');
    return `<section class="page-head"><div><span class="eyebrow">برنامه‌های مشترک</span><h1>دیت‌های برنامه‌ریزی‌شده</h1><p class="muted">جزئیات زمان و محل قرارها را مدیریت کن.</p></div></section><div class="surface-card dates-list">${rows||'<div class="empty-state"><div>◷</div><h3>هنوز قراری برنامه‌ریزی نشده</h3><p>از صفحه گفتگو می‌توانی پیشنهاد دیت بفرستی.</p></div>'}</div>`;
  }
  function renderSubscription() {
    const packages=state.packages.filter(p=>p.active);
    const subs=packages.filter(p=>p.type==='subscription'); const directs=packages.filter(p=>p.type==='direct');
    const cards=list=>list.map(p=>`<article class="package-card ${p.popular?'popular':''}">${p.popular?'<span class="popular-tag">محبوب‌ترین</span>':''}<div class="package-icon">${p.type==='direct'?'✉':'♢'}</div><h3>${esc(p.title)}</h3><div class="package-price">${money(p.price)}</div><small class="muted">${p.type==='direct'?`${faNum(p.messages)} پیام مستقیم`:`${faNum(p.days)} روز اعتبار`}</small><ul>${(p.features||[]).map(f=>`<li>✓ ${esc(f)}</li>`).join('')}</ul><button class="btn ${p.popular?'primary':'secondary'} wide" data-action="buy-package" data-id="${esc(p.id)}">انتخاب بسته</button></article>`).join('');
    return `<section class="page-head"><div><span class="eyebrow">امکانات بیشتر، ارتباط بهتر</span><h1>اشتراک و اعتبار</h1><p class="muted">بسته مناسب تجربه خودت را انتخاب کن.</p></div><div class="credits-summary"><b>${faNum(currentCredits())}</b><small>اعتبار پیام مستقیم</small></div></section><div class="package-section"><div class="section-heading"><div><h2>اشتراک ویژه</h2><p class="muted">برای کشف دقیق‌تر و مدیریت بهتر ارتباط‌ها</p></div><span class="soft-tag">پیشنهاد ما</span></div><div class="packages-grid">${cards(subs)||'<p class="muted">در حال حاضر بسته فعالی وجود ندارد.</p>'}</div></div><div class="package-section"><div class="section-heading"><div><h2>پیام مستقیم</h2><p class="muted">بدون نیاز به مچ، گفتگو را شروع کن.</p></div></div><div class="packages-grid">${cards(directs)||'<p class="muted">بسته فعالی وجود ندارد.</p>'}</div></div><div class="payment-hint">پرداخت‌ها در این نسخه نمایشی شبیه‌سازی می‌شوند و مبلغ واقعی دریافت نمی‌شود.</div>`;
  }
  function renderPayments() {
    return `<section class="page-head"><div><span class="eyebrow">سوابق مالی حساب</span><h1>تاریخچه پرداخت</h1><p class="muted">تراکنش‌ها و وضعیت پرداخت‌های خود را ببین.</p></div></section><div class="surface-card table-wrap"><table><thead><tr><th>شناسه</th><th>بسته</th><th>مبلغ</th><th>تاریخ</th><th>وضعیت</th></tr></thead><tbody>${state.payments.map(p=>`<tr><td dir="ltr">${esc(p.id)}</td><td>${esc(p.title)}</td><td>${money(p.amount)}</td><td>${dateFa(p.at)}</td><td><span class="status-pill success">${esc(p.status)}</span></td></tr>`).join('')||'<tr><td colspan="5">هنوز تراکنشی ثبت نشده است.</td></tr>'}</tbody></table></div>`;
  }
  function renderBlocked() {
    const list=state.blockedIds.map(person).filter(Boolean);
    return `<section class="page-head"><div><span class="eyebrow">کنترل ارتباط‌ها</span><h1>کاربران مسدودشده</h1><p class="muted">کاربران این فهرست نمی‌توانند با تو تعامل داشته باشند.</p></div></section><div class="surface-card blocked-list">${list.map(p=>`<div class="blocked-row"><img src="${esc(p.photo)}"><div><b>${esc(p.name)}</b><small>${esc(p.city)} · ${faNum(p.age)} ساله</small></div><button class="btn ghost" data-action="unblock" data-id="${p.id}">رفع مسدودیت</button></div>`).join('')||'<div class="empty-state"><div>⊘</div><h3>فهرست مسدودشده‌ها خالی است</h3></div>'}</div>`;
  }
  function renderPrivacy() {
    const setting=(key,title,desc)=>`<div class="setting-row"><div><b>${title}</b><small>${desc}</small></div><label class="switch-row"><input type="checkbox" data-setting="${key}" ${state[key]?'checked':''}><span class="switch"></span></label></div>`;
    return `<section class="page-head"><div><span class="eyebrow">اختیار اطلاعاتت با توست</span><h1>حریم خصوصی</h1><p class="muted">انتخاب کن دیگران چه اطلاعاتی از تو ببینند.</p></div></section><div class="surface-card settings-card">${setting('showOnlineStatus','نمایش وضعیت آنلاین','دیگران می‌توانند آنلاین بودنت را ببینند.')}${setting('showLastSeen','نمایش آخرین بازدید','زمان آخرین بازدید فقط در صورت فعال‌بودن نمایش داده می‌شود.')}${setting('showOnMap','نمایش پروفایل من روی نقشه','نمایش موقعیت تقریبی با اجازه مرورگر؛ هر زمان خواستی خاموشش کن.')}</div><div class="surface-card danger-zone"><h3>حذف حساب کاربری</h3><p class="muted small">درخواست حذف حساب، پروفایل و اطلاعات مرتبط را برای حذف به سرویس ارسال می‌کند. در نسخه دمو، این عملیات فقط روی همین مرورگر انجام می‌شود.</p><button class="btn danger-outline" data-action="delete-account">درخواست حذف حساب</button></div>`;
  }
  function renderVerification() {
    const v=state.verification;
    return `<section class="page-head"><div><span class="eyebrow">اعتماد بیشتر در آشنایی</span><h1>تأیید پروفایل</h1><p class="muted">برای دریافت نشان تأیید، یک ویدئوی کوتاه از خودت ارسال کن.</p></div></section><div class="verification-layout"><div class="surface-card verification-card"><div class="verify-illustration">✓</div><span class="status-pill ${v.status==='تأییدشده'?'success':''}">${esc(v.status)}</span><h2>${v.status==='تأییدشده'?'پروفایل تو تأیید شده است':'هویتت را تأیید کن'}</h2><p class="muted">ویدئو باید حداقل ۵ ثانیه باشد. دستورالعمل نمایش‌داده‌شده را در ویدئو اجرا کن تا بررسی انجام شود.</p>${v.reason?`<div class="notice warning">${esc(v.reason)}</div>`:''}<form data-form="verification" class="form-stack"><label>انتخاب ویدئو<input type="file" name="video" accept="video/*" required></label><small class="muted">ویدئوی ارسالی در این نسخه فقط به‌صورت محلی بررسی می‌شود.</small><button class="btn primary">ارسال برای بررسی</button></form></div><div class="surface-card verification-steps"><h3>مراحل تأیید</h3><ol><li>ویدئوی واضح با نور مناسب ضبط کن.</li><li>حداقل ۵ ثانیه جلوی دوربین باش.</li><li>ویدئو برای بررسی ارسال می‌شود.</li><li>پس از تأیید، نشان آبی کنار پروفایلت نمایش داده می‌شود.</li></ol></div></div>`;
  }
  function renderSupport() {
    if(activeTicketId) {
      const t=state.tickets.find(x=>x.id===activeTicketId); if(!t) {activeTicketId=null;}
      else return `<div class="page-head"><div><button class="text-btn" data-action="back-tickets">← بازگشت به تیکت‌ها</button><h1>${esc(t.subject)}</h1><p class="muted">شناسه ${esc(t.id)} · ${esc(t.category)}</p></div><span class="status-pill ${t.status==='بسته‌شده'?'':'success'}">${esc(t.status)}</span></div><div class="surface-card ticket-thread">${t.messages.map(m=>`<div class="ticket-message ${m.from==='me'?'mine':'theirs'}"><b>${m.from==='me'?'شما':'پشتیبانی'}</b><p>${esc(m.text)}</p>${m.attachment?`<small>پیوست: ${esc(m.attachment)}</small>`:''}<small>${dateFa(m.at)} ${timeFa(m.at)}</small></div>`).join('')}<form data-form="ticket-reply" class="ticket-reply"><textarea name="message" rows="2" placeholder="پاسخ خود را بنویس..." required></textarea><button class="btn primary">ارسال پاسخ</button></form>${t.status!=='بسته‌شده'?`<button class="text-btn" data-action="close-ticket" data-id="${esc(t.id)}">بستن تیکت</button>`:''}</div>`;
    }
    return `<section class="page-head"><div><span class="eyebrow">همراهت هستیم</span><h1>پشتیبانی و تیکت‌ها</h1><p class="muted">سؤالت را ثبت کن تا تیم پشتیبانی پیگیری کند.</p></div><button class="btn primary" data-action="new-ticket">＋ ثبت تیکت جدید</button></section><div class="surface-card ticket-list">${state.tickets.map(t=>`<button class="ticket-row" data-action="open-ticket" data-id="${esc(t.id)}"><span class="ticket-icon">?</span><span><b>${esc(t.subject)}</b><small>${esc(t.category)} · ${dateFa(t.createdAt)}</small></span><span class="status-pill ${t.status==='بسته‌شده'?'':'success'}">${esc(t.status)}</span><i>←</i></button>`).join('')||'<div class="empty-state"><h3>هنوز تیکتی ثبت نکرده‌ای</h3></div>'}</div>`;
  }

  function renderPublicPreview() {
    const p=state.profile||DEMO_PROFILE;
    const photos=p.photos||[];
    return `<section class="page-head"><div><button class="text-btn" data-action="back-profile">← بازگشت به پروفایل</button><h1>پیش‌نمایش پروفایل عمومی</h1><p class="muted">نمایی که کاربران دیگر از پروفایل تو می‌بینند.</p></div><button class="btn secondary" data-action="edit-profile">ویرایش</button></section><div class="public-preview surface-card"><div class="preview-gallery">${photos.length?photos.map((src,i)=>`<img src="${esc(src)}" alt="عکس ${faNum(i+1)}">`).join(''):'<div class="preview-placeholder">افزودن عکس به پروفایل</div>'}</div><div class="preview-content"><span class="status-pill">${esc(state.verification.status)}</span><h2>${esc(p.name)}، ${faNum(userAge())}</h2><p class="muted">${esc(p.job||'')} · ${esc(p.city||'')}</p><p>${esc(p.about||'')}</p><div class="chips">${(p.interests||[]).map(i=>`<span>${esc(i)}</span>`).join('')}</div>${p.voiceIntro?`<div class="voice-intro"><small>معرفی صوتی</small><audio controls src="${esc(p.voiceIntro)}"></audio></div>`:''}<div class="social-preview">${p.instagram?`<span>Instagram: ${esc(p.instagram)}</span>`:''}${p.spotify?`<span>Spotify: ${esc(p.spotify)}</span>`:''}</div></div></div>`;
  }

  function renderScreen() {
    if(activePersonId && modal==='person') return renderPersonDetail();
    if(state.currentScreen==='discover') return renderDiscover();
    if(state.currentScreen==='likes') return renderLikes();
    if(state.currentScreen==='map') return renderMap();
    if(state.currentScreen==='chats') return state.activeChatId ? renderChatThread(state.activeChatId) : renderChats();
    if(state.currentScreen==='profile') return renderProfile();
    if(state.currentScreen==='public-preview') return renderPublicPreview();
    if(state.currentScreen==='dates') return renderDates();
    if(state.currentScreen==='subscription') return renderSubscription();
    if(state.currentScreen==='payments') return renderPayments();
    if(state.currentScreen==='blocked') return renderBlocked();
    if(state.currentScreen==='privacy') return renderPrivacy();
    if(state.currentScreen==='verification') return renderVerification();
    if(state.currentScreen==='support') return renderSupport();
    return renderDiscover();
  }
  function renderPersonDetail() {
    const p=activePerson(); if(!p) return '';
    return `<div class="person-detail"><div class="detail-gallery"><img src="${esc(p.photo)}" alt="${esc(p.name)}"><button class="detail-back" data-action="close-person">→ بازگشت</button><div class="detail-gradient"><h1>${esc(p.name)}، ${faNum(p.age)}</h1><p>${esc(p.job)} · ${esc(p.city)} · ${faNum(p.distanceKm)} کیلومتر</p></div></div><div class="detail-body"><div class="detail-badges">${p.verified?'<span class="status-pill success">✓ تأییدشده</span>':''}<span class="status-pill">${p.online?'● آنلاین':'آفلاین'}</span><span class="status-pill">${esc(p.goal)}</span></div><h3>درباره من</h3><p>${esc(p.bio)}</p><h3>علایق</h3><div class="chips">${p.interests.map(x=>`<span>${esc(x)}</span>`).join('')}</div><div class="detail-actions"><button class="btn ghost" data-action="pass" data-id="${p.id}">رد کردن</button><button class="btn danger-outline" data-action="block" data-id="${p.id}">مسدودسازی</button><button class="btn primary" data-action="like" data-id="${p.id}">♥ لایک</button><button class="btn secondary" data-action="quick-chat" data-id="${p.id}">ارسال پیام</button></div><button class="text-btn" data-action="report" data-id="${p.id}">گزارش این پروفایل</button></div></div>`;
  }
  function renderModal() {
    if(!modal) return '';
    if(modal==='person') return '';
    if(modal==='filters') return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>فیلتر پیشنهادها</h2><button data-action="close-modal">×</button></div><form data-form="filters" class="form-stack"><div class="range-fields"><label>حداقل سن<input name="minAge" type="number" min="18" max="80" value="${state.filters.minAge}"></label><label>حداکثر سن<input name="maxAge" type="number" min="18" max="80" value="${state.filters.maxAge}"></label></div><label>حداکثر فاصله (کیلومتر)<input name="maxDistance" type="number" min="1" max="500" value="${state.filters.maxDistance}"></label><label>شهر<select name="city">${['همه شهرها','تهران','کرج','اصفهان','شیراز','تبریز','مشهد','رشت'].map(c=>`<option ${state.filters.city===c?'selected':''}>${c}</option>`).join('')}</select></label><label>هدف آشنایی<select name="goal">${['همه','رابطه جدی','دوستی ساده','ازدواج','قرار عادی'].map(c=>`<option ${state.filters.goal===c?'selected':''}>${c}</option>`).join('')}</select></label><label>شغل‌ها (با ویرگول جدا کن)<input name="jobs" value="${esc((state.filters.jobs||[]).join('، '))}" placeholder="مثلاً طراح محصول، برنامه‌نویس"></label><label>علایق (با ویرگول جدا کن)<input name="interests" value="${esc((state.filters.interests||[]).join('، '))}" placeholder="سفر، موسیقی، کتاب"></label><label class="checkbox-line"><input name="verifiedOnly" type="checkbox" ${state.filters.verifiedOnly?'checked':''}> فقط پروفایل‌های تأییدشده</label><button class="btn primary wide">اعمال فیلترها</button></form></section></div>`;
    if(modal==='date') return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>${revisionOfDateId?'پیشنهاد اصلاح دیت':'پیشنهاد دیت اول'}</h2><button data-action="close-modal">×</button></div><form data-form="create-date" data-id="${activePersonId}" data-revision="${revisionOfDateId||''}" class="form-stack"><label>نام مکان<input name="place" required placeholder="مثلاً کافه ویونا"></label><label>آدرس<input name="address" placeholder="آدرس یا توضیح محل ملاقات"></label><div class="range-fields"><label>تاریخ<input name="date" type="date" min="${new Date().toISOString().slice(0,10)}" required></label><label>ساعت<input name="time" type="time" required></label></div><label>عرض جغرافیایی (اختیاری)<input name="lat" type="number" step="any" placeholder="35.7804"></label><label>طول جغرافیایی (اختیاری)<input name="lng" type="number" step="any" placeholder="51.3762"></label><button type="button" class="btn ghost" data-action="use-location">ثبت موقعیت فعلی من</button><button class="btn primary wide">ارسال پیشنهاد</button></form></section></div>`;
    if(modal==='date-detail') {const d=state.dates.find(x=>x.id===activeDateId);const p=d&&person(d.personId);return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>جزئیات دیت</h2><button data-action="close-modal">×</button></div>${d?`<div class="date-detail"><b>${esc(p?.name||'کاربر')}</b><p>${esc(d.place)}</p><small>${esc(d.address||'')}</small><div class="divider"></div><p>تاریخ: ${faNum(d.date)}</p><p>ساعت: ${faNum(d.time)}</p><p>وضعیت: ${esc(d.status)}</p><p>پیشنهاددهنده: ${esc(d.proposer)}</p>${d.lat&&d.lng?`<p>مختصات: <span dir="ltr">${d.lat}, ${d.lng}</span></p><iframe class="date-map" title="موقعیت دیت" src="https://www.openstreetmap.org/export/embed.html?bbox=${d.lng-.01}%2C${d.lat-.008}%2C${d.lng+.01}%2C${d.lat+.008}&layer=mapnik&marker=${d.lat}%2C${d.lng}"></iframe><a target="_blank" rel="noreferrer" href="https://www.openstreetmap.org/?mlat=${d.lat}&mlon=${d.lng}#map=16/${d.lat}/${d.lng}">مشاهده موقعیت روی نقشه ↗</a>`:''}<div class="detail-actions">${d.status==='در انتظار'&&d.proposer!=='من'?`<button class="btn primary" data-action="accept-date" data-id="${esc(d.id)}">تأیید قرار</button><button class="btn danger-outline" data-action="decline-date" data-id="${esc(d.id)}">رد پیشنهاد</button>`:''}${d.status==='در انتظار'&&d.proposer==='من'?`<button class="btn danger-outline" data-action="cancel-date" data-id="${esc(d.id)}">لغو پیشنهاد</button>`:''}${d.status==='تأییدشده'?`<button class="btn secondary" data-action="revise-date" data-id="${esc(d.id)}">پیشنهاد اصلاح</button><button class="btn danger-outline" data-action="cancel-date" data-id="${esc(d.id)}">لغو دیت</button>`:''}<button class="btn ghost" data-action="close-modal">بستن</button></div></div>`:''}</section></div>`;}
    if(modal==='package-confirm') {const p=state.packages.find(x=>x.id===activePackageId);return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>تأیید خرید</h2><button data-action="close-modal">×</button></div>${p?`<div class="purchase-summary"><div class="package-icon">${p.type==='direct'?'✉':'♢'}</div><h3>${esc(p.title)}</h3><h2>${money(p.price)}</h2><p class="muted small">این پرداخت در محیط دمو شبیه‌سازی می‌شود.</p><button class="btn primary wide" data-action="confirm-purchase" data-id="${esc(p.id)}">تأیید خرید نمایشی</button></div>`:''}</section></div>`;}
    if(modal==='ticket-create') return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>ثبت تیکت جدید</h2><button data-action="close-modal">×</button></div><form data-form="create-ticket" class="form-stack"><label>موضوع تیکت<input name="subject" required placeholder="خلاصه درخواست"></label><label>دسته‌بندی<select name="category">${['حساب و ورود','پروفایل و احراز هویت','امنیت و گزارش تخلف','اشتراک و پرداخت','گفتگو و پیام‌ها','مشکل فنی','سایر موارد'].map(x=>`<option>${x}</option>`).join('')}</select></label><label>شرح درخواست<textarea name="message" rows="4" required placeholder="توضیحات کامل..."></textarea></label><label>پیوست (اختیاری، حداکثر ۱۰ مگابایت)<input name="attachment" type="file"></label><button class="btn primary wide">ثبت تیکت</button></form></section></div>`;
    if(modal==='report') return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>گزارش پروفایل</h2><button data-action="close-modal">×</button></div><form data-form="report" class="form-stack"><label>دلیل گزارش<select name="reason">${['محتوای نامناسب','رفتار توهین‌آمیز','حساب جعلی','درخواست پول یا کلاهبرداری','سایر موارد'].map(x=>`<option>${x}</option>`).join('')}</select></label><label>توضیحات<textarea name="detail" rows="3"></textarea></label><button class="btn primary wide">ارسال گزارش</button></form></section></div>`;
    if(modal==='rating') return `<div class="modal-backdrop"><section class="modal-card" data-stop><div class="modal-head"><h2>تجربه‌ات چطور بود؟</h2><button data-action="close-modal">×</button></div><p class="muted">با ثبت امتیاز به بهترشدن فست‌لاو کمک کن.</p><form data-form="rating" class="form-stack"><div class="rating-stars">${[1,2,3,4,5].map(i=>`<label><input type="radio" name="stars" value="${i}" ${i===5?'checked':''}><span>★</span></label>`).join('')}</div><textarea name="comment" rows="2" placeholder="نظر اختیاری تو..."></textarea><button class="btn primary wide">ثبت نظر</button></form></section></div>`;
    if(modal==='limit') return `<div class="modal-backdrop"><section class="modal-card" data-stop><div class="modal-head"><h2>سهمیه امروز تمام شد</h2><button data-action="close-modal">×</button></div><p class="muted">با ارتقای اشتراک، لایک‌های بیشتری در روز داشته باش.</p><button class="btn primary wide" data-action="go-subscription">مشاهده اشتراک‌ها</button></section></div>`;
    if(modal==='delete') return `<div class="modal-backdrop"><section class="modal-card" data-stop><div class="modal-head"><h2>حذف حساب کاربری</h2><button data-action="close-modal">×</button></div><p class="muted">آیا از خروج و پاک‌سازی داده‌های محلی این حساب مطمئنی؟ این عملیات در نسخه دمو قابل بازگشت نیست.</p><div class="detail-actions"><button class="btn danger-outline" data-action="confirm-delete">حذف حساب</button><button class="btn ghost" data-action="close-modal">انصراف</button></div></section></div>`;
    if(modal==='unmatch') return `<div class="modal-backdrop"><section class="modal-card" data-stop><div class="modal-head"><h2>پایان ارتباط</h2><button data-action="close-modal">×</button></div><p class="muted">با پایان ارتباط، گفتگو و دیت‌های مرتبط در این نسخه دمو از فهرست فعال خارج می‌شوند.</p><button class="btn danger-outline wide" data-action="confirm-unmatch" data-id="${activePersonId}">تأیید پایان ارتباط</button></section></div>`;
    return '';
  }
  let activeDateId = null, activePackageId = null, revisionOfDateId = null;
  function render() {
    if (!state.session) { root.innerHTML = `<div class="fastlove-app ${appVariant}">${renderLogin()}<div class="toast"></div></div>`; return; }
    if (!state.profile) { root.innerHTML = `<div class="fastlove-app ${appVariant}">${renderProfileSetup()}<div class="toast"></div></div>`; return; }
    const screen = renderScreen();
    root.innerHTML = `<div class="fastlove-app ${appVariant}" dir="rtl">${renderHeader()}<div class="app-body">${renderSidebar()}<main class="main-content">${screen}</main></div>${renderBottomNav()}${renderModal()}<div class="toast"></div><footer class="app-footer">FastLove · تجربه یکپارچه در اندروید، وب و PWA <span>نسخه کاربری دمو ۱.۰</span></footer></div>`;
    if(state.currentScreen==='chats'&&state.activeChatId) { const el=root.querySelector('#thread-messages'); if(el) el.scrollTop=el.scrollHeight; }
  }
  function startDemo() {
    state.session={phone:'09121234567',authenticatedAt:Date.now()};
    state.profile=state.profile||{...DEMO_PROFILE};
    state.currentScreen='discover';
    persist(); render();
  }
  function goChat(id) {
    id=Number(id);
    if(!canChat(id)) {
      if(currentCredits()>0) { modal='direct-confirm'; activePersonId=id; render(); }
      else { toast('برای شروع پیام مستقیم به اعتبار نیاز داری.'); navigate('subscription'); }
      return;
    }
    state.currentScreen='chats'; state.activeChatId=id; persist(); render();
  }
  function submitMessage(form) {
    const id=Number(form.dataset.id); const input=form.elements.message; const text=input.value.trim();
    if(!text&&!chatAttachment) return;
    const msgs=getMessages(id).slice();
    if(chatAttachment) msgs.push({id:uid('m'),from:'me',type:'image',url:chatAttachment,at:Date.now()});
    if(text) msgs.push({id:uid('m'),from:'me',type:'text',text,at:Date.now()});
    setMessages(id,msgs); chatAttachment=null;
    render();
    // Local deterministic demo reply; production replies arrive through the shared realtime API.
    setTimeout(()=>{
      const p=person(id); const current=getMessages(id).slice();
      const replies=['چه جالب! بیشتر برام تعریف می‌کنی؟ 😊','منم همین حس رو دارم. تو معمولاً اوقات فراغتت رو چطور می‌گذرونی؟','عالیه! شاید بتونیم یه روز درباره‌اش بیشتر صحبت کنیم.'];
      current.push({id:uid('m'),from:'them',type:'text',text:replies[current.length%replies.length],at:Date.now()});
      setMessages(id,current); if(state.currentScreen==='chats'&&Number(state.activeChatId)===id) render();
    },900);
  }

  root.addEventListener('click', async event => {
    const target=event.target.closest('[data-action]'); if(!target) return;
    if(target.classList.contains('modal-backdrop') && event.target!==target) return;
    const action=target.dataset.action, id=target.dataset.id;
    if(action==='navigate') navigate(target.dataset.screen);
    else if(action==='public-preview') navigate('public-preview');
    else if(action==='back-profile') navigate('profile');
    else if(action==='demo-login') startDemo();
    else if(action==='install-app' && installPrompt) {installPrompt.prompt();installPrompt.userChoice.finally(()=>{installPrompt=null;render();});}
    else if(action==='back-auth') {authStep=1;render();}
    else if(action==='toggle-menu') root.querySelector('.sidebar')?.classList.toggle('open');
    else if(action==='filters') {modal='filters';render();}
    else if(action==='close-modal'||action==='close-person') {modal=null;activePersonId=null;render();}
    else if(action==='person') openPerson(id);
    else if(action==='like') like(id);
    else if(action==='pass') pass(id);
    else if(action==='block') {state.blockedIds=[...new Set([...state.blockedIds,Number(id)])];state.likedIds=state.likedIds.filter(x=>x!==Number(id));state.matchedIds=state.matchedIds.filter(x=>x!==Number(id));activePersonId=null;modal=null;persist();render();toast('کاربر مسدود شد.');}
    else if(action==='quick-chat') {modal=null;activePersonId=null;goChat(id);}
    else if(action==='likes-tab') {state.likesTab=target.dataset.tab;persist();render();}
    else if(action==='open-chat') goChat(id);
    else if(action==='close-chat') {state.activeChatId=null;render();}
    else if(action==='propose-date') {if(!isMatched(id)){toast('برای پیشنهاد دیت اول باید ابتدا مچ شوید.');return;}activePersonId=Number(id);modal='date';render();}
    else if(action==='date-detail') {activeDateId=id;modal='date-detail';render();}
    else if(action==='accept-date') {const current=state.dates.find(d=>d.id===id);state.dates=state.dates.map(d=>d.id===id?{...d,status:'تأییدشده'}:(current?.revisesProposalId&&d.id===current.revisesProposalId?{...d,status:'لغوشده'}:d));persist();modal=null;render();toast('قرار تأیید شد.');}
    else if(action==='buy-package') {activePackageId=id;modal='package-confirm';render();}
    else if(action==='confirm-purchase') {
      const pack=state.packages.find(p=>p.id===id);if(!pack)return;
      state.payments=[{id:`FL-${Math.floor(10000+Math.random()*89999)}`,title:pack.title,amount:pack.price,status:'موفق',at:Date.now()},...state.payments];
      if(pack.type==='direct'){state.messageCredits+=pack.messages;}else{state.subscription={plan:pack.title.includes('وی‌آی‌پی')?'وی‌آی‌پی':'پرو',expiresAt:Date.now()+pack.days*86400000};}
      persist();modal=null;render();toast('خرید نمایشی با موفقیت ثبت شد.');
    }
    else if(action==='edit-profile') {modal='edit-profile';render();}
    else if(action==='primary-photo') {const index=Number(target.dataset.index);state.profile={...state.profile,primaryPhotoIndex:index};persist();render();}
    else if(action==='remove-photo') {const index=Number(target.dataset.index);state.profile={...state.profile,photos:(state.profile.photos||[]).filter((_,i)=>i!==index),primaryPhotoIndex:0};persist();render();}
    else if(action==='unblock') {state.blockedIds=state.blockedIds.filter(x=>x!==Number(id));persist();render();}
    else if(action==='logout') {state.session=null;state.activeChatId=null;persist();render();}
    else if(action==='delete-account') {modal='delete';render();}
    else if(action==='confirm-delete') {state=Repository.reset();modal=null;authStep=1;render();}
    else if(action==='new-ticket') {modal='ticket-create';render();}
    else if(action==='open-ticket') {activeTicketId=id;render();}
    else if(action==='back-tickets') {activeTicketId=null;render();}
    else if(action==='close-ticket') {state.tickets=state.tickets.map(t=>t.id===id?{...t,status:'بسته‌شده'}:t);persist();render();}
    else if(action==='report') {activePersonId=Number(id);modal='report';render();}
    else if(action==='copy-referral') {await navigator.clipboard?.writeText(state.profile?.referralCode||'FAST-LOVE').catch(()=>{});toast('کد معرف کپی شد.');}
    else if(action==='use-location') {if(!navigator.geolocation){toast('موقعیت‌یابی در این مرورگر در دسترس نیست.');return;}navigator.geolocation.getCurrentPosition(pos=>{const form=root.querySelector('[data-form="create-date"]');if(form){form.elements.lat.value=pos.coords.latitude.toFixed(6);form.elements.lng.value=pos.coords.longitude.toFixed(6);}toast('موقعیت فعلی ثبت شد.');},()=>toast('دسترسی به موقعیت مکانی داده نشد.'));}
    else if(action==='locate') {if(!navigator.geolocation){toast('موقعیت‌یابی در دسترس نیست.');return;}navigator.geolocation.getCurrentPosition(pos=>{const {latitude,longitude}=pos.coords;const frame=root.querySelector('.map-frame iframe');if(frame)frame.src=`https://www.openstreetmap.org/export/embed.html?bbox=${longitude-.03}%2C${latitude-.02}%2C${longitude+.03}%2C${latitude+.02}&layer=mapnik&marker=${latitude}%2C${longitude}`;toast('نقشه روی موقعیت تو تنظیم شد.');},()=>toast('برای موقعیت‌یابی، اجازه مرورگر لازم است.'));}
    else if(action==='go-subscription') navigate('subscription');
    else if(action==='unmatch') {activePersonId=Number(id);modal='unmatch';render();}
    else if(action==='confirm-unmatch') {const pid=Number(id);state.matchedIds=state.matchedIds.filter(x=>x!==pid);state.paidChatIds=state.paidChatIds.filter(x=>x!==pid);state.chats=state.chats.filter(c=>c.personId!==pid);state.dates=state.dates.filter(d=>d.personId!==pid);state.activeChatId=null;modal=null;persist();render();toast('ارتباط پایان یافت.');}
    else if(action==='voice-demo') {
      if(voiceRecorder && voiceRecorder.state==='recording'){voiceRecorder.stop();target.classList.remove('recording');return;}
      const chatId=Number(state.activeChatId);
      if(!navigator.mediaDevices?.getUserMedia || !window.MediaRecorder){toast('ضبط پیام صوتی در این مرورگر پشتیبانی نمی‌شود.');return;}
      navigator.mediaDevices.getUserMedia({audio:true}).then(stream=>{voiceChunks=[];voiceRecorder=new MediaRecorder(stream);voiceRecorder.ondataavailable=e=>{if(e.data.size)voiceChunks.push(e.data);};voiceRecorder.onstop=()=>{const blob=new Blob(voiceChunks,{type:voiceRecorder.mimeType||'audio/webm'});stream.getTracks().forEach(track=>track.stop());if(blob.size>1400*1024){toast('حجم پیام صوتی بیش از حد مجاز است.');return;}const reader=new FileReader();reader.onload=()=>{const messages=getMessages(chatId).slice();messages.push({id:uid('m'),from:'me',type:'voice',url:reader.result,at:Date.now()});setMessages(chatId,messages);if(state.currentScreen==='chats'&&Number(state.activeChatId)===chatId)render();toast('پیام صوتی ارسال شد.');};reader.readAsDataURL(blob);};voiceRecorder.start();target.classList.add('recording');toast('در حال ضبط... برای پایان دوباره دکمه را بزن.');}).catch(()=>toast('برای ضبط صدا، اجازه میکروفون را فعال کن.'));
    }
    else if(action==='export-data') {const blob=new Blob([JSON.stringify(state,null,2)],{type:'application/json'});const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='fastlove-user-data.json';a.click();URL.revokeObjectURL(a.href);}
  });

  root.addEventListener('submit', async event => {
    const form=event.target.closest('form[data-form]');if(!form)return;event.preventDefault();
    const type=form.dataset.form, fd=new FormData(form), value=name=>String(fd.get(name)||'').trim();
    if(type==='send-otp') {authPhone=value('phone').replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d)).replace(/[٠-٩]/g,d=>'٠١٢٣٤٥٦٧٨٩'.indexOf(d)).replace(/\D/g,'');if(authPhone.length<10){toast('شماره موبایل معتبر وارد کن.');return;}authStep=2;render();}
    else if(type==='verify-otp') {if(value('code')!=='123456'){toast('کد دمو نامعتبر است؛ ۱۲۳۴۵۶ را وارد کن.');return;}state.session={phone:authPhone,authenticatedAt:Date.now()};if(!state.profile)state.currentScreen='setup';persist();render();}
    else if(type==='setup-profile') {state.profile={...DEMO_PROFILE,name:value('name'),birthYear:Number(value('birthYear')),birthMonth:Number(value('birthMonth')),gender:value('gender'),lookingFor:value('lookingFor'),goal:value('goal'),city:value('city'),job:value('job'),about:value('about'),interests:value('interests').split(/[،,]/).map(x=>x.trim()).filter(Boolean),phone:authPhone,photos:selectedPhotoData.splice(0),referralCode:`FL-${Math.floor(100000+Math.random()*899999)}`};state.currentScreen='discover';persist();render();toast('پروفایل ساخته شد.');}
    else if(type==='filters') {state.filters={minAge:Number(value('minAge')),maxAge:Number(value('maxAge')),maxDistance:Number(value('maxDistance')),city:value('city'),goal:value('goal'),jobs:value('jobs').split(/[،,]/).map(x=>x.trim()).filter(Boolean),interests:value('interests').split(/[،,]/).map(x=>x.trim()).filter(Boolean),verifiedOnly:fd.has('verifiedOnly')};modal=null;persist();render();}
    else if(type==='send-message') submitMessage(form);
    else if(type==='create-date') {const d={id:uid('date'),personId:Number(form.dataset.id),place:value('place'),address:value('address'),date:value('date'),time:value('time'),status:'در انتظار',lat:value('lat')?Number(value('lat')):null,lng:value('lng')?Number(value('lng')):null,proposer:'من',isRevision:Boolean(form.dataset.revision),revisesProposalId:form.dataset.revision||null};state.dates.unshift(d);revisionOfDateId=null;modal=null;state.currentScreen='dates';persist();render();toast('پیشنهاد دیت ارسال شد.');}
    else if(type==='create-ticket') {const t={id:`T-${Math.floor(1000+Math.random()*8999)}`,subject:value('subject'),category:value('category'),status:'در انتظار پاسخ',createdAt:Date.now(),messages:[{from:'me',text:value('message'),at:Date.now(),attachment:selectedTicketAttachment?.name||''}]};selectedTicketAttachment=null;state.tickets.unshift(t);modal=null;activeTicketId=t.id;persist();render();toast('تیکت ثبت شد.');}
    else if(type==='ticket-reply') {const t=state.tickets.find(x=>x.id===activeTicketId);if(t){t.messages.push({from:'me',text:value('message'),at:Date.now()});t.status='در انتظار پاسخ';persist();render();}}
    else if(type==='report') {modal=null;persist();render();toast('گزارش برای بررسی ثبت شد.');}
    else if(type==='rating') {state.ratings=[...(state.ratings||[]),{stars:Number(value('stars')||5),comment:value('comment'),at:Date.now()}];modal=null;persist();render();toast('ممنون از ثبت نظرت!');}
    else if(type==='verification') {const file=fd.get('video');if(!file||!file.size){toast('یک ویدئو انتخاب کن.');return;}if(file.size>50*1024*1024){toast('حجم ویدئو باید کمتر از ۵۰ مگابایت باشد.');return;}const url=URL.createObjectURL(file);const video=document.createElement('video');video.preload='metadata';video.onloadedmetadata=()=>{URL.revokeObjectURL(url);if(video.duration<5){toast('مدت ویدئو باید حداقل ۵ ثانیه باشد.');return;}state.verification={status:'در انتظار بررسی',reason:'',videoName:file.name};persist();render();toast('ویدئو برای بررسی ثبت شد.');};video.onerror=()=>{URL.revokeObjectURL(url);toast('خواندن فایل ویدئو ممکن نشد.');};video.src=url;}
    else if(type==='edit-profile') {state.profile={...state.profile,name:value('name'),job:value('job'),city:value('city'),about:value('about'),goal:value('goal'),interests:value('interests').split(/[،,]/).map(x=>x.trim()).filter(Boolean),instagram:value('instagram'),spotify:value('spotify'),voiceIntro:selectedVoiceData||state.profile.voiceIntro,photos:selectedPhotoData.length?[...selectedPhotoData,...(state.profile.photos||[])].slice(0,5):state.profile.photos};selectedPhotoData=[];selectedVoiceData=null;modal=null;persist();render();toast('پروفایل به‌روزرسانی شد.');}
  });

  root.addEventListener('change', event => {
    const t=event.target;
    if(t.matches('[data-setting]')) {const key=t.dataset.setting;if(key==='showOnMap'&&t.checked&&!state.userCoords&&navigator.geolocation){navigator.geolocation.getCurrentPosition(pos=>{state.userCoords={latitude:pos.coords.latitude,longitude:pos.coords.longitude};state.showOnMap=true;persist();render();toast('موقعیت تقریبی ثبت شد.');},()=>{t.checked=false;toast('برای نمایش موقعیت، اجازه مرورگر لازم است.');});return;}state[key]=t.checked;persist();toast('تنظیمات ذخیره شد.');}
    if(t.matches('input[name="image"]')&&t.files?.[0]) {const file=t.files[0];if(file.size>800*1024){toast('حداکثر حجم عکس ۸۰۰ کیلوبایت است.');return;}const reader=new FileReader();reader.onload=()=>{chatAttachment=reader.result;toast('تصویر آماده ارسال است.');};reader.readAsDataURL(file);}
    if(t.matches('input[name="photos"]')&&t.files?.length) {selectedPhotoData=[];Array.from(t.files).slice(0,Math.max(0,5-(state.profile?.photos?.length||0))).forEach(file=>{if(file.size>500*1024){toast(`حجم ${file.name} بیشتر از ۵۰۰ کیلوبایت است.`);return;}const reader=new FileReader();reader.onload=()=>selectedPhotoData.push(reader.result);reader.readAsDataURL(file);});}
    if(t.matches('input[name="voiceIntro"]')&&t.files?.[0]) {const file=t.files[0];if(file.size>500*1024){toast('حجم معرفی صوتی باید کمتر از ۵۰۰ کیلوبایت باشد.');return;}const reader=new FileReader();reader.onload=()=>{selectedVoiceData=reader.result;toast('معرفی صوتی آماده ذخیره است.');};reader.readAsDataURL(file);}
    if(t.matches('input[name="attachment"]')&&t.files?.[0]) {const file=t.files[0];if(file.size>10*1024*1024){toast('حجم پیوست باید کمتر از ۱۰ مگابایت باشد.');t.value='';return;}selectedTicketAttachment={name:file.name,size:file.size,type:file.type};toast('پیوست انتخاب شد.');}
  });

  // Edit-profile and direct-message purchase dialogs are generated separately to keep the shared screen renderer compact.
  const originalRenderModal=renderModal;
  const baseRender=render;
  // Lightweight delegated modal extensions through the state value.
  root.addEventListener('click', event=>{
    const t=event.target.closest('[data-action]');if(!t)return;
    if(t.dataset.action==='edit-profile'){modal='edit-profile';render();}
    if(t.dataset.action==='quick-chat' && modal==='direct-confirm'){/* handled below */}
  });

  // Wrap modal markup for forms that are intentionally available from profile and gated direct chat.
  const renderModalExtended=()=>{
    if(modal==='edit-profile') {const p=state.profile||DEMO_PROFILE;return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card large-modal" data-stop><div class="modal-head"><h2>ویرایش پروفایل</h2><button data-action="close-modal">×</button></div><form data-form="edit-profile" class="form-grid"><label>نام نمایشی<input name="name" value="${esc(p.name)}" required></label><label>شغل<input name="job" value="${esc(p.job||'')}"></label><label>شهر<input name="city" value="${esc(p.city||'')}"></label><label>هدف آشنایی<select name="goal">${['رابطه جدی','دوستی ساده','ازدواج','قرار عادی'].map(x=>`<option ${p.goal===x?'selected':''}>${x}</option>`).join('')}</select></label><label class="full">درباره من<textarea name="about" rows="3">${esc(p.about||'')}</textarea></label><label class="full">علایق<input name="interests" value="${esc((p.interests||[]).join('، '))}"></label><label>Instagram<input name="instagram" value="${esc(p.instagram||'')}"></label><label>Spotify<input name="spotify" value="${esc(p.spotify||'')}"></label><label>معرفی صوتی<input name="voiceIntro" type="file" accept="audio/*"></label><label>افزودن عکس<input name="photos" type="file" accept="image/*" multiple></label><div class="photo-edit-grid full">${(p.photos||[]).map((src,i)=>`<div><img src="${esc(src)}"><button type="button" data-action="primary-photo" data-index="${i}">${p.primaryPhotoIndex===i?'عکس اصلی':'انتخاب اصلی'}</button><button type="button" class="remove-photo" data-action="remove-photo" data-index="${i}">حذف</button></div>`).join('')}</div><button class="btn primary wide full">ذخیره تغییرات</button></form></section></div>`;}
    if(modal==='direct-confirm') {const p=activePerson();return `<div class="modal-backdrop" data-action="close-modal"><section class="modal-card" data-stop><div class="modal-head"><h2>پیام مستقیم</h2><button data-action="close-modal">×</button></div><p>برای شروع گفتگو با ${esc(p?.name||'این کاربر')} یک اعتبار پیام مصرف می‌شود.</p><p class="muted">اعتبار باقی‌مانده: ${faNum(currentCredits())}</p><button class="btn primary wide" data-action="confirm-direct" data-id="${p?.id}">مصرف اعتبار و شروع گفتگو</button></section></div>`;}
    return originalRenderModal();
  };
  // Render uses this extender after the first definition.
  const renderOriginal=render;
  render = function patchedRender(){
    if(!state.session){root.innerHTML=`<div class="fastlove-app ${appVariant}">${renderLogin()}<div class="toast"></div></div>`;return;}
    if(!state.profile){root.innerHTML=`<div class="fastlove-app ${appVariant}">${renderProfileSetup()}<div class="toast"></div></div>`;return;}
    const screen=renderScreen();
    root.innerHTML=`<div class="fastlove-app ${appVariant}" dir="rtl">${renderHeader()}<div class="app-body">${renderSidebar()}<main class="main-content">${screen}</main></div>${renderBottomNav()}${renderModalExtended()}<div class="toast"></div><footer class="app-footer">FastLove · تجربه یکپارچه در اندروید، وب و PWA <span>نسخه کاربری دمو ۱.۰</span></footer></div>`;
    if(state.currentScreen==='chats'&&state.activeChatId){const el=root.querySelector('#thread-messages');if(el)el.scrollTop=el.scrollHeight;}
  };
  root.addEventListener('click',event=>{
    const t=event.target.closest('[data-action]');if(!t)return;
    if(t.classList.contains('modal-backdrop') && event.target!==t) return;
    if(t.dataset.action==='confirm-direct') {const id=Number(t.dataset.id);if(state.messageCredits<1){modal=null;navigate('subscription');return;}state.messageCredits-=1;state.paidChatIds=[...new Set([...state.paidChatIds,id])];modal=null;goChat(id);}
  });

  render();
  if(appVariant==='pwa') {
    window.addEventListener('beforeinstallprompt', event=>{event.preventDefault();installPrompt=event;render();});
    window.addEventListener('appinstalled',()=>{installPrompt=null;toast('FastLove روی دستگاه نصب شد.');});
  }
  if(appVariant==='pwa' && 'serviceWorker' in navigator) navigator.serviceWorker.register('/sw.js').catch(()=>{});
  if(api.remoteEnabled) api.health().catch(()=>toast('ارتباط با سرویس مرکزی برقرار نشد؛ حالت آفلاین دمو فعال است.'));
  return { getState:()=>structuredClone(state), reset:()=>{state=Repository.reset();render();}, destroy:()=>{root.innerHTML='';} };
}
