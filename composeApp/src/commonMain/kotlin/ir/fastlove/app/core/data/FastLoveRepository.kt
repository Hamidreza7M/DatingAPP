package ir.fastlove.app.core.data

import ir.fastlove.app.core.model.ChatMessage
import ir.fastlove.app.core.model.DateProposal
import ir.fastlove.app.core.model.FilterState
import ir.fastlove.app.core.model.PaymentRecord
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.SubscriptionPlan
import ir.fastlove.app.core.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Single aggregate repository the whole app talks to. The UI depends only on this interface,
 * so the current [MockFastLoveRepository] can later be swapped for a real backend-backed
 * implementation without touching features.
 */
interface FastLoveRepository {
    val people: List<Person>

    val session: StateFlow<SessionState>
    val user: StateFlow<UserProfile>
    val interactions: StateFlow<InteractionState>
    val subscription: StateFlow<SubscriptionState>
    val filter: StateFlow<FilterState>

    // Session / onboarding
    fun loginDemo(phone: String)
    fun completeProfile(profile: UserProfile)
    fun updateProfile(profile: UserProfile)
    fun markTourSeen()
    fun logout()

    // Discover interactions
    fun like(personId: Int): Boolean   // returns true if it became a match
    fun dislike(personId: Int)
    fun undoDislike(personId: Int)
    fun block(personId: Int)
    fun unblock(personId: Int)

    // Filters
    fun applyFilter(filter: FilterState)

    // Chat
    fun messages(personId: Int): StateFlow<List<ChatMessage>>
    fun sendMessage(personId: Int, message: ChatMessage)
    fun reactTo(personId: Int, index: Int, reaction: String?)

    // Dates
    fun dates(personId: Int): StateFlow<List<DateProposal>>
    fun allActiveDates(): List<Pair<Person, DateProposal>>
    fun proposeDate(proposal: DateProposal)
    fun updateDate(personId: Int, proposal: DateProposal)
    fun replaceDates(personId: Int, proposals: List<DateProposal>)

    // Subscription (simulated)
    fun purchase(plan: SubscriptionPlan, priceToman: Int, durationDays: Int)

    // Demo lifecycle
    fun resetDemo()
}

data class SessionState(
    val isLoggedIn: Boolean = false,
    val hasProfile: Boolean = false,
    val tourSeen: Boolean = false
)

data class InteractionState(
    val likedIds: Set<Int> = emptySet(),
    val dislikedIds: Set<Int> = emptySet(),
    val blockedIds: Set<Int> = emptySet(),
    val matchedIds: Set<Int> = emptySet(),
    val paidChatIds: Set<Int> = emptySet()
)

data class SubscriptionState(
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val expiryMillis: Long? = null,
    val payments: List<PaymentRecord> = emptyList(),
    val messageCredits: Int = 3
)
