# گزارش Build و تست

## محیط ساخت سورس
- سیستم‌عامل: Windows 10 (بدون JDK / Android SDK / Gradle / Android Studio).
- فضای دیسک محدود؛ امکان نصب کامل toolchain محلی نبود.
- نتیجه: کد نوشته شد اما **به‌صورت محلی کامپایل/Build/اجرا نشد**.

## وضعیت واقعی
| مورد | وضعیت | توضیح |
|---|---|---|
| Build اندروید (Debug APK) | ⏳ اجرا نشده (محلی) | از طریق `.github/workflows/android.yml` روی GitHub Actions ساخته می‌شود |
| Release AAB | ⏳ اجرا نشده (محلی) | در همان workflow (`bundleRelease`) |
| Target و Scheme iOS | ✅ ساخته شد | `iosApp/iosApp.xcodeproj` + اسکیم مشترک `iosApp` |
| iOS Simulator Build | ⏳ نیازمند macOS | از طریق `.github/workflows/ios.yml` |
| IPA دستگاه واقعی | ⛔ | نیازمند Signing Certificate و Provisioning Profile |
| تست‌های واحد دامین | ⏳ اجرا نشده (محلی) | در CI با `testDebugUnitTest` اجرا می‌شوند |

## تست‌های نوشته‌شده (commonTest)
- `MatchingTest` — بازه‌ی امتیاز، اثر علاقه‌ی مشترک و فاصله، دلایل، فیلتر جنسیتی.
- `JalaliTest` — تبدیل نوروز و میانه‌ی سال، رفت‌وبرگشت، روزهای ماه (کبیسه ۱۴۰۳ و غیرکبیسه ۱۴۰۴)، یکنواختی سریال ماه.
- `MapLogicTest` — فیلتر جنسیتی، حذف بلاک، برچسب تعامل، فیلتر لایک، گردکردن حریم خصوصی، آستانه‌ی Clustering.
- `DateLogicTest` — پنجره‌ی انتخاب تاریخ، مجوز پذیرش، گذارهای وضعیت، منطق Counter.
- `DataTest` — حداقل ۳۰ پروفایل، قطعی‌بودن Seed، ارقام فارسی، فرمت تومان، تشخیص موضوع چت.

## گام بعدی برای Build واقعی
1. سورس را به یک ریپوی GitHub منتقل کنید.
2. اجازه دهید workflow «Android Build & Test» اجرا شود.
3. خطاهای احتمالی کامپایل اولین اجرا را برطرف کنید (معمولاً importهای جامانده).
4. Artifact `fastlove-debug-apk` را دانلود و روی دستگاه نصب کنید.
