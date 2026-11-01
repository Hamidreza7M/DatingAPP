# FastLove — Monorepo

این مخزن برای توسعه چندنسخه‌ای FastLove آماده شده است. اپلیکیشن‌ها پوشه‌های مستقل دارند و چرخه Build، تست و انتشارشان جداست.

## ساختار

```text
FastLove/
├── apps/
│   ├── android/       # اپ Android فعلی (Kotlin + Jetpack Compose)
│   └── admin-web/     # پنل وب ادمین (Node.js + HTML/CSS/JS)
├── contracts/
│   └── openapi/       # قرارداد API برای اتصال کلاینت‌ها به بک‌اند
├── docs/              # مستندات معماری و توسعه
├── .github/workflows/ # گردش‌کارهای مستقل CI برای Android و Admin Web
└── package.json       # فرمان‌های سطح Monorepo
```

## اجرای پنل مدیریت

Node.js 20 یا بالاتر لازم است. از ریشه مخزن:

```bash
npm run admin:dev
```

سپس `http://localhost:4177` را باز کنید.

حساب دمو در محیط توسعه:

- Username: `admin`
- Password: `FastLove@2026`

این حساب فقط برای توسعه محلی است. برای Production متغیرهای امن را طبق `apps/admin-web/.env.example` به محیط سرور تزریق کنید.

### Build مستقل پنل

```bash
npm run admin:build
```

خروجی کلاینت در `apps/admin-web/dist/` است. API پنل نیز جداگانه با `npm run admin:start` اجرا می‌شود. جزئیات استقرار در `apps/admin-web/README.md` آمده است.

## ساخت Android

پروژه اندروید اکنون در `apps/android/` قرار دارد. برای Build به Java 17، Android SDK و Gradle 8.9 نیاز است:

```bash
npm run android:build
```

گردش‌کار GitHub Actions اندروید فقط با تغییرات `apps/android/**` اجرا می‌شود؛ پنل مدیریت Workflow مجزای خود را دارد.

## اصل معماری

- چهار کلاینت Android، iOS، Web User و Admin Web نباید مستقیماً به دیتابیس وصل شوند.
- همه کلاینت‌ها باید از API مرکزی و قراردادهای نسخه‌بندی‌شده استفاده کنند.
- `apps/admin-web/server.mjs` در این بسته API دمو و مستقل پنل است؛ جایگزین `services/api` مرکزی نیست.
- منطق تجاری، مجوزها، قیمت قطعی پرداخت و وضعیت حساب باید در بک‌اند مرکزی باشد.
- UI وب و UI موبایل می‌توانند مستقل باشند؛ مدل داده و قرارداد API باید مشترک باشد.

## وضعیت فعلی اتصال داده

Android فعلی نسخه دمو و محلی است و داده‌ها در SharedPreferences و فایل‌های گوشی ذخیره می‌شوند. Admin Web نیز در این بسته با دیتاست نمونه سروری (JSON) و API دمو کار می‌کند. این دو هنوز با یکدیگر همگام نیستند. قرارداد پیشنهادی API در `contracts/openapi/admin-api.yaml` قرار دارد. برای عملیاتی شدن محصول، مرحله بعد ساخت بک‌اند مرکزی با PostgreSQL، مهاجرت داده‌ها و اتصال Android/iOS/Web User به همان API است.

## استقرار مستقل Admin Web

پنل مدیریت یک سرویس مستقل است و نیاز به Build یا انتشار Android/iOS ندارد. می‌توان آن را از مسیر `apps/admin-web` به‌صورت جداگانه Deploy کرد؛ Dockerfile و workflow مخصوص پنل در همان اپ قرار دارد. برای جزئیات: `apps/admin-web/README.md`.
