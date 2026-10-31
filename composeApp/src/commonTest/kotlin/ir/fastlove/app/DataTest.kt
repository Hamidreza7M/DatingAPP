package ir.fastlove.app

import ir.fastlove.app.core.data.Seed
import ir.fastlove.app.core.domain.DemoReply
import ir.fastlove.app.core.util.formatToman
import ir.fastlove.app.core.util.latinDigits
import ir.fastlove.app.core.util.persianDigits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataTest {

    @Test
    fun seed_has_at_least_thirty_people() {
        assertTrue(Seed.people.size >= 30, "expected >= 30 demo profiles, got ${Seed.people.size}")
    }

    @Test
    fun seed_is_deterministic() {
        val a = Seed.people.map { it.id to it.name }
        val b = Seed.people.map { it.id to it.name }
        assertEquals(a, b)
    }

    @Test
    fun seed_has_tehran_located_people() {
        assertTrue(Seed.people.count { it.latitude != 0.0 } >= 20)
    }

    @Test
    fun persian_digits_round_trip() {
        assertEquals("۱۲۳۴", persianDigits("1234"))
        assertEquals("1234", latinDigits("۱۲۳۴"))
    }

    @Test
    fun toman_formatting_groups_thousands() {
        assertEquals("۱۴۹٬۰۰۰ تومان", formatToman(149000))
    }

    @Test
    fun demo_reply_is_topic_aware() {
        assertEquals("music", DemoReply.topicOf("یه آهنگ خوب بهم پیشنهاد بده"))
        assertEquals("travel", DemoReply.topicOf("آخرین سفرت کجا بود؟"))
        assertEquals("greeting", DemoReply.topicOf("سلام چطوری"))
    }
}
