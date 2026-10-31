package ir.fastlove.app

import ir.fastlove.app.core.domain.MapInteractionFilter
import ir.fastlove.app.core.domain.MapLogic
import ir.fastlove.app.core.model.Gender
import ir.fastlove.app.core.model.InteractionState
import ir.fastlove.app.core.model.LookingForGender
import ir.fastlove.app.core.model.Orientation
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapLogicTest {

    private fun p(id: Int, gender: Gender) = Person(
        id = id, name = "p$id", age = 25, gender = gender, orientation = Orientation.STRAIGHT,
        job = "آزاد", city = "تهران", about = "", interests = emptyList(), emoji = "🌟",
        tintArgb = 0xFFFFFFFF, distanceKm = 5, latitude = 35.7, longitude = 51.4
    )

    private val people = listOf(p(1, Gender.FEMALE), p(2, Gender.MALE), p(3, Gender.FEMALE), p(4, Gender.MALE))

    @Test
    fun gender_preference_is_applied() {
        val viewer = UserProfile(lookingFor = LookingForGender.FEMALE)
        val markers = MapLogic.markers(viewer, people, emptySet(), emptySet(), emptySet(), MapInteractionFilter.ALL)
        assertTrue(markers.all { it.person.gender == Gender.FEMALE })
        assertEquals(2, markers.size)
    }

    @Test
    fun blocked_people_are_excluded() {
        val viewer = UserProfile(lookingFor = LookingForGender.BOTH)
        val markers = MapLogic.markers(viewer, people, blockedIds = setOf(1), emptySet(), emptySet(), MapInteractionFilter.ALL)
        assertTrue(markers.none { it.person.id == 1 })
    }

    @Test
    fun interaction_state_is_tagged() {
        val viewer = UserProfile(lookingFor = LookingForGender.BOTH)
        val markers = MapLogic.markers(viewer, people, emptySet(), likedIds = setOf(1), dislikedIds = setOf(2), MapInteractionFilter.ALL)
        assertEquals(InteractionState.LIKED, markers.first { it.person.id == 1 }.interaction)
        assertEquals(InteractionState.DISLIKED, markers.first { it.person.id == 2 }.interaction)
        assertEquals(InteractionState.NONE, markers.first { it.person.id == 3 }.interaction)
    }

    @Test
    fun liked_filter_returns_only_liked() {
        val viewer = UserProfile(lookingFor = LookingForGender.BOTH)
        val markers = MapLogic.markers(viewer, people, emptySet(), setOf(1), setOf(2), MapInteractionFilter.LIKED)
        assertEquals(1, markers.size)
        assertEquals(1, markers.first().person.id)
    }

    @Test
    fun privacy_round_reduces_precision() {
        assertEquals(35.69, MapLogic.privacyRound(35.69439), 0.0001)
    }

    @Test
    fun clustering_threshold() {
        assertTrue(MapLogic.shouldCluster(9.0))
        assertTrue(!MapLogic.shouldCluster(13.0))
    }
}
