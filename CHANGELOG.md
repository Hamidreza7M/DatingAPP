# Changelog

## 3.0.0-kmp — بازنویسی Cross-Platform (دمو)

### افزوده‌شده
- مهاجرت از پروژه‌ی تک‌ماژول اندروید (god-file `MainActivity.kt` با ۶۵۷۵ خط) به **Kotlin Multiplatform + Compose Multiplatform** برای Android و iOS.
- تفکیک به لایه‌ها: `core/designsystem`, `core/model`, `core/domain`, `core/data`, `core/platform`, `navigation`, و ۹ فیچر مجزا.
- **Design System مشترک**: توکن‌های رنگ، تایپوگرافی Vazirmatn، شکل، فاصله، تم (RTL + Reduce-Motion) و کامپوننت‌های پایه.
- **دامین خالص و تست‌پذیر**: الگوریتم مچینگ با دلایل، تقویم هجری شمسی (با kotlinx-datetime)، منطق فیلتر نقشه، منطق وضعیت دیت، پاسخ‌های دموی چت.
- **Mock Repository** با ۴۰ پروفایل Seed قطعی + ذخیره‌سازی محلی Cross-Platform (`expect/actual` روی SharedPreferences و NSUserDefaults).
- فیچرها: Onboarding + آموزش تعاملی، کشف/سوایپ، لایک‌ها/مچ، چت، نقشه، Date Planner با تقویم شمسی، پروفایل/تنظیمات، اشتراک/تاریخچه‌ی پرداخت.
- پروژه‌ی **iOS/Xcode** با Scheme واقعی و Entry Point اندروید.
- تست‌های واحد دامین و Workflowهای CI برای Build اندروید و iOS.

### تغییر
- `applicationId` حفظ شد: `ir.fastlove.demo`. namespace به `ir.fastlove.app`.
- عکس پروفایل در دمو به آواتار ایموجی/گرادیان تغییر کرد (آپلود واقعی = Adapter آینده).

### حذف/به‌تعویق
- وابستگی مستقیم به osmdroid (Android-only) از کد مشترک؛ نقشه‌ی شماتیک مشترک جایگزین شد و کاشی واقعی به Adapter پلتفرمی موکول شد.
- هیچ قابلیت کاربری‌ای حذف نشد؛ همه‌ی فلوهای اصلی بازپیاده‌سازی شدند.
