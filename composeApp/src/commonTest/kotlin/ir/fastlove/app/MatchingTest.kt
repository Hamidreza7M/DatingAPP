package ir.fastlove.app

import ir.fastlove.app.core.domain.Matching
import ir.fastlove.app.core.model.Gender
import ir.fastlove.app.core.model.LookingForGender
import ir.fastlove.app.core.model.Orientation
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.RelationGoal
import ir.fastlove.app.core.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchingTest {

    private fun person(
        interests: List<String> = listOf("کافه", "سفر", "موسیقی"),
        distance: Int = 10,
        age: Int = 28,
        job: String = "مهندس نرم‌افزار",
        goal: RelationGoal = RelationGoal.FRIENDSHIP,
        gender: Gender = Gender.FEMALE
    ) = Person(
        id = 1, name = "تست", age = age, gender = gender, orientation = Orientation.STRAIGHT,
        goal = goal, job = job, city = "تهران", about = "", interests = interests,
        emoji = "🌸", tintArgb = 0xFFFFE0E6, distanceKm = distance
    )

    private val viewer = UserProfile(
        birthYearPersian = 1375, interests = listOf("کافه", "سفر", "موسیقی"),
        job = "برنامه‌نویس", goal = RelationGoal.FRIENDSHIP
    )

    @Test
    fun score_is_in_range() {
        val s = Matching.score(viewer, person(), currentPersianYear = 1403)
        assertTrue(s in 0f..10f, "score should be within 0..10 but was $s")
    }

    @Test
    fun shared_interests_increase_score() {
        val many = Matching.score(viewer, person(interests = listOf("کافه", "سفر", "موسیقی")), 1403)
        val none = Matching.score(viewer, person(interests = listOf("ورزش", "بازی", "مد")), 1403)
        assertTrue(many > none, "more shared interests should score higher ($many vs $none)")
    }

    @Test
    fun closer_distance_scores_higher() {
        val near = Matching.score(viewer, person(distance = 2), 1403)
        val far = Matching.score(viewer, person(distance = 70), 1403)
        assertTrue(near > far)
    }

    @Test
    fun evaluate_lists_reasons() {
        val result = Matching.evaluate(viewer, person(goal = RelationGoal.FRIENDSHIP), 1403)
        assertTrue(result.reasons.isNotEmpty())
        assertTrue(result.reasons.any { it.contains("هدف رابطه") })
    }

    @Test
    fun looking_for_filters_by_gender() {
        val people = listOf(person(gender = Gender.FEMALE).copy(id = 1), person(gender = Gender.MALE).copy(id = 2))
        val onlyFemale = Matching.compatiblePeople(viewer.copy(lookingFor = LookingForGender.FEMALE), people)
        assertEquals(1, onlyFemale.size)
        assertEquals(Gender.FEMALE, onlyFemale.first().gender)
    }
}
