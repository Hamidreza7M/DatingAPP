# همدم — نسخه نمایشی اندروید

اپلیکیشن نمایشی فارسی برای آشنایی با رابط کاربری همدم. اطلاعات کاربران و پیام‌ها محلی و نمونه‌ای هستند؛ این نسخه سرور، ورود پیامکی، تطبیق واقعی یا ذخیره‌سازی آنلاین ندارد.

## ساخت APK با GitHub Actions (بدون Android Studio)
1. یک repository جدید در GitHub بساز.
2. محتویات این پوشه را در repository قرار بده و روی شاخه `main` push کن.
3. وارد تب **Actions** شو و workflow با نام **Build Hamdam APK** را انتخاب کن.
4. روی **Run workflow** بزن یا منتظر بمان تا پس از push اجرا شود.
5. بعد از موفقیت، در صفحه اجرای workflow از بخش **Artifacts** فایل `hamdam-debug-apk` را دانلود کن.
6. ZIP دانلودشده را باز کن؛ فایل `app-debug.apk` همان فایل نصب روی گوشی اندرویدی است.

## ساخت محلی
نیازمند JDK 17 و Gradle 8.9 است. در ریشه پروژه اجرا کن:

```bash
gradle assembleDebug
```

APK در مسیر `app/build/outputs/apk/debug/app-debug.apk` ساخته می‌شود.
