# FastLove Android Testing Strategy

## سیاست توسعه

- هر Feature جدید باید پیش از Merge حداقل تست‌های متناسب با منطق و رفتار جدید را داشته باشد.
- هر Bug Fix باید یک Regression Test داشته باشد که قبل از اصلاح شکست بخورد و پس از اصلاح Pass شود.
- تغییر API/Serialization/Repository باید Integration Test مربوط به پاسخ موفق، خطا، Timeout و داده نامعتبر را به‌روزرسانی کند.
- تغییر Room یا ساختار ذخیره‌سازی باید تست Migration و سازگاری داده داشته باشد.
- تغییر UI باید تست Compose/Espresso متناسب با مسیر کاربر داشته باشد.
- تغییرات Permission، احراز هویت یا داده حساس باید تست مجوز، دسترسی غیرمجاز و رفتار در زمان رد/لغو مجوز را پوشش دهند.
- حذف یا تغییر رفتار قبلی باید تست‌های قدیمی را بازبینی کند؛ تست مرتبط نباید صرفاً برای سبز شدن CI حذف شود.

## اجرای محلی

از پوشه `apps/android`:

```bash
gradle testDebugUnitTest
gradle lintDebug
gradle jacocoTestReport
gradle assembleDebug
```

برای تست‌های Instrumentation به Emulator یا دستگاه Android نیاز است:

```bash
gradle connectedDebugAndroidTest
gradle createDebugAndroidTestCoverageReport
```

## Quality Gate در CI

Workflow فایل `.github/workflows/build-apk.yml` روی Pull Request و Push به شاخه اصلی اجرا می‌شود. نتیجه Unit Tests، Lint، Coverage و Build باید موفق باشد. تست‌های Instrumentation نیز روی Emulator اجرا می‌شوند. گزارش‌های تست و Coverage و Lint به‌صورت Artifact ذخیره می‌شوند.

## حداقل‌های بازبینی Pull Request

1. تست جدید مربوط به رفتار جدید یا Bug Fix وجود داشته باشد.
2. تست‌های قبلی بدون حذف یا Skip غیرموجه اجرا شوند.
3. خطاهای Lint جدید رفع شوند؛ برای خطاهای قدیمی باید Baseline یا برنامه اصلاح مستند وجود داشته باشد.
4. تغییر Coverage کد جدید بررسی شود؛ Coverage کلی به‌تنهایی معیار کیفیت نیست.
5. در پایان، گزارش CI و علت هر تست Skip‌شده در توضیحات Pull Request درج شود.
