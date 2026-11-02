package ir.hamdam.demo

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRulesTest {
    private fun viewer(
        lookingFor: LookingForGender? = LookingForGender.BOTH,
        interests: List<String> = listOf("موسیقی", "سفر", "کتاب"),
        job: String = "مهندس نرم‌افزار"
    ) = UserProfile(
        name = "آزمایش",
        birthYearPersian = 1380,
        lookingFor = lookingFor,
        interests = interests,
        job = job
    )

    private fun person(
        id: Int = 1,
        gender: Gender = Gender.FEMALE,
        age: Int = viewer().age,
        interests: List<String> = listOf("موسیقی", "سفر", "کتاب"),
        job: String = "مهندس نرم‌افزار",
        distanceKm: Int = 0
    ) = Person(
        id = id,
        name = "کاربر $id",
        age = age,
        gender = gender,
        orientation = Orientation.OTHER,
        job = job,
        city = "تهران",
        about = "",
        interests = interests,
        emoji = "⭐",
        tint = Color.Magenta,
        distanceKm = distanceKm
    )

    @Test fun compatiblePeopleFiltersFemaleProfiles() {
        val result = AppRules.compatiblePeople(
            viewer(LookingForGender.FEMALE),
            listOf(person(1, Gender.FEMALE), person(2, Gender.MALE), person(3, Gender.OTHER))
        )
        assertEquals(listOf(1), result.map { it.id })
    }

    @Test fun compatiblePeopleFiltersMaleProfiles() {
        val result = AppRules.compatiblePeople(
            viewer(LookingForGender.MALE),
            listOf(person(1, Gender.FEMALE), person(2, Gender.MALE), person(3, Gender.OTHER))
        )
        assertEquals(listOf(2), result.map { it.id })
    }

    @Test fun compatiblePeopleReturnsAllForBothOrNullPreference() {
        val people = listOf(person(1, Gender.FEMALE), person(2, Gender.MALE), person(3, Gender.OTHER))
        assertEquals(people, AppRules.compatiblePeople(viewer(LookingForGender.BOTH), people))
        assertEquals(people, AppRules.compatiblePeople(viewer(null), people))
    }

    @Test fun compatibilityScoreReachesTenForAnIdealMatch() {
        val currentViewer = viewer()
        assertEquals(10f, AppRules.compatibilityScore(currentViewer, person(age = currentViewer.age)), 0.001f)
    }

    @Test fun compatibilityScoreIsBoundedBetweenZeroAndTen() {
        val currentViewer = viewer(interests = emptyList(), job = "")
        val low = AppRules.compatibilityScore(
            currentViewer,
            person(age = currentViewer.age + 50, interests = emptyList(), job = "", distanceKm = 500)
        )
        val high = AppRules.compatibilityScore(currentViewer, person(age = currentViewer.age, distanceKm = -50))
        assertTrue("score should not be negative", low >= 0f)
        assertTrue("score should not exceed ten", high <= 10f)
    }

    @Test fun compatibilityScoreRewardsSharedJobCategory() {
        val scoreSameCategory = AppRules.compatibilityScore(viewer(job = "مهندس نرم‌افزار"), person(job = "برنامه‌نویس"))
        val scoreOtherCategory = AppRules.compatibilityScore(viewer(job = "مهندس نرم‌افزار"), person(job = "پزشک"))
        assertTrue(scoreSameCategory > scoreOtherCategory)
    }

    @Test fun convertsLatinDigitsToPersianWithoutChangingOtherCharacters() {
        assertEquals("نسخه ۲۳٫۵", AppRules.persianDigits("نسخه 23٫5"))
        assertEquals("ABC۱۲", AppRules.persianDigits("ABC12"))
    }

    @Test fun convertsPersianAndArabicDigitsToLatin() {
        assertEquals("Order 123", AppRules.latinDigits("Order ۱۲۳"))
        assertEquals("123", AppRules.latinDigits("١٢٣"))
    }

    @Test fun jalaliMonthLengthsFollowCalendarRules() {
        (1..6).forEach { assertEquals(31, AppRules.jalaliDaysInMonth(1403, it)) }
        (7..11).forEach { assertEquals(30, AppRules.jalaliDaysInMonth(1403, it)) }
        assertEquals(30, AppRules.jalaliDaysInMonth(1403, 12))
        assertEquals(29, AppRules.jalaliDaysInMonth(1402, 12))
    }

    @Test fun jalaliMonthRejectsInvalidInput() {
        assertThrows(IllegalArgumentException::class.java) { AppRules.jalaliDaysInMonth(1403, 0) }
        assertThrows(IllegalArgumentException::class.java) { AppRules.jalaliDaysInMonth(1403, 13) }
    }

    @Test fun buildsInstagramUrlFromUsername() {
        assertEquals("https://www.instagram.com/fastlove_user/", AppRules.socialProfileUrl("instagram", " @fastlove_user "))
    }

    @Test fun preservesExistingSocialProfileUrl() {
        val url = "HTTPS://example.com/profile/"
        assertEquals(url, AppRules.socialProfileUrl("instagram", url))
    }

    @Test fun buildsSpotifyUrlFromUserId() {
        assertEquals("https://open.spotify.com/user/user123", AppRules.socialProfileUrl("spotify", " /user123/ "))
    }

    @Test fun detectsConversationTopicsAfterTrimmingAndCaseNormalization() {
        assertEquals("greeting", AppRules.conversationTopic("  سلام، خوبی؟  "))
        assertEquals("music", AppRules.conversationTopic("I use SPOTIFY"))
        assertEquals("travel", AppRules.conversationTopic("برای تعطیلات سفر می‌رم"))
        assertEquals("cafe", AppRules.conversationTopic("قهوه و کافه"))
        assertEquals("book", AppRules.conversationTopic("مطالعه رمان"))
        assertEquals("movie", AppRules.conversationTopic("سینما"))
        assertEquals("work", AppRules.conversationTopic("دانشگاه و شغل"))
        assertEquals("sport", AppRules.conversationTopic("باشگاه"))
        assertEquals("about_person", AppRules.conversationTopic("از خودت بگو"))
        assertEquals("general", AppRules.conversationTopic("پیام نامرتبط"))
    }
}
