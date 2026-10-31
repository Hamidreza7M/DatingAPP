package ir.fastlove.app.core.domain

import ir.fastlove.app.core.model.Gender
import ir.fastlove.app.core.model.LookingForGender
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.UserProfile
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Explainable, local demo matching. The 0..10 score is NOT a scientific prediction — it is a
 * transparent weighting of a few factors, each of which also produces a human-readable reason.
 */
data class MatchResult(val score: Float, val reasons: List<String>)

object Matching {

    private val jobCategories = listOf(
        listOf("مهندس", "نرم‌افزار", "برنامه‌نویس", "کدنویس", "فناوری"),
        listOf("طراح", "عکاس", "محتوا", "هنر", "مد"),
        listOf("پزشک", "پرستار", "روانشناس", "سلامت"),
        listOf("معلم", "استاد", "دانشجو", "آموزش"),
        listOf("وکیل", "حقوق", "فروشنده", "فروش", "مدیر")
    )

    /** Filters sample people by the gender the viewer wants to meet (one direction of the preference). */
    fun compatiblePeople(viewer: UserProfile, people: List<Person>): List<Person> = people.filter { person ->
        when (viewer.lookingFor ?: LookingForGender.BOTH) {
            LookingForGender.FEMALE -> person.gender == Gender.FEMALE
            LookingForGender.MALE -> person.gender == Gender.MALE
            LookingForGender.BOTH -> true
        }
    }

    fun score(viewer: UserProfile, person: Person, currentPersianYear: Int): Float =
        evaluate(viewer, person, currentPersianYear).score

    /** Full evaluation producing the score and the reasons shown on the card. */
    fun evaluate(viewer: UserProfile, person: Person, currentPersianYear: Int): MatchResult {
        val reasons = mutableListOf<String>()

        val shared = viewer.interests.toSet().intersect(person.interests.toSet())
        val denominator = minOf(3, viewer.interests.size, person.interests.size).coerceAtLeast(1)
        val interestPoints = if (viewer.interests.isEmpty() || person.interests.isEmpty()) 0f
        else (shared.size.toFloat() / denominator).coerceIn(0f, 1f) * 4f
        if (shared.isNotEmpty()) reasons += "${shared.size} علاقه مشترک"

        val distancePoints = (2f * (1f - person.distanceKm / 80f)).coerceIn(0f, 2f)
        if (person.distanceKm <= 15) reasons += "فاصله نزدیک (${person.distanceKm} کیلومتر)"

        val viewerAge = viewer.ageFor(currentPersianYear)
        val ageDifference = abs(viewerAge - person.age)
        val agePoints = (2f * (1f - ageDifference / 20f)).coerceIn(0f, 2f)
        if (ageDifference <= 4) reasons += "بازه سنی نزدیک"

        if (viewer.goal == person.goal) reasons += "هدف رابطه مشابه (${person.goal.title})"

        val jobPoints = jobScore(viewer.job.trim(), person.job.trim())

        if (viewer.completeness >= 0.7f) {
            // complete profiles get a tiny, transparent nudge
        }

        val raw = (interestPoints + distancePoints + agePoints + jobPoints).coerceIn(0f, 10f)
        val rounded = (raw * 10f).roundToInt() / 10f

        if (reasons.isEmpty()) reasons += "پروفایل‌های تازه برای آشنایی"
        return MatchResult(rounded, reasons)
    }

    private fun jobScore(viewerJob: String, personJob: String): Float = when {
        viewerJob.isBlank() || personJob.isBlank() -> 0f
        viewerJob.equals(personJob, ignoreCase = true) -> 2f
        else -> {
            val sameCategory = jobCategories.any { group ->
                group.any { viewerJob.contains(it) } && group.any { personJob.contains(it) }
            }
            if (sameCategory) 1.2f else 0.4f
        }
    }
}
