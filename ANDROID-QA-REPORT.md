# FastLove Android QA Report

**تاریخ:** 2026-11-02  
**نسخه اپلیکیشن داخل سورس:** `2.23.1` (`versionCode=32`)  
**دامنه تغییرات:** Android testing، گزارش‌گیری و CI/CD؛ کد محصول و شماره نسخه انتشار تغییر نکرده‌اند.

## خلاصه تغییرات

1. استخراج منطق‌های مستقل اپ به `AppRules.kt` و اتصال مجدد توابع UI به همان قواعد؛ رفتار فعلی برنامه حفظ شده است.
2. افزودن ۱۴ تست JUnit برای فیلتر پروفایل، امتیاز سازگاری، ارقام فارسی/عربی، تقویم جلالی، URL شبکه‌های اجتماعی و تشخیص موضوع گفتگو.
3. افزودن یک Compose Instrumentation Smoke Test برای بررسی نمایش صفحه شروع در اجرای اول.
4. افزودن JaCoCo XML/HTML، Android Lint، گزارش JUnit و خلاصه خودکار CI.
5. اصلاح Workflow اندروید برای اجرا روی Push به `main`/`master` و Pull Requestهای مرتبط، همراه با Emulator API 35 و ذخیره گزارش و APK به‌صورت Artifact.

## نتایج اجرا در محیط فعلی

| بررسی | نتیجه | توضیح |
|---|---|---|
| AppRules Kotlin compile + smoke | PASS | کامپایل مستقل `AppRules.kt` با Kotlin compiler و اجرای ۱۰ assertion |
| منبع تست JUnit | PASS | هر ۱۴ متد تست با Kotlin compiler و مدل/Stub سبک محلی اجرا شدند؛ این جایگزین اجرای Gradle/Android نیست |
| Android `testDebugUnitTest` | NOT RUN | Gradle در محیط نصب نیست |
| Android Compose Instrumentation | NOT RUN | Android SDK و Emulator در محیط نصب/پیکربندی نیستند |
| Android Lint | NOT RUN | نیازمند Gradle و Android SDK |
| JaCoCo report واقعی | NOT RUN | به خروجی اجرای واقعی Gradle نیاز دارد |
| Android APK build | NOT RUN | `gradle` موجود نیست و `ANDROID_HOME` تنظیم نشده است |
| Admin Web regression tests | PASS | ۴ تست از ۴ تست موفق |
| Admin Web build | PASS | خروجی استاتیک پنل با Node.js ساخته شد؛ این نتیجه مربوط به Android نیست |

### توضیح محدودیت

فرمان `npm run android:verify` اجرا شد اما با خطای محیطی `gradle: not found` متوقف شد (Exit Code 127). در این محیط متغیر `ANDROID_HOME` نیز تنظیم نشده و Android SDK وجود ندارد. بنابراین **هیچ ادعایی مبنی بر Build موفق APK یا اجرای واقعی تست‌های Android/Emulator نداریم**. تست‌های Kotlin مستقل فقط درستی قواعد استخراج‌شده و تست‌کیس‌های JUnit را تا سطح کامپایل/اجرای JVM با Stub بررسی می‌کنند.

## اجرای CI پس از اتصال مخزن به GitHub

Workflow: `.github/workflows/build-apk.yml`

- Trigger: Push به `main` یا `master`، Pull Request با تغییر Android، یا اجرای دستی.
- مراحل: `testDebugUnitTest`، `lintDebug`، `assembleDebug`، `jacocoTestReport`، `connectedDebugAndroidTest` و `createDebugAndroidTestCoverageReport` روی Emulator API 35.
- گزارش‌های تست، Lint، JaCoCo، Coverage دستگاه و Instrumentation با Artifact به نام `fastlove-android-reports-<run number>` ذخیره می‌شوند.
- خلاصه تعداد تست‌ها و Coverage به GitHub Actions Summary اضافه می‌شود.
- APK دیباگ با Artifact به نام `fastlove-debug-apk-<run number>` تنها در صورت عبور مراحل اجباری آپلود می‌شود.

برای دیدن گزارش هر Build در GitHub: **Actions → انتخاب اجرای workflow → Summary / Artifacts**.

## گام‌های باقی‌مانده برای استاندارد انتشار Production

- تکمیل Integration Test برای Repository/API/Serialization و دیتابیس؛ در سورس فعلی اتصال Backend واقعی وجود ندارد.
- توسعه E2E برای مسیرهای ورود، پروفایل، Match، Chat و پرداخت، با داده Staging واقعی.
- اجرای تست سازگاری روی Matrix از نسخه‌های Android و دستگاه‌های واقعی.
- تکمیل Security Scan (OWASP MASVS/MASTG)، Dependency vulnerability و Secret scanning.
- تکمیل Performance/Macrobenchmark و Crash/ANR validation.
- تعریف Coverage Gate تدریجی برای کد جدید؛ در این مرحله Coverage کلی ۸۰٪ به‌صورت اجباری اعمال نشده، چون قسمت عمده فایل بزرگ `MainActivity.kt` هنوز تست ندارد.

## مسیرهای کلیدی تغییرکرده

```text
apps/android/app/build.gradle.kts
apps/android/app/src/main/java/ir/hamdam/demo/AppRules.kt
apps/android/app/src/test/java/ir/hamdam/demo/AppRulesTest.kt
apps/android/app/src/androidTest/java/ir/hamdam/demo/MainActivitySmokeTest.kt
apps/android/scripts/write-ci-summary.py
.github/workflows/build-apk.yml
apps/android/README.md
README.md
```
