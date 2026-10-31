package ir.fastlove.app.core.domain

import ir.fastlove.app.core.model.Gender
import ir.fastlove.app.core.model.InteractionState
import ir.fastlove.app.core.model.LookingForGender
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.UserProfile

/** The map filter the user can switch between. */
enum class MapInteractionFilter(val title: String) {
    ALL("همه"),
    UNSEEN("بررسی‌نشده"),
    LIKED("لایک‌شده"),
    DISLIKED("دیسلایک‌شده")
}

data class MapMarker(
    val person: Person,
    val interaction: InteractionState,
    val roundedLatitude: Double,
    val roundedLongitude: Double
)

object MapLogic {

    /** Approximate public coordinates — never a precise address (rounded to ~1km grid). */
    fun privacyRound(value: Double): Double = (value * 100.0).toLong() / 100.0

    private fun matchesGenderPreference(viewer: UserProfile, person: Person): Boolean =
        when (viewer.lookingFor ?: LookingForGender.BOTH) {
            LookingForGender.FEMALE -> person.gender == Gender.FEMALE
            LookingForGender.MALE -> person.gender == Gender.MALE
            LookingForGender.BOTH -> true
        }

    /**
     * Builds the visible markers: honours the viewer's gender preference, removes blocked people,
     * and tags each with the viewer's own like/dislike state (visible only to the viewer).
     */
    fun markers(
        viewer: UserProfile,
        people: List<Person>,
        blockedIds: Set<Int>,
        likedIds: Set<Int>,
        dislikedIds: Set<Int>,
        filter: MapInteractionFilter
    ): List<MapMarker> = people.asSequence()
        .filter { it.id !in blockedIds }
        .filter { matchesGenderPreference(viewer, it) }
        .map { person ->
            val interaction = when {
                person.id in likedIds -> InteractionState.LIKED
                person.id in dislikedIds -> InteractionState.DISLIKED
                else -> InteractionState.NONE
            }
            MapMarker(person, interaction, privacyRound(person.latitude), privacyRound(person.longitude))
        }
        .filter { marker ->
            when (filter) {
                MapInteractionFilter.ALL -> true
                MapInteractionFilter.UNSEEN -> marker.interaction == InteractionState.NONE
                MapInteractionFilter.LIKED -> marker.interaction == InteractionState.LIKED
                MapInteractionFilter.DISLIKED -> marker.interaction == InteractionState.DISLIKED
            }
        }
        .toList()

    /** At far zoom we cluster; closer than this we show individual avatars. */
    fun shouldCluster(zoom: Double): Boolean = zoom < 11.0
}
