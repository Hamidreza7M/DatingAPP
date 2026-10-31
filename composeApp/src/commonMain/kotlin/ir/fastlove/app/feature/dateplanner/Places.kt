package ir.fastlove.app.feature.dateplanner

import ir.fastlove.app.core.model.DateKind

data class DatePlace(val name: String, val kind: DateKind, val latitude: Double, val longitude: Double)

/** Public, well-known Tehran sample venues. Demo data — no real hours/reservations implied. */
object DatePlaces {
    val all = listOf(
        DatePlace("کافه‌ی نگارستان", DateKind.CAFE, 35.7010, 51.4210),
        DatePlace("کافه‌کتاب آرام", DateKind.CAFE, 35.7560, 51.4100),
        DatePlace("رستوران باغ ایرانی", DateKind.RESTAURANT, 35.7720, 51.4180),
        DatePlace("رستوران دریاچه", DateKind.RESTAURANT, 35.7440, 51.3330),
        DatePlace("پارک آب و آتش", DateKind.WALK, 35.7558, 51.4270),
        DatePlace("پارک جمشیدیه", DateKind.WALK, 35.8130, 51.4530),
        DatePlace("پردیس سینمایی کوروش", DateKind.CINEMA, 35.7470, 51.3360),
        DatePlace("سینما آزادی", DateKind.CINEMA, 35.7120, 51.3870),
        DatePlace("موزه‌ی هنرهای معاصر", DateKind.GALLERY, 35.7190, 51.4060),
        DatePlace("گالری طراحان آزاد", DateKind.GALLERY, 35.7800, 51.4200),
        DatePlace("بام تهران", DateKind.ACTIVITY, 35.8170, 51.3920),
        DatePlace("مجموعه‌ی تفریحی ایران‌مال", DateKind.ACTIVITY, 35.7560, 51.1850)
    )

    fun forKind(kind: DateKind) = all.filter { it.kind == kind }
}
