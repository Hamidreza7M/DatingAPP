export const PRODUCT_FEATURES = [
  { id: 'discover', label: 'کشف', icon: '✦', description: 'دیدن پیشنهادهای سازگار و فیلتر پیشرفته' },
  { id: 'likes', label: 'لایک‌ها', icon: '♡', description: 'لایک‌های ارسالی، دریافتی و مچ‌ها' },
  { id: 'map', label: 'نقشه', icon: '⌖', description: 'دیدن کاربران اطراف با کنترل حریم خصوصی' },
  { id: 'chats', label: 'چت', icon: '▤', description: 'گفتگو، عکس، پیام صوتی و پیشنهاد دیت' },
  { id: 'profile', label: 'پروفایل من', icon: '◉', description: 'مدیریت پروفایل، اشتراک و تنظیمات حساب' },
  { id: 'dates', label: 'دیت‌های برنامه‌ریزی‌شده', icon: '◷', description: 'مشاهده و مدیریت قرارهای آینده' },
  { id: 'subscription', label: 'اشتراک و اعتبار', icon: '♢', description: 'خرید پلن ویژه و اعتبار پیام مستقیم' },
  { id: 'payments', label: 'تاریخچه پرداخت', icon: '▧', description: 'مشاهده تراکنش‌ها و رسیدها' },
  { id: 'blocked', label: 'کاربران مسدودشده', icon: '⊘', description: 'مدیریت فهرست مسدودشده‌ها' },
  { id: 'privacy', label: 'حریم خصوصی', icon: '⌑', description: 'کنترل وضعیت آنلاین، آخرین بازدید و نقشه' },
  { id: 'verification', label: 'تأیید پروفایل', icon: '✓', description: 'ارسال ویدئوی احراز هویت' },
  { id: 'support', label: 'پشتیبانی و تیکت‌ها', icon: '?', description: 'ثبت درخواست و پیگیری پاسخ پشتیبانی' },
];

const images = [
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1529139574466-a303027c1d8b?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1512316609839-ce289d3eba0a?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1521119989659-a83eee488004?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=900&auto=format&fit=crop&q=85',
  'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=900&auto=format&fit=crop&q=85',
];

const names = ['سارا','آرمان','نیلوفر','پارسا','مهسا','امیرعلی','ترانه','سامان','هلیا','کیان','آوا','رادین','الناز','بردیا','مریم','اشکان','یگانه','شایان','نازنین','سپهر','غزل','مانی','بهار','آریا'];
const cities = ['تهران','کرج','اصفهان','شیراز','تبریز','مشهد','رشت','تهران','تهران','قم','کرج','تهران'];
const jobs = ['طراح محصول','برنامه‌نویس','معمار','پزشک','مدیر مارکتینگ','دانشجوی هنر','عکاس','تحلیلگر داده','روان‌شناس','کارآفرین','مدرس زبان','وکیل'];
const interests = [['سفر','عکاسی','کافه'],['موسیقی','کوهنوردی','کتاب'],['هنر','سینما','یوگا'],['تکنولوژی','ورزش','بازی'],['طبیعت','غذا','سفر'],['کتاب','موسیقی','تنیس']];

export const PEOPLE = names.map((name, i) => ({
  id: i + 1,
  name,
  age: 23 + ((i * 3) % 12),
  gender: i % 2 ? 'مرد' : 'زن',
  job: jobs[i % jobs.length],
  city: cities[i % cities.length],
  distanceKm: 1 + ((i * 7) % 36),
  online: i % 4 !== 1,
  verified: i % 3 === 0,
  likedYou: [1, 3, 4, 7, 10, 12, 16, 19, 22].includes(i + 1),
  goal: ['دوستی ساده','رابطه جدی','ازدواج','قرار عادی'][i % 4],
  bio: [
    'عاشق تجربه‌های تازه، گفت‌وگوهای عمیق و کشف کافه‌های جدیدم.',
    'تعادل بین کار، سفر و لحظه‌های ساده زندگی برام مهمه.',
    'به هنر، طبیعت و آدم‌های خوش‌انرژی علاقه دارم.',
    'دنبال آشنایی واقعی و ساختن خاطره‌های خوب هستم.'
  ][i % 4],
  interests: interests[i % interests.length],
  photo: images[i % images.length],
  photos: [images[i % images.length], images[(i + 3) % images.length]],
  latitude: 35.69 + (((i * 17) % 35) - 17) / 100,
  longitude: 51.39 + (((i * 13) % 42) - 21) / 100,
}));

export const DEFAULT_PACKAGES = [
  { id: 'vip-30', type: 'subscription', title: 'وی‌آی‌پی یک‌ماهه', days: 30, messages: 0, price: 249000, active: true, popular: true, features: ['مشاهده تمام لایک‌ها','۵۰ لایک روزانه','فیلترهای پیشرفته','نشان ویژه'] },
  { id: 'pro-30', type: 'subscription', title: 'پرو یک‌ماهه', days: 30, messages: 0, price: 149000, active: true, popular: false, features: ['۵۰ لایک روزانه','فیلترهای پیشرفته','تجربه بدون محدودیت پایه'] },
  { id: 'vip-90', type: 'subscription', title: 'وی‌آی‌پی سه‌ماهه', days: 90, messages: 0, price: 599000, active: true, popular: false, features: ['تمام امکانات وی‌آی‌پی','صرفه‌جویی در هزینه'] },
  { id: 'msg-5', type: 'direct', title: '۵ پیام مستقیم', days: 0, messages: 5, price: 59000, active: true, popular: false, features: ['شروع گفتگو بدون مچ'] },
  { id: 'msg-15', type: 'direct', title: '۱۵ پیام مستقیم', days: 0, messages: 15, price: 139000, active: true, popular: false, features: ['شروع گفتگو بدون مچ','اعتبار بیشتر'] },
];

const demoChats = [
  { personId: 1, messages: [
    { id: 'm1', from: 'them', type: 'text', text: 'سلام! از دیدن پروفایلت خوشحال شدم 🌸', at: Date.now() - 7200000 },
    { id: 'm2', from: 'me', type: 'text', text: 'سلام، منم همینطور. روزت چطور بود؟', at: Date.now() - 6800000 },
    { id: 'm3', from: 'them', type: 'text', text: 'عالی، امروز یه کافه جدید کشف کردم. تو اهل کافه‌گردی هستی؟', at: Date.now() - 6200000 },
  ]},
  { personId: 3, messages: [
    { id: 'm4', from: 'them', type: 'text', text: 'سلام، دیدم به عکاسی علاقه داری. بیشتر چه سبکی؟', at: Date.now() - 3600000 },
    { id: 'm5', from: 'me', type: 'text', text: 'بیشتر عکاسی خیابونی و طبیعت رو دوست دارم.', at: Date.now() - 3100000 },
  ]},
  { personId: 5, messages: [{ id: 'm6', from: 'them', type: 'text', text: 'سلام، خوشحالم که مچ شدیم 😊', at: Date.now() - 1800000 }]},
];

export function createInitialState() {
  return {
    version: 1,
    session: null,
    profile: null,
    likedIds: [2, 6, 9],
    matchedIds: [1, 3, 5],
    blockedIds: [12],
    passedIds: [],
    paidChatIds: [5],
    dailySwipes: 0,
    swipeDay: new Date().toISOString().slice(0, 10),
    subscription: { plan: 'رایگان', expiresAt: null },
    messageCredits: 3,
    showOnMap: false,
    showOnlineStatus: true,
    showLastSeen: true,
    chats: demoChats,
    dates: [
      { id: 'date-1', personId: 1, place: 'کافه ویونا، سعادت‌آباد', address: 'تهران، سعادت‌آباد، میدان کاج', date: '2026-11-05', time: '18:30', status: 'تأییدشده', lat: 35.7804, lng: 51.3762, proposer: 'من' },
      { id: 'date-2', personId: 3, place: 'باغ کتاب تهران', address: 'تهران، بزرگراه حقانی، باغ کتاب', date: '2026-11-08', time: '16:00', status: 'در انتظار', lat: 35.7525, lng: 51.4246, proposer: 'طرف مقابل' },
    ],
    payments: [{ id: 'FL-20418', title: '۵ پیام مستقیم', amount: 59000, status: 'موفق', at: Date.now() - 86400000 * 4 }],
    packages: DEFAULT_PACKAGES,
    tickets: [{ id: 'T-1024', subject: 'سؤال درباره اعتبار پیام', category: 'اشتراک و پرداخت', status: 'پاسخ داده‌شده', createdAt: Date.now() - 86400000, messages: [{ from: 'me', text: 'اعتبار پیام مستقیم چه زمانی فعال می‌شود؟', at: Date.now() - 86400000 }, { from: 'support', text: 'اعتبار بلافاصله پس از تأیید پرداخت به حساب شما اضافه می‌شود.', at: Date.now() - 80000000 }] }],
    verification: { status: 'تأییدنشده', reason: '', videoName: '' },
    ratings: [],
    filters: { minAge: 18, maxAge: 45, maxDistance: 50, city: 'همه شهرها', goal: 'همه', jobs: [], interests: [], verifiedOnly: false },
    currentScreen: 'discover',
    tutorialSeen: true,
  };
}

export const DEMO_PROFILE = {
  name: 'آرمان رضایی',
  birthYear: 1375,
  birthMonth: 7,
  gender: 'مرد',
  lookingFor: 'زن',
  goal: 'رابطه جدی',
  city: 'تهران',
  job: 'طراح محصول',
  about: 'عاشق سفر، طراحی و کشف کافه‌های جدید. دنبال یک ارتباط واقعی و پر از گفت‌وگوهای خوبم.',
  interests: ['سفر','عکاسی','کافه','موسیقی','طبیعت'],
  photos: [images[1], images[5]],
  primaryPhotoIndex: 0,
  phone: '09121234567',
  instagram: '@arman.design',
  spotify: 'arman-music',
  referralCode: 'FAST-ARMAN',
};
