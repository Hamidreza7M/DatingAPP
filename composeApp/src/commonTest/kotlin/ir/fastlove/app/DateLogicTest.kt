package ir.fastlove.app

import ir.fastlove.app.core.domain.DateLogic
import ir.fastlove.app.core.model.DateProposal
import ir.fastlove.app.core.model.DateProposalStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateLogicTest {

    private fun proposal(
        status: DateProposalStatus = DateProposalStatus.PENDING,
        mine: Boolean = true
    ) = DateProposal(
        id = "p1", personId = 1, place = "کافه", scheduledAtMillis = 1000L,
        proposerIsMe = mine, status = status, createdAtMillis = 0L
    )

    @Test
    fun selectable_day_window() {
        assertTrue(DateLogic.isSelectableDay(dayEpochDays = 100, todayEpochDays = 100))
        assertTrue(DateLogic.isSelectableDay(190, 100))     // +90 days
        assertFalse(DateLogic.isSelectableDay(99, 100))     // past
        assertFalse(DateLogic.isSelectableDay(191, 100))    // beyond window
    }

    @Test
    fun recipient_can_accept_pending() {
        assertTrue(DateLogic.canAccept(proposal(mine = false)))
        assertFalse(DateLogic.canAccept(proposal(mine = true)))
    }

    @Test
    fun cancel_transitions() {
        assertEquals(DateProposalStatus.CANCELLED, DateLogic.cancel(proposal()).status)
    }

    @Test
    fun accept_transitions() {
        assertEquals(DateProposalStatus.ACCEPTED, DateLogic.accept(proposal()).status)
    }

    @Test
    fun counter_keeps_original_until_reaccept() {
        val original = proposal(status = DateProposalStatus.ACCEPTED)
        val (revised, newProposal) = DateLogic.counter(original, newId = "p2", newTimeMillis = 2000L, nowMillis = 50L)
        assertEquals(DateProposalStatus.COUNTER, revised.status)
        assertEquals(DateProposalStatus.PENDING, newProposal.status)
        assertEquals("p1", newProposal.revisesProposalId)
        assertEquals(2000L, newProposal.scheduledAtMillis)
        assertTrue(newProposal.isRevision)
    }

    @Test
    fun cancelled_is_not_active() {
        assertFalse(DateLogic.isActive(proposal(status = DateProposalStatus.CANCELLED)))
        assertTrue(DateLogic.isActive(proposal(status = DateProposalStatus.PENDING)))
    }
}
