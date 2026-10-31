package ir.fastlove.app.core.domain

import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.util.persianDigits
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime

data class JalaliDate(val year: Int, val month: Int, val day: Int)
private data class JalaliCalibration(val gregorianYear: Int, val marchDay: Int, val leap: Int)

/**
 * Persian (Jalali) calendar — a pure-Kotlin port of the established break-year algorithm,
 * using kotlinx-datetime's LocalDate instead of java.util.Calendar so it runs on Android & iOS.
 */
object Jalali {

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private fun calibration(jalaliYear: Int): JalaliCalibration {
        val gregorianYear = jalaliYear + 621
        var leapJ = -14
        var previousBreak = breaks.first()
        var currentBreak: Int
        var jump = 0
        for (index in 1 until breaks.size) {
            currentBreak = breaks[index]
            jump = currentBreak - previousBreak
            if (jalaliYear < currentBreak) break
            leapJ += (jump / 33) * 8 + (jump % 33) / 4
            previousBreak = currentBreak
        }
        val yearsSinceBreak = jalaliYear - previousBreak
        leapJ += (yearsSinceBreak / 33) * 8 + ((yearsSinceBreak % 33) + 3) / 4
        if (jump % 33 == 4 && jump - yearsSinceBreak == 4) leapJ += 1

        val leapG = gregorianYear / 4 - ((gregorianYear / 100 + 1) * 3) / 4 - 150
        val marchDay = 20 + leapJ - leapG
        var leap = (((yearsSinceBreak + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        return JalaliCalibration(gregorianYear, marchDay, leap)
    }

    private fun newYearEpochDays(jalaliYear: Int): Int {
        val c = calibration(jalaliYear)
        return LocalDate(c.gregorianYear, 3, c.marchDay).toEpochDays()
    }

    fun fromGregorian(date: LocalDate): JalaliDate {
        val targetDays = date.toEpochDays()
        var jalaliYear = date.year - 621
        var dayOfYear = targetDays - newYearEpochDays(jalaliYear)
        if (dayOfYear < 0) {
            jalaliYear -= 1
            dayOfYear = targetDays - newYearEpochDays(jalaliYear)
        }
        return if (dayOfYear <= 185) {
            JalaliDate(jalaliYear, 1 + dayOfYear / 31, dayOfYear % 31 + 1)
        } else {
            val remaining = dayOfYear - 186
            JalaliDate(jalaliYear, 7 + remaining / 30, remaining % 30 + 1)
        }
    }

    fun toGregorian(year: Int, month: Int, day: Int): LocalDate {
        val daysBeforeMonth = if (month <= 7) (month - 1) * 31 else 186 + (month - 7) * 30
        val epochDays = newYearEpochDays(year) + daysBeforeMonth + day - 1
        return LocalDate.fromEpochDays(epochDays)
    }

    fun daysInMonth(year: Int, month: Int): Int = when {
        month in 1..6 -> 31
        month in 7..11 -> 30
        calibration(year).leap == 0 -> 30
        else -> 29
    }

    /** year*12 + month, useful for comparing / stepping months. */
    fun monthSerial(date: JalaliDate): Int = date.year * 12 + date.month - 1

    fun fromEpochMillis(millis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): JalaliDate =
        fromGregorian(Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZone).date)

    /** Epoch millis for noon of the given Jalali date (noon avoids DST edge cases). */
    fun toEpochMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Long {
        val date = toGregorian(year, month, day)
        val startOfDay = date.atStartOfDayIn(timeZone).toEpochMilliseconds()
        return startOfDay + (hour * 60L + minute) * 60_000L
    }

    fun label(date: JalaliDate, withWeekday: Boolean = true): String {
        val month = FastLoveData.persianMonths[date.month - 1]
        val base = "${persianDigits(date.day.toString())} $month ${persianDigits(date.year.toString())}"
        if (!withWeekday) return base
        val gregorian = toGregorian(date.year, date.month, date.day)
        val isoDay = gregorian.dayOfWeek.ordinal + 1 // Monday=1 .. Sunday=7
        val weekday = FastLoveData.persianWeekdays[(isoDay + 1) % 7]
        return "$weekday $base"
    }

    fun labelFromMillis(millis: Long): String = label(fromEpochMillis(millis))
}
