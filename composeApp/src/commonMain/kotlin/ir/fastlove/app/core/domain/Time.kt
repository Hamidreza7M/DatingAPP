package ir.fastlove.app.core.domain

import ir.fastlove.app.core.util.persianDigits
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Current-time helpers built on kotlinx-datetime so they work identically on Android & iOS. */
object FastLoveClock {
    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    fun currentJalali(): JalaliDate =
        Jalali.fromEpochMillis(nowMillis())

    fun currentPersianYear(): Int = currentJalali().year

    /** HH:MM in Persian digits for a given epoch-millis timestamp. */
    fun timeLabel(millis: Long): String {
        val t = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val hh = t.hour.toString().padStart(2, '0')
        val mm = t.minute.toString().padStart(2, '0')
        return persianDigits("$hh:$mm")
    }

    fun relativeLastSeen(minutesAgo: Int): String = when {
        minutesAgo < 1 -> "همین حالا آنلاین"
        minutesAgo < 60 -> "${persianDigits(minutesAgo.toString())} دقیقه پیش"
        minutesAgo < 24 * 60 -> "${persianDigits((minutesAgo / 60).toString())} ساعت پیش"
        else -> "${persianDigits((minutesAgo / (24 * 60)).toString())} روز پیش"
    }
}
