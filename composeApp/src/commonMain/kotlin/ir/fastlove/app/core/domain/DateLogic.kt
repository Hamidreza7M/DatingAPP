package ir.fastlove.app.core.domain

import ir.fastlove.app.core.model.DateProposal
import ir.fastlove.app.core.model.DateProposalStatus

/** Suggested time slots shown before the precise time picker. */
enum class TimeSlot(val title: String, val startHour: Int, val endHour: Int) {
    AFTERNOON("عصر", 16, 18),
    EVENING("غروب", 18, 20),
    NIGHT("شب", 20, 22)
}

object DateLogic {

    const val MAX_DAYS_AHEAD = 90

    /** A proposal date must be today or later, and within the booking window. */
    fun isSelectableDay(dayEpochDays: Int, todayEpochDays: Int): Boolean =
        dayEpochDays in todayEpochDays..(todayEpochDays + MAX_DAYS_AHEAD)

    /** Legal status transitions for the recipient / proposer. */
    fun canAccept(p: DateProposal): Boolean =
        !p.proposerIsMe && p.status in setOf(DateProposalStatus.PENDING, DateProposalStatus.COUNTER)

    fun canDecline(p: DateProposal): Boolean =
        !p.proposerIsMe && p.status in setOf(DateProposalStatus.PENDING, DateProposalStatus.COUNTER)

    fun canCancel(p: DateProposal): Boolean =
        p.status in setOf(DateProposalStatus.PENDING, DateProposalStatus.ACCEPTED, DateProposalStatus.COUNTER)

    /** Proposing a change never silently overwrites the prior accepted date; it needs re-acceptance. */
    fun canCounter(p: DateProposal): Boolean =
        p.status in setOf(DateProposalStatus.PENDING, DateProposalStatus.ACCEPTED)

    fun accept(p: DateProposal): DateProposal = p.copy(status = DateProposalStatus.ACCEPTED)
    fun decline(p: DateProposal): DateProposal = p.copy(status = DateProposalStatus.DECLINED)
    fun cancel(p: DateProposal): DateProposal = p.copy(status = DateProposalStatus.CANCELLED)

    /**
     * A counter-proposal is a NEW pending proposal that references the original; the original moves
     * to COUNTER and stays until the other side accepts the revision.
     */
    fun counter(original: DateProposal, newId: String, newTimeMillis: Long, nowMillis: Long): Pair<DateProposal, DateProposal> {
        val revised = original.copy(status = DateProposalStatus.COUNTER)
        val proposal = original.copy(
            id = newId,
            scheduledAtMillis = newTimeMillis,
            status = DateProposalStatus.PENDING,
            proposerIsMe = true,
            isRevision = true,
            revisesProposalId = original.id,
            createdAtMillis = nowMillis
        )
        return revised to proposal
    }

    fun isActive(p: DateProposal): Boolean =
        p.status in setOf(DateProposalStatus.PENDING, DateProposalStatus.ACCEPTED, DateProposalStatus.COUNTER)
}
