# فست‌لاو (FastLove) — نسخه‌ی Cross-Platform دمو

اپلیکیشن دوست‌یابی و آشنایی هدفمند فارسی، بازنویسی‌شده روی **Kotlin Multiplatform + Compose Multiplatform** برای **Android** و **iOS** از یک کدبیس مشترک.

> **این یک Demo است.** هیچ Backend، دیتابیس ابری، احراز هویت واقعی، پیامک، یا پرداخت واقعی وجود ندارد. همه‌ی قابلیت‌ها با داده‌ی نمونه و Mock Repository کار می‌کنند و در ذخیره‌سازی محلی دستگاه باقی می‌مانند. ساختار کد طوری است که در آینده می‌توان یک Backend واقعی را بدون بازنویسی UI جایگزین کرد.

---

## وضعیت صادقانه‌ی Build

این سورس روی محیطی نوشته شده که **JDK/Android SDK/Gradle نداشت**، بنابراین:

- ❗ **هیچ Build محلی اجرا و verify نشده است.** کد با دقت و بر اساس سازگاری رسمی نسخه‌ها نوشته شده، اما تا وقتی روی یک محیط دارای ابزار (یا GitHub Actions) کامپایل نشود، نمی‌توان ادعای «Build موفق» کرد.
- ✅ مسیر واقعی Build آماده است: Workflowهای GitHub Actions (`.github/workflows/android.yml` و `ios.yml`) APK/AAB اندروید و Build شبیه‌ساز iOS را روی runnerهای ابری می‌سازند.
- برای گرفتن APK واقعی: سورس را به یک ریپوی GitHub push کنید؛ workflow «Android Build & Test» به‌صورت خودکار تست‌ها را اجرا و `fastlove-debug-apk` را به‌عنوان Artifact آپلود می‌کند.

خطاهای کامپایل احتمالی (import جامانده و…) باید در اولین اجرای CI برطرف شوند؛ این طبیعی و بخشی از فرایند است.

---

## معماری

```
FastLove/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/ir/fastlove/app/
│       │   ├── app/            # ریشه‌ی App، پوسته‌ی اصلی، CompositionLocalها
│       │   ├── navigation/     # Navigator و مقصدها
│       │   ├── core/
│       │   │   ├── designsystem/  # رنگ، تایپوگرافی، شکل، فاصله، تم، کامپوننت‌ها
│       │   │   ├── model/         # مدل‌ها و ثابت‌ها (خالص، تست‌پذیر)
│       │   │   ├── domain/        # مچینگ، تقویم شمسی، منطق نقشه/دیت، پاسخ دمو
│       │   │   ├── data/          # Repository + Mock + Seed + ذخیره‌سازی محلی
│       │   │   ├── platform/      # expect: BackHandler، Share
│       │   │   └── util/          # متن فارسی، Idها
│       │   └── feature/        # onboarding, discover, likes, chat, map,
│       │                       # dateplanner, profile, settings, subscription
│       ├── androidMain/        # MainActivity، Manifest، actualها، منابع
│       ├── iosMain/            # MainViewController، actualها
│       ├── commonTest/         # تست‌های دامین
│       └── commonMain/composeResources/  # فونت Vazirmatn، JSON مکان‌ها
├── iosApp/                     # پروژه‌ی Xcode (SwiftUI + Scheme واقعی)
├── gradle/libs.versions.toml   # Version Catalog
└── .github/workflows/          # CI اندروید و iOS
```

- **MVVM/UDF سبک**: State در `FastLoveRepository` به‌صورت `StateFlow` نگهداری می‌شود؛ UI آن را `collectAsState` می‌کند.
- **کد مشترک**: UI، Design System، ناوبری و منطق دامین همه در `commonMain`.
- **تفاوت پلتفرم‌ها** با `expect/actual`: ذخیره‌سازی (SharedPreferences / NSUserDefaults)، دکمه‌ی بازگشت، Share Sheet.
- **RTL کامل** و فونت **Vazirmatn** به‌صورت سراسری.

### نقشه
نقشه در این نسخه یک نمای شماتیک مشترک (Compose) است که مارکرها را از Mock Repository می‌خواند و همه‌ی قواعد خواسته‌شده را دارد: فیلتر جنسیتی دوطرفه، حذف بلاک‌شده‌ها، نشان لایک/دیسلایک فقط برای خود کاربر، Clustering در زوم دور، موقعیت تقریبی (گرد‌شده) و مخفی‌شدن. یکپارچه‌سازی کاشی‌های واقعی خیابان (osmdroid در اندروید / MapKit در iOS) به‌عنوان یک Adapter پلتفرمی قابل افزودن است؛ Attribution باید حفظ و کاشی‌ها انبوه دانلود نشوند.

### عکس پروفایل
برای پرهیز از پیچیدگی و ریسک File/URI بین دو پلتفرم، آواتارها در دمو از ایموجی + گرادیان ساخته می‌شوند. آپلود عکس واقعی یک Adapter پلتفرمی مشخص برای توسعه‌ی بعدی است.

---

## ساخت اندروید

نیازمند JDK 17 و Android SDK (API 35).

```bash
gradle :composeApp:assembleDebug
```

خروجی: `composeApp/build/outputs/apk/debug/`. برای AAB ریلیز: `gradle :composeApp:bundleRelease`.

اجرای تست‌ها:

```bash
gradle :composeApp:testDebugUnitTest
```

## ساخت iOS (نیازمند macOS + Xcode)

```bash
gradle :composeApp:linkDebugFrameworkIosSimulatorArm64
```

سپس `iosApp/iosApp.xcodeproj` را در Xcode باز کنید، اسکیم **iosApp** را انتخاب و روی شبیه‌ساز Run کنید. برای دستگاه واقعی به `TEAM_ID` در `iosApp/Configuration/Config.xcconfig` و Provisioning Profile نیاز است. ساخت IPA بدون امضای معتبر ممکن نیست.

> روی ویندوز/لینوکس Build iOS ممکن نیست؛ از Workflow `ios.yml` روی macOS استفاده کنید.

---

## نسخه‌ها

Kotlin 2.1.0 · Compose Multiplatform 1.7.3 · AGP 8.7.3 · Gradle 8.10.2 · kotlinx-datetime 0.6.1 · multiplatform-settings 1.2.0.

هیچ API Key یا Secret در سورس نیست.
