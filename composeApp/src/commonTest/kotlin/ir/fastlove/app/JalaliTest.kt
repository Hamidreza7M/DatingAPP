package ir.fastlove.app

import ir.fastlove.app.core.domain.Jalali
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JalaliTest {

    @Test
    fun known_nowruz_conversion() {
        // 2024-03-20 is 1 Farvardin 1403.
        val j = Jalali.fromGregorian(LocalDate(2024, 3, 20))
        assertEquals(1403, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun known_mid_year_conversion() {
        // 2024-09-22 is 1 Mehr 1403.
        val j = Jalali.fromGregorian(LocalDate(2024, 9, 22))
        assertEquals(1403, j.year)
        assertEquals(7, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun round_trip_is_stable() {
        val original = LocalDate(2025, 1, 15)
        val j = Jalali.fromGregorian(original)
        val back = Jalali.toGregorian(j.year, j.month, j.day)
        assertEquals(original, back)
    }

    @Test
    fun days_in_month_rules() {
        assertEquals(31, Jalali.daysInMonth(1403, 1))   // first 6 months
        assertEquals(30, Jalali.daysInMonth(1403, 8))   // months 7..11
        // 1403 is a leap year → Esfand has 30 days
        assertEquals(30, Jalali.daysInMonth(1403, 12))
        // 1404 is not a leap year → Esfand has 29 days
        assertEquals(29, Jalali.daysInMonth(1404, 12))
    }

    @Test
    fun month_serial_is_monotonic() {
        val a = Jalali.monthSerial(Jalali.fromGregorian(LocalDate(2024, 3, 20)))
        val b = Jalali.monthSerial(Jalali.fromGregorian(LocalDate(2024, 4, 25)))
        assertTrue(b > a)
    }
}
