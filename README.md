# FastLove — Monorepo

این مخزن برای توسعه چندنسخه‌ای FastLove آماده شده است. اپلیکیشن‌ها پوشه‌های مستقل دارند و چرخه Build، تست و انتشارشان جداست.

## تغییرات نسخه 1.0.1 پنل ادمین

- رفع باقی‌ماندن فرم ورود پس از ورود موفق با رعایت صحیح ویژگی `hidden` در CSS.
- اصلاح نمایش صفحه‌های منو و داده‌های نمونه هر بخش.
- افزودن نمایش وضعیت آنلاین/آفلاین و فیلتر مستقل کاربران آنلاین و آفلاین.
- API فیلتر آنلاین با پارامتر `online=online|offline` و تست خودکار.

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

در ویندوز ساده‌ترین روش اجرای فایل `START-ADMIN.bat` از ریشه پروژه است. این فایل نسخه Node.js را بررسی می‌کند و سرور پنل (شامل API ورود) را اجرا می‌کند.

یا از ریشه مخزن دستور زیر را اجرا کنید:

```bash
npm run admin:dev
```

سپس `http://localhost:4177` را باز کنید. پنجره سرور باید هنگام استفاده باز بماند. اجرای `index.html`، پوشه `dist` با Live Server یا `npx serve` به‌تنهایی کافی نیست؛ آن ابزارها API ورود را اجرا نمی‌کنند و باعث خطای 404 می‌شوند.

حساب دمو در محیط توسعه:

- Username: `admin`
- Password: `123456`

این حساب فقط برای توسعه محلی است. سرور JSON دمو عمداً در Production اجرا نمی‌شود؛ برای انتشار عمومی باید بک‌اند واقعی مطابق `SECURITY-REQUIREMENTS.md` پیاده‌سازی شود.

### Build مستقل پنل

```bash
npm run admin:build
```

خروجی کلاینت در `apps/admin-web/dist/` است. API پنل نیز جداگانه با `npm run admin:start` اجرا می‌شود. جزئیات استقرار در `apps/admin-web/README.md` آمده است.

## ساخت، تست و گزارش Android

پروژه اندروید اکنون در `apps/android/` قرار دارد. برای Build و تست به Java 17، Android SDK (API 36)، Android Gradle Plugin 8.9.1 و Gradle 8.11.1 نیاز است:

```bash
npm run android:build       # ساخت APK دیباگ
npm run android:test        # تست‌های JVM
npm run android:verify      # Unit Test + Lint + Coverage + APK
npm run android:device-test # تست UI روی Emulator متصل
```

Workflow اندروید در `.github/workflows/build-apk.yml` روی Pull Request و Push به شاخه‌های اصلی اجرا می‌شود. گزارش‌های JUnit، JaCoCo، Lint و Instrumentation به‌صورت Artifact در هر اجرای موفق/ناموفق ذخیره می‌شوند؛ خلاصه نتایج نیز در Summary همان Workflow قرار می‌گیرد. APK فقط در صورت عبور Quality Gate آپلود می‌شود. جزئیات تست‌ها و مسیر گزارش‌ها در `apps/android/README.md` آمده است.

گردش‌کار GitHub Actions اندروید فقط با تغییرات `apps/android/**` اجرا می‌شود؛ پنل مدیریت Workflow مجزای خود را دارد.

### پایپ‌لاین امنیتی

Workflow امنیتی `.github/workflows/security.yml` روی Push، Pull Request و اجرای دستی فعال است و کنترل‌های امنیتی پیکربندی، تست‌های API پنل، Build وب، تست/Lint/Release اندروید، اعتبارسنجی منابع XML، Gitleaks، CodeQL و OSV را اجرا می‌کند. گزارش تجمیعی امنیت به‌صورت Artifact ذخیره می‌شود و شکست هر کنترل الزامی، Security Gate را Fail می‌کند.

برای اجرای کنترل‌های قابل اجرا در محیط محلی (بدون نیاز به Gradle/Android SDK):

```bash
npm run security:verify
```

این فرمان کنترل‌های ایستای تنظیمات، تست‌های Node پنل و Build وب را اجرا می‌کند؛ مراحل Android، اسکن‌های GitHub و تست Emulator فقط در CI اجرا می‌شوند. جزئیات کنترل‌ها و موانع انتشار در `SECURITY-REQUIREMENTS.md` و `SECURITY-REVIEW-REPORT.md` ثبت شده‌اند.

## اصل معماری

- چهار کلاینت Android، iOS، Web User و Admin Web نباید مستقیماً به دیتابیس وصل شوند.
- همه کلاینت‌ها باید از API مرکزی و قراردادهای نسخه‌بندی‌شده استفاده کنند.
- `apps/admin-web/server.mjs` در این بسته API دمو و مستقل پنل است؛ جایگزین `services/api` مرکزی نیست.
- منطق تجاری، مجوزها، قیمت قطعی پرداخت و وضعیت حساب باید در بک‌اند مرکزی باشد.
- UI وب و UI موبایل می‌توانند مستقل باشند؛ مدل داده و قرارداد API باید مشترک باشد.

## وضعیت فعلی اتصال داده

Android فعلی نسخه دمو و محلی است و داده‌ها در SharedPreferences و فایل‌های گوشی ذخیره می‌شوند. Admin Web نیز در این بسته با دیتاست نمونه سروری (JSON) و API دمو کار می‌کند. این دو هنوز با یکدیگر همگام نیستند. قرارداد پیشنهادی API در `contracts/openapi/admin-api.yaml` قرار دارد. برای عملیاتی شدن محصول، مرحله بعد ساخت بک‌اند مرکزی با PostgreSQL، مهاجرت داده‌ها و اتصال Android/iOS/Web User به همان API است.

## استقرار مستقل Admin Web

پنل مدیریت یک سرویس مستقل است و نیاز به Build یا انتشار Android/iOS ندارد. می‌توان Build کلاینت را مستقل انجام داد؛ اما سرور JSON فعلی فقط برای دمو محلی است و Dockerfile آن نیز صرفاً اجرای آزمایشگاهی را هدف می‌گیرد، نه استقرار عمومی. برای جزئیات: `apps/admin-web/README.md`.

## Security review (demo scope)

- Security requirements and open release blockers: [SECURITY-REQUIREMENTS.md](SECURITY-REQUIREMENTS.md)
- Current demo review and test status: [SECURITY-REVIEW-REPORT.md](SECURITY-REVIEW-REPORT.md)
- Run local web/static security checks: `npm run security:verify`
- CI security pipeline: `.github/workflows/security.yml`

**Do not deploy the current Admin JSON demo API publicly or enter real personal data.** The central production backend and PWA are not implemented in this repository.
