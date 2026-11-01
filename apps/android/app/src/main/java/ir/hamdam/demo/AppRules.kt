package ir.hamdam.demo

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pure application rules extracted from the UI entry point so they can be tested on the JVM.
 * Keep Android framework and Compose UI dependencies out of this class where possible.
 */
internal object AppRules {
    private val jalaliBreaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    fun compatiblePeople(viewer: UserProfile, people: List<Person>): List<Person> = people.filter { person ->
        when (viewer.lookingFor ?: LookingForGender.BOTH) {
            LookingForGender.FEMALE -> person.gender == Gender.FEMALE
            LookingForGender.MALE -> person.gender == Gender.MALE
            LookingForGender.BOTH -> true
        }
    }

    fun compatibilityScore(viewer: UserProfile, person: Person): Float {
        val sharedInterests = viewer.interests.toSet().intersect(person.interests.toSet()).size
        val denominator = minOf(3, viewer.interests.size, person.interests.size).coerceAtLeast(1)
        val interestPoints = if (viewer.interests.isEmpty() || person.interests.isEmpty()) 0f
            else (sharedInterests.toFloat() / denominator).coerceIn(0f, 1f) * 4f

        val distancePoints = (2f * (1f - person.distanceKm / 80f)).coerceIn(0f, 2f)
        val ageDifference = abs(viewer.age - person.age)
        val agePoints = (2f * (1f - ageDifference / 20f)).coerceIn(0f, 2f)
        val viewerJob = viewer.job.trim()
        val personJob = person.job.trim()
        val jobPoints = when {
            viewerJob.isBlank() || personJob.isBlank() -> 0f
            viewerJob.equals(personJob, ignoreCase = true) -> 2f
            else -> {
                val categories = listOf(
                    listOf("مهندس", "نرم‌افزار", "برنامه‌نویس", "کدنویس", "فناوری"),
                    listOf("طراح", "عکاس", "محتوا", "هنر", "مد"),
                    listOf("پزشک", "پرستار", "روانشناس", "سلامت"),
                    listOf("معلم", "استاد", "دانشجو", "آموزش"),
                    listOf("وکیل", "حقوق", "فروشنده", "فروش", "مدیر")
                )
                val sameCategory = categories.any { group ->
                    group.any { viewerJob.contains(it) } && group.any { personJob.contains(it) }
                }
                if (sameCategory) 1.2f else 0.4f
            }
        }
        return ((interestPoints + distancePoints + agePoints + jobPoints)
            .coerceIn(0f, 10f) * 10f).roundToInt() / 10f
    }

    fun persianDigits(value: String): String = value.map { character ->
        if (character in '0'..'9') ('۰'.code + (character - '0')).toChar() else character
    }.joinToString("")

    fun latinDigits(value: String): String = value.map { character ->
        when (character) {
            in '۰'..'۹' -> ('0'.code + (character - '۰')).toChar()
            in '٠'..'٩' -> ('0'.code + (character - '٠')).toChar()
            else -> character
        }
    }.joinToString("")

    fun jalaliDaysInMonth(year: Int, month: Int): Int {
        require(year in jalaliBreaks.first() until jalaliBreaks.last()) { "Unsupported Jalali year: $year" }
        require(month in 1..12) { "Jalali month must be between 1 and 12" }
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            jalaliLeapIndicator(year) == 0 -> 30
            else -> 29
        }
    }

    private fun jalaliLeapIndicator(jalaliYear: Int): Int {
        var previousBreak = jalaliBreaks.first()
        for (index in 1 until jalaliBreaks.size) {
            val currentBreak = jalaliBreaks[index]
            if (jalaliYear < currentBreak) break
            previousBreak = currentBreak
        }
        val yearsSinceBreak = jalaliYear - previousBreak
        var leap = (((yearsSinceBreak + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        // Same indicator convention used by the Jalali conversion in MainActivity.
        return leap
    }

    fun socialProfileUrl(platform: String, rawValue: String): String {
        val value = rawValue.trim()
        if (value.startsWith("https://", true) || value.startsWith("http://", true)) return value
        return when (platform) {
            "instagram" -> "https://www.instagram.com/${value.removePrefix("@").trim('/')}/"
            else -> "https://open.spotify.com/user/${value.trim('/') }"
        }
    }

    fun conversationTopic(text: String): String {
        val message = text.trim().lowercase(Locale.ROOT)
        return when {
            listOf("سلام", "درود", "خوبی", "چطوری", "چه خبر").any { it in message } -> "greeting"
            listOf("موسیقی", "آهنگ", "اسپاتیفای", "spotify", "خواننده", "کنسرت").any { it in message } -> "music"
            listOf("سفر", "مسافرت", "گردش", "شهر", "مسیر", "تعطیلات").any { it in message } -> "travel"
            listOf("کافه", "قهوه", "کافی", "رستوران", "غذا").any { it in message } -> "cafe"
            listOf("کتاب", "مطالعه", "رمان", "داستان").any { it in message } -> "book"
            listOf("فیلم", "سریال", "سینما", "بازیگر").any { it in message } -> "movie"
            listOf("کار", "شغل", "درس", "دانشگاه", "شرکت", "حرفه").any { it in message } -> "work"
            listOf("ورزش", "باشگاه", "فوتبال", "یوگا", "پیاده", "دویدن").any { it in message } -> "sport"
            listOf("خودت", "تو چی", "تو چطور", "از خودت", "علاقه‌ات").any { it in message } -> "about_person"
            else -> "general"
        }
    }
}
