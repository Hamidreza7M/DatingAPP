# FastLove — Monorepo v1.1.0

نسخه چندکلاینتی FastLove شامل اپ اندروید، پنل ادمین، وب واکنش‌گرا و PWA است. نسخه‌های Responsive Web و PWA از یک هسته محصول مشترک استفاده می‌کنند تا قابلیت‌ها و رفتارهای محصول بین دو کلاینت وب یکسان بماند.

## ساختار مخزن

```text
FastLove/
├── apps/
│   ├── android/       # Android / Kotlin + Jetpack Compose
│   ├── web-user/      # وب کاربران / Responsive
│   ├── pwa/           # وب‌اپ قابل نصب / Offline shell
│   └── admin-web/     # پنل مدیریت مستقل
├── packages/
│   └── user-core/     # هسته مشترک کاربران: UI، مدل، state، repository و API client
├── services/api/      # مرز بک‌اند مرکزی (پیاده‌سازی Production هنوز لازم است)
├── contracts/openapi/ # قراردادهای نسخه‌بندی‌شده API ادمین و کاربران
├── scripts/           # اسکریپت‌های توسعه و Build
└── docs/              # مستندات معماری و هم‌ترازی قابلیت‌ها
```

## اجرای نسخه وب کاربران

Node.js نسخه ۲۰ یا بالاتر لازم است.

```bash
npm run web:dev
```

آدرس: `http://localhost:4178`

## اجرای PWA

```bash
npm run pwa:dev
```

آدرس: `http://localhost:4179`

برای نصب PWA در Chrome/Edge از دکمه «نصب اپلیکیشن» یا گزینه Install مرورگر استفاده کن. در iOS Safari از Share → Add to Home Screen استفاده می‌شود. سرویس‌ورکر پوسته برنامه را برای بازشدن آفلاین ذخیره می‌کند؛ کاشی‌های نقشه و محتوای Remote ممکن است آفلاین در دسترس نباشند.

## ورود دمو

در صفحه ورود از «ورود به حساب نمایشی» استفاده کن، یا شماره آزمایشی وارد کن و کد `123456` را تأیید کن. داده‌های دمو روی همان مرورگر ذخیره می‌شوند.

## Build و تست

```bash
npm run user:test
npm run user:build
npm run web:build
npm run pwa:build
npm run web:preview
npm run pwa:preview
```

Build وب در `apps/web-user/dist/` و Build مربوط به PWA در `apps/pwa/dist/` ساخته می‌شود. هرکدام مستقل Deploy می‌شوند.

## فرمان‌های محصولات فعلی

```bash
npm run admin:dev
npm run admin:build
npm run admin:test
npm run android:build
```

## همسانی قابلیت‌ها

هسته مشترک Web و PWA تمام جریان‌های محصول کاربر را پوشش می‌دهد: احراز هویت و ساخت پروفایل، عکس‌ها و معرفی صوتی، کشف و فیلتر، لایک و مچ، نقشه و تنظیمات نمایش موقعیت، چت متنی/تصویری/صوتی، پیشنهاد و جزئیات دیت، اشتراک و اعتبار پیام مستقیم، تاریخچه پرداخت، احراز هویت ویدئویی، حریم خصوصی، کاربران مسدودشده، پشتیبانی و حذف حساب. جزئیات در `docs/MONOREPO-ARCHITECTURE.md` درج شده است.

## وضعیت بک‌اند و همگام‌سازی واقعی

اپ Android فعلی و پنل ادمین همچنان در حالت دمو/محلی هستند و `services/api` هنوز بک‌اند عملیاتی نیست. بنابراین Web و PWA در این بسته از repository محلی مرورگر استفاده می‌کنند و همگام‌سازی واقعی داده‌ها بین دستگاه‌ها/Android/Web/PWA هنوز برقرار نیست. قرارداد مشترک API کاربران در `contracts/openapi/user-api.yaml` اضافه شده و `packages/user-core/src/api-client.js` آماده هدف‌گیری API مرکزی است؛ برای Production باید سرویس مرکزی، PostgreSQL، احراز هویت واقعی، realtime chat، ذخیره‌سازی خصوصی رسانه و درگاه پرداخت پیاده‌سازی و اپ اندروید نیز به همان API متصل شود.

**مهم:** قیمت خرید، OTP، پیام‌ها، احراز ویدئویی، تیکت‌ها و وضعیت‌ها در حالت فعلی برای UX دمو هستند؛ برای استفاده عمومی باید منطق حساس سمت سرور باشد و localStorage منبع داده نهایی تلقی نشود.
