# FastLove Admin Web

پنل مستقل مدیریت FastLove. این برنامه با Node.js و JavaScript بدون وابستگی npm ساخته شده و چرخه اجرا و انتشار آن مستقل از Android و iOS است.

## نیازمندی‌ها

- Node.js نسخه 20 یا جدیدتر
- بدون نیاز به `npm install`

## اجرای توسعه

از ریشه مخزن:

```bash
npm run admin:dev
```

یا از همین پوشه:

```bash
npm run dev
```

سپس آدرس `http://localhost:4177` را باز کنید.

### حساب آزمایشی توسعه

- نام کاربری: `admin`
- رمز عبور: `FastLove@2026`

این اطلاعات فقط در حالت `NODE_ENV` غیر production فعال‌اند و نباید برای محیط عمومی استفاده شوند.

## Build مستقل

```bash
npm run admin:build
```

خروجی کلاینت استاتیک در `apps/admin-web/dist/` قرار می‌گیرد. API سرور با دستور `npm run admin:start` اجرا می‌شود. در این نسخه API و کلاینت توسط یک Node server در یک مبدأ ارائه می‌شوند؛ می‌توان آن‌ها را در محیط استقرار جدا کرد، به شرط اینکه آدرس API و تنظیمات کوکی/مبدأ به‌درستی پیکربندی شوند.

## تنظیمات Production

متغیرهای زیر را در محیط امن سرور تنظیم کنید (فایل `.env` در مخزن قرار ندهید):

```dotenv
NODE_ENV=production
PORT=4177
PUBLIC_ORIGIN=https://admin.example.com
ADMIN_USERNAME=your-admin-user
ADMIN_PASSWORD=your-long-unique-password
SESSION_SECRET=your-random-secret-at-least-32-bytes
```

در Production کوکی نشست `HttpOnly`, `SameSite=Strict` و `Secure` است؛ HTTPS اجباری است. نشست‌ها در حافظه سرور نگهداری می‌شوند و با Restart سرور منقضی خواهند شد.

## محدوده قابلیت‌ها

- ورود مدیر با نام کاربری و رمز عبور سمت سرور و محدودسازی تلاش ورود
- داشبورد BI نمونه شامل کاربران، درآمد، تبدیل، پرداخت‌ها و فعالیت اخیر
- جست‌وجوی کاربران، صفحه جزئیات و تب‌های اطلاعات، فعالیت، پرداخت‌ها، لایک/مچ، دیت، تیکت و احراز هویت
- مشاهده جزئیات پرداخت‌ها
- بررسی و تأیید/رد گزارش تخلف
- حذف نرم حساب کاربر با نگهداری سوابق مالی و نظارتی
- فهرست لایک‌ها، مچ‌ها و دیت‌های برنامه‌ریزی‌شده
- بررسی درخواست‌های احراز هویت ویدئویی و ثبت نتیجه
- مشاهده تاریخچه گفتگوهای تیکت، پاسخ مدیر و بستن/بازکردن مجدد تیکت
- ویرایش قیمت و فعال/غیرفعال‌کردن بسته‌ها
- ثبت رویدادهای مدیریتی در Audit Log API

## هشدار مهم درباره داده‌ها و اتصال به اپ موبایل

نسخه فعلی Android از `SharedPreferences` و فایل‌های محلی برای کاربران نمونه، پرداخت‌ها، تیکت‌ها، ویدئو و تعامل‌ها استفاده می‌کند. بنابراین داده‌های موبایل به این پنل ارسال نمی‌شوند و پنل فعلی یک محیط دمو با دیتاست سروری مستقل است. تغییرات پنل در فایل `data/db.json` ذخیره می‌شوند.

برای Production واقعی باید:

1. یک بک‌اند مرکزی و دیتابیس رابطه‌ای (مثلاً PostgreSQL) ایجاد شود.
2. Android، iOS و Web User از قرارداد API مشترک استفاده کنند.
3. احراز هویت کاربر و مدیر، RBAC، سیاست حذف/نگهداری داده و Audit سمت سرور پیاده‌سازی شود.
4. ویدئوها و پیوست‌ها در Object Storage خصوصی با URL کوتاه‌عمر نگهداری شوند؛ این نسخه فایل ویدئویی واقعی ارائه نمی‌کند.
5. درگاه پرداخت فقط قیمت ثبت‌شده در سمت سرور را مبنا قرار دهد؛ تغییر قیمت این نسخه هنوز به جریان خرید اندروید متصل نیست.
6. ذخیره‌سازی JSON دمو با PostgreSQL و نشست حافظه‌ای با Session Store پایدار جایگزین شود.

## Reset داده‌های نمونه

سرور را متوقف کنید و `apps/admin-web/data/db.json` را حذف کنید. در اجرای بعدی، دیتابیس دمو از `data/seed.json` دوباره ساخته می‌شود.

## Docker (مناسب برای استقرار مستقل)

از همین پوشه:

```bash
docker build -t fastlove-admin .
docker run -d --name fastlove-admin \
  -p 4177:4177 \
  -e ADMIN_USERNAME='your-admin-user' \
  -e ADMIN_PASSWORD='a-long-unique-password' \
  -e SESSION_SECRET='a-random-secret-with-at-least-32-bytes' \
  -e PUBLIC_ORIGIN='https://admin.example.com' \
  -e DATA_DIR=/var/lib/fastlove/data \
  -v fastlove-admin-data:/var/lib/fastlove/data \
  fastlove-admin
```

Volume باعث ماندگاری دیتای دمو JSON پس از تعویض کانتینر می‌شود. برای Production پایدار و چندنمونه‌ای، این JSON store باید با PostgreSQL و session store مشترک جایگزین شود.
