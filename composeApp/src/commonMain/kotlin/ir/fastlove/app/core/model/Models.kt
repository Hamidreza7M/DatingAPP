package ir.fastlove.app.core.model

/**
 * Domain models — pure Kotlin, free of Compose / platform types so they can be unit-tested
 * in commonTest and reused by a future real backend without touching the UI.
 */

import kotlinx.serialization.Serializable

@Serializable
enum class Gender { MALE, FEMALE, OTHER }

@Serializable
enum class Orientation { STRAIGHT, GAY, LESBIAN, BISEXUAL, OTHER }

@Serializable
enum class LookingForGender(val title: String) {
    FEMALE("دختر"), MALE("پسر"), BOTH("هر دو جنس")
}

@Serializable
enum class MsgType { TEXT, IMAGE, VOICE }

@Serializable
enum class RelationGoal(val title: String) {
    MARRIAGE("ازدواج"),
    FRIENDSHIP("دوستی ساده"),
    CASUAL("قرار عادی"),
    PARTNER("پارتنر")
}

@Serializable
enum class SubscriptionPlan(val title: String, val dailySwipes: Int?) {
    FREE("رایگان", 10), PLUS("پلاس", 50), VIP("وی‌آی‌پی", null)
}

@Serializable
data class UserProfile(
    val name: String = "",
    val birthYearPersian: Int = 1380,
    val birthMonth: Int = 1,
    val gender: Gender = Gender.OTHER,
    val lookingFor: LookingForGender? = LookingForGender.BOTH,
    val goal: RelationGoal = RelationGoal.FRIENDSHIP,
    val job: String = "",
    val city: String = "",
    val about: String = "",
    val interests: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val primaryPhotoIndex: Int = 0,
    val phone: String = "",
    val voiceIntro: String = "",
    val instagram: String = "",
    val spotify: String = "",
    val idealFirstDate: String = "",
    val makesMeSmile: String = "",
    val relationshipValue: String = "",
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val showOnMap: Boolean = true,
    val hiddenFromMap: Boolean = false,
    val referralCode: String = "",
    val referredByCode: String = ""
) {
    fun ageFor(currentPersianYear: Int): Int = (currentPersianYear - birthYearPersian).coerceAtLeast(18)

    /** 0f..1f completeness score for the profile strength meter. */
    val completeness: Float
        get() {
            val checks = listOf(
                name.isNotBlank(),
                job.isNotBlank(),
                city.isNotBlank(),
                about.isNotBlank(),
                interests.isNotEmpty(),
                photos.isNotEmpty(),
                idealFirstDate.isNotBlank(),
                makesMeSmile.isNotBlank(),
                relationshipValue.isNotBlank(),
                lookingFor != null
            )
            return checks.count { it }.toFloat() / checks.size
        }
}

data class Person(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: Gender,
    val orientation: Orientation,
    val goal: RelationGoal = RelationGoal.FRIENDSHIP,
    val job: String,
    val city: String,
    val about: String,
    val interests: List<String>,
    val emoji: String,
    val tintArgb: Long,
    val distanceKm: Int,
    val likedYou: Boolean = false,
    val instagram: String = "",
    val spotify: String = "",
    val idealFirstDate: String = "",
    val makesMeSmile: String = "",
    val relationshipValue: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isOnline: Boolean = ((id * 37) % 10) < 4,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val lastSeenMinutesAgo: Int = 7 + ((id * 29) % 420)
)

@Serializable
data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val timeMillis: Long,
    val type: MsgType = MsgType.TEXT,
    val read: Boolean = false,
    val reaction: String? = null
)

@Serializable
enum class DateKind(val title: String, val emoji: String) {
    CAFE("کافه", "☕"),
    RESTAURANT("رستوران", "🍽️"),
    WALK("پیاده‌روی", "🚶"),
    CINEMA("سینما", "🎬"),
    GALLERY("گالری و موزه", "🖼️"),
    ACTIVITY("فعالیت تفریحی", "🎯")
}

@Serializable
enum class DateProposalStatus(val title: String) {
    DRAFT("پیش‌نویس"),
    PENDING("در انتظار پاسخ"),
    ACCEPTED("تأییدشده"),
    DECLINED("ردشده"),
    COUNTER("پیشنهاد اصلاح"),
    CANCELLED("لغوشده")
}

@Serializable
data class DateProposal(
    val id: String,
    val personId: Int,
    val kind: DateKind = DateKind.CAFE,
    val place: String,
    val scheduledAtMillis: Long,
    val proposerIsMe: Boolean,
    val status: DateProposalStatus = DateProposalStatus.PENDING,
    val createdAtMillis: Long,
    val isRevision: Boolean = false,
    val revisesProposalId: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val alternativeTimesMillis: List<Long> = emptyList()
)

@Serializable
data class PaymentRecord(
    val planTitle: String,
    val priceToman: Int,
    val purchasedAtMillis: Long,
    val expiresAtMillis: Long,
    val durationDays: Int = 0
)

@Serializable
data class FilterState(
    val minAge: Int = 18,
    val maxAge: Int = 45,
    val jobs: Set<String> = emptySet(),
    val interests: Set<String> = emptySet(),
    val maxDistance: Int = 50,
    val cities: Set<String> = emptySet(),
    val goals: Set<RelationGoal> = emptySet()
)

data class ReportReason(val id: String, val title: String)

/** The interaction state a viewer has with a person (for map markers & filters). */
enum class InteractionState { NONE, LIKED, DISLIKED }

data class MapCity(val name: String, val county: String, val latitude: Double, val longitude: Double)
data class MapArea(val name: String, val latitude: Double, val longitude: Double)
