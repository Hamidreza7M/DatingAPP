package ir.fastlove.app.core.data

import com.russhwolf.settings.Settings
import ir.fastlove.app.core.domain.DemoReply
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.model.ChatMessage
import ir.fastlove.app.core.model.DateProposal
import ir.fastlove.app.core.model.DateProposalStatus
import ir.fastlove.app.core.model.FilterState
import ir.fastlove.app.core.model.MsgType
import ir.fastlove.app.core.model.PaymentRecord
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.SubscriptionPlan
import ir.fastlove.app.core.model.UserProfile
import ir.fastlove.app.core.util.Ids
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * In-memory + Settings-backed implementation. All mutations update a StateFlow (so Compose
 * recomposes) and then persist to the key-value store so demo state survives app restarts.
 */
class MockFastLoveRepository(
    private val settings: Settings = provideSettings(),
    override val people: List<Person> = Seed.people
) : FastLoveRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _session = MutableStateFlow(loadSession())
    override val session: StateFlow<SessionState> = _session.asStateFlow()

    private val _user = MutableStateFlow(loadUser())
    override val user: StateFlow<UserProfile> = _user.asStateFlow()

    private val _interactions = MutableStateFlow(loadInteractions())
    override val interactions: StateFlow<InteractionState> = _interactions.asStateFlow()

    private val _subscription = MutableStateFlow(loadSubscription())
    override val subscription: StateFlow<SubscriptionState> = _subscription.asStateFlow()

    private val _filter = MutableStateFlow(loadFilter())
    override val filter: StateFlow<FilterState> = _filter.asStateFlow()

    private val chatFlows = mutableMapOf<Int, MutableStateFlow<List<ChatMessage>>>()
    private val dateFlows = mutableMapOf<Int, MutableStateFlow<List<DateProposal>>>()

    private fun personById(id: Int): Person? = people.firstOrNull { it.id == id }

    // ── Session / onboarding ────────────────────────────────────────────────
    override fun loginDemo(phone: String) {
        _user.value = _user.value.copy(phone = phone, referralCode = _user.value.referralCode.ifBlank { Ids.referralCode() })
        persistUser()
        _session.value = _session.value.copy(isLoggedIn = true)
        persistSession()
    }

    override fun completeProfile(profile: UserProfile) {
        _user.value = profile.copy(referralCode = profile.referralCode.ifBlank { Ids.referralCode() })
        persistUser()
        _session.value = _session.value.copy(isLoggedIn = true, hasProfile = true)
        persistSession()
    }

    override fun updateProfile(profile: UserProfile) {
        _user.value = profile
        persistUser()
    }

    override fun markTourSeen() {
        _session.value = _session.value.copy(tourSeen = true)
        persistSession()
    }

    override fun logout() {
        _session.value = _session.value.copy(isLoggedIn = false)
        persistSession()
    }

    // ── Discover interactions ───────────────────────────────────────────────
    override fun like(personId: Int): Boolean {
        val state = _interactions.value
        if (personId in state.likedIds) return personId in state.matchedIds
        val person = personById(personId)
        val becomesMatch = person?.likedYou == true
        _interactions.value = state.copy(
            likedIds = state.likedIds + personId,
            dislikedIds = state.dislikedIds - personId,
            matchedIds = if (becomesMatch) state.matchedIds + personId else state.matchedIds
        )
        persistInteractions()
        if (becomesMatch && person != null) seedOpeningMessage(person)
        return becomesMatch
    }

    override fun dislike(personId: Int) {
        val state = _interactions.value
        if (personId in state.dislikedIds) return
        _interactions.value = state.copy(
            dislikedIds = state.dislikedIds + personId,
            likedIds = state.likedIds - personId
        )
        persistInteractions()
    }

    override fun undoDislike(personId: Int) {
        val state = _interactions.value
        _interactions.value = state.copy(dislikedIds = state.dislikedIds - personId)
        persistInteractions()
    }

    override fun block(personId: Int) {
        val state = _interactions.value
        _interactions.value = state.copy(
            blockedIds = state.blockedIds + personId,
            matchedIds = state.matchedIds - personId
        )
        persistInteractions()
    }

    override fun unblock(personId: Int) {
        val state = _interactions.value
        _interactions.value = state.copy(blockedIds = state.blockedIds - personId)
        persistInteractions()
    }

    // ── Filters ─────────────────────────────────────────────────────────────
    override fun applyFilter(filter: FilterState) {
        _filter.value = filter
        settings.putString(KEY_FILTER, json.encodeToString(FilterState.serializer(), filter))
    }

    // ── Chat ────────────────────────────────────────────────────────────────
    override fun messages(personId: Int): StateFlow<List<ChatMessage>> =
        chatFlow(personId).asStateFlow()

    private fun chatFlow(personId: Int): MutableStateFlow<List<ChatMessage>> =
        chatFlows.getOrPut(personId) { MutableStateFlow(loadChat(personId)) }

    override fun sendMessage(personId: Int, message: ChatMessage) {
        val flow = chatFlow(personId)
        flow.value = flow.value + message
        persistChat(personId, flow.value)
    }

    override fun reactTo(personId: Int, index: Int, reaction: String?) {
        val flow = chatFlow(personId)
        val list = flow.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(reaction = reaction)
            flow.value = list
            persistChat(personId, list)
        }
    }

    private fun seedOpeningMessage(person: Person) {
        val flow = chatFlow(person.id)
        if (flow.value.isEmpty()) {
            val opening = ChatMessage(
                text = DemoReply.openingMessage(person),
                isMe = false,
                timeMillis = FastLoveClock.nowMillis(),
                type = MsgType.TEXT
            )
            flow.value = listOf(opening)
            persistChat(person.id, flow.value)
        }
    }

    // ── Dates ───────────────────────────────────────────────────────────────
    override fun dates(personId: Int): StateFlow<List<DateProposal>> = dateFlow(personId).asStateFlow()

    private fun dateFlow(personId: Int): MutableStateFlow<List<DateProposal>> =
        dateFlows.getOrPut(personId) { MutableStateFlow(loadDates(personId)) }

    override fun proposeDate(proposal: DateProposal) {
        val flow = dateFlow(proposal.personId)
        flow.value = flow.value + proposal
        persistDates(proposal.personId, flow.value)
    }

    override fun updateDate(personId: Int, proposal: DateProposal) {
        val flow = dateFlow(personId)
        flow.value = flow.value.map { if (it.id == proposal.id) proposal else it }
        persistDates(personId, flow.value)
    }

    override fun replaceDates(personId: Int, proposals: List<DateProposal>) {
        val flow = dateFlow(personId)
        flow.value = proposals
        persistDates(personId, proposals)
    }

    override fun allActiveDates(): List<Pair<Person, DateProposal>> {
        val result = mutableListOf<Pair<Person, DateProposal>>()
        for (person in people) {
            val proposals = loadDates(person.id)
            proposals.filter {
                it.status in setOf(DateProposalStatus.PENDING, DateProposalStatus.ACCEPTED, DateProposalStatus.COUNTER)
            }.forEach { result += person to it }
        }
        return result.sortedBy { it.second.scheduledAtMillis }
    }

    // ── Subscription (simulated) ─────────────────────────────────────────────
    override fun purchase(plan: SubscriptionPlan, priceToman: Int, durationDays: Int) {
        val now = FastLoveClock.nowMillis()
        val expiry = now + durationDays.toLong() * 24 * 60 * 60 * 1000
        val record = PaymentRecord(plan.title, priceToman, now, expiry, durationDays)
        val state = _subscription.value
        _subscription.value = state.copy(
            plan = plan,
            expiryMillis = expiry,
            payments = state.payments + record,
            messageCredits = state.messageCredits + 10
        )
        persistSubscription()
    }

    // ── Demo lifecycle ───────────────────────────────────────────────────────
    override fun resetDemo() {
        settings.clear()
        chatFlows.values.forEach { it.value = emptyList() }
        dateFlows.values.forEach { it.value = emptyList() }
        _session.value = SessionState()
        _user.value = UserProfile()
        _interactions.value = InteractionState()
        _subscription.value = SubscriptionState()
        _filter.value = FilterState()
    }

    // ── Persistence helpers ───────────────────────────────────────────────────
    private val intListSerializer = ListSerializer(Int.serializer())

    private fun persistSession() {
        settings.putBoolean(KEY_LOGGED_IN, _session.value.isLoggedIn)
        settings.putBoolean(KEY_HAS_PROFILE, _session.value.hasProfile)
        settings.putBoolean(KEY_TOUR_SEEN, _session.value.tourSeen)
    }

    private fun loadSession() = SessionState(
        isLoggedIn = settings.getBoolean(KEY_LOGGED_IN, false),
        hasProfile = settings.getBoolean(KEY_HAS_PROFILE, false),
        tourSeen = settings.getBoolean(KEY_TOUR_SEEN, false)
    )

    private fun persistUser() {
        settings.putString(KEY_USER, json.encodeToString(UserProfile.serializer(), _user.value))
    }

    private fun loadUser(): UserProfile =
        settings.getStringOrNull(KEY_USER)?.let {
            runCatching { json.decodeFromString(UserProfile.serializer(), it) }.getOrNull()
        } ?: UserProfile()

    private fun persistInteractions() {
        val s = _interactions.value
        settings.putString(KEY_LIKED, encodeInts(s.likedIds))
        settings.putString(KEY_DISLIKED, encodeInts(s.dislikedIds))
        settings.putString(KEY_BLOCKED, encodeInts(s.blockedIds))
        settings.putString(KEY_MATCHED, encodeInts(s.matchedIds))
        settings.putString(KEY_PAID_CHAT, encodeInts(s.paidChatIds))
    }

    private fun loadInteractions() = InteractionState(
        likedIds = decodeInts(KEY_LIKED),
        dislikedIds = decodeInts(KEY_DISLIKED),
        blockedIds = decodeInts(KEY_BLOCKED),
        matchedIds = decodeInts(KEY_MATCHED),
        paidChatIds = decodeInts(KEY_PAID_CHAT)
    )

    private fun persistSubscription() {
        val s = _subscription.value
        settings.putString(KEY_PLAN, s.plan.name)
        s.expiryMillis?.let { settings.putLong(KEY_PLAN_EXPIRY, it) } ?: settings.remove(KEY_PLAN_EXPIRY)
        settings.putInt(KEY_CREDITS, s.messageCredits)
        settings.putString(KEY_PAYMENTS, json.encodeToString(ListSerializer(PaymentRecord.serializer()), s.payments))
    }

    private fun loadSubscription(): SubscriptionState {
        val plan = settings.getStringOrNull(KEY_PLAN)?.let { name ->
            runCatching { SubscriptionPlan.valueOf(name) }.getOrNull()
        } ?: SubscriptionPlan.FREE
        val expiry = if (settings.hasKey(KEY_PLAN_EXPIRY)) settings.getLong(KEY_PLAN_EXPIRY, 0L) else null
        val payments = settings.getStringOrNull(KEY_PAYMENTS)?.let {
            runCatching { json.decodeFromString(ListSerializer(PaymentRecord.serializer()), it) }.getOrNull()
        } ?: emptyList()
        return SubscriptionState(plan, expiry, payments, settings.getInt(KEY_CREDITS, 3))
    }

    private fun loadFilter(): FilterState =
        settings.getStringOrNull(KEY_FILTER)?.let {
            runCatching { json.decodeFromString(FilterState.serializer(), it) }.getOrNull()
        } ?: FilterState()

    private fun persistChat(personId: Int, messages: List<ChatMessage>) {
        settings.putString("chat_$personId", json.encodeToString(ListSerializer(ChatMessage.serializer()), messages))
    }

    private fun loadChat(personId: Int): List<ChatMessage> =
        settings.getStringOrNull("chat_$personId")?.let {
            runCatching { json.decodeFromString(ListSerializer(ChatMessage.serializer()), it) }.getOrNull()
        } ?: emptyList()

    private fun persistDates(personId: Int, proposals: List<DateProposal>) {
        settings.putString("dates_$personId", json.encodeToString(ListSerializer(DateProposal.serializer()), proposals))
    }

    private fun loadDates(personId: Int): List<DateProposal> =
        settings.getStringOrNull("dates_$personId")?.let {
            runCatching { json.decodeFromString(ListSerializer(DateProposal.serializer()), it) }.getOrNull()
        } ?: emptyList()

    private fun encodeInts(set: Set<Int>) = json.encodeToString(intListSerializer, set.toList())
    private fun decodeInts(key: String): Set<Int> =
        settings.getStringOrNull(key)?.let {
            runCatching { json.decodeFromString(intListSerializer, it).toSet() }.getOrNull()
        } ?: emptySet()

    private companion object {
        const val KEY_LOGGED_IN = "isLoggedIn"
        const val KEY_HAS_PROFILE = "hasProfile"
        const val KEY_TOUR_SEEN = "tourSeen"
        const val KEY_USER = "user"
        const val KEY_LIKED = "liked"
        const val KEY_DISLIKED = "disliked"
        const val KEY_BLOCKED = "blocked"
        const val KEY_MATCHED = "matched"
        const val KEY_PAID_CHAT = "paidChat"
        const val KEY_PLAN = "plan"
        const val KEY_PLAN_EXPIRY = "planExpiry"
        const val KEY_CREDITS = "credits"
        const val KEY_PAYMENTS = "payments"
        const val KEY_FILTER = "filter"
    }
}
