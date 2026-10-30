package ir.hamdam.demo

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.graphics.Bitmap
import android.graphics.Canvas as NativeCanvas
import android.graphics.RectF
import android.graphics.Paint as NativePaint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// ─── Font & Theme ─────────────────────────────────────────────────────────────
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

// FastLove visual system — warm coral, soft violet and layered midnight surfaces.
private val TinderPink = Color(0xFFFF648C)
private val TinderOrange = Color(0xFFFF9A78)
private val BrandViolet = Color(0xFF9B8CFF)
private val DarkBg = Color(0xFF0B1020)
private val DarkSurface = Color(0xFF151B2D)
private val DarkCard = Color(0xFF20283D)
private val TextWhite = Color(0xFFF9F8FF)
private val TextGray = Color(0xFFB9C1D8)
private val TextMuted = Color(0xFF818BA7)
private val SuccessGreen = Color(0xFF52D6A5)
private val GradientTinder = listOf(Color(0xFFFF648C), Color(0xFFFF8A83), Color(0xFF9B8CFF))

// ─── Data ─────────────────────────────────────────────────────────────────────
enum class Gender { MALE, FEMALE, OTHER }
enum class Orientation { STRAIGHT, GAY, LESBIAN, BISEXUAL, OTHER }
enum class LookingForGender(val title: String) {
    FEMALE("دختر"), MALE("پسر"), BOTH("هر دو جنس")
}
enum class MsgType { TEXT, IMAGE, VOICE }
enum class RelationGoal(val title: String) {
    MARRIAGE("ازدواج"),
    FRIENDSHIP("دوستی ساده"),
    CASUAL("قرار عادی"),
    PARTNER("پارتنر")
}

enum class SubscriptionPlan(val title: String, val dailySwipes: Int?) {
    FREE("رایگان", 10), PRO("پرو", 50), VIP("وی‌آی‌پی", null)
}

private fun formatToman(amount: Int): String =
    "%,d".format(amount).replace(',', '٬') + " تومان"

private fun currentPersianYear(): Int {
    val g = Calendar.getInstance().get(Calendar.YEAR)
    val m = Calendar.getInstance().get(Calendar.MONTH) + 1
    return if (m >= 3) g - 621 else g - 622
}

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
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val referralCode: String = "",
    val referredByCode: String = ""
) {
    val age: Int get() = (currentPersianYear() - birthYearPersian).coerceAtLeast(18)
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
    val tint: Color,
    val distanceKm: Int,
    val likedYou: Boolean = false,
    val instagram: String = "",
    val spotify: String = "",
    val isOnline: Boolean = ((id * 37) % 10) < 4,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val lastSeenMinutesAgo: Int = 7 + ((id * 29) % 420)
)

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val time: String,
    val type: MsgType = MsgType.TEXT
)

enum class DateProposalStatus { PENDING, ACCEPTED, DECLINED, CANCELLED }

data class DateProposal(
    val id: String,
    val personId: Int,
    val place: String,
    val scheduledAtMillis: Long,
    val proposerIsMe: Boolean,
    val status: DateProposalStatus = DateProposalStatus.PENDING,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isRevision: Boolean = false,
    val revisesProposalId: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null
)

data class PaymentRecord(
    val planTitle: String,
    val priceToman: Int,
    val purchasedAtMillis: Long,
    val expiresAtMillis: Long,
    val durationDays: Int = 0
)

private fun formatDatePersian(timestampMillis: Long): String =
    persianDigits(SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(timestampMillis)))

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

val REPORT_REASONS = listOf(
    ReportReason("fake", "پروفایل جعلی / عکس دیگران"),
    ReportReason("harassment", "آزار و اذیت یا پیام نامناسب"),
    ReportReason("spam", "اسپم یا تبلیغات"),
    ReportReason("underage", "احتمال زیر ۱۸ سال بودن"),
    ReportReason("other", "سایر موارد")
)

private val persianMonths = listOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")

private val iranianCities = listOf(
    "تهران", "کرج", "مشهد", "اصفهان", "شیراز", "تبریز", "اهواز", "قم",
    "کرمانشاه", "ارومیه", "رشت", "زاهدان", "همدان", "کرمان", "یزد",
    "اردبیل", "بندرعباس", "اراک", "اسلام‌شهر", "زنجان", "سنندج", "قزوین",
    "خرم‌آباد", "گرگان", "ساری", "بوشهر", "بیرجند", "ایلام", "سمنان", "یاسوج",
    "بجنورد", "شهرکرد", "کیش", "قشم"
)

private val availableInterests = listOf(
    "کتاب", "کافه", "سفر", "موسیقی", "فیلم", "ورزش", "عکاسی", "آشپزی",
    "هنر", "طبیعت", "یوگا", "کدنویسی", "رقص", "بازی", "مد", "حیوانات", "کوهنوردی"
)
private val availableJobs = listOf(
    "طراح گرافیک", "مهندس نرم‌افزار", "دانشجو", "معلم", "پزشک", "عکاس",
    "محتواساز", "برنامه‌نویس", "روانشناس", "آزاد", "وکیل", "پرستار", "معمار", "فروشنده"
)

private val femaleNames = listOf(
    "رها","سارا","نیلوفر","مهسا","آتنا","پریا","لیلا","نازنین","هستی","یاسمن",
    "مهدیس","گلناز","شیما","فرناز","الهام","مینا","سپیده","زهرا","فاطمه","مریم",
    "نگار","آیدا","سحر","پگاه","دنیا","کیمیا","رویا","بهار","شیدا","نرگس",
    "آرزو","ترانه","مهتاب","پریسا","ساناز","طناز","غزل","هانیه","مرجان","سمیرا"
)
private val maleNames = listOf(
    "آرمین","کاوه","نوید","امیر","رضا","حسین","علی","محمد","سینا","پارسا",
    "آرش","کیان","مانی","دانیال","نیما","بهراد","سپهر","پویا","سامان","فرهاد",
    "مهدی","سعید","بابک","کامران","شهاب","یوسف","ایمان","رامین","بهنام","کوروش"
)
private val emojisF = listOf("🌷","🌿","🌼","🪻","✨","🌙","🔥","🌸","🌺","🌹","💖","🦋","🌻","🍀","🎀")
private val emojisM = listOf("🏔️","📷","🩺","🎸","⚽","🎯","🚀","💻","📚","🎬","🏋️","🎮","☕","🌊","🦊")
private val tintsF = listOf(0xFFFFE0E6,0xFFE0F2E9,0xFFFFF0D6,0xFFEDE0FF,0xFFFFE8D6,0xFFE0E8FF,0xFFFFE0CC,0xFFFFE4EC,0xFFE8F5E9,0xFFF3E5F5)
private val tintsM = listOf(0xFFD6EAF8,0xFFF5D6E0,0xFFD5F5E3,0xFFE8DAEF,0xFFD6EAF8,0xFFFADBD8,0xFFD5F5E3,0xFFD4E6F1,0xFFFCF3CF,0xFFE8F8F5)
private val abouts = listOf(
    "اهل کافه‌گردی و کتاب. دنبال آشنایی واقعی.",
    "طبیعت‌گردی و موسیقی حال من را خوب می‌کند.",
    "کنجکاوم دنیا را بیشتر بشناسم.",
    "رابطه‌ای می‌خواهم که در آن هر دو رشد کنیم.",
    "عاشق ساخت ویدیو و داستان‌گویی.",
    "گوش دادن خوب بلدم و عاشق گفت‌وگوهای عمیقم.",
    "عاشق کوهنوردی و قهوه.",
    "زندگی را از پشت لنز می‌بینم.",
    "پر از انرژی و عاشق ماجراجویی.",
    "به دنبال آرامش و ارتباط عمیق."
)

/** 100 demo people generated deterministically */
private val samplePeople: List<Person> = buildList {
    var id = 1
    // 55 female
    repeat(55) { i ->
        val name = femaleNames[i % femaleNames.size] + if (i >= femaleNames.size) " ${i / femaleNames.size + 1}" else ""
        val orient = when (i % 5) {
            0, 1, 2 -> Orientation.STRAIGHT
            3 -> Orientation.BISEXUAL
            else -> Orientation.LESBIAN
        }
        add(Person(
            id = id++,
            name = name,
            age = 20 + (i % 18),
            gender = Gender.FEMALE,
            orientation = orient,
            job = availableJobs[i % availableJobs.size],
            city = iranianCities[i % iranianCities.size],
            about = abouts[i % abouts.size],
            interests = availableInterests.shuffled().take(3 + i % 3),
            emoji = emojisF[i % emojisF.size],
            tint = Color(tintsF[i % tintsF.size]),
            distanceKm = 1 + (i * 3) % 45,
            likedYou = i % 3 == 0,
            instagram = if (i % 3 != 0) "fastlove_${id - 1}" else "",
            spotify = if (i % 4 == 0) "fastlove_user${id - 1}" else ""
        ))
    }
    // 40 male
    repeat(40) { i ->
        val name = maleNames[i % maleNames.size] + if (i >= maleNames.size) " ${i / maleNames.size + 1}" else ""
        val orient = when (i % 5) {
            0, 1, 2 -> Orientation.STRAIGHT
            3 -> Orientation.GAY
            else -> Orientation.BISEXUAL
        }
        add(Person(
            id = id++,
            name = name,
            age = 21 + (i % 20),
            gender = Gender.MALE,
            orientation = orient,
            job = availableJobs[(i + 3) % availableJobs.size],
            city = iranianCities[(i + 5) % iranianCities.size],
            about = abouts[(i + 2) % abouts.size],
            interests = availableInterests.shuffled().take(3 + i % 4),
            emoji = emojisM[i % emojisM.size],
            tint = Color(tintsM[i % tintsM.size]),
            distanceKm = 2 + (i * 5) % 48,
            likedYou = i % 4 == 0,
            instagram = if (i % 4 != 1) "fastlove_${id - 1}" else "",
            spotify = if (i % 3 == 0) "fastlove_user${id - 1}" else ""
        ))
    }
    // 5 other
    repeat(5) { i ->
        add(Person(
            id = id++,
            name = listOf("آریا","سام","رها","کیان","نیکا")[i],
            age = 24 + i,
            gender = Gender.OTHER,
            orientation = Orientation.OTHER,
            job = availableJobs[i],
            city = iranianCities[i * 3],
            about = abouts[i],
            interests = availableInterests.take(4),
            emoji = "⭐",
            tint = Color(0xFFE0E0E0),
            distanceKm = 5 + i * 4,
            likedYou = i % 2 == 0,
            instagram = if (i % 2 == 0) "fastlove_${id - 1}" else "",
            spotify = if (i % 2 == 1) "fastlove_user${id - 1}" else ""
        ))
    }
}

/** Fifty stable demo profiles distributed across the Tehran urban area. */
private data class MapLocatedPerson(val person: Person, val latitude: Double, val longitude: Double)

private val tehranMapDemoPeople: List<Person> = buildList {
    val women = samplePeople.filter { it.gender == Gender.FEMALE }.take(25)
    val men = samplePeople.filter { it.gender == Gender.MALE }.take(25)
    (women + men).forEach { add(it.copy(city = "تهران")) }
}

private val tehranMapDemoLocations: List<MapLocatedPerson> = run {
    val random = java.util.Random(20261030L)
    tehranMapDemoPeople.map { person ->
        // Stable random-looking coordinates inside Tehran's approximate urban bounds.
        MapLocatedPerson(
            person = person,
            latitude = 35.565 + random.nextDouble() * 0.265,
            longitude = 51.185 + random.nextDouble() * 0.445
        )
    }
}

/** Filter sample profiles by the gender the user wants to meet. */
private fun compatiblePeople(viewer: UserProfile): List<Person> = samplePeople.filter { person ->
    when (viewer.lookingFor ?: LookingForGender.BOTH) {
        LookingForGender.FEMALE -> person.gender == Gender.FEMALE
        LookingForGender.MALE -> person.gender == Gender.MALE
        LookingForGender.BOTH -> true
    }
}

// ─── Local persistence ────────────────────────────────────────────────────────
private const val PREFS = "fastlove_prefs"
private val gson = Gson()

private fun generateReferralCode(): String =
    "FL" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase(Locale.US)

private fun prefs(ctx: Context): SharedPreferences =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

private fun saveSession(
    ctx: Context,
    isLoggedIn: Boolean,
    hasProfile: Boolean,
    user: UserProfile,
    plan: SubscriptionPlan,
    likedIds: List<Int>,
    matchedIds: List<Int>,
    blockedIds: List<Int>,
    paidIds: List<Int>,
    passedIds: List<Int>,
    dailySwipes: Int,
    swipeDay: String,
    messageCredits: Int
) {
    prefs(ctx).edit()
        .putBoolean("isLoggedIn", isLoggedIn)
        .putBoolean("hasProfile", hasProfile)
        .putString("user", gson.toJson(user))
        .putString("plan", plan.name)
        .putString("likedIds", gson.toJson(likedIds))
        .putString("matchedIds", gson.toJson(matchedIds))
        .putString("blockedIds", gson.toJson(blockedIds))
        .putString("paidIds", gson.toJson(paidIds))
        .putString("passedIds", gson.toJson(passedIds))
        .putInt("dailySwipes", dailySwipes)
        .putString("swipeDay", swipeDay)
        .putInt("messageCredits", messageCredits)
        .apply()
}

private fun loadIsLoggedIn(ctx: Context) = prefs(ctx).getBoolean("isLoggedIn", false)
private fun loadHasProfile(ctx: Context) = prefs(ctx).getBoolean("hasProfile", false)
private fun loadUser(ctx: Context): UserProfile {
    val json = prefs(ctx).getString("user", null) ?: return UserProfile()
    return try {
        val loaded = gson.fromJson(json, UserProfile::class.java) ?: UserProfile()
        val durablePhotos = loaded.photos.orEmpty().mapNotNull { stored ->
            val uri = runCatching { Uri.parse(stored) }.getOrNull() ?: return@mapNotNull null
            if (uri.scheme == "content" || uri.scheme == "file") {
                storeUriInAppFiles(ctx, uri, "profile_photos") ?: stored
            } else stored
        }
        val hasOnlinePreference = runCatching {
            com.google.gson.JsonParser.parseString(json).asJsonObject.has("showOnlineStatus")
        }.getOrDefault(false)
        val hasLastSeenPreference = runCatching {
            com.google.gson.JsonParser.parseString(json).asJsonObject.has("showLastSeen")
        }.getOrDefault(false)
        val normalized = loaded.copy(
            photos = durablePhotos,
            lookingFor = loaded.lookingFor ?: LookingForGender.BOTH,
            voiceIntro = loaded.voiceIntro.orEmpty(),
            instagram = loaded.instagram.orEmpty(),
            spotify = loaded.spotify.orEmpty(),
            showOnlineStatus = if (hasOnlinePreference) loaded.showOnlineStatus else true,
            showLastSeen = if (hasLastSeenPreference) loaded.showLastSeen else true
        )
        prefs(ctx).edit().putString("user", gson.toJson(normalized)).apply()
        normalized
    } catch (_: Exception) { UserProfile() }
}
private fun loadPlan(ctx: Context): SubscriptionPlan {
    val n = prefs(ctx).getString("plan", "FREE") ?: "FREE"
    val plan = try { SubscriptionPlan.valueOf(n) } catch (_: Exception) { SubscriptionPlan.FREE }
    val expiry = prefs(ctx).getLong("planExpiresAtMillis", 0L)
    return if (plan != SubscriptionPlan.FREE && expiry > 0L && expiry <= System.currentTimeMillis()) SubscriptionPlan.FREE else plan
}

private fun loadPlanExpiry(ctx: Context): Long? {
    val expiry = prefs(ctx).getLong("planExpiresAtMillis", 0L)
    return expiry.takeIf { it > System.currentTimeMillis() && loadPlan(ctx) != SubscriptionPlan.FREE }
}

private fun loadPaymentHistory(ctx: Context): List<PaymentRecord> {
    val json = prefs(ctx).getString("paymentHistory", "[]") ?: "[]"
    return try { gson.fromJson(json, object : TypeToken<List<PaymentRecord>>() {}.type) ?: emptyList() }
    catch (_: Exception) { emptyList() }
}

private fun savePaymentHistory(ctx: Context, records: List<PaymentRecord>) {
    prefs(ctx).edit().putString("paymentHistory", gson.toJson(records)).apply()
}
private fun loadIntList(ctx: Context, key: String): List<Int> {
    val json = prefs(ctx).getString(key, "[]") ?: "[]"
    return try {
        gson.fromJson(json, object : TypeToken<List<Int>>() {}.type) ?: emptyList()
    } catch (_: Exception) { emptyList() }
}
private fun storeUriInAppFiles(context: Context, source: Uri, bucket: String): String? {
    return try {
        if (source.scheme == "file") {
            val existing = source.path?.let { File(it) }
            if (existing != null && existing.exists() && existing.canonicalPath.startsWith(context.filesDir.canonicalPath)) {
                return source.toString()
            }
        }
        val directory = File(context.filesDir, "media/$bucket").apply { mkdirs() }
        val mime = context.contentResolver.getType(source).orEmpty()
        val extension = when {
            mime.contains("png", true) -> ".png"
            mime.contains("webp", true) -> ".webp"
            mime.contains("jpeg", true) || mime.contains("jpg", true) -> ".jpg"
            mime.startsWith("image/") -> ".img"
            bucket.contains("photo") || bucket.contains("image") -> ".jpg"
            else -> ".bin"
        }
        val destination = File(directory, "${System.currentTimeMillis()}_${UUID.randomUUID()}$extension")
        val input = context.contentResolver.openInputStream(source) ?: return null
        input.use { stream -> destination.outputStream().use { output -> stream.copyTo(output) } }
        Uri.fromFile(destination).toString()
    } catch (_: Exception) {
        null
    }
}

private fun createPrivateAudioFile(context: Context, category: String): File {
    val directory = File(context.filesDir, "media/$category").apply { mkdirs() }
    return File(directory, "${System.currentTimeMillis()}_${UUID.randomUUID()}.m4a")
}

@Suppress("DEPRECATION")
private fun createAudioRecorder(context: Context, output: File): MediaRecorder {
    val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    recorder.setAudioEncodingBitRate(96_000)
    recorder.setAudioSamplingRate(44_100)
    recorder.setOutputFile(output.absolutePath)
    recorder.prepare()
    recorder.start()
    return recorder
}

private fun saveChatMessages(context: Context, personId: Int, messages: List<ChatMessage>) {
    prefs(context).edit().putString("chat_$personId", gson.toJson(messages)).apply()
}

private fun loadChatMessages(context: Context, personId: Int): List<ChatMessage> {
    val json = prefs(context).getString("chat_$personId", null) ?: return emptyList()
    return try {
        gson.fromJson(json, object : TypeToken<List<ChatMessage>>() {}.type) ?: emptyList()
    } catch (_: Exception) { emptyList() }
}

private fun loadDateProposals(context: Context, personId: Int): List<DateProposal> {
    val json = prefs(context).getString("date_proposals_$personId", "[]") ?: "[]"
    return try {
        gson.fromJson(json, object : TypeToken<List<DateProposal>>() {}.type) ?: emptyList()
    } catch (_: Exception) { emptyList() }
}

private fun saveDateProposals(context: Context, personId: Int, proposals: List<DateProposal>) {
    prefs(context).edit().putString("date_proposals_$personId", gson.toJson(proposals)).apply()
}

private fun deleteDateProposals(context: Context, personId: Int) {
    prefs(context).edit().remove("date_proposals_$personId").apply()
}

private fun formatLastSeen(minutesAgo: Int): String = when {
    minutesAgo < 60 -> "${persianDigits(minutesAgo.coerceAtLeast(1).toString())} دقیقه پیش"
    minutesAgo < 24 * 60 -> "${persianDigits((minutesAgo / 60).toString())} ساعت پیش"
    else -> "${persianDigits((minutesAgo / (24 * 60)).toString())} روز پیش"
}

private fun formatDateTimePersian(timestampMillis: Long): String {
    val jalali = gregorianToJalali(timestampMillis)
    val weekdayIndex = Calendar.getInstance().apply { timeInMillis = timestampMillis }.get(Calendar.DAY_OF_WEEK)
    val weekdays = listOf("", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
    val time = SimpleDateFormat("HH:mm", Locale.US).format(Date(timestampMillis))
    return persianDigits("${weekdays.getOrElse(weekdayIndex) { "" }} ${jalali.day} ${persianMonths[jalali.month - 1]} ${jalali.year} · $time")
}

private fun deleteChatMessages(context: Context, personId: Int) {
    val mediaRoot = File(context.filesDir, "media").canonicalFile
    loadChatMessages(context, personId).forEach { message ->
        val mediaFile = when (message.type) {
            MsgType.VOICE -> File(message.text)
            MsgType.IMAGE -> runCatching { Uri.parse(message.text).path?.let(::File) }.getOrNull()
            MsgType.TEXT -> null
        }
        runCatching {
            if (mediaFile != null && mediaFile.exists() && mediaFile.canonicalPath.startsWith(mediaRoot.canonicalPath + File.separator)) {
                mediaFile.delete()
            }
        }
    }
    prefs(context).edit().remove("chat_$personId").apply()
}

private fun clearSession(ctx: Context) {
    prefs(ctx).edit().clear().apply()
}

// ─── Entry ────────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FastLoveApp() }
    }
}

@Composable
private fun FastLoveApp() {
    val context = LocalContext.current

    var isLoggedIn by remember { mutableStateOf(loadIsLoggedIn(context)) }
    var hasProfile by remember { mutableStateOf(loadHasProfile(context)) }
    var currentUser by remember {
        mutableStateOf(loadUser(context).let { loaded ->
            if (loaded.name.isNotBlank() && loaded.referralCode.isBlank()) {
                loaded.copy(referralCode = generateReferralCode()).also { updated ->
                    prefs(context).edit().putString("user", gson.toJson(updated)).apply()
                }
            } else loaded
        })
    }
    var subscriptionPlan by remember { mutableStateOf(loadPlan(context)) }
    var planExpiresAtMillis by remember { mutableStateOf(loadPlanExpiry(context)) }
    val paymentHistory = remember { mutableStateListOf<PaymentRecord>().also { it.addAll(loadPaymentHistory(context)) } }
    var paidMessageCredits by remember { mutableIntStateOf(prefs(context).getInt("messageCredits", 0)) }

    var tab by remember { mutableIntStateOf(0) }
    var discoverIndex by remember { mutableIntStateOf(0) }
    val likedPeople = remember {
        mutableStateListOf<Person>().also { list ->
            loadIntList(context, "likedIds").forEach { id ->
                samplePeople.find { it.id == id }?.let { list.add(it) }
            }
        }
    }
    val matchedPeople = remember {
        mutableStateListOf<Person>().also { list ->
            loadIntList(context, "matchedIds").forEach { id ->
                samplePeople.find { it.id == id }?.let { list.add(it) }
            }
        }
    }
    val blockedIds = remember {
        mutableStateListOf<Int>().also { it.addAll(loadIntList(context, "blockedIds")) }
    }
    val unmatchedIds = remember {
        mutableStateListOf<Int>().also { it.addAll(loadIntList(context, "unmatchedIds")) }
    }
    val paidDirectChatIds = remember {
        mutableStateListOf<Int>().also { it.addAll(loadIntList(context, "paidIds")) }
    }
    val passedIds = remember {
        mutableStateListOf<Int>().also { it.addAll(loadIntList(context, "passedIds")) }
    }
    var showOnMap by remember { mutableStateOf(prefs(context).getBoolean("showOnMap", false)) }

    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var selectedDetailFromDiscover by remember { mutableStateOf(false) }
    var chatPerson by remember { mutableStateOf<Person?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var purchaseInitialSection by remember { mutableIntStateOf(0) }
    var showTutorial by remember { mutableStateOf(isLoggedIn && hasProfile && prefs(context).getInt("tutorialVersion", 0) < 230) }
    var tutorialStep by remember { mutableIntStateOf(0) }
    var chatRefreshVersion by remember { mutableIntStateOf(0) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showPaymentHistoryPage by remember { mutableStateOf(false) }
    var showPrivacySettingsPage by remember { mutableStateOf(false) }
    var showScheduledDatesPage by remember { mutableStateOf(false) }
    var showPublicProfilePreview by remember { mutableStateOf(false) }
    var showPayToMessage by remember { mutableStateOf<Person?>(null) }
    var showMatchPopup by remember { mutableStateOf<Person?>(null) }
    var showReport by remember { mutableStateOf<Person?>(null) }
    var showBlockedList by remember { mutableStateOf(false) }
    var showDeleteRequest by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(FilterState()) }

    fun currentDayKey(): String = Calendar.getInstance().let {
        "%04d-%02d-%02d".format(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
    }
    var swipeDay by remember { mutableStateOf(prefs(context).getString("swipeDay", currentDayKey()) ?: currentDayKey()) }
    var dailySwipes by remember { mutableIntStateOf(prefs(context).getInt("dailySwipes", 0)) }

    fun persist() {
        saveSession(
            context, isLoggedIn, hasProfile, currentUser, subscriptionPlan,
            likedPeople.map { it.id }, matchedPeople.map { it.id },
            blockedIds.toList(), paidDirectChatIds.toList(), passedIds.toList(),
            dailySwipes, swipeDay, paidMessageCredits
        )
    }

    fun refreshDailySwipes() {
        val nowKey = currentDayKey()
        if (swipeDay != nowKey) { swipeDay = nowKey; dailySwipes = 0; persist() }
    }
    fun canSwipe(): Boolean {
        refreshDailySwipes()
        val limit = subscriptionPlan.dailySwipes
        if (limit != null && dailySwipes >= limit) { showSubscription = true; return false }
        dailySwipes += 1
        persist()
        return true
    }

    fun doLike(p: Person) {
        // Rebuild the entry by ID so details-page likes always update the same source of truth.
        passedIds.remove(p.id)
        likedPeople.removeAll { it.id == p.id }
        likedPeople.add(p)
        if (p.likedYou && matchedPeople.none { it.id == p.id }) {
            matchedPeople.add(p)
            showMatchPopup = p
        }
        // Write the interaction immediately as well as through the complete session snapshot.
        prefs(context).edit()
            .putString("likedIds", gson.toJson(likedPeople.map { it.id }.distinct()))
            .putString("matchedIds", gson.toJson(matchedPeople.map { it.id }.distinct()))
            .putString("passedIds", gson.toJson(passedIds.toList()))
            .apply()
        persist()
    }

    fun doPass(p: Person) {
        if (p.id !in passedIds) passedIds.add(p.id)
        likedPeople.removeAll { it.id == p.id }
        matchedPeople.removeAll { it.id == p.id }
        paidDirectChatIds.removeAll { it == p.id }
        persist()
    }

    val orientationFiltered = remember(currentUser) { compatiblePeople(currentUser) }

    // Snapshot mutable ID lists so the derived discovery set is recalculated after Like/Pass/Block.
    val blockedIdSnapshot = blockedIds.toList()
    val unmatchedIdSnapshot = unmatchedIds.toList()
    val passedIdSnapshot = passedIds.toList()
    val filteredPeople = remember(filters, blockedIdSnapshot, unmatchedIdSnapshot, passedIdSnapshot, orientationFiltered) {
        orientationFiltered.filter { p ->
            p.id !in blockedIdSnapshot && p.id !in unmatchedIdSnapshot && p.id !in passedIdSnapshot &&
            p.age in filters.minAge..filters.maxAge &&
            p.distanceKm <= filters.maxDistance &&
            (filters.cities.isEmpty() || p.city in filters.cities) &&
            (filters.jobs.isEmpty() || p.job in filters.jobs) &&
            (filters.interests.isEmpty() || p.interests.any { it in filters.interests }) &&
            (filters.goals.isEmpty() || p.goal in filters.goals)
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = TinderPink, secondary = TinderOrange, tertiary = BrandViolet,
            background = DarkBg, surface = DarkSurface, surfaceVariant = DarkCard,
            onPrimary = TextWhite, onSecondary = TextWhite,
            onBackground = TextWhite, onSurface = TextWhite,
            outline = Color(0xFF3B4561), outlineVariant = Color(0xFF2A334A)
        ),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(26.dp),
            extraLarge = RoundedCornerShape(32.dp)
        ),
        typography = Typography().let { base ->
            base.copy(
                displayLarge = base.displayLarge.copy(fontFamily = Vazirmatn),
                displayMedium = base.displayMedium.copy(fontFamily = Vazirmatn),
                displaySmall = base.displaySmall.copy(fontFamily = Vazirmatn),
                headlineLarge = base.headlineLarge.copy(fontFamily = Vazirmatn),
                headlineMedium = base.headlineMedium.copy(fontFamily = Vazirmatn),
                headlineSmall = base.headlineSmall.copy(fontFamily = Vazirmatn),
                titleLarge = base.titleLarge.copy(fontFamily = Vazirmatn),
                titleMedium = base.titleMedium.copy(fontFamily = Vazirmatn),
                titleSmall = base.titleSmall.copy(fontFamily = Vazirmatn),
                bodyLarge = base.bodyLarge.copy(fontFamily = Vazirmatn),
                bodyMedium = base.bodyMedium.copy(fontFamily = Vazirmatn),
                bodySmall = base.bodySmall.copy(fontFamily = Vazirmatn),
                labelLarge = base.labelLarge.copy(fontFamily = Vazirmatn),
                labelMedium = base.labelMedium.copy(fontFamily = Vazirmatn),
                labelSmall = base.labelSmall.copy(fontFamily = Vazirmatn)
            )
        }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), color = DarkBg) {
                when {
                    !isLoggedIn -> AuthFlow(
                        onSuccess = { phone, register, enteredReferralCode ->
                            if (register) {
                                currentUser = UserProfile(
                                    phone = phone,
                                    referralCode = generateReferralCode(),
                                    referredByCode = enteredReferralCode
                                )
                                hasProfile = false
                            } else {
                                // Login: try restore by phone
                                val saved = loadUser(context)
                                if (saved.phone == phone && saved.name.isNotBlank()) {
                                    currentUser = saved.copy(referralCode = saved.referralCode.ifBlank { generateReferralCode() })
                                    hasProfile = loadHasProfile(context)
                                    subscriptionPlan = loadPlan(context)
                                    planExpiresAtMillis = loadPlanExpiry(context)
                                } else {
                                    currentUser = UserProfile(
                                        phone = phone,
                                        referralCode = generateReferralCode(),
                                        referredByCode = enteredReferralCode
                                    )
                                    // If no local profile for this phone, treat as need profile
                                    hasProfile = false
                                }
                            }
                            isLoggedIn = true
                            persist()
                        }
                    )
                    !hasProfile -> ProfileSetupScreen(
                        phone = currentUser.phone,
                        onBack = {
                            isLoggedIn = false
                            hasProfile = false
                            currentUser = UserProfile()
                            persist()
                        },
                        onComplete = { profile ->
                            currentUser = profile.copy(
                                referralCode = currentUser.referralCode.ifBlank { generateReferralCode() },
                                referredByCode = currentUser.referredByCode
                            )
                            hasProfile = true
                            prefs(context).edit().putInt("tutorialVersion", 0).apply()
                            showTutorial = true
                            tutorialStep = 0
                            tab = 0
                            persist()
                        }
                    )
                    showPaymentHistoryPage -> PaymentHistoryScreen(
                        records = paymentHistory.toList(),
                        onBack = { showPaymentHistoryPage = false }
                    )
                    showPrivacySettingsPage -> PrivacySettingsScreen(
                        user = currentUser,
                        isVisibleOnMap = showOnMap,
                        onToggleOnlineStatus = { enabled -> currentUser = currentUser.copy(showOnlineStatus = enabled); persist() },
                        onToggleLastSeen = { enabled -> currentUser = currentUser.copy(showLastSeen = enabled); persist() },
                        onToggleMapVisibility = { enabled ->
                            showOnMap = enabled
                            prefs(context).edit().putBoolean("showOnMap", enabled).apply()
                        },
                        onDeleteRequest = { showPrivacySettingsPage = false; showDeleteRequest = true },
                        onBack = { showPrivacySettingsPage = false }
                    )
                    showScheduledDatesPage -> ScheduledDatesScreen(
                        upcomingDates = matchedPeople.flatMap { matched ->
                            loadDateProposals(context, matched.id)
                                .filter { it.status == DateProposalStatus.ACCEPTED && it.scheduledAtMillis >= System.currentTimeMillis() }
                                .map { matched to it }
                        }.sortedBy { it.second.scheduledAtMillis },
                        onBack = { showScheduledDatesPage = false }
                    )
                    showPublicProfilePreview -> PublicProfilePreviewScreen(
                        user = currentUser,
                        onBack = { showPublicProfilePreview = false },
                        onEdit = { showPublicProfilePreview = false; showEditProfile = true }
                    )
                    showSubscription -> SubscriptionScreen(
                        initialSection = purchaseInitialSection,
                        currentPlan = subscriptionPlan,
                        currentPlanExpiry = planExpiresAtMillis,
                        messageCredits = paidMessageCredits,
                        onBack = { showSubscription = false; purchaseInitialSection = 0 },
                        onChoosePlan = { plan, durationDays, price ->
                            if (plan == SubscriptionPlan.FREE) {
                                subscriptionPlan = SubscriptionPlan.FREE
                                planExpiresAtMillis = null
                                prefs(context).edit().remove("planExpiresAtMillis").putString("plan", "FREE").apply()
                                persist()
                                null
                            } else {
                                val purchasedAt = System.currentTimeMillis()
                                val baseExpiry = planExpiresAtMillis?.takeIf { subscriptionPlan != SubscriptionPlan.FREE && it > purchasedAt } ?: purchasedAt
                                val expiresAt = baseExpiry + durationDays.toLong() * 24L * 60L * 60L * 1000L
                                subscriptionPlan = plan
                                planExpiresAtMillis = expiresAt
                                prefs(context).edit().putString("plan", plan.name).putLong("planExpiresAtMillis", expiresAt).apply()
                                val record = PaymentRecord(plan.title, price, purchasedAt, expiresAt, durationDays)
                                paymentHistory.add(0, record)
                                savePaymentHistory(context, paymentHistory.toList())
                                persist()
                                expiresAt
                            }
                        },
                        onBuyMessages = { count ->
                            paidMessageCredits += count
                            persist()
                        }
                    )
                    showEditProfile -> EditProfileScreen(
                        user = currentUser,
                        onSave = { updated ->
                            currentUser = updated
                            showEditProfile = false
                            persist()
                        },
                        onBack = { showEditProfile = false }
                    )
                    showPayToMessage != null -> PayToMessageDialog(
                        person = showPayToMessage!!,
                        availableCredits = paidMessageCredits,
                        onConfirm = {
                            val p = showPayToMessage!!
                            if (paidMessageCredits > 0) {
                                paidMessageCredits -= 1
                                if (p.id !in paidDirectChatIds) paidDirectChatIds.add(p.id)
                                chatPerson = p
                                showPayToMessage = null
                                persist()
                            } else {
                                showPayToMessage = null
                                purchaseInitialSection = 1
                                showSubscription = true
                            }
                        },
                        onDismiss = { showPayToMessage = null }
                    )
                    showBlockedList -> BlockedListScreen(
                        people = samplePeople.filter { it.id in blockedIds },
                        onUnblock = { id -> blockedIds.remove(id); persist() },
                        onBack = { showBlockedList = false }
                    )
                    showDeleteRequest -> DeleteAccountDialog(
                        onConfirm = {
                            showDeleteRequest = false
                            clearSession(context)
                            isLoggedIn = false
                            hasProfile = false
                            currentUser = UserProfile()
                            subscriptionPlan = SubscriptionPlan.FREE
                            dailySwipes = 0
                            likedPeople.clear()
                            matchedPeople.clear()
                            blockedIds.clear()
                            unmatchedIds.clear()
                            paidDirectChatIds.clear()
                            passedIds.clear()
                            showOnMap = false
                            paidMessageCredits = 0
                            planExpiresAtMillis = null
                            paymentHistory.clear()
                        },
                        onDismiss = { showDeleteRequest = false; showPrivacySettingsPage = true }
                    )
                    showReport != null -> ReportDialog(
                        person = showReport!!,
                        onSubmit = { _, _ ->
                            // Demo: just close
                            showReport = null
                        },
                        onDismiss = { showReport = null }
                    )
                    showMatchPopup != null -> MatchPopup(
                        person = showMatchPopup!!,
                        onChat = {
                            chatPerson = showMatchPopup
                            showMatchPopup = null
                        },
                        onDismiss = { showMatchPopup = null }
                    )
                    selectedPerson != null -> {
                        val detailPerson = selectedPerson!!
                        PersonDetailScreen(
                            person = detailPerson,
                            canSeeLikes = subscriptionPlan != SubscriptionPlan.FREE,
                            isMatched = matchedPeople.any { it.id == detailPerson.id },
                            canDirectMessage = detailPerson.id in paidDirectChatIds || matchedPeople.any { it.id == detailPerson.id },
                            isLiked = likedPeople.any { it.id == detailPerson.id },
                            onBack = { selectedPerson = null; selectedDetailFromDiscover = false },
                            onLike = {
                                val alreadyLiked = likedPeople.any { it.id == detailPerson.id }
                                if (alreadyLiked) {
                                    android.widget.Toast.makeText(context, "این پروفایل قبلاً لایک شده است ❤️", android.widget.Toast.LENGTH_SHORT).show()
                                    selectedPerson = null
                                    tab = 0
                                    selectedDetailFromDiscover = false
                                } else if (canSwipe()) {
                                    val canonicalPerson = samplePeople.firstOrNull { it.id == detailPerson.id } ?: detailPerson
                                    doLike(canonicalPerson)
                                    // Detail-page likes return directly to discovery; match state is still saved,
                                    // while the full-screen match popup is intentionally skipped here.
                                    showMatchPopup = null
                                    if (selectedDetailFromDiscover) {
                                        // Detail was opened from the current discovery card.
                                        discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                    } else {
                                        // When opened from another tab, resume at the next matching discovery card.
                                        val detailIndex = filteredPeople.indexOfFirst { it.id == detailPerson.id }
                                        if (detailIndex >= discoverIndex) {
                                            discoverIndex = (detailIndex + 1).coerceAtMost(filteredPeople.size)
                                        }
                                    }
                                    selectedPerson = null
                                    selectedDetailFromDiscover = false
                                    tab = 0
                                    android.widget.Toast.makeText(
                                        context,
                                        if (canonicalPerson.likedYou) "لایک ثبت شد و ارتباط شما مچ شد 💕" else "لایک با موفقیت ثبت شد ❤️",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(context, "سهمیه روزانه تمام شده است؛ برای ادامه اشتراک را ارتقا بده.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            onPass = {
                                if (canSwipe()) {
                                    doPass(detailPerson)
                                    selectedPerson = null
                                    selectedDetailFromDiscover = false
                                    if (tab != 0) tab = 0
                                } else {
                                    android.widget.Toast.makeText(context, "سهمیه روزانه تمام شده است؛ برای ادامه اشتراک را ارتقا بده.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            onBlock = {
                                blockedIds.add(detailPerson.id)
                                likedPeople.removeAll { it.id == detailPerson.id }
                                matchedPeople.removeAll { it.id == detailPerson.id }
                                selectedPerson = null
                                selectedDetailFromDiscover = false
                                persist()
                            },
                            onChat = {
                                if (detailPerson.id in paidDirectChatIds || matchedPeople.any { it.id == detailPerson.id }) {
                                    chatPerson = detailPerson
                                    selectedPerson = null
                                    selectedDetailFromDiscover = false
                                } else showPayToMessage = detailPerson
                            },
                            onReport = { showReport = detailPerson }
                        )
                    }
                    chatPerson != null -> ChatScreen(
                        person = chatPerson!!,
                        canUnmatch = matchedPeople.any { it.id == chatPerson!!.id },
                        canSeeLikes = subscriptionPlan != SubscriptionPlan.FREE,
                        onBack = { chatPerson = null; chatRefreshVersion++ },
                        onUnmatch = {
                            val id = chatPerson!!.id
                            matchedPeople.removeAll { it.id == id }
                            likedPeople.removeAll { it.id == id }
                            paidDirectChatIds.removeAll { it == id }
                            if (id !in unmatchedIds) unmatchedIds.add(id)
                            prefs(context).edit().putString("unmatchedIds", gson.toJson(unmatchedIds.toList())).apply()
                            deleteChatMessages(context, id)
                            deleteDateProposals(context, id)
                            chatPerson = null
                            chatRefreshVersion++
                            persist()
                        }
                    )
                    showFilters -> FilterScreen(
                        current = filters,
                        onApply = { filters = it; showFilters = false; discoverIndex = 0 },
                        onClose = { showFilters = false }
                    )
                    else -> {
                        Box(Modifier.fillMaxSize()) {
                            Column(Modifier.fillMaxSize()) {
                                Box(Modifier.weight(1f)) {
                                    when (tab) {
                                    0 -> DiscoverScreen(
                                        viewer = currentUser,
                                        person = filteredPeople.getOrNull(discoverIndex),
                                        onOpen = { selectedPerson = it; selectedDetailFromDiscover = true },
                                        onLike = { p ->
                                            if (canSwipe()) {
                                                doLike(p)
                                                discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                            }
                                        },
                                        onPass = { personToPass ->
                                            if (canSwipe()) {
                                                doPass(personToPass)
                                                discoverIndex = discoverIndex.coerceAtMost(filteredPeople.size)
                                            }
                                        },
                                        onFilter = { showFilters = true },
                                        swipesRemaining = subscriptionPlan.dailySwipes?.let { (it - dailySwipes).coerceAtLeast(0) },
                                        canSeeLikes = subscriptionPlan != SubscriptionPlan.FREE,
                                        onUpgrade = { showSubscription = true },
                                        onBlock = { p ->
                                            blockedIds.add(p.id)
                                            discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                            persist()
                                        }
                                    )
                                    1 -> LikesScreen(
                                        liked = likedPeople.filter { it.id !in blockedIds },
                                        received = orientationFiltered.filter { it.likedYou && it.id !in blockedIds && it.id !in unmatchedIds && it.id !in passedIds },
                                        matched = matchedPeople,
                                        paidIds = paidDirectChatIds,
                                        isFree = subscriptionPlan == SubscriptionPlan.FREE,
                                        onUpgrade = { showSubscription = true },
                                        onOpen = { selectedPerson = it; selectedDetailFromDiscover = false },
                                        onChat = { p ->
                                            if (p in matchedPeople || p.id in paidDirectChatIds) chatPerson = p
                                            else showPayToMessage = p
                                        },
                                        onLikeBack = { p -> if (canSwipe()) doLike(p) },
                                        onBlock = { p -> blockedIds.add(p.id); persist() }
                                    )
                                    2 -> MapScreen(
                                        user = currentUser,
                                        people = samplePeople.filter { it.id !in blockedIds && it.id !in unmatchedIds && it.id !in passedIds },
                                        isVisibleOnMap = showOnMap,
                                        onToggleVisibility = { enabled ->
                                            showOnMap = enabled
                                            prefs(context).edit().putBoolean("showOnMap", enabled).apply()
                                        },
                                        onOpenPerson = { selectedPerson = it; selectedDetailFromDiscover = false }
                                    )
                                    3 -> {
                                        val chatMatches = (matchedPeople.filter { it.id !in blockedIds } +
                                            samplePeople.filter { it.id in paidDirectChatIds && it !in matchedPeople && it.id !in blockedIds }).distinctBy { it.id }
                                        val pendingReplies = remember(chatRefreshVersion, chatMatches.map { it.id }) {
                                            chatMatches.filter { person ->
                                                val saved = loadChatMessages(context, person.id)
                                                saved.isEmpty() || saved.lastOrNull()?.isMe == false
                                            }.map { it.id }.toSet()
                                        }
                                        MatchesChatList(
                                            matches = chatMatches,
                                            pendingReplyIds = pendingReplies,
                                            onChat = { chatPerson = it }
                                        )
                                    }
                                    4 -> MyProfileScreen(
                                        user = currentUser,
                                        upcomingDates = remember(chatRefreshVersion, matchedPeople.map { it.id }) {
                                            matchedPeople.flatMap { matched ->
                                                loadDateProposals(context, matched.id)
                                                    .filter { it.status == DateProposalStatus.ACCEPTED && it.scheduledAtMillis >= System.currentTimeMillis() }
                                                    .map { matched to it }
                                            }.sortedBy { it.second.scheduledAtMillis }
                                        },
                                        blockedCount = blockedIds.size,
                                        plan = subscriptionPlan,
                                        planExpiryMillis = planExpiresAtMillis,
                                        matchCount = matchedPeople.size,
                                        likeCount = likedPeople.size,
                                        onUpgrade = { showSubscription = true },
                                        onEdit = { showEditProfile = true },
                                        onPreview = { showPublicProfilePreview = true },
                                        onPaymentHistory = { showPaymentHistoryPage = true },
                                        onPrivacySettings = { showPrivacySettingsPage = true },
                                        onScheduledDates = { showScheduledDatesPage = true },
                                        onShowBlocked = { showBlockedList = true },
                                        onLogout = {
                                            clearSession(context)
                                            isLoggedIn = false
                                            hasProfile = false
                                            currentUser = UserProfile()
                                            subscriptionPlan = SubscriptionPlan.FREE
                                            planExpiresAtMillis = null
                                            paymentHistory.clear()
                                            dailySwipes = 0
                                            swipeDay = currentDayKey()
                                            likedPeople.clear()
                                            matchedPeople.clear()
                                            blockedIds.clear()
                                            unmatchedIds.clear()
                                            paidDirectChatIds.clear()
                                            passedIds.clear()
                                            showOnMap = false
                                            prefs(context).edit().putBoolean("showOnMap", false).remove("passedIds").apply()
                                            paidMessageCredits = 0
                                            discoverIndex = 0
                                            tab = 0
                                        }
                                    )
                                }
                                }
                                BottomNav(
                                    tab = tab,
                                    onTab = { selectedTab ->
                                        tab = selectedTab
                                        if (showTutorial) {
                                            val matchingStep = PRODUCT_TOUR_STEPS.indexOfFirst { it.tab == selectedTab }
                                            if (matchingStep >= 0) tutorialStep = matchingStep
                                        }
                                    },
                                    matchCount = matchedPeople.count { it.id !in blockedIds },
                                    likedMeCount = orientationFiltered.count { it.likedYou && it.id !in blockedIds && it.id !in unmatchedIds && it.id !in passedIds },
                                    highlightTab = if (showTutorial) tab else null
                                )
                            }
                            if (showTutorial) ProductTourOverlay(
                                step = tutorialStep,
                                onNext = {
                                    if (tutorialStep >= PRODUCT_TOUR_STEPS.lastIndex) {
                                        prefs(context).edit().putInt("tutorialVersion", 230).apply()
                                        showTutorial = false
                                    } else {
                                        tutorialStep += 1
                                        tab = PRODUCT_TOUR_STEPS[tutorialStep].tab
                                    }
                                },
                                onSkip = {
                                    prefs(context).edit().putInt("tutorialVersion", 230).apply()
                                    showTutorial = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Match Popup ──────────────────────────────────────────────────────────────
@Composable
private fun MatchPopup(person: Person, onChat: () -> Unit, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        launch { alpha.animateTo(1f, tween(400)) }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f * alpha.value))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .padding(32.dp)
                    .scale(scale.value)
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A1520), DarkCard)), RoundedCornerShape(28.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text("مچ شدید!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TinderPink, fontFamily = Vazirmatn)
                Spacer(Modifier.height(8.dp))
                Text("شما و ${person.name} همدیگر را لایک کردید", color = TextGray, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.size(80.dp).clip(CircleShape).background(person.tint), contentAlignment = Alignment.Center) {
                    Text(person.emoji, fontSize = 40.sp)
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onChat,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
                ) {
                    Icon(Icons.Default.Chat, null)
                    Spacer(Modifier.width(8.dp))
                    Text("شروع گفتگو", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text("ادامه کشف", color = TextMuted, fontFamily = Vazirmatn)
                }
            }
        }
    }
}

// ─── Report Dialog ────────────────────────────────────────────────────────────
@Composable
private fun BlockedListScreen(people: List<Person>, onUnblock: (Int) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Text("کاربران بلاک‌شده", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        }
        if (people.isEmpty()) {
            EmptyState("کسی بلاک نشده", "از جزئیات پروفایل می‌توانید بلاک کنید", "🚫")
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(people, key = { it.id }) { p ->
                    Surface(shape = RoundedCornerShape(14.dp), color = DarkSurface, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(CircleShape).background(p.tint), contentAlignment = Alignment.Center) {
                                Text(p.emoji, fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${p.name}, ${p.age}", fontWeight = FontWeight.SemiBold, color = TextWhite, fontFamily = Vazirmatn)
                                Text("${p.job} · ${p.city}", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                            }
                            TextButton(onClick = { onUnblock(p.id) }) {
                                Text("رفع بلاک", color = SuccessGreen, fontFamily = Vazirmatn, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("درخواست حذف حساب", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "با تأیید، درخواست حذف حساب ثبت و داده‌های محلی پاک می‌شود. در نسخه دمو فوری اعمال می‌گردد.",
                fontFamily = Vazirmatn,
                color = TextGray
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))) {
                Text("ثبت درخواست حذف", fontFamily = Vazirmatn)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", fontFamily = Vazirmatn) } },
        containerColor = DarkSurface
    )
}

@Composable
private fun ReportDialog(person: Person, onSubmit: (ReportReason, String) -> Unit, onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf<ReportReason?>(null) }
    var details by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("گزارش ${person.name}", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.imePadding().heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                Text("دلیل گزارش را انتخاب کنید:", color = TextGray, fontFamily = Vazirmatn, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                REPORT_REASONS.forEach { r ->
                    val sel = selected?.id == r.id
                    Surface(
                        onClick = { selected = r },
                        shape = RoundedCornerShape(10.dp),
                        color = if (sel) TinderPink.copy(alpha = 0.2f) else DarkCard,
                        border = BorderStroke(1.dp, if (sel) TinderPink else Color.Transparent),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sel, onClick = { selected = r }, colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                            Text(r.title, color = TextWhite, fontFamily = Vazirmatn, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("توضیحات بیشتر (اجباری)...", fontFamily = Vazirmatn) },
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors()
                )
                if (error.isNotEmpty()) Text(error, color = TinderPink, fontSize = 12.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        selected == null -> error = "یک دلیل انتخاب کنید"
                        details.isBlank() -> error = "متن توضیحات الزامی است"
                        else -> onSubmit(selected!!, details.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
            ) { Text("ارسال گزارش", fontFamily = Vazirmatn) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", fontFamily = Vazirmatn) } },
        containerColor = DarkSurface
    )
}

// ─── Subscription / Upgrade ───────────────────────────────────────────────────
@Composable
private fun SubscriptionScreen(
    initialSection: Int,
    currentPlan: SubscriptionPlan,
    currentPlanExpiry: Long?,
    messageCredits: Int,
    onBack: () -> Unit,
    onChoosePlan: (SubscriptionPlan, Int, Int) -> Long?,
    onBuyMessages: (Int) -> Unit
) {
    var selectedSection by remember(initialSection) { mutableIntStateOf(initialSection.coerceIn(0, 1)) }
    var periodWeekly by remember { mutableStateOf(true) }
    var purchaseSuccess by remember { mutableStateOf<PaymentRecord?>(null) }
    val messagePackages = listOf(
        Triple(1, 1_000_000, 0),
        Triple(5, 4_500_000, 10),
        Triple(10, 8_000_000, 20),
        Triple(20, 14_000_000, 30)
    )

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 20.dp, top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("خرید امکانات", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Text("انتخاب کن چه قابلیتی به حسابت اضافه شود", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                .background(DarkSurface, RoundedCornerShape(16.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("اشتراک", "پیام مستقیم").forEachIndexed { index, label ->
                val selected = selectedSection == index
                Surface(
                    onClick = { selectedSection = index },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) TinderPink else Color.Transparent
                ) {
                    Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (index == 0) Icons.Default.Star else Icons.Default.MarkChatUnread, null, tint = if (selected) TextWhite else TextMuted, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(label, color = if (selected) TextWhite else TextGray, fontFamily = Vazirmatn, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }
        }
        Text("خریدها در نسخه دمو شبیه‌سازی می‌شوند و پرداخت واقعی انجام نمی‌شود.", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp))

        when (selectedSection) {
            0 -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(CircleShape).background(TinderOrange.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, null, tint = TinderOrange, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("اشتراک فعلی", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                            Text(currentPlan.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = Vazirmatn)
                            if (currentPlan != SubscriptionPlan.FREE) {
                                Text(
                                    currentPlanExpiry?.let { "فعال تا ${formatDatePersian(it)}" } ?: "تاریخ خرید قدیمی در این نسخه ثبت نشده است",
                                    color = if (currentPlanExpiry != null) SuccessGreen else TextMuted,
                                    fontSize = 10.sp, fontFamily = Vazirmatn
                                )
                            }
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = SuccessGreen.copy(alpha = 0.12f)) {
                            Text("فعال", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontFamily = Vazirmatn)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("دوره پرداخت", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().background(DarkSurface, RoundedCornerShape(14.dp)).padding(4.dp)) {
                    listOf(true to "هفتگی", false to "ماهانه").forEach { (weekly, label) ->
                        val selected = periodWeekly == weekly
                        Surface(onClick = { periodWeekly = weekly }, shape = RoundedCornerShape(10.dp), color = if (selected) TinderPink else Color.Transparent, modifier = Modifier.weight(1f)) {
                            Text(label, Modifier.padding(vertical = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = TextWhite, fontFamily = Vazirmatn, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                listOf(SubscriptionPlan.PRO to (if (periodWeekly) 99000 else 299000), SubscriptionPlan.VIP to (if (periodWeekly) 149000 else 449000)).forEach { (plan, price) ->
                    val isCurrent = currentPlan == plan
                    Card(
                        onClick = {
                            val purchasedAt = System.currentTimeMillis()
                            val durationDays = if (periodWeekly) 7 else 30
                            val expiry = onChoosePlan(plan, durationDays, price)
                            if (expiry != null) purchaseSuccess = PaymentRecord(plan.title, price, purchasedAt, expiry, durationDays)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isCurrent) TinderPink.copy(alpha = 0.16f) else DarkSurface),
                        border = BorderStroke(if (isCurrent || plan == SubscriptionPlan.VIP) 1.5.dp else 1.dp, if (isCurrent) TinderPink else if (plan == SubscriptionPlan.VIP) BrandViolet.copy(alpha = 0.6f) else DarkCard)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(plan.title, color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                    Text(if (plan == SubscriptionPlan.PRO) "برای استفاده بیشتر و دیدن لایک‌ها" else "تجربه کامل و بدون محدودیت", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatToman(price), color = TinderOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
                                    Text(if (periodWeekly) "هفتگی" else "ماهانه", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            val benefits = if (plan == SubscriptionPlan.PRO) listOf("۵۰ سوایپ روزانه", "مشاهده لایک‌کنندگان", "اولویت در پیشنهادها") else listOf("سوایپ نامحدود", "مشاهده لایک‌کنندگان", "همه امکانات ویژه", "تجربه بدون محدودیت روزانه")
                            benefits.forEach { benefit ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(benefit, color = TextGray, fontSize = 12.sp, fontFamily = Vazirmatn)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(if (isCurrent) "اشتراک فعال شما" else "انتخاب این اشتراک", color = if (isCurrent) SuccessGreen else TinderPink, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
                OutlinedButton(onClick = { onChoosePlan(SubscriptionPlan.FREE, 0, 0) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(14.dp)) {
                    Text("ادامه با پلن رایگان", fontFamily = Vazirmatn, color = TextGray)
                }
                Spacer(Modifier.height(24.dp))
            }
            else -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(CircleShape).background(TinderPink.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MarkChatUnread, null, tint = TinderPink, modifier = Modifier.size(23.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("اعتبار پیام مستقیم", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            Text("برای شروع گفتگو با افراد بدون مچ", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                        Text("$messageCredits", color = TinderOrange, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Vazirmatn)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("بسته موردنظرت را انتخاب کن", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = Vazirmatn)
                Text("هر اعتبار برای شروع یک گفتگوی جدید مصرف می‌شود.", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                Spacer(Modifier.height(10.dp))
                messagePackages.forEach { (count, price, discount) ->
                    Card(
                        onClick = { onBuyMessages(count) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(if (count == 10) 1.5.dp else 1.dp, if (count == 10) BrandViolet else Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(BrandViolet.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Text("$count", color = BrandViolet, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("$count اعتبار", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                    if (discount > 0) Surface(color = SuccessGreen.copy(alpha = 0.14f), shape = RoundedCornerShape(20.dp)) {
                                        Text("$discount٪ تخفیف", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontFamily = Vazirmatn)
                                    }
                                }
                                Text("هر اعتبار: ${formatToman(price / count)}", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatToman(price), color = TinderOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = Vazirmatn)
                                Text("خرید بسته", color = TinderPink, fontSize = 10.sp, fontFamily = Vazirmatn)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    purchaseSuccess?.let { purchased ->
        AlertDialog(
            onDismissRequest = { purchaseSuccess = null },
            containerColor = DarkSurface,
            icon = { Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(34.dp)) },
            title = { Text("خرید با موفقیت انجام شد", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("اشتراک ${purchased.planTitle} با موفقیت به حساب شما اضافه شد.", color = TextGray, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(8.dp))
                    Text("مبلغ: ${formatToman(purchased.priceToman)}", color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Text("تاریخ انقضا: ${formatDatePersian(purchased.expiresAtMillis)}", color = SuccessGreen, fontSize = 12.sp, fontFamily = Vazirmatn)
                }
            },
            confirmButton = { TextButton(onClick = { purchaseSuccess = null }) { Text("متوجه شدم", color = TinderPink, fontFamily = Vazirmatn) } }
        )
    }
}

@Composable
private fun UpgradeTeaserScreen(onUpgrade: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🔒", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("کسانی که تو را لایک کرده‌اند", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("با ارتقا می‌توانی ببینی چه کسانی به تو علاقه دارند.", color = TextGray, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        TinderButton("مشاهده پلن‌ها") { onUpgrade() }
    }
}

// ─── First-run product tour: coach marks over the real app tabs ────────────────
private data class ProductTourStep(val tab: Int, val title: String, val body: String, val action: String)

private val PRODUCT_TOUR_STEPS = listOf(
    ProductTourStep(0, "کشف افراد و فیلترها", "کارت‌ها را بررسی کن؛ برای لایک به راست و برای ردکردن به چپ بکش. با لمس کارت جزئیات را می‌بینی و از آیکن فیلتر بالای صفحه می‌توانی سن، شهر و علایق را تنظیم کنی.", "دیدن لایک‌ها"),
    ProductTourStep(1, "بخش لایک‌ها", "در این بخش بین لایک‌هایی که فرستاده‌ای و لایک‌هایی که دریافت کرده‌ای جابه‌جا شو؛ از همین‌جا می‌توانی پروفایل‌ها را باز کنی و لایک را متقابل کنی.", "دیدن نقشه"),
    ProductTourStep(2, "نقشه کاربران", "موقعیت تقریبی افراد شهر را ببین. نمایش خودت روی نقشه اختیاری است و دسترسی موقعیت فقط با اجازه تو فعال می‌شود.", "رفتن به گفتگوها"),
    ProductTourStep(3, "مچ‌ها و گفتگوها", "وقتی علاقه دوطرفه باشد، مچ می‌شوید. از سربرگ گفتگو می‌توانی کارت پروفایل طرف مقابل را هم ببینی.", "دیدن پروفایل"),
    ProductTourStep(4, "پروفایل و منوهای حساب", "اطلاعات و عکس‌هایت را ویرایش کن، اشتراک و اعتبار پیام را مدیریت کن و گزینه‌هایی مثل کاربران مسدودشده و درخواست حذف حساب را از همین بخش پیدا کن.", "شروع استفاده")
)

@Composable
private fun ProductTourOverlay(step: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val page = PRODUCT_TOUR_STEPS[step.coerceIn(PRODUCT_TOUR_STEPS.indices)]
    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier.fillMaxSize().graphicsLayer {
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            }
        ) {
            drawRect(Color.Black.copy(alpha = 0.72f))
            val navHeight = 86.dp.toPx()
            drawRoundRect(
                color = Color.Transparent,
                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - navHeight),
                size = androidx.compose.ui.geometry.Size(size.width, navHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
        }
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 94.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2439)),
            border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(20.dp), color = TinderPink.copy(alpha = 0.16f)) {
                        Text("راهنمای سریع", color = TinderPink, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${step + 1} از ${PRODUCT_TOUR_STEPS.size}", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                    TextButton(onClick = onSkip, contentPadding = PaddingValues(start = 8.dp, end = 0.dp)) {
                        Text("رد کردن", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    PRODUCT_TOUR_STEPS.indices.forEach { i ->
                        Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (i <= step) Brush.horizontalGradient(GradientTinder) else Brush.horizontalGradient(listOf(DarkCard, DarkCard))))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(page.title, color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Spacer(Modifier.height(6.dp))
                Text(page.body, color = TextGray, fontSize = 12.sp, lineHeight = 22.sp, fontFamily = Vazirmatn)
                Spacer(Modifier.height(15.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
                ) {
                    Text(if (step == PRODUCT_TOUR_STEPS.lastIndex) "شروع استفاده از اپ" else page.action, color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowBack, null, tint = TextWhite, modifier = Modifier.size(17.dp))
                }
                Text("برای آشنایی با هر بخش، تب پایین صفحه را هم ببین.", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
            }
        }
    }
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
@Composable
private fun AnimatedGifAuthBackground() {
    val pulse = rememberInfiniteTransition(label = "authLightEffects")
    val glowA by pulse.animateFloat(
        initialValue = 0.30f, targetValue = 0.78f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lightPulseA"
    )
    val glowB by pulse.animateFloat(
        initialValue = 0.18f, targetValue = 0.58f,
        animationSpec = infiniteRepeatable(tween(3400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lightPulseB"
    )
    val glowC by pulse.animateFloat(
        initialValue = 0.20f, targetValue = 0.62f,
        animationSpec = infiniteRepeatable(tween(2100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lightPulseC"
    )
    val heartBloom by pulse.animateFloat(
        initialValue = 0.80f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "heartBloomPulse"
    )
    Box(Modifier.fillMaxSize()) {
        // The scene itself is a single still frame. No GIF playback, scaling, panning or rotation.
        AsyncImage(
            model = R.drawable.login_background_static,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
            Color(0xFF080D1A).copy(alpha = 0.28f), Color(0xFF0B1020).copy(alpha = 0.62f)
        ))))
        // Only the soft light blooms change brightness; their position and the artwork remain fixed.
        // A restrained coral halo sits over the artwork's heart; only its light intensity pulses.
        Box(
            Modifier.align(Alignment.Center).offset(y = (-92).dp).size(252.dp)
                .graphicsLayer { alpha = heartBloom }
                .background(Brush.radialGradient(listOf(
                    Color(0xFFFF7FA8).copy(alpha = 0.42f),
                    TinderPink.copy(alpha = 0.24f),
                    Color.Transparent
                )), CircleShape)
                .blur(11.dp)
        )
        Box(
            Modifier.align(Alignment.Center).offset(y = (-92).dp).size(166.dp)
                .graphicsLayer { alpha = heartBloom }
                .background(Brush.radialGradient(listOf(
                    TinderOrange.copy(alpha = 0.27f), Color.Transparent
                )), CircleShape)
                .blur(7.dp)
        )
        Box(
            Modifier.align(Alignment.TopEnd).offset(x = (-34).dp, y = 84.dp).size(118.dp)
                .graphicsLayer { alpha = glowA }
                .background(Brush.radialGradient(listOf(TinderOrange.copy(alpha = 0.54f), Color.Transparent)), CircleShape)
                .blur(16.dp)
        )
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = 22.dp, y = (-24).dp).size(92.dp)
                .graphicsLayer { alpha = glowB }
                .background(Brush.radialGradient(listOf(TinderPink.copy(alpha = 0.48f), Color.Transparent)), CircleShape)
                .blur(18.dp)
        )
        Box(
            Modifier.align(Alignment.BottomEnd).offset(x = (-52).dp, y = (-128).dp).size(104.dp)
                .graphicsLayer { alpha = glowC }
                .background(Brush.radialGradient(listOf(BrandViolet.copy(alpha = 0.42f), Color.Transparent)), CircleShape)
                .blur(18.dp)
        )
    }
}

@Composable
private fun AnimatedAuthBackground() {
    val infinite = rememberInfiniteTransition(label = "authAtmosphere")
    val drift by infinite.animateFloat(
        initialValue = -28f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(12500, easing = LinearEasing), RepeatMode.Reverse),
        label = "orbDrift"
    )
    val orbit by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(32000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbit"
    )
    val breathe by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF171326), DarkBg, Color(0xFF080D1A))))) {
        Box(
            Modifier.size(330.dp).align(Alignment.TopStart).offset(x = (-145).dp, y = 45.dp)
                .graphicsLayer { alpha = 0.62f * breathe; translationX = drift; translationY = drift * 0.24f }
                .background(Brush.radialGradient(listOf(TinderPink.copy(alpha = 0.42f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(290.dp).align(Alignment.BottomEnd).offset(x = 115.dp, y = 55.dp)
                .graphicsLayer { alpha = 0.58f * breathe; translationX = -drift * 0.7f; translationY = -drift * 0.3f }
                .background(Brush.radialGradient(listOf(BrandViolet.copy(alpha = 0.40f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(190.dp).align(Alignment.TopEnd).offset(x = 65.dp, y = 215.dp)
                .graphicsLayer { alpha = 0.30f * breathe }
                .background(Brush.radialGradient(listOf(TinderOrange.copy(alpha = 0.34f), Color.Transparent)), CircleShape)
        )

        // Thin orbit rings add depth without competing with the form.
        Box(
            Modifier.size(300.dp).align(Alignment.Center).offset(x = 115.dp, y = 45.dp)
                .graphicsLayer { rotationZ = orbit; alpha = 0.13f }
                .border(1.dp, Color.White.copy(alpha = 0.32f), CircleShape)
        )
        Box(
            Modifier.size(230.dp).align(Alignment.Center).offset(x = 115.dp, y = 45.dp)
                .graphicsLayer { rotationZ = -orbit * 0.72f; alpha = 0.10f }
                .border(1.dp, TinderPink.copy(alpha = 0.55f), CircleShape)
        )

        Icon(
            Icons.Default.Favorite, null, tint = TinderPink.copy(alpha = 0.14f),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 28.dp, top = 80.dp).size(30.dp)
                .graphicsLayer { rotationZ = orbit * 0.08f; translationY = drift * 0.35f }
        )
        Icon(
            Icons.Default.Favorite, null, tint = BrandViolet.copy(alpha = 0.18f),
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 48.dp, top = 115.dp).size(20.dp)
                .graphicsLayer { rotationZ = -orbit * 0.12f; translationY = -drift * 0.3f }
        )
        Icon(
            Icons.Default.Favorite, null, tint = TinderOrange.copy(alpha = 0.14f),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 72.dp, bottom = 135.dp).size(16.dp)
                .graphicsLayer { translationY = drift * 0.25f }
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1020).copy(alpha = 0.18f), Color(0xFF080D1A).copy(alpha = 0.54f)))))
    }
}

@Composable
private fun AuthChoiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        border = if (primary) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Row(
            Modifier
                .background(
                    if (primary) Brush.linearGradient(GradientTinder)
                    else Brush.linearGradient(listOf(Color(0xFF252D45), Color(0xFF1B2237)))
                )
                .padding(horizontal = 17.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                    .background(if (primary) Color.White.copy(alpha = 0.18f) else BrandViolet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextWhite, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
                Text(subtitle, color = TextWhite.copy(alpha = if (primary) 0.88f else 0.66f), fontSize = 11.sp, fontFamily = Vazirmatn)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = TextWhite.copy(alpha = 0.88f), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun AuthStepIndicator(step: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(3) { index ->
            Box(
                Modifier.weight(if (index == step) 2.2f else 1f).height(4.dp)
                    .clip(CircleShape)
                    .background(if (index <= step) Brush.horizontalGradient(GradientTinder) else Brush.horizontalGradient(listOf(DarkCard, DarkCard)))
            )
        }
    }
}

@Composable
private fun AuthFlow(onSuccess: (String, Boolean, String) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var registerMode by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableIntStateOf(120) }
    var resendToken by remember { mutableIntStateOf(0) }
    BackHandler(enabled = step > 0) {
        step = if (step == 2) 1 else 0
        error = ""
    }
    val authScroll = rememberScrollState()
    val authKeyboard = LocalSoftwareKeyboardController.current
    val logoTransition = rememberInfiniteTransition(label = "authLogo")
    val logoScale by logoTransition.animateFloat(
        0.97f, 1.035f,
        infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logoScale"
    )

    LaunchedEffect(step, resendToken) {
        if (step == 2) {
            secondsLeft = 120
            while (secondsLeft > 0) { delay(1000); secondsLeft-- }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedGifAuthBackground()
        Column(
            Modifier.fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(authScroll)
                .padding(horizontal = 18.dp, vertical = if (step == 0) 18.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (step == 0) {
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier.size(82.dp).graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                        .clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(GradientTinder))
                        .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Favorite, null, tint = TextWhite, modifier = Modifier.size(40.dp))
                    Box(Modifier.align(Alignment.BottomEnd).padding(9.dp).size(10.dp).background(Color.White.copy(alpha = 0.9f), CircleShape))
                }
                Spacer(Modifier.height(12.dp))
                Text("فست لاو", fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite, fontFamily = Vazirmatn)
                Text("آشنایی‌های واقعی، از یک شروع ساده", fontSize = 12.sp, color = TextGray, fontFamily = Vazirmatn)
                Spacer(Modifier.height(18.dp))
            } else {
                Row(Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Favorite, null, tint = TextWhite, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Text("فست لاو", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                }
            }

            Card(
                Modifier.fillMaxWidth().widthIn(max = 500.dp).animateContentSize(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE8171F34)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(Modifier.padding(horizontal = if (step == 0) 21.dp else 16.dp, vertical = if (step == 0) 20.dp else 14.dp)) {
                    if (step > 0) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                step = if (step == 2) 1 else 0
                                error = ""
                            }, contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp)) {
                                Icon(Icons.Default.ArrowForward, null, tint = TextGray, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("بازگشت", color = TextGray, fontSize = 12.sp, fontFamily = Vazirmatn)
                            }
                        }
                    }
                    AuthStepIndicator(step)
                    Spacer(Modifier.height(23.dp))
                    Crossfade(targetState = step, label = "auth-step-content") { activeStep ->
                        Column {
                            when (activeStep) {
                                0 -> {
                                    Spacer(Modifier.height(8.dp))
                                    AuthChoiceCard(
                                        title = "ساخت حساب جدید",
                                        subtitle = "چند قدم تا آشنایی‌های تازه",
                                        icon = Icons.Default.PersonAdd,
                                        primary = true
                                    ) { registerMode = true; step = 1; error = "" }
                                    Spacer(Modifier.height(12.dp))
                                    AuthChoiceCard(
                                        title = "ورود به حساب",
                                        subtitle = "دسترسی به حساب قبلی",
                                        icon = Icons.Default.Login,
                                        primary = false
                                    ) { registerMode = false; step = 1; error = "" }
                                    Spacer(Modifier.height(15.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Default.Shield, null, tint = SuccessGreen, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("اطلاعاتت فقط برای حساب کاربری دمو استفاده می‌شود", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                                    }
                                }
                                1 -> {
                                    Text(if (registerMode) "شماره‌ات رو وارد کن" else "ورود به حساب", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextWhite, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(6.dp))
                                    Text("کد تأیید برای ادامه به شماره موبایلت ارسال می‌شود.", color = TextGray, fontSize = 13.sp, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { entered ->
                                            val normalized = latinDigits(entered).filter { it.isDigit() }
                                            if (normalized.length <= 11) phone = normalized
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("شماره موبایل", fontFamily = Vazirmatn) },
                                        placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹", fontFamily = Vazirmatn) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { authKeyboard?.hide() }),
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = TinderPink) }
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = referralCode,
                                        onValueChange = { entered ->
                                            referralCode = entered.filter { it.isLetterOrDigit() }.take(12).uppercase(Locale.US)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("کد معرف (اختیاری)", fontFamily = Vazirmatn) },
                                        placeholder = { Text("مثلاً FL12AB34CD", fontFamily = Vazirmatn) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fieldColors(),
                                        leadingIcon = { Icon(Icons.Default.CardGiftcard, null, tint = BrandViolet) }
                                    )
                                    Text("کد معرف اختیاری است و فقط هنگام ساخت حساب جدید ثبت می‌شود.", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 4.dp))
                                    if (error.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(error, color = Color(0xFFFF879D), fontSize = 12.sp, fontFamily = Vazirmatn)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    TinderButton(if (registerMode) "ادامه ثبت‌نام" else "ادامه ورود") {
                                        if (phone.length < 10) error = "لطفاً یک شماره موبایل معتبر وارد کن"
                                        else { error = ""; step = 2 }
                                    }
                                }
                                else -> {
                                    Text("تأیید شماره موبایل", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextWhite, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(7.dp))
                                    Text("کد شش‌رقمی ارسال‌شده را وارد کن.", color = TextGray, fontSize = 13.sp, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(14.dp))
                                    Surface(shape = RoundedCornerShape(14.dp), color = BrandViolet.copy(alpha = 0.13f), border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.28f))) {
                                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Code, null, tint = BrandViolet, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(9.dp))
                                            Column {
                                                Text("حالت نمایشی", color = BrandViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                                Text("کد تست: ۱۲۳۴۵۶", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = Vazirmatn)
                                            }
                                            Spacer(Modifier.weight(1f))
                                            Text(if (secondsLeft > 0) "${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')}" else "منقضی", color = if (secondsLeft > 0) TextGray else TinderPink, fontFamily = Vazirmatn, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(15.dp))
                                    OutlinedTextField(
                                        value = otp,
                                        onValueChange = { entered ->
                                            val normalized = latinDigits(entered).filter { it.isDigit() }
                                            if (normalized.length <= 6) otp = normalized
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("کد تأیید", fontFamily = Vazirmatn) },
                                        placeholder = { Text("۱۲۳۴۵۶", fontFamily = Vazirmatn) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { authKeyboard?.hide() }),
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TinderPink) }
                                    )
                                    if (error.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(error, color = Color(0xFFFF879D), fontSize = 12.sp, fontFamily = Vazirmatn)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    TinderButton("تأیید و ادامه") {
                                        if (secondsLeft <= 0) error = "کد منقضی شده است"
                                        else if (otp == "123456") { error = ""; onSuccess(phone, registerMode, referralCode.trim()) }
                                        else error = "کد واردشده صحیح نیست؛ کد تست ۱۲۳۴۵۶ است"
                                    }
                                    TextButton(onClick = { if (secondsLeft <= 0) { otp = ""; secondsLeft = 120; resendToken++ } }) {
                                        Text("درخواست مجدد کد", color = if (secondsLeft <= 0) TinderPink else TextMuted, fontFamily = Vazirmatn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("با ادامه، شرایط استفاده از نسخه آزمایشی را می‌پذیری.", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Profile Setup ────────────────────────────────────────────────────────────
@Composable
private fun ProfileSetupScreen(phone: String, onBack: () -> Unit, onComplete: (UserProfile) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var nameConfirmed by remember { mutableStateOf(false) }
    var birthYearPersian by remember { mutableIntStateOf(0) }
    var birthMonth by remember { mutableIntStateOf(0) }
    var gender by remember { mutableStateOf(Gender.FEMALE) }
    var lookingFor by remember { mutableStateOf(LookingForGender.BOTH) }
    var goal by remember { mutableStateOf(RelationGoal.FRIENDSHIP) }
    var job by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("تهران") }
    var about by remember { mutableStateOf("") }
    var voiceIntro by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var spotify by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var selectedPhotos by remember { mutableStateOf(listOf<String>()) }
    var primaryPhotoIndex by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showJobPicker by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val age = if (birthYearPersian > 0) (currentPersianYear() - birthYearPersian).coerceIn(18, 80) else 18
    val context = LocalContext.current
    val setupKeyboard = LocalSoftwareKeyboardController.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showInterestPicker by remember { mutableStateOf(false) }
    BackHandler {
        if (step > 0) {
            step = (step - 1).coerceAtLeast(0)
            error = ""
        } else {
            onBack()
        }
    }

    fun createImageUri(ctx: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: ctx.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selected ->
            val localPhoto = storeUriInAppFiles(context, selected, "profile_photos")
            if (localPhoto != null && selectedPhotos.size < 5) {
                selectedPhotos = selectedPhotos + localPhoto
                if (selectedPhotos.size == 1) primaryPhotoIndex = 0
            }
        }
        showPhotoSheet = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { uri ->
            val localPhoto = storeUriInAppFiles(context, uri, "profile_photos")
            if (localPhoto != null && selectedPhotos.size < 5) {
                selectedPhotos = selectedPhotos + localPhoto
                if (selectedPhotos.size == 1) primaryPhotoIndex = 0
            }
        }
        showPhotoSheet = false
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        // after grant user can retry
    }
    fun ensurePermissionsAndPick(fromCamera: Boolean) {
        if (fromCamera && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }
        if (fromCamera) { val uri = createImageUri(context); cameraUri = uri; cameraLauncher.launch(uri) }
        else galleryLauncher.launch("image/*")
    }

    Column(Modifier.fillMaxSize().background(DarkBg).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (step > 0) { step = (step - 1).coerceAtLeast(0); error = "" }
                else onBack()
            }) {
                Icon(Icons.Default.ArrowForward, if (step > 0) "بازگشت به مرحله قبل" else "بازگشت به ورود", tint = TextWhite)
            }
            Column(Modifier.weight(1f)) {
                Text("ساخت پروفایل", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Text("مرحله ${step + 1} از ۴", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
            }
            Surface(shape = RoundedCornerShape(18.dp), color = TinderPink.copy(alpha = 0.13f)) {
                Text("${(step + 1) * 25}٪", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, fontSize = 11.sp)
            }
        }
        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i -> Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (i <= step) TinderPink else DarkCard)) }
            }
            Text(when (step) { 0 -> "اطلاعات پایه"; 1 -> "هویت و موقعیت"; 2 -> "عکس و علایق"; else -> "درباره تو" }, color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 24.dp)) {
            when (step) {
                0 -> {
                    SectionLabel("جنسیت")
                    Text("برای نمایش پیشنهادهای سازگار، جنسیتت را انتخاب کن", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 9.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Triple(Gender.FEMALE, "زن", "♀"), Triple(Gender.MALE, "مرد", "♂"), Triple(Gender.OTHER, "دیگر", "⚧")).forEach { (g, label, symbol) ->
                            val selected = gender == g
                            Surface(
                                onClick = { gender = g },
                                modifier = Modifier.weight(1f).height(82.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) TinderPink.copy(alpha = 0.20f) else DarkSurface,
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) TinderPink else DarkCard)
                            ) {
                                Column(Modifier.fillMaxSize().padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(symbol, fontSize = 25.sp, color = if (selected) TinderPink else TextGray, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(3.dp))
                                    Text(label, color = if (selected) TextWhite else TextGray, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("نام نمایشی")
                        Spacer(Modifier.weight(1f))
                        if (nameConfirmed) {
                            Surface(shape = RoundedCornerShape(14.dp), color = SuccessGreen.copy(alpha = 0.14f)) {
                                Row(Modifier.padding(horizontal = 9.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("نام تأیید شد", color = SuccessGreen, fontSize = 10.sp, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                    if (name.isBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
                            colors = CardDefaults.cardColors(containerColor = TinderPink.copy(alpha = 0.10f)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.28f))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.AutoAwesome, null, tint = TinderOrange, modifier = Modifier.padding(top = 2.dp).size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("با چه نامی شناخته شوی؟", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                    Text("یک نام کوتاه و دلخواه انتخاب کن؛ این نام روی کارت پروفایل و در گفتگوها نمایش داده می‌شود.", color = TextGray, fontSize = 10.sp, lineHeight = 17.sp, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameConfirmed = false; error = "" },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نامی که دیگران می‌بینند", fontFamily = Vazirmatn) },
                        placeholder = { Text("مثلاً سارا یا علی", fontFamily = Vazirmatn) },
                        supportingText = { Text("حداقل ۲ حرف؛ هر زمان خواستی قابل ویرایش است.", fontFamily = Vazirmatn, fontSize = 10.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { setupKeyboard?.hide() }),
                        shape = RoundedCornerShape(14.dp), colors = fieldColors(),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = TinderPink) }
                    )
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("ماه تولد")
                    var showMonthPicker by remember { mutableStateOf(false) }
                    Surface(onClick = { showMonthPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = TinderPink); Spacer(Modifier.width(12.dp)); Text(if (birthMonth in 1..12) persianMonths[birthMonth - 1] else "انتخاب ماه تولد", color = if (birthMonth in 1..12) TextWhite else TextMuted, fontFamily = Vazirmatn); Spacer(Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showMonthPicker) {
                        AlertDialog(
                            onDismissRequest = { showMonthPicker = false },
                            title = { Text("ماه تولد", fontFamily = Vazirmatn) },
                            text = {
                                Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                                    persianMonths.forEachIndexed { index, month ->
                                        TextButton(
                                            onClick = { birthMonth = index + 1; showMonthPicker = false },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(month, color = if (birthMonth == index + 1) TinderPink else TextWhite, fontFamily = Vazirmatn)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showMonthPicker = false }) {
                                    Text("بستن", fontFamily = Vazirmatn)
                                }
                            },
                            containerColor = DarkSurface
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("سال تولد")
                    Surface(onClick = { showYearPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, null, tint = TinderPink); Spacer(Modifier.width(12.dp))
                            Text(if (birthYearPersian > 0) persianDigits(birthYearPersian.toString()) else "انتخاب سال تولد", Modifier.weight(1f), color = if (birthYearPersian > 0) TextWhite else TextMuted, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showYearPicker) YearPickerDialog(selected = birthYearPersian.takeIf { it > 0 } ?: (currentPersianYear() - 25), minYear = currentPersianYear() - 80, maxYear = currentPersianYear() - 18, onSelect = { birthYearPersian = it; showYearPicker = false }, onDismiss = { showYearPicker = false })
                }
                1 -> {
                    Text("چند انتخاب ساده برای پیشنهادهای بهتر", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(14.dp))
                    SectionLabel("دنبال دوست با چه جنسیتی هستی؟")
                    Text("می‌توانی دختر، پسر یا هر دو جنس را برای دیدن در پیشنهادها انتخاب کنی.", color = TextMuted, fontSize = 11.sp, lineHeight = 18.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LookingForGender.values().forEach { option ->
                            val selected = lookingFor == option
                            Surface(
                                onClick = { lookingFor = option }, modifier = Modifier.weight(1f).height(66.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) TinderPink.copy(alpha = 0.18f) else DarkSurface,
                                border = BorderStroke(if (selected) 1.8.dp else 1.dp, if (selected) TinderPink else DarkCard)
                            ) {
                                Column(Modifier.fillMaxSize().padding(horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(when (option) { LookingForGender.FEMALE -> "👩"; LookingForGender.MALE -> "👨"; LookingForGender.BOTH -> "👥" }, fontSize = 19.sp)
                                    Text(option.title, color = if (selected) TextWhite else TextGray, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontFamily = Vazirmatn, maxLines = 1)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    SectionLabel("موقعیت و شغل")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Surface(onClick = { showCityPicker = true }, modifier = Modifier.weight(1f).height(76.dp), shape = RoundedCornerShape(15.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard)) {
                            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = TinderPink, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("شهر", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.ExpandMore, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(city, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = Vazirmatn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Surface(onClick = { showJobPicker = true }, modifier = Modifier.weight(1f).height(76.dp), shape = RoundedCornerShape(15.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard)) {
                            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Work, null, tint = BrandViolet, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("شغل", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.ExpandMore, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(if (job.isBlank()) "انتخاب شغل" else job, color = if (job.isBlank()) TextMuted else TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = Vazirmatn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    if (showCityPicker) CityPickerDialog(cities = iranianCities, selected = city, onSelect = { city = it; showCityPicker = false }, onDismiss = { showCityPicker = false })
                    if (showJobPicker) CityPickerDialog(cities = availableJobs, selected = job, title = "انتخاب شغل", onSelect = { job = it; showJobPicker = false }, onDismiss = { showJobPicker = false })
                    Spacer(Modifier.height(18.dp))
                    SectionLabel("هدفت از آشنایی")
                    Text("نزدیک‌ترین گزینه به چیزی که دنبالش هستی را انتخاب کن", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 9.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RelationGoal.entries.chunked(2).forEach { pair ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { goalOption ->
                                    val selected = goal == goalOption
                                    Surface(
                                        onClick = { goal = goalOption },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) TinderPink.copy(alpha = 0.18f) else DarkSurface,
                                        border = BorderStroke(if (selected) 1.8.dp else 1.dp, if (selected) TinderPink else DarkCard)
                                    ) {
                                        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = selected, onClick = { goal = goalOption }, modifier = Modifier.size(28.dp), colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                                            Spacer(Modifier.width(5.dp))
                                            Text(goalOption.title, color = if (selected) TextWhite else TextGray, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontFamily = Vazirmatn)
                                        }
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                2 -> {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel("عکس‌های پروفایل")
                            Text("تا ۵ عکس؛ همه را یک‌جا ببین و عکس اصلی را انتخاب کن", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = TinderPink.copy(alpha = 0.14f)) {
                            Text("${selectedPhotos.size} از ۵", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TinderPink, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val photoTiles: List<String?> = if (selectedPhotos.size < 5) listOf(null) + selectedPhotos else selectedPhotos.map { it }
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        photoTiles.chunked(3).forEach { rowItems ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                rowItems.forEachIndexed { slotIndex, uriValue ->
                                    if (uriValue == null) {
                                        Box(
                                            Modifier.weight(1f).aspectRatio(0.82f)
                                                .clip(RoundedCornerShape(15.dp))
                                                .background(Brush.linearGradient(listOf(Color(0xFF30223E), Color(0xFF1D263B))))
                                                .border(1.5.dp, TinderPink.copy(alpha = 0.75f), RoundedCornerShape(15.dp))
                                                .clickable { showPhotoSheet = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.AddAPhoto, null, tint = TinderPink, modifier = Modifier.size(25.dp))
                                                Spacer(Modifier.height(6.dp))
                                                Text("افزودن عکس", fontSize = 11.sp, color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                                                Text("${selectedPhotos.size}/۵", fontSize = 9.sp, color = TextMuted, fontFamily = Vazirmatn)
                                            }
                                        }
                                    } else {
                                        val imageIndex = selectedPhotos.indexOf(uriValue)
                                        val isPrimary = imageIndex == primaryPhotoIndex
                                        Box(
                                            Modifier.weight(1f).aspectRatio(0.82f)
                                                .clip(RoundedCornerShape(15.dp))
                                                .border(if (isPrimary) 2.3.dp else 1.dp, if (isPrimary) TinderPink else Color.White.copy(alpha = 0.10f), RoundedCornerShape(15.dp))
                                                .clickable { primaryPhotoIndex = imageIndex }
                                        ) {
                                            AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(uriValue)).crossfade(true).build(), contentDescription = "عکس شماره ${imageIndex + 1}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            Box(Modifier.align(Alignment.TopStart).padding(5.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.62f)).padding(horizontal = 7.dp, vertical = 3.dp)) {
                                                Text(persianDigits((imageIndex + 1).toString()), color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                            }
                                            IconButton(
                                                onClick = {
                                                    val updated = selectedPhotos.toMutableList().also { it.removeAt(imageIndex) }
                                                    selectedPhotos = updated
                                                    primaryPhotoIndex = when {
                                                        updated.isEmpty() -> 0
                                                        primaryPhotoIndex > imageIndex -> primaryPhotoIndex - 1
                                                        primaryPhotoIndex == imageIndex -> imageIndex.coerceAtMost(updated.lastIndex)
                                                        else -> primaryPhotoIndex
                                                    }
                                                },
                                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(25.dp).background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                            ) { Icon(Icons.Default.Close, null, tint = TextWhite, modifier = Modifier.size(14.dp)) }
                                            if (isPrimary) {
                                                Surface(shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp), color = TinderPink, modifier = Modifier.align(Alignment.BottomStart)) {
                                                    Text("اصلی", modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                                }
                                            }
                                        }
                                    }
                                }
                                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                    if (showPhotoSheet) ModernPhotoPickerSheet(
                        onGallery = { ensurePermissionsAndPick(false) },
                        onCamera = { ensurePermissionsAndPick(true) },
                        onDismiss = { showPhotoSheet = false }
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel("علایق")
                            Text("حداقل ۳ مورد انتخاب کن تا پیشنهادها دقیق‌تر شوند", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                        }
                        Text("${selectedInterests.size} انتخاب", color = if (selectedInterests.size >= 3) SuccessGreen else TinderOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(13.dp)) {
                            Text(
                                if (selectedInterests.isEmpty()) "علایقت را اضافه کن" else selectedInterests.joinToString("  •  "),
                                color = if (selectedInterests.isEmpty()) TextMuted else TextWhite,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = Vazirmatn
                            )
                            Spacer(Modifier.height(9.dp))
                            OutlinedButton(
                                onClick = { showInterestPicker = true },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.65f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.Tune, null, tint = TinderPink, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(if (selectedInterests.isEmpty()) "انتخاب علایق" else "ویرایش علایق", color = TextWhite, fontFamily = Vazirmatn, fontSize = 12.sp)
                            }
                        }
                    }
                    if (showInterestPicker) InterestPickerDialog(
                        selected = selectedInterests,
                        onToggle = { interest -> selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest },
                        onDismiss = { showInterestPicker = false }
                    )
                }
                3 -> {
                    SectionLabel("درباره من")
                    OutlinedTextField(value = about, onValueChange = { about = it }, modifier = Modifier.fillMaxWidth().height(140.dp), placeholder = { Text("اهل کافه و کتابم...", fontFamily = Vazirmatn) }, shape = RoundedCornerShape(14.dp), colors = fieldColors())
                    Spacer(Modifier.height(16.dp))
                    VoiceIntroEditor(path = voiceIntro, onPathChange = { voiceIntro = it })
                    Spacer(Modifier.height(18.dp))
                    SectionLabel("شبکه‌های اجتماعی (اختیاری)")
                    Text("می‌توانی اینستاگرام و اسپاتیفای را اضافه کنی؛ هر دو فیلد اختیاری هستند.", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = instagram, onValueChange = { instagram = it }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, label = { Text("اینستاگرام", fontFamily = Vazirmatn) },
                        placeholder = { Text("نام کاربری یا لینک پروفایل", fontFamily = Vazirmatn) },
                        leadingIcon = { Icon(Icons.Default.CameraAlt, null, tint = TinderPink) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { setupKeyboard?.hide() }),
                        shape = RoundedCornerShape(14.dp), colors = fieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = spotify, onValueChange = { spotify = it }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, label = { Text("اسپاتیفای", fontFamily = Vazirmatn) },
                        placeholder = { Text("نام کاربری یا لینک پروفایل", fontFamily = Vazirmatn) },
                        leadingIcon = { Icon(Icons.Default.MusicNote, null, tint = SuccessGreen) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { setupKeyboard?.hide() }),
                        shape = RoundedCornerShape(14.dp), colors = fieldColors()
                    )
                    Spacer(Modifier.height(18.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            if (selectedPhotos.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedPhotos.chunked(3).forEach { photoRow ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            photoRow.forEachIndexed { _, uriStr ->
                                                val idx = selectedPhotos.indexOf(uriStr)
                                                val isMain = idx == primaryPhotoIndex
                                                Box(Modifier.weight(1f).aspectRatio(0.9f).clip(RoundedCornerShape(9.dp)).border(if (isMain) 2.dp else 0.dp, TinderPink, RoundedCornerShape(9.dp))) {
                                                    AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                                    if (isMain) Text("اصلی", Modifier.align(Alignment.BottomCenter).background(TinderPink).padding(horizontal = 5.dp, vertical = 2.dp), color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                                }
                                            }
                                            repeat(3 - photoRow.size) { Spacer(Modifier.weight(1f)) }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                                    val primary = selectedPhotos.getOrNull(primaryPhotoIndex)
                                    if (primary != null) AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(primary)).build(), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                    else Text("♡", fontSize = 28.sp, color = TextWhite)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (name.isBlank()) "نام شما" else name, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 18.sp, fontFamily = Vazirmatn)
                                    Text("$age ساله  •  $city", color = TextGray, fontFamily = Vazirmatn, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
            if (error.isNotEmpty()) Text(error, color = TinderPink, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 16.dp))
            Spacer(Modifier.height(24.dp))
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) OutlinedButton(onClick = { step--; error = "" }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, TextMuted)) { Text("قبلی", fontFamily = Vazirmatn, color = TextWhite) }
            Button(onClick = {
                when (step) {
                    0 -> {
                        if (!nameConfirmed) {
                            if (name.trim().length < 2) {
                                error = "نام را وارد کن و سپس تأیید نام را بزن"
                            } else {
                                nameConfirmed = true
                                error = ""
                                setupKeyboard?.hide()
                            }
                        } else when {
                            name.trim().length < 2 -> { nameConfirmed = false; error = "نام الزامی است" }
                            birthMonth !in 1..12 -> error = "ماه تولدت را انتخاب کن"
                            birthYearPersian !in (currentPersianYear() - 80)..(currentPersianYear() - 18) -> error = "سال تولد معتبرت را انتخاب کن (۱۸ سال به بالا)"
                            else -> { error = ""; step = 1 }
                        }
                    }
                    1 -> if (job.isBlank()) error = "شغل را انتخاب کنید" else { error = ""; step = 2 }
                    2 -> when {
                        selectedPhotos.isEmpty() -> error = "حداقل یک عکس واقعی اضافه کنید"
                        selectedInterests.size < 3 -> error = "حداقل ۳ علاقه انتخاب کنید"
                        else -> { error = ""; step = 3 }
                    }
                    3 -> onComplete(UserProfile(name = name.trim(), birthYearPersian = birthYearPersian, birthMonth = birthMonth, gender = gender, lookingFor = lookingFor, goal = goal, job = job, city = city, about = about.trim().ifBlank { "هنوز چیزی ننوشتم 😊" }, interests = selectedInterests.toList(), photos = selectedPhotos, primaryPhotoIndex = primaryPhotoIndex.coerceIn(0, (selectedPhotos.size - 1).coerceAtLeast(0)), phone = phone, voiceIntro = voiceIntro, instagram = instagram.trim(), spotify = spotify.trim()))
                }
            }, modifier = Modifier.weight(if (step > 0) 1.5f else 1f).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = TinderPink)) {
                Text(when { step == 0 && !nameConfirmed -> "تأیید نام"; step == 3 -> "شروع کن 🚀"; step == 0 -> "ادامه"; else -> "بعدی" }, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModernPhotoPickerSheet(onGallery: () -> Unit, onCamera: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.width(40.dp).height(4.dp).background(TextMuted, RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(20.dp))
            Text("افزودن عکس", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Text("از گالری انتخاب کنید یا با دوربین بگیرید", color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    onClick = onGallery,
                    shape = RoundedCornerShape(20.dp),
                    color = DarkCard,
                    modifier = Modifier.weight(1f).height(120.dp)
                ) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(52.dp).clip(CircleShape).background(TinderPink.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhotoLibrary, null, tint = TinderPink, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("گالری", fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    }
                }
                Surface(
                    onClick = onCamera,
                    shape = RoundedCornerShape(20.dp),
                    color = DarkCard,
                    modifier = Modifier.weight(1f).height(120.dp)
                ) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(52.dp).clip(CircleShape).background(TinderOrange.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, null, tint = TinderOrange, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("دوربین", fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss) { Text("انصراف", color = TextMuted, fontFamily = Vazirmatn) }
        }
    }
}

// ─── Edit Profile ─────────────────────────────────────────────────────────────
@Composable
private fun EditProfileScreen(user: UserProfile, onSave: (UserProfile) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var job by remember { mutableStateOf(user.job) }
    var city by remember { mutableStateOf(user.city) }
    var about by remember { mutableStateOf(user.about) }
    var voiceIntro by remember { mutableStateOf(user.voiceIntro.orEmpty()) }
    var instagram by remember { mutableStateOf(user.instagram.orEmpty()) }
    var spotify by remember { mutableStateOf(user.spotify.orEmpty()) }
    var lookingFor by remember { mutableStateOf(user.lookingFor ?: LookingForGender.BOTH) }
    var selectedInterests by remember { mutableStateOf(user.interests.toSet()) }
    var selectedPhotos by remember { mutableStateOf(user.photos) }
    var primaryPhotoIndex by remember { mutableIntStateOf(user.primaryPhotoIndex) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showJobPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    fun createImageUri(ctx: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: ctx.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selected -> storeUriInAppFiles(context, selected, "profile_photos")?.let { local -> if (selectedPhotos.size < 5) { selectedPhotos = selectedPhotos + local; if (selectedPhotos.size == 1) primaryPhotoIndex = 0 } } }
        showPhotoSheet = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { captured -> storeUriInAppFiles(context, captured, "profile_photos")?.let { local -> if (selectedPhotos.size < 5) { selectedPhotos = selectedPhotos + local; if (selectedPhotos.size == 1) primaryPhotoIndex = 0 } } }
        showPhotoSheet = false
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    fun ensurePermissionsAndPick(fromCamera: Boolean) {
        if (fromCamera && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }
        if (fromCamera) { val uri = createImageUri(context); cameraUri = uri; cameraLauncher.launch(uri) }
        else galleryLauncher.launch("image/*")
    }
    Column(Modifier.fillMaxSize().background(DarkBg).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Text("ویرایش پروفایل", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("جنسیت و سن قابل تغییر نیستند", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
            Text("${user.age} ساله", color = TextGray, fontFamily = Vazirmatn)
            Spacer(Modifier.height(12.dp))
            SectionLabel("نام")
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors())
            Spacer(Modifier.height(12.dp))
            SectionLabel("شهر")
            Surface(onClick = { showCityPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) { Text(city, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted) }
            }
            if (showCityPicker) CityPickerDialog(cities = iranianCities, selected = city, onSelect = { city = it; showCityPicker = false }, onDismiss = { showCityPicker = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("شغل")
            Surface(onClick = { showJobPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) { Text(job, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted) }
            }
            if (showJobPicker) CityPickerDialog(cities = availableJobs, selected = job, title = "شغل", onSelect = { job = it; showJobPicker = false }, onDismiss = { showJobPicker = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("دنبال دوست با چه جنسیتی هستی؟")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LookingForGender.values().forEach { option ->
                    val selected = lookingFor == option
                    Surface(
                        onClick = { lookingFor = option }, modifier = Modifier.weight(1f).height(62.dp),
                        shape = RoundedCornerShape(13.dp),
                        color = if (selected) TinderPink.copy(alpha = 0.18f) else DarkSurface,
                        border = BorderStroke(if (selected) 1.8.dp else 1.dp, if (selected) TinderPink else DarkCard)
                    ) {
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(option.title, color = if (selected) TextWhite else TextGray, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontFamily = Vazirmatn)
                            if (selected) Icon(Icons.Default.CheckCircle, null, tint = TinderPink, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            SectionLabel("عکس‌ها")
            LazyRow(modifier = Modifier.fillMaxWidth().height(132.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (selectedPhotos.size < 5) item(key = "edit-add-photo-first") {
                    Box(
                        Modifier.size(width = 98.dp, height = 124.dp).clip(RoundedCornerShape(15.dp))
                            .background(DarkCard).border(1.5.dp, TinderPink.copy(alpha = 0.65f), RoundedCornerShape(15.dp))
                            .clickable { showPhotoSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = TinderPink, modifier = Modifier.size(27.dp))
                            Spacer(Modifier.height(7.dp))
                            Text("افزودن عکس", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            Text("${selectedPhotos.size}/۵", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
                itemsIndexed(selectedPhotos, key = { _, uri -> uri }) { i, uriStr ->
                    val isPrimary = i == primaryPhotoIndex
                    Box(Modifier.size(width = 98.dp, height = 124.dp).clip(RoundedCornerShape(15.dp)).border(if (isPrimary) 2.dp else 1.dp, if (isPrimary) TinderPink else DarkCard, RoundedCornerShape(15.dp)).clickable { primaryPhotoIndex = i }) {
                        AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(), contentDescription = "عکس شماره ${i + 1}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Text(persianDigits((i + 1).toString()), Modifier.align(Alignment.TopStart).padding(5.dp).background(Color.Black.copy(alpha = 0.58f), CircleShape).padding(horizontal = 7.dp, vertical = 3.dp), color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        IconButton(onClick = {
                            val updated = selectedPhotos.toMutableList().also { it.removeAt(i) }
                            selectedPhotos = updated
                            primaryPhotoIndex = when {
                                updated.isEmpty() -> 0
                                primaryPhotoIndex > i -> primaryPhotoIndex - 1
                                primaryPhotoIndex == i -> i.coerceAtMost(updated.lastIndex)
                                else -> primaryPhotoIndex
                            }
                        }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.62f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = TextWhite, modifier = Modifier.size(13.dp))
                        }
                        if (isPrimary) Text("اصلی", Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(TinderPink.copy(alpha = 0.94f)).padding(vertical = 3.dp), color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
                    }
                }
            }
            if (showPhotoSheet) ModernPhotoPickerSheet(onGallery = { ensurePermissionsAndPick(false) }, onCamera = { ensurePermissionsAndPick(true) }, onDismiss = { showPhotoSheet = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("درباره من")
            OutlinedTextField(value = about, onValueChange = { about = it }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(14.dp), colors = fieldColors())
            Spacer(Modifier.height(14.dp))
            VoiceIntroEditor(path = voiceIntro, onPathChange = { voiceIntro = it })
            Spacer(Modifier.height(18.dp))
            SectionLabel("شبکه‌های اجتماعی (اختیاری)")
            Text("نام کاربری یا لینک پروفایل را وارد کن؛ هرکدام را خواستی خالی بگذار.", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = instagram, onValueChange = { instagram = it }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("اینستاگرام", fontFamily = Vazirmatn) },
                placeholder = { Text("username یا لینک پروفایل", fontFamily = Vazirmatn) },
                leadingIcon = { Icon(Icons.Default.CameraAlt, null, tint = TinderPink) },
                shape = RoundedCornerShape(14.dp), colors = fieldColors()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = spotify, onValueChange = { spotify = it }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("اسپاتیفای", fontFamily = Vazirmatn) },
                placeholder = { Text("username یا لینک پروفایل", fontFamily = Vazirmatn) },
                leadingIcon = { Icon(Icons.Default.MusicNote, null, tint = SuccessGreen) },
                shape = RoundedCornerShape(14.dp), colors = fieldColors()
            )
            Spacer(Modifier.height(16.dp))
            SectionLabel("علایق")
            InterestFlow(selectedInterests) { interest -> selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest }
            Spacer(Modifier.height(24.dp))
        }
        Button(onClick = {
            onSave(user.copy(name = name.trim(), job = job, city = city, about = about.trim(), lookingFor = lookingFor, interests = selectedInterests.toList(), photos = selectedPhotos, primaryPhotoIndex = primaryPhotoIndex.coerceIn(0, (selectedPhotos.size - 1).coerceAtLeast(0)), voiceIntro = voiceIntro, instagram = instagram.trim(), spotify = spotify.trim()))
        }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = TinderPink)) {
            Text("ذخیره تغییرات", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VoiceIntroEditor(path: String, onPathChange: (String) -> Unit) {
    val context = LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun startRecording() {
        try {
            val file = createPrivateAudioFile(context, "profile_voice")
            val active = createAudioRecorder(context, file)
            recorder = active
            recordingFile = file
            isRecording = true
            message = "در حال ضبط معرفی صوتی..."
        } catch (_: Exception) {
            recorder?.release()
            recorder = null
            recordingFile?.delete()
            recordingFile = null
            isRecording = false
            message = "ضبط صدا شروع نشد؛ مجوز میکروفون را بررسی کن."
        }
    }

    fun stopRecording() {
        val active = recorder
        val file = recordingFile
        try {
            active?.stop()
            if (file != null && file.exists() && file.length() > 500) {
                onPathChange(file.absolutePath)
                message = "معرفی صوتی ذخیره شد."
            } else {
                file?.delete()
                message = "صدای ضبط‌شده خیلی کوتاه بود؛ دوباره تلاش کن."
            }
        } catch (_: Exception) {
            file?.delete()
            message = "ذخیره صدا ناموفق بود؛ دوباره ضبط کن."
        } finally {
            active?.release()
            recorder = null
            recordingFile = null
            isRecording = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording() else message = "برای ضبط معرفی صوتی، دسترسی میکروفون لازم است."
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.let { active ->
                runCatching { active.stop() }
                active.release()
                recordingFile?.takeIf { it.exists() && it.length() > 500 }?.let { onPathChange(it.absolutePath) }
            }
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(BrandViolet.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.RecordVoiceOver, null, tint = BrandViolet)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("معرفی صوتی من", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Text("اختیاری · یک معرفی کوتاه صوتی ضبط کن", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (isRecording) stopRecording()
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording()
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }, colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) TinderOrange else TinderPink), shape = RoundedCornerShape(12.dp)) {
                    Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isRecording) "پایان ضبط" else if (path.isBlank()) "ضبط صدا" else "ضبط دوباره", fontFamily = Vazirmatn, fontSize = 12.sp)
                }
                if (path.isNotBlank() && File(path).exists()) {
                    VoiceIntroPlayer(path)
                    TextButton(onClick = { runCatching { File(path).delete() }; onPathChange(""); message = "معرفی صوتی حذف شد." }) {
                        Icon(Icons.Default.DeleteOutline, null, tint = TinderOrange, modifier = Modifier.size(17.dp))
                    }
                }
            }
            if (message.isNotBlank()) Text(message, color = if (message.contains("ذخیره")) SuccessGreen else TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun VoiceIntroPlayer(path: String) {
    var isPlaying by remember(path) { mutableStateOf(false) }
    var player by remember(path) { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(path) { onDispose { player?.release() } }
    IconButton(onClick = {
        try {
            if (isPlaying) {
                player?.pause()
                isPlaying = false
            } else {
                player?.release()
                val media = MediaPlayer()
                media.setDataSource(path)
                media.setOnCompletionListener {
                    isPlaying = false
                    player?.release()
                    player = null
                }
                media.prepare()
                media.start()
                player = media
                isPlaying = true
            }
        } catch (_: Exception) {
            isPlaying = false
            player?.release()
            player = null
        }
    }) {
        Icon(if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle, if (isPlaying) "توقف" else "پخش", tint = SuccessGreen, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun PayToMessageDialog(person: Person, availableCredits: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("پیام مستقیم", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("هنوز با ${person.name} مچ نشده‌اید.", fontFamily = Vazirmatn, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("برای شروع گفتگو از یک اعتبار پیام مستقیم استفاده می‌شود.", fontFamily = Vazirmatn, color = TextWhite)
                Text("اعتبار فعلی: ${persianDigits(availableCredits.toString())}", fontSize = 13.sp, color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                if (availableCredits == 0) Text("برای ادامه، از بخش اشتراک یک بسته پیام بخرید.", fontSize = 12.sp, color = TextMuted, fontFamily = Vazirmatn)
                Text("پرداخت در نسخه دمو نمایشی است.", fontSize = 11.sp, color = TextMuted, fontFamily = Vazirmatn)
            }
        },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = TinderPink)) {
            Text(if (availableCredits > 0) "استفاده از یک اعتبار" else "مشاهده بسته‌ها", fontFamily = Vazirmatn)
        } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", fontFamily = Vazirmatn) } },
        containerColor = DarkSurface)
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
@Composable private fun SectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, color = TextWhite, fontSize = 15.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun TinderButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(55.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextWhite),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
    ) {
        Box(
            Modifier.fillMaxSize().background(Brush.linearGradient(GradientTinder), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronLeft, null, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TinderPink,
    unfocusedBorderColor = Color(0xFF3A4560),
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    cursorColor = TinderPink,
    focusedContainerColor = DarkCard,
    unfocusedContainerColor = DarkCard,
    focusedLabelColor = TinderPink,
    unfocusedLabelColor = TextMuted
)

@Composable
private fun chipColors(selected: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = TinderPink.copy(alpha = 0.22f),
    selectedLabelColor = TextWhite,
    containerColor = DarkSurface,
    labelColor = TextGray
)

@Composable
private fun InterestPickerDialog(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = availableInterests.filter { it.contains(query.trim(), ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("علایق تو", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Text("حداقل ۳ مورد انتخاب کن", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 420.dp).imePadding()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("جست‌وجوی علاقه…", fontFamily = Vazirmatn, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    shape = RoundedCornerShape(13.dp),
                    colors = fieldColors()
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${selected.size} مورد انتخاب شده", color = if (selected.size >= 3) SuccessGreen else TinderOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Spacer(Modifier.weight(1f))
                    if (selected.isNotEmpty()) TextButton(onClick = { selected.toList().forEach(onToggle) }) { Text("پاک کردن", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn) }
                }
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    filtered.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { interest ->
                                val isSelected = interest in selected
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggle(interest) },
                                    label = { Text(interest, fontFamily = Vazirmatn, fontSize = 11.sp, maxLines = 1) },
                                    leadingIcon = if (isSelected) ({ Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }) else null,
                                    modifier = Modifier.weight(1f),
                                    colors = chipColors(isSelected),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    if (filtered.isEmpty()) Text("علاقه‌ای با این نام پیدا نشد", color = TextMuted, fontFamily = Vazirmatn, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (selected.size >= 3) "ثبت انتخاب‌ها" else "ادامه (${selected.size}/۳)", color = TinderPink, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
            }
        },
        containerColor = DarkSurface,
        titleContentColor = TextWhite,
        textContentColor = TextGray
    )
}

@Composable
private fun InterestFlow(selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (selected.isEmpty()) "حداقل ۳ مورد انتخاب کنید" else "${selected.size} مورد انتخاب شده",
            color = if (selected.size >= 3) SuccessGreen else TextMuted,
            fontSize = 12.sp,
            fontFamily = Vazirmatn
        )
        availableInterests.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { interest ->
                    val sel = interest in selected
                    Surface(
                        onClick = { onToggle(interest) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (sel) TinderPink.copy(alpha = 0.25f) else DarkCard,
                        border = BorderStroke(1.dp, if (sel) TinderPink else DarkCard.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                interest,
                                color = if (sel) TextWhite else TextGray,
                                fontFamily = Vazirmatn,
                                fontSize = 13.sp,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun YearPickerDialog(selected: Int, minYear: Int, maxYear: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("سال تولد", fontFamily = Vazirmatn) }, text = {
        Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
            (maxYear downTo minYear).forEach { y -> TextButton(onClick = { onSelect(y) }, modifier = Modifier.fillMaxWidth()) { Text("$y", color = if (y == selected) TinderPink else TextWhite, fontFamily = Vazirmatn) } }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("بستن", fontFamily = Vazirmatn) } }, containerColor = DarkSurface)
}

@Composable
private fun CityPickerDialog(cities: List<String>, selected: String, title: String = "انتخاب شهر", onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = cities.filter { it.contains(query) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontFamily = Vazirmatn) }, text = {
        Column(Modifier.imePadding().heightIn(max = 380.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("جستجو...", fontFamily = Vazirmatn) }, singleLine = true, colors = fieldColors(), shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(8.dp))
            Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                filtered.forEach { c -> TextButton(onClick = { onSelect(c) }, modifier = Modifier.fillMaxWidth()) { Text(c, color = if (c == selected) TinderPink else TextWhite, fontFamily = Vazirmatn) } }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("بستن", fontFamily = Vazirmatn) } }, containerColor = DarkSurface)
}

@Composable
private fun FilterScreen(current: FilterState, onApply: (FilterState) -> Unit, onClose: () -> Unit) {
    var minAge by remember { mutableIntStateOf(current.minAge) }
    var maxAge by remember { mutableIntStateOf(current.maxAge) }
    var maxDistance by remember { mutableIntStateOf(current.maxDistance) }
    var selectedCities by remember { mutableStateOf(current.cities) }
    var selectedJobs by remember { mutableStateOf(current.jobs) }
    var selectedInterests by remember { mutableStateOf(current.interests) }
    var selectedGoals by remember { mutableStateOf(current.goals) }
    val selectedCount = selectedCities.size + selectedJobs.size + selectedInterests.size + selectedGoals.size +
        (if (minAge != 18 || maxAge != 45) 1 else 0) + (if (maxDistance != 50) 1 else 0)

    Column(Modifier.fillMaxSize().background(DarkBg).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("تنظیمات کشف", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Text(if (selectedCount == 0) "بدون فیلتر اضافه" else "$selectedCount انتخاب فعال", fontSize = 11.sp, color = TextMuted, fontFamily = Vazirmatn)
            }
            TextButton(onClick = {
                minAge = 18; maxAge = 45; maxDistance = 50
                selectedCities = emptySet(); selectedJobs = emptySet(); selectedInterests = emptySet(); selectedGoals = emptySet()
            }) { Text("پاک‌سازی", color = TinderOrange, fontFamily = Vazirmatn) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cake, null, tint = TinderPink)
                        Spacer(Modifier.width(8.dp))
                        Text("محدوده سنی", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        Spacer(Modifier.weight(1f))
                        Text("$minAge تا $maxAge سال", color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, fontSize = 12.sp)
                    }
                    RangeSlider(
                        value = minAge.toFloat()..maxAge.toFloat(),
                        onValueChange = { minAge = it.start.roundToInt(); maxAge = it.endInclusive.roundToInt() },
                        valueRange = 18f..60f,
                        colors = SliderDefaults.colors(thumbColor = TinderPink, activeTrackColor = TinderPink)
                    )
                    Divider(color = DarkCard)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NearMe, null, tint = BrandViolet)
                        Spacer(Modifier.width(8.dp))
                        Text("حداکثر فاصله", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        Spacer(Modifier.weight(1f))
                        Text("$maxDistance کیلومتر", color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, fontSize = 12.sp)
                    }
                    Slider(value = maxDistance.toFloat(), onValueChange = { maxDistance = it.roundToInt() }, valueRange = 1f..100f,
                        colors = SliderDefaults.colors(thumbColor = BrandViolet, activeTrackColor = BrandViolet))
                }
            }
            Spacer(Modifier.height(12.dp))
            CompactFilterPicker("شهرهای موردنظر", iranianCities, selectedCities, "شهر را جست‌وجو کن") { city ->
                selectedCities = if (city in selectedCities) selectedCities - city else selectedCities + city
            }
            CompactFilterPicker("علایق", availableInterests, selectedInterests, "مثلاً سفر، موسیقی، کتاب") { interest ->
                selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest
            }
            CompactFilterPicker("شغل", availableJobs, selectedJobs, "شغل را جست‌وجو کن") { job ->
                selectedJobs = if (job in selectedJobs) selectedJobs - job else selectedJobs + job
            }
            Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 18.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("هدف رابطه", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        RelationGoal.entries.forEach { goal ->
                            val selected = goal in selectedGoals
                            FilterChip(selected = selected, onClick = { selectedGoals = if (selected) selectedGoals - goal else selectedGoals + goal },
                                label = { Text(goal.title, fontFamily = Vazirmatn, fontSize = 11.sp) }, colors = chipColors(selected))
                        }
                    }
                }
            }
        }
        Button(onClick = { onApply(FilterState(minAge, maxAge, selectedJobs, selectedInterests, maxDistance, selectedCities, selectedGoals)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).height(52.dp), shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TinderPink)) {
            Text("نمایش افراد مطابق فیلتر", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactFilterPicker(
    title: String,
    options: List<String>,
    selected: Set<String>,
    placeholder: String,
    onToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(selected.isNotEmpty()) }
    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }
    val filtered = options.filter { it.contains(query.trim(), ignoreCase = true) }
    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Text(if (selected.isEmpty()) "همه موارد" else selected.take(3).joinToString("، ") + if (selected.size > 3) " و ${selected.size - 3} مورد دیگر" else "",
                        color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = Vazirmatn)
                }
                if (selected.isNotEmpty()) Surface(shape = CircleShape, color = TinderPink.copy(alpha = 0.18f)) {
                    Text("${selected.size}", color = TinderPink, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontFamily = Vazirmatn)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = TextGray)
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = query, onValueChange = { query = it; showAll = false }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(placeholder, fontSize = 12.sp, fontFamily = Vazirmatn) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                        singleLine = true, shape = RoundedCornerShape(13.dp), colors = fieldColors())
                    Spacer(Modifier.height(8.dp))
                    if (filtered.isEmpty()) Text("موردی پیدا نشد", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                    else ChipGrid(if (showAll || query.isNotBlank()) filtered else filtered.take(9), selected, onToggle)
                    if (query.isBlank() && filtered.size > 9) {
                        TextButton(onClick = { showAll = !showAll }, modifier = Modifier.align(Alignment.End)) {
                            Text(if (showAll) "نمایش کمتر" else "نمایش همه ${filtered.size} مورد", color = TinderPink, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipGrid(items: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { item ->
                    val sel = item in selected
                    FilterChip(selected = sel, onClick = { onToggle(item) },
                        label = { Text(item, fontFamily = Vazirmatn, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = chipColors(sel), modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun compatibilityScore(viewer: UserProfile, person: Person): Float {
    val sharedInterests = viewer.interests.toSet().intersect(person.interests.toSet()).size
    val denominator = minOf(3, viewer.interests.size, person.interests.size).coerceAtLeast(1)
    val interestPoints = if (viewer.interests.isEmpty() || person.interests.isEmpty()) 0f
        else (sharedInterests.toFloat() / denominator).coerceIn(0f, 1f) * 4f

    val distancePoints = (2f * (1f - person.distanceKm / 80f)).coerceIn(0f, 2f)
    val ageDifference = abs(viewer.age - person.age)
    val agePoints = (2f * (1f - ageDifference / 20f)).coerceIn(0f, 2f)
    val viewerJob = viewer.job.trim()
    val personJob = person.job.trim()
    val jobPoints = when {
        viewerJob.isBlank() || personJob.isBlank() -> 0f
        viewerJob.equals(personJob, ignoreCase = true) -> 2f
        else -> {
            val categories = listOf(
                listOf("مهندس", "نرم‌افزار", "برنامه‌نویس", "کدنویس", "فناوری"),
                listOf("طراح", "عکاس", "محتوا", "هنر", "مد"),
                listOf("پزشک", "پرستار", "روانشناس", "سلامت"),
                listOf("معلم", "استاد", "دانشجو", "آموزش"),
                listOf("وکیل", "حقوق", "فروشنده", "فروش", "مدیر")
            )
            val sameCategory = categories.any { group -> group.any { viewerJob.contains(it) } && group.any { personJob.contains(it) } }
            if (sameCategory) 1.2f else 0.4f
        }
    }
    return ((interestPoints + distancePoints + agePoints + jobPoints).coerceIn(0f, 10f) * 10f).roundToInt() / 10f
}

private fun persianDigits(value: String): String = value.map { c -> if (c in '0'..'9') ('۰'.code + (c - '0')).toChar() else c }.joinToString("")

private fun latinDigits(value: String): String = value.map { c ->
    when (c) {
        in '۰'..'۹' -> ('0'.code + (c - '۰')).toChar()
        in '٠'..'٩' -> ('0'.code + (c - '٠')).toChar()
        else -> c
    }
}.joinToString("")

@Composable
private fun CompatibilityBadge(score: Float, modifier: Modifier = Modifier) {
    val scoreColor = when {
        score >= 8f -> SuccessGreen
        score >= 6f -> TinderOrange
        else -> BrandViolet
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(15.dp), color = Color(0xD9101525), border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.45f))) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = scoreColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text("انطباق", color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn)
                Spacer(Modifier.width(7.dp))
                Text("${persianDigits("%.1f".format(Locale.US, score).replace(".", "٫"))}/۱۰", color = scoreColor, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Vazirmatn)
            }
            Spacer(Modifier.height(5.dp))
            LinearProgressIndicator(progress = (score / 10f).coerceIn(0f, 1f), modifier = Modifier.width(104.dp).height(4.dp).clip(CircleShape), color = scoreColor, trackColor = Color.White.copy(alpha = 0.16f))
        }
    }
}

@Composable
private fun DiscoverScreen(
    viewer: UserProfile,
    person: Person?,
    onOpen: (Person) -> Unit,
    onLike: (Person) -> Unit,
    onPass: (Person) -> Unit,
    onFilter: () -> Unit,
    swipesRemaining: Int?,
    canSeeLikes: Boolean,
    onUpgrade: () -> Unit,
    onBlock: (Person) -> Unit
) {
    val scope = rememberCoroutineScope()
    var offsetX by remember(person?.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(person?.id) { mutableFloatStateOf(0f) }
    var isAnimating by remember(person?.id) { mutableStateOf(false) }
    val isAnimatingState = rememberUpdatedState(isAnimating)
    val rotation = (offsetX / 52f).coerceIn(-11f, 11f)
    val likeAlpha = (offsetX / 150f).coerceIn(0f, 1f)
    val passAlpha = (-offsetX / 150f).coerceIn(0f, 1f)

    fun animateSwipe(like: Boolean) {
        val current = person ?: return
        if (isAnimating) return
        isAnimating = true
        scope.launch {
            // Like exits to the right; pass/dislike exits to the left.
            val target = if (like) 1120f else -1120f
            val anim = Animatable(offsetX)
            anim.animateTo(
                target,
                tween(durationMillis = 520, easing = CubicBezierEasing(0.18f, 0.72f, 0.25f, 1f))
            ) { offsetX = value }
            if (like) onLike(current) else onPass(current)
            offsetX = 0f
            offsetY = 0f
            isAnimating = false
        }
    }

    fun returnCard() {
        scope.launch {
            val ax = Animatable(offsetX)
            val ay = Animatable(offsetY)
            launch { ax.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 230f)) { offsetX = value } }
            launch { ay.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 230f)) { offsetY = value } }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Favorite, null, tint = TextWhite, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("فست لاو", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite, fontFamily = Vazirmatn)
                Text("کشف ارتباط‌های تازه", fontSize = 10.sp, color = TextMuted, fontFamily = Vazirmatn)
            }
            Spacer(Modifier.weight(1f))
            if (swipesRemaining != null) {
                Surface(shape = RoundedCornerShape(14.dp), color = if (swipesRemaining <= 2) TinderOrange.copy(alpha = 0.13f) else DarkSurface, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
                    Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FavoriteBorder, null, tint = if (swipesRemaining <= 2) TinderOrange else TextGray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("$swipesRemaining", color = if (swipesRemaining <= 2) TinderOrange else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    }
                }
                IconButton(onClick = onUpgrade) { Icon(Icons.Default.Star, null, tint = TinderOrange) }
            }
            IconButton(onClick = onFilter, modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(DarkSurface)) {
                Icon(Icons.Default.Tune, null, tint = TextWhite)
            }
        }
        if (person == null) EmptyState("دیگه کسی نیست!", "فیلترها رو تغییر بده یا بعداً بیا", "🎉")
        else {
            Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp)) {
                Card(
                    Modifier.fillMaxSize()
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .graphicsLayer {
                            rotationZ = rotation
                            val travel = (abs(offsetX) / 1120f).coerceIn(0f, 1f)
                            scaleX = 1f - travel * 0.035f
                            scaleY = 1f - travel * 0.035f
                            alpha = 1f - travel * 0.10f
                        }
                        .pointerInput(person.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    when {
                                        offsetX > 140f -> animateSwipe(true)
                                        offsetX < -140f -> animateSwipe(false)
                                        else -> returnCard()
                                    }
                                },
                                onDragCancel = { if (!isAnimatingState.value) returnCard() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (!isAnimatingState.value) {
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y * 0.24f
                                    }
                                }
                            )
                        }
                        .clickable(enabled = !isAnimating) { onOpen(person) },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(person.tint, DarkCard))), contentAlignment = Alignment.Center) {
                            Text(person.emoji, fontSize = 140.sp)
                        }
                        if (likeAlpha > 0.05f) {
                            Text("LIKE", Modifier.align(Alignment.TopStart).padding(24.dp).rotate(-20f).border(3.dp, SuccessGreen, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = SuccessGreen.copy(alpha = likeAlpha), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                        if (passAlpha > 0.05f) {
                            Text("NOPE", Modifier.align(Alignment.TopEnd).padding(24.dp).rotate(20f).border(3.dp, TinderPink, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = TinderPink.copy(alpha = passAlpha), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                        CompatibilityBadge(compatibilityScore(viewer, person), Modifier.align(Alignment.TopCenter).padding(top = 15.dp))
                        if (person.showOnlineStatus) OnlineStatusBadge(
                            isOnline = person.isOnline,
                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 14.dp)
                        )
                        Box(Modifier.fillMaxWidth().fillMaxHeight(0.43f).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            if (person.likedYou && canSeeLikes) {
                                Surface(shape = RoundedCornerShape(20.dp), color = TinderPink) {
                                    Text("❤️ تو رو لایک کرده", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            Text("${person.name}, ${persianDigits(person.age.toString())}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                            Text("${person.job} · ${person.distanceKm} کیلومتر · ${person.city}", color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn)
                            Spacer(Modifier.height(6.dp))
                            Text(person.about, color = TextWhite.copy(alpha = 0.9f), fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontFamily = Vazirmatn)
                            if (person.instagram.isNotBlank() || person.spotify.isNotBlank()) {
                                Spacer(Modifier.height(7.dp))
                                SocialProfileBadges(instagram = person.instagram, spotify = person.spotify, compact = true)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(onClick = { animateSwipe(true) }, enabled = !isAnimating, modifier = Modifier.size(60.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink, contentColor = TextWhite)) {
                    Icon(Icons.Default.Favorite, "لایک", modifier = Modifier.size(30.dp))
                }
                FilledIconButton(onClick = { if (!isAnimating) onBlock(person) }, enabled = !isAnimating, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkCard, contentColor = Color(0xFFFF6B6B))) {
                    Icon(Icons.Default.Block, "بلاک", modifier = Modifier.size(22.dp))
                }
                FilledIconButton(onClick = { animateSwipe(false) }, enabled = !isAnimating, modifier = Modifier.size(60.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkCard, contentColor = TextGray)) {
                    Icon(Icons.Default.Close, "دیسلایک", modifier = Modifier.size(30.dp))
                }
            }
            Text("برای لایک به راست بکش  •  برای دیسلایک به چپ بکش", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun LikedByMeScreen(liked: List<Person>, matched: List<Person>, paidIds: List<Int>, onOpen: (Person) -> Unit, onChat: (Person) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("لایک‌های من", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(12.dp))
        if (liked.isEmpty()) EmptyState("هنوز کسی را لایک نکردی", "برو به کشف و سوایپ کن", "💔")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(liked) { p ->
                val isMatched = p in matched
                val canChat = isMatched || p.id in paidIds
                PersonRow(person = p, onClick = { onOpen(p) }) {
                    if (canChat) TextButton(onClick = { onChat(p) }) { Text(if (isMatched) "مچ 💕" else "چت", color = TinderPink, fontSize = 12.sp, fontFamily = Vazirmatn) }
                    else TextButton(onClick = { onChat(p) }) { Text("پیام مستقیم", color = TinderOrange, fontSize = 11.sp, fontFamily = Vazirmatn) }
                }
            }
        }
    }
}


@Composable
private fun LikesScreen(
    liked: List<Person>, received: List<Person>, matched: List<Person>, paidIds: List<Int>,
    isFree: Boolean, onUpgrade: () -> Unit, onOpen: (Person) -> Unit,
    onChat: (Person) -> Unit, onLikeBack: (Person) -> Unit, onBlock: (Person) -> Unit
) {
    var section by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text("لایک‌ها", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Text("لایک‌های ارسالی و دریافتی را از همین‌جا مدیریت کن.", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(DarkSurface, RoundedCornerShape(14.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("لایک‌های من" to liked.size, "دریافتی" to received.size).forEachIndexed { index, pair ->
                Surface(
                    onClick = { section = index }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp),
                    color = if (section == index) TinderPink.copy(alpha = 0.2f) else Color.Transparent,
                    border = if (section == index) BorderStroke(1.dp, TinderPink.copy(alpha = 0.45f)) else null
                ) {
                    Row(Modifier.padding(vertical = 10.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(pair.first, color = if (section == index) TextWhite else TextMuted, fontSize = 11.sp, fontWeight = if (section == index) FontWeight.Bold else FontWeight.Normal, fontFamily = Vazirmatn)
                        Spacer(Modifier.width(5.dp))
                        Text(persianDigits(pair.second.toString()), color = if (section == index) TinderPink else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (section == 0) {
            if (liked.isEmpty()) EmptyState("هنوز کسی را لایک نکردی", "از بخش کشف افراد جدید را پیدا کن", "💗")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(liked, key = { it.id }) { p ->
                    val isMatched = matched.any { it.id == p.id }
                    val canChat = isMatched || p.id in paidIds
                    PersonRow(person = p, onClick = { onOpen(p) }) {
                        TextButton(onClick = { onChat(p) }) {
                            Text(when { isMatched -> "مچ 💕"; canChat -> "چت"; else -> "پیام مستقیم" }, color = if (canChat) TinderPink else TinderOrange, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
        } else if (isFree) {
            Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = TinderPink)
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${persianDigits(received.size.toString())} نفر تو را لایک کرده‌اند", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            Text("پروفایل افراد در طرح رایگان مخفی است.", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                        }
                        Icon(Icons.Default.Lock, null, tint = TinderOrange)
                    }
                    if (received.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(received.take(8), key = { it.id }) { p ->
                                Box(Modifier.size(72.dp, 88.dp).clip(RoundedCornerShape(14.dp)).background(p.tint.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                                    Text(p.emoji, fontSize = 32.sp, modifier = Modifier.blur(8.dp))
                                    Icon(Icons.Default.Lock, null, tint = TextWhite, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(15.dp))
                    Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TinderPink), shape = RoundedCornerShape(13.dp)) {
                        Text("مشاهده لایک‌های دریافتی", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (received.isEmpty()) EmptyState("فعلاً لایک دریافتی نداری", "با تکمیل پروفایل شانس آشنایی را بیشتر کن", "💌")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(received, key = { it.id }) { p ->
                val isMatched = matched.any { it.id == p.id }
                PersonRow(person = p, onClick = { onOpen(p) }) {
                    if (isMatched) Text("مچ 💕", color = TinderPink, fontSize = 11.sp, fontFamily = Vazirmatn)
                    else Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onLikeBack(p) }) { Text("لایک متقابل", color = TinderPink, fontSize = 11.sp, fontFamily = Vazirmatn) }
                        IconButton(onClick = { onBlock(p) }) { Icon(Icons.Default.Block, null, tint = TextMuted) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeLikedMePreviewScreen(people: List<Person>, onUpgrade: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("لایک‌ها", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(14.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.28f))
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(TinderPink.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Favorite, null, tint = TinderPink, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("تعداد لایک‌های دریافتی", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                        Text("${persianDigits(people.size.toString())} نفر تو را لایک کرده‌اند", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = Vazirmatn)
                    }
                    Icon(Icons.Default.Lock, null, tint = TinderOrange)
                }
                if (people.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text("پیش‌نمایش ناشناس", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(9.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(people, key = { it.id }) { person ->
                            Box(
                                Modifier.width(88.dp).height(104.dp).clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(person.tint.copy(alpha = 0.9f), DarkCard)))
                                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(person.emoji, fontSize = 48.sp, modifier = Modifier.blur(18.dp))
                                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.28f)).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Lock, null, tint = TextWhite.copy(alpha = 0.92f), modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Text("برای حفظ حریم خصوصی، تصویر و هویت افراد در پلن رایگان مخفی است.", color = TextMuted, fontSize = 10.sp, lineHeight = 17.sp, fontFamily = Vazirmatn)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF211B35)), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.30f))) {
            Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✨", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text("با اشتراک، علاقه‌مندان را بشناس", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
                Text("با ارتقا می‌توانی عکس‌ها و پروفایل افرادی را که تو را لایک کرده‌اند ببینی.", color = TextGray, fontSize = 12.sp, lineHeight = 21.sp, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                TinderButton("مشاهده اشتراک‌ها") { onUpgrade() }
            }
        }
    }
}

@Composable
private fun LikedMeScreen(people: List<Person>, matched: List<Person>, onLikeBack: (Person) -> Unit, onOpen: (Person) -> Unit, onBlock: (Person) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("کسانی که تو را لایک کرده‌اند", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(12.dp))
        if (people.isEmpty()) EmptyState("هنوز کسی لایکت نکرده", "", "🌸")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(people) { p ->
                val isMatched = p in matched
                PersonRow(person = p, onClick = { onOpen(p) }) {
                    if (isMatched) Surface(shape = RoundedCornerShape(12.dp), color = TinderPink.copy(alpha = 0.2f)) {
                        Text("مچ 💕", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TinderPink, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    } else TextButton(onClick = { onLikeBack(p) }) { Text("لایک شدید", color = SuccessGreen, fontFamily = Vazirmatn, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun MatchesChatList(matches: List<Person>, pendingReplyIds: Set<Int>, onChat: (Person) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("مچ‌ها و چت‌ها", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Text("گفتگوهایی که نوبت پاسخ توست مشخص شده‌اند", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            }
            if (pendingReplyIds.isNotEmpty()) Surface(shape = RoundedCornerShape(20.dp), color = TinderOrange.copy(alpha = 0.14f)) {
                Text("${pendingReplyIds.size} پاسخ", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TinderOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (matches.isEmpty()) EmptyState("هنوز مچی نداری", "با لایک شدید مچ شو", "💬")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(matches, key = { it.id }) { p ->
                val needsReply = p.id in pendingReplyIds
                PersonRow(person = p, onClick = { onChat(p) }) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (p.showOnlineStatus) Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).background(if (p.isOnline) SuccessGreen else TextMuted, CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(if (p.isOnline) "آنلاین" else "آفلاین", color = if (p.isOnline) SuccessGreen else TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                        }
                        if (needsReply) Surface(shape = RoundedCornerShape(10.dp), color = TinderOrange.copy(alpha = 0.16f)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(6.dp).background(TinderOrange, CircleShape))
                                Spacer(Modifier.width(5.dp))
                                Text("منتظر پاسخ تو", color = TinderOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            }
                        } else Icon(Icons.Default.Chat, null, tint = TextMuted)
                    }
                }
            }
        }
    }
}


private data class MapCity(val name: String, val county: String, val latitude: Double, val longitude: Double)
private data class MapProvinceData(val cities: List<MapCity>)
private data class MapArea(val name: String, val latitude: Double, val longitude: Double)

private fun loadTehranProvinceCities(context: Context): List<MapCity> = runCatching {
    context.assets.open("tehran_province_places.json").bufferedReader(Charsets.UTF_8).use { reader ->
        Gson().fromJson(reader, MapProvinceData::class.java).cities
    }
}.getOrElse { emptyList() }

private fun mapAvatarDrawable(
    context: Context,
    symbol: String,
    backgroundColor: Int,
    isOnline: Boolean? = null,
    isCount: Boolean = false,
    label: String? = null
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val hasLabel = !isCount && !label.isNullOrBlank()
    val width = ((if (hasLabel) 98 else 54) * density).toInt().coerceAtLeast(54)
    val height = ((if (hasLabel) 72 else 58) * density).toInt().coerceAtLeast(58)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = NativeCanvas(bitmap)
    val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG)
    val cx = width / 2f
    val cy = 23 * density

    // A light shadow and outlined avatar keep markers readable without looking like large pins.
    paint.color = android.graphics.Color.argb(65, 0, 0, 0)
    canvas.drawCircle(cx, cy + 2.5f * density, 17.5f * density, paint)
    paint.color = backgroundColor
    canvas.drawCircle(cx, cy, 17.5f * density, paint)
    paint.color = android.graphics.Color.WHITE
    paint.style = NativePaint.Style.STROKE
    paint.strokeWidth = 2f * density
    canvas.drawCircle(cx, cy, 17.5f * density, paint)
    paint.style = NativePaint.Style.FILL
    paint.textAlign = NativePaint.Align.CENTER
    paint.color = android.graphics.Color.WHITE
    paint.textSize = if (isCount) 15 * density else 18 * density
    canvas.drawText(symbol, cx, cy + (if (isCount) 5f else 6.5f) * density, paint)

    if (isOnline != null) {
        val statusX = cx + 13 * density
        val statusY = cy - 13 * density
        paint.color = android.graphics.Color.rgb(18, 24, 39)
        canvas.drawCircle(statusX, statusY, 6.8f * density, paint)
        paint.color = if (isOnline) android.graphics.Color.rgb(67, 225, 166) else android.graphics.Color.rgb(147, 158, 179)
        canvas.drawCircle(statusX, statusY, 4.6f * density, paint)
    }

    if (hasLabel) {
        val left = cx - 44 * density
        val top = cy + 22 * density
        val right = cx + 44 * density
        val bottom = cy + 42 * density
        paint.color = android.graphics.Color.argb(238, 18, 24, 39)
        canvas.drawRoundRect(RectF(left, top, right, bottom), 8 * density, 8 * density, paint)
        paint.color = android.graphics.Color.argb(235, 255, 255, 255)
        paint.style = NativePaint.Style.STROKE
        paint.strokeWidth = 0.8f * density
        canvas.drawRoundRect(RectF(left, top, right, bottom), 8 * density, 8 * density, paint)
        paint.style = NativePaint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        paint.textAlign = NativePaint.Align.CENTER
        paint.textSize = 9.5f * density
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        canvas.drawText(label!!.take(8), cx, top + 13.5f * density, paint)
    }
    return BitmapDrawable(context.resources, bitmap)
}

private fun normalizeMapCityName(name: String): String = name.replace("‌", "").replace(" ", "").trim()

private fun mapAreasFor(city: MapCity): List<MapArea> {
    if (normalizeMapCityName(city.name) == normalizeMapCityName("تهران")) return listOf(
        MapArea("شمال تهران", 35.7900, 51.4250),
        MapArea("مرکز تهران", 35.7040, 51.4100),
        MapArea("غرب تهران", 35.7200, 51.3150),
        MapArea("شرق تهران", 35.7100, 51.5050),
        MapArea("جنوب تهران", 35.6250, 51.4250)
    )
    return listOf(MapArea(city.county.ifBlank { city.name }, city.latitude, city.longitude))
}

/** Approximate public coordinates are intentionally deterministic and never reveal a precise address. */
private fun privacyRoundedPoint(point: GeoPoint): GeoPoint = GeoPoint(
    (point.latitude * 100.0).roundToInt() / 100.0,
    (point.longitude * 100.0).roundToInt() / 100.0
)

@Composable
private fun MapScreen(
    user: UserProfile, people: List<Person>, isVisibleOnMap: Boolean,
    onToggleVisibility: (Boolean) -> Unit, onOpenPerson: (Person) -> Unit
) {
    val context = LocalContext.current
    val cityList = remember { loadTehranProvinceCities(context) }
    val defaultCity = remember(cityList, user.city) {
        cityList.firstOrNull { normalizeMapCityName(it.name) == normalizeMapCityName(user.city) }?.name ?: "تهران"
    }
    var selectedCityName by remember { mutableStateOf(defaultCity) }
    val selectedCity = cityList.firstOrNull { it.name == selectedCityName }
        ?: cityList.firstOrNull { normalizeMapCityName(it.name) == normalizeMapCityName("تهران") }
        ?: MapCity("تهران", "تهران", 35.69439, 51.42151)
    var cityMenuOpen by remember { mutableStateOf(false) }
    var locationGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var pendingVisibilityEnable by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationMessage by remember { mutableStateOf("") }
    var showOnlineOnly by remember { mutableStateOf(false) }
    val mapZoom = remember { mutableFloatStateOf(11.35f) }
    val lastInitializedCity = remember { mutableStateOf("") }
    val showAreaSummary = mapZoom.floatValue < 13.3f

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        locationGranted = granted
        if (granted && pendingVisibilityEnable) onToggleVisibility(true)
        pendingVisibilityEnable = false
        if (!granted) locationMessage = "دسترسی موقعیت داده نشد؛ نقشه شهر همچنان قابل مشاهده است."
    }
    LaunchedEffect(locationGranted, selectedCityName) {
        if (locationGranted) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val last = runCatching {
                lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }.getOrNull()
            if (last != null && normalizeMapCityName(selectedCityName) == normalizeMapCityName(user.city.ifBlank { "تهران" })) {
                currentLocation = GeoPoint(last.latitude, last.longitude)
                locationMessage = "موقعیت دستگاه فقط به‌صورت تقریبی استفاده می‌شود."
            }
        }
    }

    val cityCenter = selectedCity
    val areas = remember(selectedCityName) { mapAreasFor(cityCenter) }
    val eligibleIds = remember(people) { people.map { it.id }.toSet() }
    val locatedUsers = remember(selectedCityName, people) {
        if (normalizeMapCityName(selectedCityName) == normalizeMapCityName("تهران")) {
            tehranMapDemoLocations.filter { it.person.id in eligibleIds }
        } else {
            people.filter { normalizeMapCityName(it.city) == normalizeMapCityName(selectedCityName) }
                .map { person ->
                    val seed = person.id * 53
                    MapLocatedPerson(
                        person,
                        cityCenter.latitude + (((seed % 31) - 15) / 1000.0),
                        cityCenter.longitude + ((((seed * 7) % 35) - 17) / 1000.0)
                    )
                }
        }
    }
    val visibleUsers = remember(locatedUsers, showOnlineOnly) {
        if (showOnlineOnly) locatedUsers.filter { it.person.isOnline && it.person.showOnlineStatus } else locatedUsers
    }
    fun closestAreaIndex(location: MapLocatedPerson): Int = areas.indices.minByOrNull { index ->
        kotlin.math.abs(location.latitude - areas[index].latitude) + kotlin.math.abs(location.longitude - areas[index].longitude) * 0.8
    } ?: 0
    val areaCounts = remember(visibleUsers, areas) {
        areas.mapIndexed { index, area -> area to visibleUsers.count { closestAreaIndex(it) == index } }
    }
    val onlineCount = locatedUsers.count { it.person.isOnline && it.person.showOnlineStatus }
    val offlineCount = (locatedUsers.size - onlineCount).coerceAtLeast(0)

    Column(Modifier.fillMaxSize().background(DarkBg).padding(horizontal = 10.dp, vertical = 7.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.22f))
        ) {
            Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF292447), Color(0xFF17263C), DarkSurface))).padding(horizontal = 13.dp, vertical = 11.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(39.dp).clip(RoundedCornerShape(13.dp)).background(TinderPink.copy(alpha = 0.17f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Explore, null, tint = TinderPink, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text("نقشه اطراف تو", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
                            Text(if (showAreaSummary) "نمای کلی شهر · برای دیدن افراد زوم کن" else "افراد نزدیک در این محدوده", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                        }
                        Box {
                            Surface(
                                onClick = { cityMenuOpen = true }, shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.08f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationCity, null, tint = TinderOrange, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(selectedCityName, color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn)
                                    Icon(Icons.Default.ExpandMore, null, tint = TextGray, modifier = Modifier.size(15.dp))
                                }
                            }
                            DropdownMenu(expanded = cityMenuOpen, onDismissRequest = { cityMenuOpen = false }) {
                                cityList.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city.name, fontFamily = Vazirmatn) },
                                        onClick = { selectedCityName = city.name; cityMenuOpen = false; currentLocation = null }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        MapStatPill("${persianDigits(locatedUsers.size.toString())} نفر", "در شهر", BrandViolet, Modifier.weight(1f))
                        MapStatPill("${persianDigits(onlineCount.toString())} آنلاین", "قابل نمایش", SuccessGreen, Modifier.weight(1f))
                        MapStatPill("${persianDigits(offlineCount.toString())} آفلاین", "تقریبی", TextMuted, Modifier.weight(1f))
                    }
                    if (showAreaSummary) {
                        Spacer(Modifier.height(7.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(areaCounts, key = { it.first.name }) { (area, count) ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.07f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                                ) {
                                    Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(area.name, color = TextGray, fontSize = 8.sp, fontFamily = Vazirmatn)
                                        Spacer(Modifier.width(5.dp))
                                        Text(persianDigits(count.toString()), color = BrandViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(7.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, DarkCard)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, null, tint = TinderPink, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                Column(Modifier.weight(1f)) {
                    Text("نمایش من روی نقشه", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = Vazirmatn)
                    Text(if (isVisibleOnMap) "موقعیت تقریبی تو فعال است" else "موقعیت تو برای دیگران مخفی است", color = TextMuted, fontSize = 8.sp, fontFamily = Vazirmatn)
                }
                Switch(
                    checked = isVisibleOnMap,
                    onCheckedChange = { enabled ->
                        if (enabled && !locationGranted) {
                            pendingVisibilityEnable = true
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        } else onToggleVisibility(enabled)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = TinderPink)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    if (locationGranted) {
                        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                        val last = runCatching { lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
                        if (last != null) currentLocation = GeoPoint(last.latitude, last.longitude)
                        locationMessage = if (last != null) "موقعیت تقریبی به‌روز شد." else "موقعیت فعلی در دسترس نیست؛ مرکز شهر نمایش داده می‌شود."
                    } else locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                shape = RoundedCornerShape(11.dp), border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.48f)),
                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Default.MyLocation, null, tint = TinderPink, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (locationGranted) "موقعیت من" else "دسترسی موقعیت", color = TextWhite, fontSize = 9.sp, fontFamily = Vazirmatn)
            }
            Spacer(Modifier.weight(1f))
            Text("فقط آنلاین‌ها", color = TextGray, fontSize = 9.sp, fontFamily = Vazirmatn)
            Switch(checked = showOnlineOnly, onCheckedChange = { showOnlineOnly = it }, modifier = Modifier.height(29.dp), colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = SuccessGreen))
        }
        if (locationMessage.isNotBlank()) Text(locationMessage, Modifier.padding(top = 2.dp), color = TextMuted, fontSize = 8.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(5.dp))

        Box(
            Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(22.dp))
                .border(1.dp, Color.White.copy(alpha = 0.11f), RoundedCornerShape(22.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                    Configuration.getInstance().userAgentValue = "FastLove-Android/2.18.0 (ir.fastlove.demo)"
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(if (normalizeMapCityName(selectedCityName) == normalizeMapCityName("تهران")) 11.35 else 12.0)
                        controller.setCenter(GeoPoint(cityCenter.latitude, cityCenter.longitude))
                        setUseDataConnection(true)
                        val mapReference = this
                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent): Boolean = false
                            override fun onZoom(event: ZoomEvent): Boolean {
                                mapZoom.floatValue = mapReference.zoomLevelDouble.toFloat()
                                return false
                            }
                        })
                    }
                },
                update = { map ->
                    val cityKey = selectedCityName
                    if (lastInitializedCity.value != cityKey) {
                        map.controller.setCenter(GeoPoint(cityCenter.latitude, cityCenter.longitude))
                        map.controller.setZoom(if (normalizeMapCityName(cityKey) == normalizeMapCityName("تهران")) 11.35 else 12.0)
                        lastInitializedCity.value = cityKey
                    }
                    val zoom = map.zoomLevelDouble
                    if (kotlin.math.abs(mapZoom.floatValue - zoom.toFloat()) > 0.01f) mapZoom.floatValue = zoom.toFloat()
                    map.overlays.removeAll { it is Marker }
                    val ownCity = normalizeMapCityName(selectedCityName) == normalizeMapCityName(user.city.ifBlank { "تهران" })
                    if (showAreaSummary) {
                        // City overview: five compact count bubbles, no individual avatars.
                        areas.forEachIndexed { index, area ->
                            val count = areaCounts.getOrNull(index)?.second ?: 0
                            val marker = Marker(map).apply {
                                position = GeoPoint(area.latitude, area.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                title = area.name
                                snippet = "${persianDigits(count.toString())} کاربر در این محدوده · تعداد تقریبی"
                                icon = mapAvatarDrawable(map.context, persianDigits(count.toString()), BrandViolet.toArgb(), isCount = true)
                            }
                            map.overlays.add(marker)
                        }
                    } else {
                        // Zoomed-in view: show people only; region summary bubbles are completely removed.
                        visibleUsers.forEach { located ->
                            val person = located.person
                            val marker = Marker(map).apply {
                                position = GeoPoint(located.latitude, located.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "${person.name} · ${persianDigits(person.age.toString())} سال"
                                snippet = "${person.job} · ${if (person.showOnlineStatus) if (person.isOnline) "آنلاین" else "آفلاین" else "وضعیت پنهان"} · موقعیت تقریبی"
                                icon = mapAvatarDrawable(
                                    map.context, person.emoji, person.tint.toArgb(),
                                    if (person.showOnlineStatus) person.isOnline else null,
                                    label = person.name
                                )
                                setOnMarkerClickListener { _, _ -> onOpenPerson(person); true }
                            }
                            map.overlays.add(marker)
                        }
                    }
                    if (!showAreaSummary && isVisibleOnMap && locationGranted && ownCity) {
                        val point = currentLocation?.let { privacyRoundedPoint(it) } ?: GeoPoint(cityCenter.latitude, cityCenter.longitude)
                        val selfMarker = Marker(map).apply {
                            position = point
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "موقعیت تقریبی تو"
                            snippet = "${user.age} سال · ${user.job} · نمایش برای دیگران فعال است"
                            icon = mapAvatarDrawable(map.context, "من", TinderPink.toArgb(), true)
                        }
                        map.overlays.add(selfMarker)
                    }
                    map.invalidate()
                }
            )
            Surface(
                Modifier.align(Alignment.TopStart).padding(9.dp), shape = RoundedCornerShape(11.dp),
                color = Color(0xE611182B), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, null, tint = TinderPink, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (showAreaSummary) "نمای کلی مناطق · زوم کن" else "نمای افراد · بدون خلاصه مناطق", color = TextWhite, fontSize = 8.sp, fontFamily = Vazirmatn)
                }
            }
            Surface(
                Modifier.align(Alignment.BottomCenter).padding(8.dp).fillMaxWidth(),
                shape = RoundedCornerShape(13.dp), color = Color(0xE8171D31),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(SuccessGreen, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("آنلاین", color = TextWhite, fontSize = 8.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.size(7.dp).background(TextMuted, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("آفلاین", color = TextWhite, fontSize = 8.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (showAreaSummary) "${persianDigits(areaCounts.size.toString())} محدوده" else "${persianDigits(visibleUsers.size.toString())} کاربر",
                        color = BrandViolet, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn
                    )
                }
            }
        }
        Text("© OpenStreetMap contributors · نقشه خیابان‌ها به اینترنت نیاز دارد.", color = TextMuted, fontSize = 7.sp, fontFamily = Vazirmatn, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
    }
}

@Composable
private fun MapStatPill(value: String, subtitle: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = tint.copy(alpha = 0.09f),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.16f))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(tint, CircleShape))
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(value, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, maxLines = 1)
                Text(subtitle, color = TextMuted, fontSize = 7.sp, fontFamily = Vazirmatn, maxLines = 1)
            }
        }
    }
}

private data class JalaliDateParts(val year: Int, val month: Int, val day: Int)
private data class JalaliCalibration(val gregorianYear: Int, val marchDay: Int, val leap: Int)

private val jalaliBreaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178)
private const val MILLIS_PER_DAY = 86_400_000L

/** Persian-calendar conversion based on the established Jalali break-year algorithm. */
private fun jalaliCalibration(jalaliYear: Int): JalaliCalibration {
    val gregorianYear = jalaliYear + 621
    var leapJ = -14
    var previousBreak = jalaliBreaks.first()
    var currentBreak = 0
    var jump = 0
    for (index in 1 until jalaliBreaks.size) {
        currentBreak = jalaliBreaks[index]
        jump = currentBreak - previousBreak
        if (jalaliYear < currentBreak) break
        leapJ += (jump / 33) * 8 + (jump % 33) / 4
        previousBreak = currentBreak
    }
    val yearsSinceBreak = jalaliYear - previousBreak
    leapJ += (yearsSinceBreak / 33) * 8 + ((yearsSinceBreak % 33) + 3) / 4
    if (jump % 33 == 4 && jump - yearsSinceBreak == 4) leapJ += 1

    val leapG = gregorianYear / 4 - ((gregorianYear / 100 + 1) * 3) / 4 - 150
    val marchDay = 20 + leapJ - leapG
    var leap = (((yearsSinceBreak + 1) % 33) - 1) % 4
    if (leap == -1) leap = 4
    return JalaliCalibration(gregorianYear, marchDay, leap)
}

private fun gregorianDateUtcMillis(year: Int, month: Int, day: Int): Long =
    java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC"), Locale.US).apply {
        clear()
        set(year, month - 1, day, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun gregorianToJalali(timestampMillis: Long): JalaliDateParts {
    val local = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val gregorianYear = local.get(Calendar.YEAR)
    val gregorianMonth = local.get(Calendar.MONTH) + 1
    val gregorianDay = local.get(Calendar.DAY_OF_MONTH)
    val targetDay = gregorianDateUtcMillis(gregorianYear, gregorianMonth, gregorianDay)

    var jalaliYear = gregorianYear - 621
    var calibration = jalaliCalibration(jalaliYear)
    var newYearDay = gregorianDateUtcMillis(calibration.gregorianYear, 3, calibration.marchDay)
    var dayOfYear = ((targetDay - newYearDay) / MILLIS_PER_DAY).toInt()
    if (dayOfYear < 0) {
        jalaliYear -= 1
        calibration = jalaliCalibration(jalaliYear)
        newYearDay = gregorianDateUtcMillis(calibration.gregorianYear, 3, calibration.marchDay)
        dayOfYear = ((targetDay - newYearDay) / MILLIS_PER_DAY).toInt()
    }

    return if (dayOfYear <= 185) {
        JalaliDateParts(jalaliYear, 1 + dayOfYear / 31, dayOfYear % 31 + 1)
    } else {
        val remaining = dayOfYear - 186
        JalaliDateParts(jalaliYear, 7 + remaining / 30, remaining % 30 + 1)
    }
}

private fun persianDateToMillis(year: Int, month: Int, day: Int): Long {
    val calibration = jalaliCalibration(year)
    val daysBeforeMonth = if (month <= 7) (month - 1) * 31 else 186 + (month - 7) * 30
    val utcMillis = gregorianDateUtcMillis(calibration.gregorianYear, 3, calibration.marchDay) +
        (daysBeforeMonth + day - 1) * MILLIS_PER_DAY
    val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"), Locale.US).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun jalaliDaysInMonth(year: Int, month: Int): Int = when {
    month in 1..6 -> 31
    month in 7..11 -> 30
    jalaliCalibration(year).leap == 0 -> 30
    else -> 29
}

private fun persianMonthSerial(timestampMillis: Long): Int {
    val date = gregorianToJalali(timestampMillis)
    return date.year * 12 + date.month - 1
}

private fun persianDateLabel(timestampMillis: Long): String {
    val jalali = gregorianToJalali(timestampMillis)
    val weekdayIndex = Calendar.getInstance().apply { timeInMillis = timestampMillis }.get(Calendar.DAY_OF_WEEK)
    val weekdays = listOf("", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
    return "${weekdays.getOrElse(weekdayIndex) { "" }} ${persianDigits(jalali.day.toString())} ${persianMonths[jalali.month - 1]} ${persianDigits(jalali.year.toString())}"
}

private fun monthStartDay(timestampMillis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestampMillis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

@Composable
private fun DateProposalDialog(
    personName: String,
    isRevision: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (place: String, scheduledAtMillis: Long, latitude: Double?, longitude: Double?) -> Unit
) {
    val todayStart = remember { monthStartDay(System.currentTimeMillis()) }
    val maxAllowedDay = remember(todayStart) {
        Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, 14)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val maxAllowedEnd = remember(todayStart) {
        Calendar.getInstance().apply {
            timeInMillis = maxAllowedDay
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    val initialDate = remember(todayStart) {
        Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis
    }
    var selectedDateMillis by remember { mutableLongStateOf(initialDate) }
    var displayedMonthSerial by remember { mutableIntStateOf(persianMonthSerial(initialDate)) }
    var selectedHour by remember { mutableIntStateOf(19) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var place by remember { mutableStateOf("") }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }

    val currentMonthSerial = persianMonthSerial(todayStart)
    val maxMonthSerial = persianMonthSerial(maxAllowedDay)
    val displayedYear = displayedMonthSerial / 12
    val displayedMonth = displayedMonthSerial % 12 + 1
    val firstDayMillis = remember(displayedYear, displayedMonth) {
        persianDateToMillis(displayedYear, displayedMonth, 1)
    }
    val firstDayCalendar = remember(firstDayMillis) { Calendar.getInstance().apply { timeInMillis = firstDayMillis } }
    val firstDayOffset = firstDayCalendar.get(Calendar.DAY_OF_WEEK) % 7 // Saturday is column zero.
    val daysInMonth = jalaliDaysInMonth(displayedYear, displayedMonth)
    val weekLabels = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val selectedDayStart = monthStartDay(selectedDateMillis)
    val proposedDateTime = remember(selectedDateMillis, selectedHour, selectedMinute) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val isDateWithinWindow = selectedDayStart >= todayStart && selectedDateMillis <= maxAllowedEnd
    val canSubmit = (place.trim().length >= 3 || selectedLatitude != null) &&
        isDateWithinWindow && proposedDateTime > System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Column {
                Text(if (isRevision) "پیشنهاد اصلاح دیت" else "پیشنهاد دیت اول", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Text("برای $personName مکان و زمان مناسبی پیشنهاد بده.", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 570.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("مکان قرار", fontFamily = Vazirmatn) },
                    placeholder = { Text("مثلاً کافه‌ای در خیابان ولیعصر", fontFamily = Vazirmatn, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Place, null, tint = TinderPink) },
                    singleLine = true,
                    colors = fieldColors()
                )
                OutlinedButton(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, if (selectedLatitude != null) SuccessGreen.copy(alpha = 0.6f) else BrandViolet.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Map, null, tint = if (selectedLatitude != null) SuccessGreen else BrandViolet)
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedLatitude != null) "موقعیت روی نقشه انتخاب شد" else "انتخاب اختیاری محل روی نقشه", color = TextWhite, fontFamily = Vazirmatn, fontSize = 11.sp)
                    Spacer(Modifier.weight(1f))
                    if (selectedLatitude != null) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(17.dp))
                    } else {
                        Icon(Icons.Default.ChevronLeft, null, tint = TextMuted)
                    }
                }
                if (selectedLatitude != null && selectedLongitude != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${"%.4f".format(Locale.US, selectedLatitude)}، ${"%.4f".format(Locale.US, selectedLongitude)}",
                            color = SuccessGreen, fontSize = 9.sp, fontFamily = Vazirmatn
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            selectedLatitude = null
                            selectedLongitude = null
                            if (place == "موقعیت انتخاب‌شده روی نقشه") place = ""
                        }) {
                            Text("حذف موقعیت", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("تاریخ قرار · تقویم شمسی", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = Vazirmatn)
                        Text("فقط تا ۱۴ روز آینده امکان انتخاب داری", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                    Surface(color = TinderPink.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                        Text(persianDateLabel(selectedDateMillis), Modifier.padding(horizontal = 9.dp, vertical = 6.dp), color = TinderPink, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171E31)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
                ) {
                    Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${persianMonths[displayedMonth - 1]} ${persianDigits(displayedYear.toString())}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = Vazirmatn)
                            Spacer(Modifier.weight(1f))
                            if (displayedMonthSerial > currentMonthSerial) {
                                TextButton(
                                    onClick = { displayedMonthSerial = currentMonthSerial },
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 2.dp)
                                ) { Text("ماه جاری", color = TextGray, fontSize = 9.sp, fontFamily = Vazirmatn) }
                            }
                            if (displayedMonthSerial < maxMonthSerial) {
                                IconButton(
                                    onClick = { displayedMonthSerial = (displayedMonthSerial + 1).coerceAtMost(maxMonthSerial) },
                                    modifier = Modifier.size(31.dp)
                                ) { Icon(Icons.Default.ChevronLeft, "ماه بعد", tint = TinderPink) }
                            }
                        }
                        Row(Modifier.fillMaxWidth()) {
                            weekLabels.forEach { label ->
                                Box(Modifier.weight(1f).height(25.dp), contentAlignment = Alignment.Center) {
                                    Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                }
                            }
                        }
                        val dayCells: List<Int?> = List(firstDayOffset) { null } + (1..daysInMonth).map { it }
                        dayCells.chunked(7).forEach { week ->
                            Row(Modifier.fillMaxWidth()) {
                                week.forEach { day ->
                                    if (day == null) {
                                        Spacer(Modifier.weight(1f).height(38.dp).padding(2.dp))
                                    } else {
                                        val dayMillis = persianDateToMillis(displayedYear, displayedMonth, day)
                                        val dayStart = monthStartDay(dayMillis)
                                        val enabled = dayStart >= todayStart && dayMillis <= maxAllowedDay
                                        val selected = monthStartDay(selectedDateMillis) == dayStart
                                        Surface(
                                            onClick = { selectedDateMillis = dayMillis },
                                            enabled = enabled,
                                            modifier = Modifier.weight(1f).padding(2.dp).height(36.dp),
                                            shape = RoundedCornerShape(11.dp),
                                            color = when {
                                                selected -> TinderPink
                                                enabled -> DarkCard
                                                else -> Color.Transparent
                                            },
                                            border = if (enabled && !selected) BorderStroke(1.dp, Color.White.copy(alpha = 0.045f)) else null
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    persianDigits(day.toString()),
                                                    color = when { selected -> TextWhite; enabled -> TextGray; else -> TextMuted.copy(alpha = 0.35f) },
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    fontFamily = Vazirmatn
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat((7 - week.size).coerceAtLeast(0)) {
                                    Spacer(Modifier.weight(1f).height(38.dp).padding(2.dp))
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("ساعت قرار", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = Vazirmatn)
                        Text("عقربه را برای ساعت یا دقیقه بچرخان / لمس کن", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                    Surface(color = BrandViolet.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            persianDigits("%02d:%02d".format(selectedHour, selectedMinute)),
                            Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = BrandViolet,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = Vazirmatn
                        )
                    }
                }
                AnalogTimePicker(
                    hour = selectedHour,
                    minute = selectedMinute,
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it }
                )
                if (!isDateWithinWindow) {
                    Text("تاریخ باید بین امروز و ۱۴ روز آینده باشد.", color = Color(0xFFFF8B8B), fontSize = 10.sp, fontFamily = Vazirmatn)
                } else if (proposedDateTime <= System.currentTimeMillis()) {
                    Text("این ساعت گذشته است؛ ساعت دیگری انتخاب کن.", color = Color(0xFFFF8B8B), fontSize = 10.sp, fontFamily = Vazirmatn)
                }
                Text("قرار پس از تأیید طرف مقابل نهایی و در بالای چت سنجاق می‌شود.", color = TextMuted, fontSize = 10.sp, lineHeight = 18.sp, fontFamily = Vazirmatn)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val selectedDate = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    if (selectedDate > now && selectedDate <= maxAllowedEnd && (place.trim().length >= 3 || selectedLatitude != null)) {
                        val finalPlace = place.trim().ifBlank { "موقعیت انتخاب‌شده روی نقشه" }
                        onSubmit(finalPlace, selectedDate, selectedLatitude, selectedLongitude)
                    }
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = TinderPink),
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (isRevision) "ارسال پیشنهاد اصلاح" else "ارسال پیشنهاد", fontFamily = Vazirmatn) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = TextGray, fontFamily = Vazirmatn) } }
    )
    if (showMapPicker) {
        DatePlaceMapPicker(
            onDismiss = { showMapPicker = false },
            onConfirm = { latitude, longitude ->
                selectedLatitude = latitude
                selectedLongitude = longitude
                if (place.isBlank()) place = "موقعیت انتخاب‌شده روی نقشه"
                showMapPicker = false
            }
        )
    }
}

@Composable
private fun AnalogTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    var selectingHour by remember { mutableStateOf(true) }
    val isPm = hour >= 12
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = { selectingHour = true },
                color = if (selectingHour) BrandViolet else DarkCard,
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.dp, if (selectingHour) BrandViolet else Color.White.copy(alpha = 0.07f))
            ) {
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                Text(persianDigits("ساعت $displayHour"), Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
            }
            Surface(
                onClick = { selectingHour = false },
                color = if (!selectingHour) BrandViolet else DarkCard,
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.dp, if (!selectingHour) BrandViolet else Color.White.copy(alpha = 0.07f))
            ) {
                Text(persianDigits("دقیقه %02d".format(minute)), Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
            }
            Surface(
                onClick = { onHourChange(if (isPm) hour - 12 else hour + 12) },
                color = TinderPink.copy(alpha = 0.13f), shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isPm) "ب.ظ" else "ق.ظ", Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = TinderPink, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
            }
        }
        BoxWithConstraints(Modifier.size(244.dp)) {
            val dialSize = maxWidth
            val center = dialSize / 2
            val labelRadius = dialSize * 0.355f
            Canvas(
                Modifier.fillMaxSize().pointerInput(selectingHour, hour, minute, isPm) {
                    detectTapGestures { tap ->
                        val dx = tap.x - size.width / 2f
                        val dy = tap.y - size.height / 2f
                        val rawDegrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 90.0
                        val normalized = (rawDegrees + 360.0) % 360.0
                        if (selectingHour) {
                            val index = (kotlin.math.round(normalized / 30.0).toInt() % 12)
                            val hour12 = if (index == 0) 12 else index
                            val nextHour = if (isPm) hour12 % 12 + 12 else hour12 % 12
                            onHourChange(nextHour)
                            selectingHour = false
                        } else {
                            val nextMinute = kotlin.math.round(normalized / 6.0).toInt() % 60
                            onMinuteChange(nextMinute)
                        }
                    }
                }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outer = size.minDimension * 0.475f
                val selectorIndex = if (selectingHour) hour % 12 else minute
                val selectorDegrees = (if (selectingHour) selectorIndex * 30.0 else selectorIndex * 6.0) - 90.0
                val selectorRadians = selectorDegrees * PI / 180.0
                val selectorRadius = if (selectingHour) outer * 0.76f else outer * 0.9f
                val end = Offset(cx + cos(selectorRadians).toFloat() * selectorRadius, cy + sin(selectorRadians).toFloat() * selectorRadius)

                drawCircle(Color(0xFF171E31), radius = outer, center = Offset(cx, cy))
                drawCircle(BrandViolet.copy(alpha = 0.32f), radius = outer, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
                for (tick in 0 until 60) {
                    val angle = (tick * 6.0 - 90.0) * PI / 180.0
                    val major = tick % 5 == 0
                    val r1 = outer * if (major) 0.91f else 0.95f
                    val r2 = outer * 0.985f
                    drawLine(
                        color = if (major) TextGray.copy(alpha = 0.65f) else TextMuted.copy(alpha = 0.28f),
                        start = Offset(cx + cos(angle).toFloat() * r1, cy + sin(angle).toFloat() * r1),
                        end = Offset(cx + cos(angle).toFloat() * r2, cy + sin(angle).toFloat() * r2),
                        strokeWidth = if (major) 1.5.dp.toPx() else 0.7.dp.toPx()
                    )
                }
                drawLine(BrandViolet, Offset(cx, cy), end, strokeWidth = 3.dp.toPx())
                drawCircle(BrandViolet, radius = 13.dp.toPx(), center = end)
                drawCircle(TextWhite, radius = 4.dp.toPx(), center = Offset(cx, cy))
            }
            for (index in 0 until 12) {
                val angle = (index * 30.0 - 90.0) * PI / 180.0
                val dx = labelRadius * cos(angle).toFloat()
                val dy = labelRadius * sin(angle).toFloat()
                val labelValue = if (selectingHour) {
                    if (index == 0) "۱۲" else persianDigits(index.toString())
                } else {
                    persianDigits("%02d".format(index * 5))
                }
                val selected = if (selectingHour) hour % 12 == index else minute / 5 == index && minute % 5 == 0
                Box(
                    Modifier.offset(x = center + dx - 14.dp, y = center + dy - 14.dp).size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(labelValue, color = if (selected) TextWhite else TextGray, fontSize = if (selectingHour) 11.sp else 8.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontFamily = Vazirmatn)
                }
            }
        }
        Text(
            if (selectingHour) "عدد ساعت را انتخاب کن؛ سپس صفحه دقیقه باز می‌شود." else "برای ثبت دقیقه، هر نقطه از صفحه ساعت را لمس کن.",
            color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DatePlaceMapPicker(
    onDismiss: () -> Unit,
    onConfirm: (latitude: Double, longitude: Double) -> Unit
) {
    val context = LocalContext.current
    var mapViewState by remember { mutableStateOf<MapView?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).heightIn(min = 520.dp, max = 720.dp),
            color = DarkSurface,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
        ) {
            Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TinderPink)
                    Spacer(Modifier.width(7.dp))
                    Column(Modifier.weight(1f)) {
                        Text("انتخاب محل قرار", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Vazirmatn)
                        Text("نقشه را جابه‌جا کن تا نقطهٔ موردنظر زیر نشانگر قرار بگیرد.", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "بستن", tint = TextGray) }
                }
                Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(17.dp))) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                            Configuration.getInstance().userAgentValue = "FastLove-Android/2.18.0 (ir.fastlove.demo)"
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                setUseDataConnection(true)
                                controller.setZoom(14.5)
                                controller.setCenter(GeoPoint(35.69439, 51.42151))
                                onResume()
                                mapViewState = this
                            }
                        },
                        update = { }
                    )
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = TinderPink,
                        modifier = Modifier.align(Alignment.Center).size(42.dp).offset(y = (-17).dp)
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                        shape = RoundedCornerShape(20.dp), color = Color(0xE8171D31)
                    ) {
                        Text("نشانگر ثابت است؛ نقشه را زیر آن حرکت بده", Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = TextWhite, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("انصراف", color = TextGray, fontFamily = Vazirmatn)
                    }
                    Button(
                        onClick = {
                            val point = mapViewState?.mapCenter
                            if (point != null) onConfirm(point.latitude, point.longitude)
                            else onConfirm(35.69439, 51.42151)
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = TinderPink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(5.dp))
                        Text("ثبت این موقعیت", fontFamily = Vazirmatn)
                    }
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mapViewState?.onPause()
            mapViewState?.onDetach()
            mapViewState = null
        }
    }
}

@Composable
private fun ChatScreen(person: Person, canUnmatch: Boolean, canSeeLikes: Boolean, onBack: () -> Unit, onUnmatch: () -> Unit) {
    val context = LocalContext.current
    val storedMessages = remember(person.id) { loadChatMessages(context, person.id) }
    val messages = remember(person.id) {
        mutableStateListOf<ChatMessage>().apply {
            if (storedMessages.isNotEmpty()) addAll(storedMessages)
            else {
                add(ChatMessage("سلام! خوشحالم که مچ شدیم 😊", false, "۱۰:۳۰"))
            }
        }
    }
    val dateProposals = remember(person.id) {
        mutableStateListOf<DateProposal>().apply { addAll(loadDateProposals(context, person.id)) }
    }
    var showDateProposalDialog by remember { mutableStateOf(false) }
    var dateDialogIsRevision by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingStartedAt by remember { mutableLongStateOf(0L) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }
    var voiceError by remember { mutableStateOf("") }
    var showChatMenu by remember { mutableStateOf(false) }
    var showChatProfile by remember(person.id) { mutableStateOf(false) }
    var showUnmatchConfirm by remember { mutableStateOf(false) }
    var pendingDemoReplies by remember { mutableIntStateOf(0) }
    val chatScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    // A pending change takes precedence over the currently confirmed date. If a change is
    // declined, the previous accepted date remains active; cancelled dates are not shown.
    val activeDateProposal = dateProposals.filter { it.status == DateProposalStatus.PENDING }
        .maxByOrNull { it.createdAtMillis }
        ?: dateProposals.filter { it.status == DateProposalStatus.ACCEPTED }
            .maxByOrNull { it.createdAtMillis }

    fun updateDateProposal(updated: DateProposal) {
        val index = dateProposals.indexOfFirst { it.id == updated.id }
        if (index >= 0) dateProposals[index] = updated else dateProposals.add(updated)
        if (updated.status == DateProposalStatus.ACCEPTED && !updated.revisesProposalId.isNullOrBlank()) {
            val previousIndex = dateProposals.indexOfFirst { it.id == updated.revisesProposalId }
            if (previousIndex >= 0) {
                dateProposals[previousIndex] = dateProposals[previousIndex].copy(status = DateProposalStatus.CANCELLED)
            }
        }
        saveDateProposals(context, person.id, dateProposals.toList())
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selected ->
            val local = storeUriInAppFiles(context, selected, "chat_images")
            if (local != null) {
                messages.add(ChatMessage(local, true, SimpleDateFormat("HH:mm", Locale("fa")).format(Date()), MsgType.IMAGE))
                saveChatMessages(context, person.id, messages.toList())
            }
        }
    }

    fun startRecording() {
        try {
            val file = createPrivateAudioFile(context, "chat_voice_${person.id}")
            recorder = createAudioRecorder(context, file)
            recordingFile = file
            recordingStartedAt = System.currentTimeMillis()
            isRecording = true
            voiceError = "در حال ضبط پیام صوتی..."
        } catch (_: Exception) {
            recorder?.release()
            recorder = null
            recordingFile?.delete()
            recordingFile = null
            isRecording = false
            voiceError = "ضبط صدا شروع نشد؛ مجوز میکروفون را بررسی کن."
        }
    }

    fun stopRecording() {
        val active = recorder
        val file = recordingFile
        try {
            active?.stop()
            val duration = System.currentTimeMillis() - recordingStartedAt
            if (file != null && file.exists() && file.length() > 500 && duration >= 700) {
                messages.add(ChatMessage(file.absolutePath, true, SimpleDateFormat("HH:mm", Locale("fa")).format(Date()), MsgType.VOICE))
                saveChatMessages(context, person.id, messages.toList())
                voiceError = "پیام صوتی ذخیره شد و آماده پخش است."
            } else {
                file?.delete()
                voiceError = "پیام خیلی کوتاه بود؛ برای ضبط دوباره نگه دار."
            }
        } catch (_: Exception) {
            file?.delete()
            voiceError = "ذخیره پیام صوتی ناموفق بود."
        } finally {
            active?.release()
            recorder = null
            recordingFile = null
            isRecording = false
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording() else voiceError = "برای ارسال پیام صوتی، دسترسی میکروفون لازم است."
    }

    fun toggleVoicePlayback(message: ChatMessage) {
        val key = message.text
        try {
            if (playingVoiceKey == key) {
                player?.pause()
                playingVoiceKey = null
            } else {
                player?.release()
                val media = MediaPlayer()
                media.setDataSource(message.text)
                media.setOnCompletionListener {
                    playingVoiceKey = null
                    it.release()
                    player = null
                }
                media.prepare()
                media.start()
                player = media
                playingVoiceKey = key
                voiceError = ""
            }
        } catch (_: Exception) {
            player?.release()
            player = null
            playingVoiceKey = null
            voiceError = "فایل صوتی در دسترس نیست یا قابل پخش نیست."
        }
    }

    LaunchedEffect(person.id, messages.size) {
        if (messages.isNotEmpty()) {
            saveChatMessages(context, person.id, messages.toList())
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    DisposableEffect(person.id) {
        onDispose {
            if (isRecording) stopRecording()
            else recorder?.let { active -> runCatching { active.stop() }; active.release() }
            player?.release()
        }
    }

    if (showChatProfile) {
        PersonDetailScreen(
            person = person, canSeeLikes = canSeeLikes, isMatched = canUnmatch, canDirectMessage = true,
            onBack = { showChatProfile = false }, onLike = { showChatProfile = false },
            onPass = { showChatProfile = false }, onBlock = { showChatProfile = false },
            onChat = { showChatProfile = false }, onReport = { showChatProfile = false }
        )
    } else Column(Modifier.fillMaxSize().background(DarkBg).imePadding()) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { showChatProfile = true }.padding(vertical = 4.dp, horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(person.tint), contentAlignment = Alignment.Center) { Text(person.emoji, fontSize = 22.sp) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(person.name, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (person.showOnlineStatus) {
                            Box(Modifier.size(7.dp).background(if (person.isOnline) SuccessGreen else TextMuted, CircleShape))
                            Spacer(Modifier.width(5.dp))
                            Text(if (person.isOnline) "آنلاین" else "آفلاین", color = if (person.isOnline) SuccessGreen else TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                        } else {
                            Text("وضعیت حضور پنهان است", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                        }
                    }
                    if (canUnmatch && person.showLastSeen && !person.isOnline) {
                        Text("آخرین بازدید: ${formatLastSeen(person.lastSeenMinutesAgo)}", color = BrandViolet, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                }
            }
            Box {
                IconButton(onClick = { showChatMenu = true }) { Icon(Icons.Default.MoreVert, "گزینه‌های گفتگو", tint = TextGray) }
                DropdownMenu(expanded = showChatMenu, onDismissRequest = { showChatMenu = false }) {
                    if (canUnmatch) DropdownMenuItem(
                        text = { Text("لغو مچ و حذف گفتگو", color = Color(0xFFFF7777), fontFamily = Vazirmatn, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFFF7777)) },
                        onClick = { showChatMenu = false; showUnmatchConfirm = true }
                    )
                    DropdownMenuItem(
                        text = { Text("بازگشت به گفتگوها", fontFamily = Vazirmatn, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.ArrowBack, null, tint = TextGray) },
                        onClick = { showChatMenu = false; onBack() }
                    )
                }
            }
        }
        if (canUnmatch) {
            if (activeDateProposal != null) {
                val proposal = activeDateProposal
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                    colors = CardDefaults.cardColors(containerColor = if (proposal.status == DateProposalStatus.ACCEPTED) Color(0xFF17352F) else DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (proposal.status == DateProposalStatus.ACCEPTED) SuccessGreen.copy(alpha = 0.55f) else TinderOrange.copy(alpha = 0.38f))
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (proposal.status == DateProposalStatus.ACCEPTED) Icons.Default.PushPin else Icons.Default.Event, null, tint = if (proposal.status == DateProposalStatus.ACCEPTED) SuccessGreen else TinderOrange, modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(if (proposal.status == DateProposalStatus.ACCEPTED) "دیت اول هماهنگ شد" else if (proposal.isRevision) "پیشنهاد اصلاح دیت" else "پیشنهاد دیت اول", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Vazirmatn)
                            Spacer(Modifier.weight(1f))
                            Text(if (proposal.status == DateProposalStatus.ACCEPTED) "سنجاق‌شده" else "در انتظار تأیید", color = if (proposal.status == DateProposalStatus.ACCEPTED) SuccessGreen else TinderOrange, fontSize = 9.sp, fontFamily = Vazirmatn)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(proposal.place, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = Vazirmatn)
                        Text(formatDateTimePersian(proposal.scheduledAtMillis), color = TextGray, fontSize = 10.sp, fontFamily = Vazirmatn)
                        if (proposal.locationLatitude != null && proposal.locationLongitude != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, null, tint = SuccessGreen, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("موقعیت دقیق انتخاب‌شده روی نقشه پیوست است", color = SuccessGreen, fontSize = 9.sp, fontFamily = Vazirmatn)
                            }
                        }
                        if (proposal.status == DateProposalStatus.PENDING) {
                            Spacer(Modifier.height(8.dp))
                            if (proposal.proposerIsMe) {
                                Text(if (proposal.isRevision) "پیشنهاد اصلاح برای ${person.name} ارسال شده و منتظر تأیید است." else "پیشنهاد برای ${person.name} ارسال شده و منتظر پاسخ است.", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                                // In this offline demo, this control simulates the other person's approval.
                                TextButton(onClick = { updateDateProposal(proposal.copy(status = DateProposalStatus.ACCEPTED)) }) {
                                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (proposal.isRevision) "تأیید اصلاح توسط طرف مقابل (دمو)" else "تأیید طرف مقابل (دمو)", color = SuccessGreen, fontSize = 10.sp, fontFamily = Vazirmatn)
                                }
                            } else {
                                Text(if (proposal.isRevision) "${person.name} اصلاح زمان یا مکان را پیشنهاد داده است." else "${person.name} این زمان و مکان را پیشنهاد داده است.", color = TextGray, fontSize = 10.sp, fontFamily = Vazirmatn)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { updateDateProposal(proposal.copy(status = DateProposalStatus.DECLINED)) },
                                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.dp, TextMuted)
                                    ) { Text("رد پیشنهاد", color = TextGray, fontSize = 10.sp, fontFamily = Vazirmatn) }
                                    Button(
                                        onClick = { updateDateProposal(proposal.copy(status = DateProposalStatus.ACCEPTED)) },
                                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) { Icon(Icons.Default.Check, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("تأیید و سنجاق", fontSize = 10.sp, fontFamily = Vazirmatn) }
                                }
                            }
                        } else if (proposal.status == DateProposalStatus.ACCEPTED) {
                            Spacer(Modifier.height(9.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { updateDateProposal(proposal.copy(status = DateProposalStatus.CANCELLED)) },
                                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF7777))
                                ) {
                                    Icon(Icons.Default.EventBusy, null, tint = Color(0xFFFF7777), modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("لغو دیت", color = Color(0xFFFF7777), fontSize = 10.sp, fontFamily = Vazirmatn)
                                }
                                Button(
                                    onClick = { dateDialogIsRevision = true; showDateProposalDialog = true },
                                    modifier = Modifier.weight(1.25f), shape = RoundedCornerShape(11.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet)
                                ) {
                                    Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("پیشنهاد اصلاح", fontSize = 10.sp, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                }
            }
            if (activeDateProposal == null) {
                OutlinedButton(
                    onClick = { dateDialogIsRevision = false; showDateProposalDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.52f)),
                    contentPadding = PaddingValues(vertical = 7.dp)
                ) {
                    Icon(Icons.Default.EventAvailable, null, tint = TinderPink, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("پیشنهاد زمان و مکان دیت اول", color = TextWhite, fontSize = 11.sp, fontFamily = Vazirmatn)
                }
            }
        } else {
            Text("برای برنامه‌ریزی دیت اول، ابتدا باید با هم مچ شوید.", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp))
        }
        if (showDateProposalDialog) {
            DateProposalDialog(
                personName = person.name,
                isRevision = dateDialogIsRevision,
                onDismiss = { showDateProposalDialog = false },
                onSubmit = { place, scheduledAt, latitude, longitude ->
                    val proposal = DateProposal(
                        id = UUID.randomUUID().toString(),
                        personId = person.id,
                        place = place,
                        scheduledAtMillis = scheduledAt,
                        proposerIsMe = true,
                        isRevision = dateDialogIsRevision,
                        revisesProposalId = if (dateDialogIsRevision) activeDateProposal?.id else null,
                        locationLatitude = latitude,
                        locationLongitude = longitude
                    )
                    updateDateProposal(proposal)
                    val mapLabel = if (latitude != null && longitude != null) " · موقعیت روی نقشه هم پیوست شد" else ""
                    messages.add(ChatMessage("${if (dateDialogIsRevision) "پیشنهاد اصلاح دیت" else "پیشنهاد دیت اول"}: $place · ${formatDateTimePersian(scheduledAt)}$mapLabel", true, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                    saveChatMessages(context, person.id, messages.toList())
                    showDateProposalDialog = false
                    dateDialogIsRevision = false
                }
            )
        }
        if (showUnmatchConfirm) AlertDialog(
            onDismissRequest = { showUnmatchConfirm = false },
            title = { Text("لغو مچ با ${person.name}؟", fontFamily = Vazirmatn, color = TextWhite) },
            text = { Text("این ارتباط از فهرست مچ‌ها حذف می‌شود و تمام پیام‌های این گفتگو نیز از این دستگاه پاک خواهد شد. این کار قابل بازگشت نیست.", fontFamily = Vazirmatn, color = TextGray, lineHeight = 22.sp) },
            confirmButton = {
                TextButton(onClick = { showUnmatchConfirm = false; onUnmatch() }) {
                    Text("لغو مچ و حذف پیام‌ها", color = Color(0xFFFF7777), fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                }
            },
            dismissButton = { TextButton(onClick = { showUnmatchConfirm = false }) { Text("انصراف", color = TextGray, fontFamily = Vazirmatn) } },
            containerColor = DarkSurface
        )
        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(messages) { msg ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isMe) Arrangement.Start else Arrangement.End) {
                    Surface(shape = RoundedCornerShape(16.dp), color = if (msg.isMe) TinderPink else DarkCard, modifier = Modifier.widthIn(max = 280.dp)) {
                        when (msg.type) {
                            MsgType.TEXT -> Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(msg.text, color = TextWhite, fontFamily = Vazirmatn, fontSize = 15.sp)
                                Text(msg.time, color = TextWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.align(Alignment.End))
                            }
                            MsgType.IMAGE -> Column(Modifier.padding(8.dp)) {
                                AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(msg.text)).crossfade(true).build(), contentDescription = null, modifier = Modifier.size(180.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                Text(msg.time, color = TextWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 4.dp))
                            }
                            MsgType.VOICE -> {
                                val playable = File(msg.text).exists()
                                val playing = playingVoiceKey == msg.text
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp).clickable(enabled = playable) { toggleVoicePlayback(msg) }, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = if (playable) TextWhite else TextMuted, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(if (playable) "پیام صوتی" else "فایل صوتی در دسترس نیست", color = TextWhite, fontSize = 12.sp, fontFamily = Vazirmatn)
                                        Box(Modifier.width(112.dp).height(4.dp).background(if (playing) SuccessGreen else TextWhite.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                                        Text(if (playing) "در حال پخش · ${msg.time}" else msg.time, color = TextWhite.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = Vazirmatn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (pendingDemoReplies > 0) {
            Text("${person.name} در حال نوشتن است…", color = SuccessGreen, fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 3.dp))
        }
        if (voiceError.isNotBlank()) Text(voiceError, color = if (voiceError.contains("ذخیره شد")) SuccessGreen else TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(8.dp), verticalAlignment = Alignment.Bottom) {
            IconButton(onClick = { galleryLauncher.launch("image/*") }) { Icon(Icons.Default.Image, "عکس", tint = TinderPink) }
            IconButton(onClick = {
                if (isRecording) stopRecording()
                else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording()
                else microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }) {
                Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, "ضبط پیام صوتی", tint = if (isRecording) TinderPink else TextGray)
            }
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text(if (isRecording) "در حال ضبط... برای پایان، توقف را بزن" else "پیام بنویس...", fontFamily = Vazirmatn) }, shape = RoundedCornerShape(24.dp), colors = fieldColors(), maxLines = 4, enabled = !isRecording)
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = {
                if (input.isNotBlank() && !isRecording) {
                    val sentText = input.trim()
                    messages.add(ChatMessage(sentText, true, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                    saveChatMessages(context, person.id, messages.toList())
                    input = ""; keyboard?.hide()
                    pendingDemoReplies += 1
                    chatScope.launch {
                        delay(900L + (person.id % 5) * 180L)
                        val reply = buildDemoReply(sentText, person, messages.toList())
                        messages.add(ChatMessage(reply, false, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                        val normalizedText = sentText.lowercase()
                        val confirmedDateForRevision = dateProposals
                            .filter { it.status == DateProposalStatus.ACCEPTED }
                            .maxByOrNull { it.createdAtMillis }
                        val hasPendingDateProposal = dateProposals.any { it.status == DateProposalStatus.PENDING }
                        val asksForRevision = normalizedText.contains("اصلاح") || normalizedText.contains("تغییر") || normalizedText.contains("جابجا") || normalizedText.contains("عوض")
                        val asksForDate = normalizedText.contains("قرار") || normalizedText.contains("دیت") || normalizedText.contains("کافه")
                        if (canUnmatch && asksForRevision && confirmedDateForRevision != null && !hasPendingDateProposal) {
                            val proposedDate = Calendar.getInstance().apply {
                                timeInMillis = confirmedDateForRevision.scheduledAtMillis
                                add(Calendar.DAY_OF_YEAR, 1)
                                set(Calendar.HOUR_OF_DAY, 19)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            updateDateProposal(
                                DateProposal(
                                    id = UUID.randomUUID().toString(),
                                    personId = person.id,
                                    place = "پیشنهاد تغییر مکان نزدیک ${person.city}",
                                    scheduledAtMillis = proposedDate,
                                    proposerIsMe = false,
                                    isRevision = true,
                                    revisesProposalId = confirmedDateForRevision.id
                                )
                            )
                            messages.add(ChatMessage("${person.name} یک پیشنهاد اصلاح برای زمان یا مکان دیت فرستاده؛ بالای گفتگو بررسی کن 😊", false, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                        } else if (canUnmatch && asksForDate && confirmedDateForRevision == null && !hasPendingDateProposal) {
                            val proposedDate = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, 2)
                                set(Calendar.HOUR_OF_DAY, 18)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            updateDateProposal(
                                DateProposal(
                                    id = UUID.randomUUID().toString(),
                                    personId = person.id,
                                    place = "کافه‌ای نزدیک ${person.city}",
                                    scheduledAtMillis = proposedDate,
                                    proposerIsMe = false
                                )
                            )
                            messages.add(ChatMessage("من هم یک پیشنهاد برای دیت اول دارم؛ بالای گفتگو ببین 😊", false, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                        }
                        saveChatMessages(context, person.id, messages.toList())
                        pendingDemoReplies = (pendingDemoReplies - 1).coerceAtLeast(0)
                    }
                }
            }, enabled = !isRecording, colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink)) {
                Icon(Icons.Default.Send, null)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonDetailScreen(
    person: Person, canSeeLikes: Boolean, isMatched: Boolean, canDirectMessage: Boolean, isLiked: Boolean = false,
    onBack: () -> Unit, onLike: () -> Unit, onPass: () -> Unit, onBlock: () -> Unit, onChat: () -> Unit, onReport: () -> Unit
) {
    val galleryLabels = remember(person.id, person.interests) {
        listOf("پروفایل", person.interests.getOrNull(0) ?: "علایق", person.interests.getOrNull(1) ?: "زندگی روزمره")
    }
    val gallerySymbols = remember(person.id, person.interests) {
        fun symbolFor(value: String): String = when {
            value.contains("کافه") || value.contains("قهوه") -> "☕"
            value.contains("موسیقی") -> "🎧"
            value.contains("سفر") || value.contains("گردش") -> "✈️"
            value.contains("کتاب") -> "📚"
            value.contains("فیلم") -> "🎬"
            value.contains("ورزش") -> "🏃"
            value.contains("طبیعت") || value.contains("کوه") -> "🌿"
            value.contains("عکاسی") || value.contains("هنر") -> "📷"
            value.contains("آشپزی") || value.contains("غذا") -> "🍓"
            else -> person.emoji
        }
        listOf(person.emoji, symbolFor(person.interests.getOrNull(0).orEmpty()), symbolFor(person.interests.getOrNull(1).orEmpty()))
    }
    val pagerState = rememberPagerState(pageCount = { galleryLabels.size })

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("پروفایل", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
                Text("نمایی که قبل از آشنایی می‌بینی", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            }
            IconButton(onClick = onReport) { Icon(Icons.Default.Flag, "گزارش", tint = TextGray) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
                    .height(448.dp).clip(RoundedCornerShape(24.dp))
            ) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val pageColor = when (page) {
                        0 -> person.tint
                        1 -> Color(0xFF51416E)
                        else -> Color(0xFF315B68)
                    }
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(pageColor, Color(0xFF141B30), DarkBg))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(270.dp).background(
                                Brush.radialGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)), CircleShape
                            )
                        )
                        Text(gallerySymbols[page], fontSize = if (page == 0) 132.sp else 106.sp)
                        Surface(
                            Modifier.align(Alignment.TopCenter).padding(top = 15.dp),
                            shape = RoundedCornerShape(18.dp), color = Color.Black.copy(alpha = 0.25f)
                        ) {
                            Text(
                                if (page == 0) "عکس اصلی دمو" else galleryLabels[page],
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = TextWhite.copy(alpha = 0.92f), fontSize = 10.sp, fontFamily = Vazirmatn
                            )
                        }
                    }
                }
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(172.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.84f))))
                )
                Column(Modifier.align(Alignment.BottomStart).padding(19.dp)) {
                    Text(
                        "${person.name}, ${persianDigits(person.age.toString())}",
                        color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 25.sp, fontFamily = Vazirmatn
                    )
                    Text(
                        "${person.job} · ${person.city} · ${persianDigits(person.distanceKm.toString())} کیلومتر",
                        color = TextWhite.copy(alpha = 0.9f), fontSize = 12.sp, fontFamily = Vazirmatn
                    )
                }
                Surface(
                    Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        "${persianDigits((pagerState.currentPage + 1).toString())} / ${persianDigits(galleryLabels.size.toString())}",
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn
                    )
                }
                if (person.showOnlineStatus) OnlineStatusBadge(isOnline = person.isOnline, modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
                if (person.likedYou && canSeeLikes) {
                    Surface(
                        Modifier.align(Alignment.BottomEnd).padding(14.dp),
                        shape = RoundedCornerShape(20.dp), color = TinderPink
                    ) {
                        Text("❤️ تو را لایک کرده", Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                repeat(galleryLabels.size) { page ->
                    Box(
                        Modifier.padding(horizontal = 3.dp).width(if (pagerState.currentPage == page) 20.dp else 6.dp)
                            .height(5.dp).clip(CircleShape)
                            .background(if (pagerState.currentPage == page) TinderPink else TextMuted.copy(alpha = 0.65f))
                    )
                }
            }

            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("درباره ${person.name}", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(8.dp))
                    Text(person.about, color = TextGray, fontFamily = Vazirmatn, lineHeight = 22.sp, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        InfoChip(Icons.Default.Work, person.job)
                        InfoChip(Icons.Default.LocationOn, person.city)
                    }
                }
            }

            if (person.interests.isNotEmpty()) {
                Text("علایق", Modifier.padding(horizontal = 18.dp), color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                FlowRow(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    person.interests.forEach { interest ->
                        Surface(
                            shape = RoundedCornerShape(20.dp), color = TinderPink.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.32f))
                        ) {
                            Text(interest, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = TextWhite, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
            if (person.instagram.isNotBlank() || person.spotify.isNotBlank()) {
                Text("شبکه‌های اجتماعی", Modifier.padding(horizontal = 18.dp), color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                SocialProfileBadges(
                    instagram = person.instagram, spotify = person.spotify,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }

            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    if (!isMatched) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onPass, modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.5.dp, TextGray)
                            ) {
                                Icon(Icons.Default.Close, null, tint = TextGray); Spacer(Modifier.width(6.dp))
                                Text("رد کردن", fontFamily = Vazirmatn, color = TextGray)
                            }
                            Button(
                                onClick = onLike,
                                enabled = !isLiked,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TinderPink,
                                    disabledContainerColor = SuccessGreen.copy(alpha = 0.22f),
                                    disabledContentColor = SuccessGreen
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (isLiked) "لایک شده" else "لایک کن", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    Button(
                        onClick = onChat, modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (canDirectMessage) SuccessGreen else TinderOrange),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            when { isMatched -> "چت"; canDirectMessage -> "چت (پرداخت‌شده)"; else -> "پیام مستقیم" },
                            fontFamily = Vazirmatn, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onBlock, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFFF6B6B))
                    ) {
                        Text("بلاک", color = Color(0xFFFF6B6B), fontFamily = Vazirmatn)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── My Profile — modern redesign ─────────────────────────────────────────────
private fun socialProfileUrl(platform: String, rawValue: String): String {
    val value = rawValue.trim()
    if (value.startsWith("https://", true) || value.startsWith("http://", true)) return value
    return when (platform) {
        "instagram" -> "https://www.instagram.com/${value.removePrefix("@").trim('/')}/"
        else -> "https://open.spotify.com/user/${value.trim('/') }"
    }
}

@Composable
private fun SocialProfileBadges(
    instagram: String,
    spotify: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val context = LocalContext.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        if (instagram.isNotBlank()) {
            Surface(
                onClick = { runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(socialProfileUrl("instagram", instagram)))) } },
                shape = RoundedCornerShape(18.dp), color = Color(0xFFB84E83).copy(alpha = 0.18f), border = BorderStroke(1.dp, Color(0xFFDB78A6).copy(alpha = 0.40f))
            ) {
                Row(Modifier.padding(horizontal = if (compact) 7.dp else 10.dp, vertical = if (compact) 4.dp else 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color(0xFFFF9FC4), modifier = Modifier.size(if (compact) 12.dp else 16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (instagram.startsWith("http", true)) "Instagram" else "@${instagram.removePrefix("@")}", modifier = Modifier.widthIn(max = if (compact) 98.dp else 125.dp), color = TextWhite, fontSize = if (compact) 9.sp else 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = Vazirmatn)
                }
            }
        }
        if (spotify.isNotBlank()) {
            Surface(
                onClick = { runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(socialProfileUrl("spotify", spotify)))) } },
                shape = RoundedCornerShape(18.dp), color = SuccessGreen.copy(alpha = 0.13f), border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
            ) {
                Row(Modifier.padding(horizontal = if (compact) 7.dp else 10.dp, vertical = if (compact) 4.dp else 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, null, tint = SuccessGreen, modifier = Modifier.size(if (compact) 12.dp else 16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (spotify.startsWith("http", true)) "Spotify" else spotify, modifier = Modifier.widthIn(max = if (compact) 98.dp else 125.dp), color = TextWhite, fontSize = if (compact) 9.sp else 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = Vazirmatn)
                }
            }
        }
    }
}

@Composable
private fun MyProfileScreen(
    user: UserProfile,
    upcomingDates: List<Pair<Person, DateProposal>>,
    blockedCount: Int,
    plan: SubscriptionPlan,
    planExpiryMillis: Long?,
    matchCount: Int,
    likeCount: Int,
    onUpgrade: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onPaymentHistory: () -> Unit,
    onPrivacySettings: () -> Unit,
    onScheduledDates: () -> Unit,
    onShowBlocked: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val referralCode = user.referralCode.ifBlank { "FL${user.phone.takeLast(6).ifBlank { "FASTLOVE" }}" }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(DarkBg).padding(bottom = 22.dp)) {
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(76.dp).clip(CircleShape).background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                        if (user.photos.isNotEmpty()) {
                            AsyncImage(
                                model = user.photos.getOrNull(user.primaryPhotoIndex.coerceIn(0, user.photos.lastIndex)),
                                contentDescription = "عکس پروفایل",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else Text(user.name.take(1).ifEmpty { "♡" }, fontSize = 32.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user.name.ifBlank { "پروفایل من" }, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                        Text("${persianDigits(user.age.toString())} ساله · ${user.city.ifBlank { "شهر ثبت نشده" }}", color = TextGray, fontSize = 12.sp, fontFamily = Vazirmatn)
                        Spacer(Modifier.height(7.dp))
                        Text("${user.photos.size} عکس · ${user.interests.size} علاقه‌مندی", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                    }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "ویرایش پروفایل", tint = TinderPink) }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileMiniStat("مچ‌ها", matchCount.toString(), Modifier.weight(1f))
                    ProfileMiniStat("لایک‌ها", likeCount.toString(), Modifier.weight(1f))
                    ProfileMiniStat("دیت‌ها", upcomingDates.size.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onPreview,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("پیش‌نمایش پروفایل عمومی", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(TinderOrange.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Star, null, tint = TinderOrange, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("اشتراک ${plan.title}", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, fontSize = 13.sp)
                    Text(
                        if (plan == SubscriptionPlan.FREE) "امکانات بیشتر با ارتقای اشتراک" else planExpiryMillis?.let { "فعال تا ${formatDatePersian(it)}" } ?: "اشتراک فعال",
                        color = if (plan == SubscriptionPlan.FREE) TextMuted else SuccessGreen,
                        fontSize = 10.sp, fontFamily = Vazirmatn
                    )
                }
                TextButton(onClick = onUpgrade) {
                    Text(if (plan == SubscriptionPlan.FREE) "ارتقا" else "مدیریت", color = TinderOrange, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(19.dp),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.22f))
        ) {
            Column(Modifier.fillMaxWidth().padding(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(BrandViolet.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CardGiftcard, null, tint = BrandViolet, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("دعوت دوستان با کد معرف", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        Text("کدت را کپی کن یا برای دوستانت بفرست", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF10172A),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.35f))
                ) {
                    Text(
                        referralCode,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = TextWhite,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FastLove referral code", referralCode))
                            android.widget.Toast.makeText(context, "کد معرف کپی شد", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = BrandViolet, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("کپی کد", color = TextWhite, fontFamily = Vazirmatn, fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "من در فست‌لاو هستم 💜 با کد معرف $referralCode ثبت‌نام کن.")
                            }
                            runCatching { context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری کد معرف")) }
                                .onFailure { android.widget.Toast.makeText(context, "اشتراک‌گذاری در دسترس نیست", android.widget.Toast.LENGTH_SHORT).show() }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("اشتراک‌گذاری", fontFamily = Vazirmatn, fontSize = 10.sp)
                    }
                }
                if (user.referredByCode.isNotBlank()) {
                    Text("با کد ${user.referredByCode} دعوت شده‌ای.", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 7.dp))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("تنظیمات و برنامه‌ها", Modifier.padding(horizontal = 20.dp), color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, fontSize = 14.sp)
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column {
                ProfileActionRow(Icons.Default.Security, "حریم خصوصی", BrandViolet, onPrivacySettings)
                HorizontalDivider(color = DarkCard)
                ProfileActionRow(Icons.Default.EventAvailable, "دیت‌های برنامه‌ریزی‌شده · ${persianDigits(upcomingDates.size.toString())}", SuccessGreen, onScheduledDates)
                HorizontalDivider(color = DarkCard)
                ProfileActionRow(Icons.Default.Edit, "ویرایش اطلاعات و عکس‌ها", TinderPink, onEdit)
                HorizontalDivider(color = DarkCard)
                ProfileActionRow(Icons.Default.ReceiptLong, "تاریخچه پرداخت", TinderOrange, onPaymentHistory)
                HorizontalDivider(color = DarkCard)
                ProfileActionRow(Icons.Default.Block, "کاربران مسدودشده · ${persianDigits(blockedCount.toString())}", TextMuted, onShowBlocked)
            }
        }
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, TinderPink)
        ) {
            Icon(Icons.Default.Logout, null, tint = TinderPink)
            Spacer(Modifier.width(8.dp))
            Text("خروج از حساب کاربری", color = TinderPink, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PrivacySettingsScreen(
    user: UserProfile,
    isVisibleOnMap: Boolean,
    onToggleOnlineStatus: (Boolean) -> Unit,
    onToggleLastSeen: (Boolean) -> Unit,
    onToggleMapVisibility: (Boolean) -> Unit,
    onDeleteRequest: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    val mapPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onToggleMapVisibility(true)
        else android.widget.Toast.makeText(context, "برای نمایش روی نقشه، دسترسی تقریبی موقعیت لازم است.", android.widget.Toast.LENGTH_LONG).show()
    }
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("حریم خصوصی", color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Text("کنترل کن دیگران چه اطلاعاتی از تو ببینند", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            }
            Icon(Icons.Default.Security, null, tint = BrandViolet, modifier = Modifier.padding(end = 12.dp).size(22.dp))
        }
        Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(20.dp)) {
            Column {
                PrivacySwitchRow(Icons.Default.Circle, "نمایش وضعیت آنلاین", if (user.showOnlineStatus) "دیگران می‌توانند آنلاین بودنت را ببینند" else "وضعیت آنلاین تو پنهان است", SuccessGreen, user.showOnlineStatus, onToggleOnlineStatus)
                HorizontalDivider(color = DarkCard)
                PrivacySwitchRow(Icons.Default.History, "نمایش آخرین بازدید", if (user.showLastSeen) "فقط برای مچ‌ها قابل مشاهده است" else "زمان آخرین بازدید پنهان است", BrandViolet, user.showLastSeen, onToggleLastSeen)
                HorizontalDivider(color = DarkCard)
                PrivacySwitchRow(
                    Icons.Default.Map,
                    "نمایش پروفایل من روی نقشه",
                    if (isVisibleOnMap) "موقعیت تقریبی تو فعال است" else "موقعیت تو برای دیگران مخفی است",
                    TinderPink,
                    isVisibleOnMap,
                    { enabled ->
                        if (enabled && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            mapPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        } else onToggleMapVisibility(enabled)
                    }
                )
            }
        }
        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color(0x55FF7777))) {
            Column(Modifier.padding(14.dp)) {
                Text("حذف حساب کاربری", color = Color(0xFFFF7777), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Spacer(Modifier.height(4.dp))
                Text("با حذف حساب، اطلاعات محلی این حساب از دستگاه پاک می‌شود.", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                TextButton(onClick = onDeleteRequest, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFFF7777))
                    Spacer(Modifier.width(5.dp))
                    Text("حذف حساب", color = Color(0xFFFF7777), fontFamily = Vazirmatn)
                }
            }
        }
    }
}

@Composable
private fun PrivacySwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = Vazirmatn)
            Text(subtitle, color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = tint, uncheckedThumbColor = TextGray, uncheckedTrackColor = DarkCard))
    }
}

@Composable
private fun ScheduledDatesScreen(upcomingDates: List<Pair<Person, DateProposal>>, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val sortedDates = remember(upcomingDates) { upcomingDates.sortedBy { it.second.scheduledAtMillis } }
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("دیت‌های برنامه‌ریزی‌شده", color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Text("از نزدیک‌ترین قرار تا زمان‌های بعدی", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            }
            Icon(Icons.Default.EventAvailable, null, tint = SuccessGreen, modifier = Modifier.padding(end = 12.dp).size(22.dp))
        }
        if (sortedDates.isEmpty()) {
            EmptyState("هنوز دیتی برنامه‌ریزی نشده", "پس از تأیید یک پیشنهاد در چت، قرار در این صفحه نمایش داده می‌شود.", "📅")
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sortedDates, key = { "${it.first.id}_${it.second.id}" }) { (datePerson, dateProposal) ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.22f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(SuccessGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EventAvailable, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text("با ${datePerson.name}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Vazirmatn)
                                Text(dateProposal.place, color = TextGray, fontSize = 10.sp, fontFamily = Vazirmatn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatDateTimePersian(dateProposal.scheduledAtMillis), color = SuccessGreen, fontSize = 10.sp, fontFamily = Vazirmatn)
                            }
                            Icon(Icons.Default.PushPin, null, tint = SuccessGreen, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = DarkCard) {
        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(persianDigits(value), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = Vazirmatn)
            Text(label, color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun PaymentHistoryScreen(records: List<PaymentRecord>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("تاریخچه پرداخت", color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                Text("سوابق خرید اشتراک و تمدیدها", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            }
            Icon(Icons.Default.ReceiptLong, null, tint = TinderOrange, modifier = Modifier.padding(end = 12.dp).size(23.dp))
        }
        if (records.isEmpty()) {
            EmptyState("هنوز خریدی ثبت نشده", "پس از خرید اشتراک، رسید آن در این صفحه نگهداری می‌شود.", "🧾")
        } else {
            val total = records.sumOf { it.priceToman }
            Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("مجموع پرداخت‌های ثبت‌شده", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                        Spacer(Modifier.height(4.dp))
                        Text(formatToman(total), color = TinderOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    }
                    Text("${persianDigits(records.size.toString())} خرید", color = TextGray, fontSize = 11.sp, fontFamily = Vazirmatn)
                }
            }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(records, key = { "${it.purchasedAtMillis}_${it.planTitle}" }) { payment ->
                    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(TinderOrange.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ReceiptLong, null, tint = TinderOrange, modifier = Modifier.size(21.dp))
                            }
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text("اشتراک ${payment.planTitle}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Vazirmatn)
                                Spacer(Modifier.height(3.dp))
                                Text("خرید: ${formatDatePersian(payment.purchasedAtMillis)}", color = TextGray, fontSize = 10.sp, fontFamily = Vazirmatn)
                                Text("انقضا: ${formatDatePersian(payment.expiresAtMillis)}", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
                                if (payment.durationDays > 0) Text("دوره ${if (payment.durationDays == 7) "هفتگی" else "ماهانه"}", color = TextMuted, fontSize = 9.sp, fontFamily = Vazirmatn)
                            }
                            Text(formatToman(payment.priceToman), color = TinderOrange, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublicProfilePreviewScreen(user: UserProfile, onBack: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    val photos = remember(user.photos, user.primaryPhotoIndex) {
        val limited = user.photos.take(5)
        val primary = limited.getOrNull(user.primaryPhotoIndex)
        if (primary == null) limited else listOf(primary) + limited.filterIndexed { index, _ -> index != user.primaryPhotoIndex }
    }
    val pagerState = rememberPagerState(pageCount = { photos.size.coerceAtLeast(1) })
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Column(Modifier.weight(1f)) {
                Text("پیش‌نمایش پروفایل عمومی", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
                Text("این همان چیزی است که دیگران می‌بینند", color = TextMuted, fontSize = 10.sp, fontFamily = Vazirmatn)
            }
            TextButton(onClick = onEdit) { Text("ویرایش", color = TinderPink, fontFamily = Vazirmatn) }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp).height(460.dp).clip(RoundedCornerShape(24.dp))) {
                if (photos.isNotEmpty()) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(Uri.parse(photos[page])).crossfade(true).build(),
                            contentDescription = "عکس ${page + 1} از ${photos.size}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                        Text(user.name.take(1).ifEmpty { "♡" }, color = TextWhite, fontSize = 100.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(150.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.80f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(19.dp)) {
                    Text("${user.name.ifBlank { "نام کاربر" }}, ${persianDigits(user.age.toString())}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 25.sp, fontFamily = Vazirmatn)
                    Text("${user.job.ifBlank { "" }}${if (user.job.isNotBlank() && user.city.isNotBlank()) " · " else ""}${user.city}", color = TextWhite.copy(alpha = 0.9f), fontSize = 12.sp, fontFamily = Vazirmatn)
                }
                Surface(Modifier.align(Alignment.TopEnd).padding(12.dp), shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.55f)) {
                    Text("${persianDigits((pagerState.currentPage + 1).toString())} / ${persianDigits(photos.size.coerceAtLeast(1).toString())}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn)
                }
                if (user.showOnlineStatus) {
                    OnlineStatusBadge(isOnline = true, modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
                }
            }
            if (photos.size > 1) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    repeat(photos.size) { page ->
                        Box(Modifier.padding(horizontal = 3.dp).width(if (pagerState.currentPage == page) 20.dp else 6.dp).height(5.dp).clip(CircleShape).background(if (pagerState.currentPage == page) TinderPink else TextMuted.copy(alpha = 0.65f)))
                    }
                }
            }
            Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("درباره من", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(8.dp))
                    Text(user.about.ifBlank { "هنوز توضیحی اضافه نشده است." }, color = TextGray, fontSize = 13.sp, lineHeight = 22.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (user.job.isNotBlank()) InfoChip(Icons.Default.Work, user.job)
                        if (user.city.isNotBlank()) InfoChip(Icons.Default.LocationOn, user.city)
                    }
                }
            }
            if (user.interests.isNotEmpty()) {
                Text("علایق", Modifier.padding(horizontal = 18.dp), color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                FlowRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    user.interests.forEach { interest ->
                        Surface(shape = RoundedCornerShape(20.dp), color = TinderPink.copy(alpha = 0.14f), border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.32f))) {
                            Text(interest, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = TextWhite, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
            if (user.instagram.isNotBlank() || user.spotify.isNotBlank()) {
                Text("شبکه‌های اجتماعی", Modifier.padding(horizontal = 18.dp), color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                SocialProfileBadges(instagram = user.instagram, spotify = user.spotify, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnlineStatusBadge(isOnline: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xDD101626),
        border = BorderStroke(1.dp, (if (isOnline) SuccessGreen else TextMuted).copy(alpha = 0.45f))
    ) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(if (isOnline) SuccessGreen else TextMuted, CircleShape))
            Spacer(Modifier.width(5.dp))
            Text(if (isOnline) "آنلاین" else "آفلاین", color = if (isOnline) SuccessGreen else TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
        }
    }
}

private fun demoConversationTopic(text: String): String {
    val message = text.trim().lowercase(Locale.ROOT)
    return when {
        listOf("سلام", "درود", "خوبی", "چطوری", "چه خبر").any { it in message } -> "greeting"
        listOf("موسیقی", "آهنگ", "اسپاتیفای", "spotify", "خواننده", "کنسرت").any { it in message } -> "music"
        listOf("سفر", "مسافرت", "گردش", "شهر", "مسیر", "تعطیلات").any { it in message } -> "travel"
        listOf("کافه", "قهوه", "کافی", "رستوران", "غذا").any { it in message } -> "cafe"
        listOf("کتاب", "مطالعه", "رمان", "داستان").any { it in message } -> "book"
        listOf("فیلم", "سریال", "سینما", "بازیگر").any { it in message } -> "movie"
        listOf("کار", "شغل", "درس", "دانشگاه", "شرکت", "حرفه").any { it in message } -> "work"
        listOf("ورزش", "باشگاه", "فوتبال", "یوگا", "پیاده", "دویدن").any { it in message } -> "sport"
        listOf("خودت", "تو چی", "تو چطور", "از خودت", "علاقه‌ات").any { it in message } -> "about_person"
        else -> "general"
    }
}

private fun buildDemoReply(userText: String, person: Person, conversation: List<ChatMessage>): String {
    val message = userText.trim()
    val normalizedMessage = message.lowercase(Locale.ROOT)
    val isShortFollowUp = normalizedMessage in setOf(
        "آره", "اره", "بله", "نه", "دقیقاً", "دقیقا", "موافقم", "منم همینطور", "منم", "درسته", "چه خوب", "جالبه", "مرسی", "ممنون"
    )
    val shortFollowUps = setOf("آره", "اره", "بله", "نه", "دقیقاً", "دقیقا", "موافقم", "منم همینطور", "منم", "درسته", "چه خوب", "جالبه", "مرسی", "ممنون")
    val previousUserText = conversation.dropLast(1).lastOrNull {
        it.isMe && it.type == MsgType.TEXT && it.text.trim().lowercase(Locale.ROOT) !in shortFollowUps
    }?.text
    val topic = if (isShortFollowUp && previousUserText != null) demoConversationTopic(previousUserText) else demoConversationTopic(message)
    val hasOpeningGreeting = conversation.firstOrNull()?.let {
        !it.isMe && it.type == MsgType.TEXT && it.text.startsWith("سلام! خوشحالم")
    } == true
    val replyTurn = (conversation.count { !it.isMe && it.type == MsgType.TEXT } - if (hasOpeningGreeting) 1 else 0).coerceAtLeast(0)
    val otherInterest = person.interests.firstOrNull() ?: "گفت‌وگوهای خوب"
    val secondInterest = person.interests.getOrNull(1) ?: "موسیقی"
    val city = person.city.ifBlank { "شهر خودم" }
    val clipped = message.take(70) + if (message.length > 70) "…" else ""

    val replies = when (topic) {
        "greeting" -> listOf(
            "سلام، چه خوب که پیام دادی 😊 روزت تا اینجا چطور گذشته؟ امروز اتفاق جالبی افتاده؟",
            "مرسی، خوبم! خوشحالم داریم بیشتر آشنا می‌شیم. معمولاً وقتی وقت آزاد داری دوست داری چیکار کنی؟",
            "سلام دوباره 😊 راستی امروز بیشتر روز شلوغی داشتی یا تونستی برای خودت وقت بذاری؟",
            "خوشحالم که گپ می‌زنیم. این روزها چیزی هست که حسابی منتظرش باشی؟",
            "راستی آخر هفته‌ات رو معمولاً چطور می‌گذرونی؛ استراحت یا بیرون رفتن؟"
        )
        "music" -> listOf(
            "من معمولاً سراغ ${otherInterest} و آهنگ‌های آروم می‌رم 🎧 تو بیشتر با آهنگ انرژی می‌گیری یا آهنگ‌های ملایم؟",
            "چه انتخاب خوبی! اخیراً آهنگی بوده که چند بار پشت سر هم گوشش بدی؟ اسمش رو بهم می‌گی؟",
            "من پلی‌لیست‌های متنوع رو دوست دارم. برای رانندگی یا پیاده‌روی چه آهنگی رو پیشنهاد می‌کنی؟",
            "اگه بخوای فقط یک خواننده رو برای یک سفر طولانی انتخاب کنی، انتخابت کیه؟",
            "معمولاً آهنگ‌ها رو بر اساس حال و هوات انتخاب می‌کنی یا سبک خاصی رو همیشه دنبال می‌کنی؟"
        )
        "travel" -> listOf(
            "سفر همیشه حال آدم رو عوض می‌کنه ✈️ من دوست دارم یک بار بیشتر جاهای دیدنی ${city} رو بگردم. تو سفر برنامه‌ریزی‌شده رو ترجیح می‌دی یا یهویی؟",
            "بین مقصدهای شلوغ و طبیعت خلوت، من معمولاً طبیعت رو انتخاب می‌کنم. بهترین خاطره سفرت چی بوده؟",
            "چه خوب! اگه همین آخر هفته فرصت سفر کوتاه داشته باشی، کجا رو انتخاب می‌کنی و چرا؟",
            "من از کشف کوچه‌ها و جاهای کمترشناخته‌شده هم لذت می‌برم. تو بیشتر جاذبه‌های معروف رو می‌بینی یا جاهای محلی؟",
            "سفر با دوست‌ها رو بیشتر دوست داری یا یک سفر آروم و دونفره؟"
        )
        "cafe" -> listOf(
            "من کافه‌های دنج با موسیقی ملایم رو خیلی دوست دارم ☕ تو بیشتر فضای خلوت رو می‌پسندی یا کافه‌های پرجنب‌وجوش؟",
            "یک قهوه خوب و گفت‌وگوی طولانی ترکیب جذابیه! نوشیدنی همیشگی‌ات چیه؟",
            "پس باید یک روز چند کافه خوب رو امتحان کنیم 😊 بیشتر کافه با منظره رو دوست داری یا فضای گرم و جمع‌وجور؟",
            "اگه بخوای یک کافه رو چند بار انتخاب کنی، بیشتر به قهوه‌اش اهمیت می‌دی یا فضای اونجا؟",
            "من معمولاً کنار قهوه یک دسر کوچیک هم می‌گیرم 😄 تو شیرینی دوست داری یا خوراکی شور؟"
        )
        "book" -> listOf(
            "کتاب خوب آدم رو تا چند روز درگیر خودش می‌کنه 📚 بیشتر رمان می‌خونی یا کتاب‌های غیرداستانی؟",
            "من داستان‌هایی رو دوست دارم که شخصیت‌هاشون واقعی و باورپذیر باشن. کتابی هست که دوست داشته باشی دوباره بخونیش؟",
            "این یکی رو باید به فهرست مطالعه‌ام اضافه کنم. آخرین چیزی که خوندی و بهت پیشنهاد می‌کنی چی بوده؟",
            "برای کتاب خوندن سکوت رو ترجیح می‌دی یا موسیقی بی‌کلام؟",
            "شخصیت کتابی‌ای هست که هنوز بعد از تموم‌شدن داستان یادت مونده باشه؟"
        )
        "movie" -> listOf(
            "فیلم خوب با یک همراه خوش‌سلیقه خیلی می‌چسبه 🎬 پایان‌های غیرقابل‌پیش‌بینی رو دوست داری یا داستان‌های آروم؟",
            "من معمولاً فیلمی رو انتخاب می‌کنم که بعدش بشه درباره‌اش حرف زد. آخرین فیلمی که دیدی ارزش دیدن داشت؟",
            "اگه قرار باشه امشب فیلم ببینیم، تو انتخابت کمدیه یا معمایی؟",
            "سریال کوتاه رو ترجیح می‌دی یا داستانی که چند فصل ادامه داشته باشه؟",
            "فیلمی هست که هر بار ببینیش باز هم برات جذاب باشه؟"
        )
        "work" -> listOf(
            "جالبه، به نظر میاد برات مهمه کاری که انجام می‌دی معنا داشته باشه. کدوم بخش کارت بیشتر بهت انرژی می‌ده؟",
            "مسیر رسیدن به این حوزه چطور بود؟ از اول بهش علاقه داشتی یا کم‌کم انتخابش کردی؟",
            "بیرون از کار چه چیزی کمک می‌کنه ذهنت رو از مشغله‌ها جدا کنی؟",
            "اگه می‌تونستی یک مهارت تازه یاد بگیری، چی انتخاب می‌کردی؟",
            "تعادل کار و زندگی برات آسونه یا هنوز داری راه خودت رو پیدا می‌کنی؟"
        )
        "sport" -> listOf(
            "چه خوب! من هم فعالیت و تحرک رو دوست دارم 💪 بیشتر ورزش انفرادی رو می‌پسندی یا گروهی؟",
            "ثبات توی ورزش از خود تمرین سخت‌تره! چه چیزی باعث می‌شه انگیزه‌ات رو حفظ کنی؟",
            "اگه بخوای یک فعالیت تازه امتحان کنی، چی رو انتخاب می‌کنی؟",
            "بیشتر صبح‌ها ورزش می‌کنی یا عصرها؟ من فکر می‌کنم زمانش خیلی روی انگیزه اثر داره.",
            "ورزشی هست که همیشه دلت خواسته امتحانش کنی ولی هنوز فرصت نشده؟"
        )
        "about_person" -> listOf(
            "من به ${otherInterest} علاقه دارم و از تجربه‌های تازه خوشم میاد 😊 معمولاً ${secondInterest} هم جزو برنامه‌هامه. تو چه چیزی رو هیچ‌وقت خسته‌کننده نمی‌دونی؟",
            "فکر کنم از گفت‌وگوی صمیمی و آدم‌های کنجکاو خوشم میاد. دوست دارم بدونم چه چیزی بیشتر از همه خوشحالت می‌کنه؟",
            "من بیشتر آدم تجربه‌کردنم تا فقط برنامه‌ریزی کردن! تو بیشتر اهل تجربه‌های تازه‌ای یا چیزهای آشنا و مطمئن؟",
            "یک روز ایده‌آل برای تو چه شکلیه؟ از صبح تا شبش رو چطور می‌گذرونی؟",
            "چه ویژگی‌ای در آدم‌ها باعث می‌شه از همون اول باهاشون احساس راحتی کنی؟"
        )
        else -> listOf(
            "جالبه که گفتی «$clipped» 😊 دوست دارم بیشتر از زاویه نگاهت بدونم؛ چی باعث شد به این موضوع فکر کنی؟",
            "می‌فهمم چی می‌گی. به نظرت جذاب‌ترین قسمت این ماجرا چیه؟ دوست دارم بیشتر برام تعریف کنی.",
            "این موضوع چند تا جنبه جالب داره! خودت کدوم بخشش رو بیشتر دوست داری یا برات مهم‌تره؟",
            "خوبه که اینو گفتی. راستی بین ${otherInterest} و ${secondInterest} کدوم رو بیشتر ترجیح می‌دی؟",
            "از حرفت این‌طور برداشت کردم که برات مهمه با جزئیات به موضوع نگاه کنی. درست متوجه شدم؟",
            "دوست دارم بیشتر با سلیقه‌ات آشنا بشم؛ یک چیز کوچیک که همیشه حالت رو خوب می‌کنه چیه؟"
        )
    }
    return replies[replyTurn % replies.size]
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Text(label, fontSize = 12.sp, color = TextMuted, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = DarkCard, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TinderPink, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp))
            Text(text, color = TextGray, fontSize = 12.sp, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun ProfileActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, tint: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(14.dp))
            Text(title, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn, fontSize = 15.sp)
            Icon(Icons.Default.ChevronLeft, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PersonRow(person: Person, onClick: () -> Unit, action: @Composable () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = DarkSurface, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).clip(CircleShape).background(Brush.linearGradient(listOf(person.tint, Color.White.copy(alpha = 0.72f)))).border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) { Text(person.emoji, fontSize = 26.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${person.name}, ${person.age}", fontWeight = FontWeight.SemiBold, color = TextWhite, fontFamily = Vazirmatn)
                Text("${person.job} · ${person.city}", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
            }
            action()
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, emoji: String) {
    val pulseTransition = rememberInfiniteTransition(label = "emptyState")
    val pulse by pulseTransition.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "emptyPulse"
    )
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            Modifier.size(116.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }
                .background(Brush.radialGradient(listOf(TinderPink.copy(alpha = 0.20f), BrandViolet.copy(alpha = 0.06f), Color.Transparent)), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 52.sp) }
        Spacer(Modifier.height(20.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(7.dp))
            Text(subtitle, color = TextMuted, fontSize = 13.sp, lineHeight = 21.sp, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BottomNav(tab: Int, onTab: (Int) -> Unit, matchCount: Int, likedMeCount: Int, highlightTab: Int? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        shadowElevation = 14.dp
    ) {
        NavigationBar(containerColor = Color.Transparent, contentColor = TextWhite, tonalElevation = 0.dp) {
            val items = listOf(
                Triple(0, Icons.Default.Explore, "کشف"),
                Triple(1, Icons.Default.FavoriteBorder, "لایک‌ها"),
                Triple(2, Icons.Default.Map, "نقشه"),
                Triple(3, Icons.Default.Chat, "چت"),
                Triple(4, Icons.Default.Person, "من")
            )
            items.forEach { (idx, icon, label) ->
                NavigationBarItem(
                    selected = tab == idx,
                    onClick = { onTab(idx) },
                    icon = {
                        val badgeCount = when (idx) {
                            1 -> likedMeCount
                            3 -> matchCount
                            else -> 0
                        }
                        Box(
                            Modifier.size(28.dp).then(
                                if (highlightTab == idx) Modifier.border(2.dp, TinderOrange, CircleShape).padding(2.dp) else Modifier
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
                            if (badgeCount > 0) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).offset(x = 7.dp, y = (-5).dp)
                                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                                        .clip(CircleShape)
                                        .background(TinderPink)
                                        .border(1.5.dp, DarkSurface, CircleShape)
                                        .padding(horizontal = 3.dp, vertical = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        persianDigits(if (badgeCount > 99) "99+" else badgeCount.toString()),
                                        color = TextWhite, fontSize = 8.sp, lineHeight = 9.sp,
                                        fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, maxLines = 1
                                    )
                                }
                            }
                        }
                    },
                    label = { Text(label, fontFamily = Vazirmatn, fontSize = 10.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TinderPink,
                        selectedTextColor = TinderPink,
                        indicatorColor = TinderPink.copy(alpha = 0.13f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}
