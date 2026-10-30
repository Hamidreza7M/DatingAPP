package ir.hamdam.demo

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val orientation: Orientation = Orientation.STRAIGHT,
    val goal: RelationGoal = RelationGoal.FRIENDSHIP,
    val job: String = "",
    val city: String = "",
    val about: String = "",
    val interests: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val primaryPhotoIndex: Int = 0,
    val phone: String = "",
    val voiceIntro: String = ""
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
    val likedYou: Boolean = false
)

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val time: String,
    val type: MsgType = MsgType.TEXT
)

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
            likedYou = i % 3 == 0
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
            likedYou = i % 4 == 0
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
            likedYou = i % 2 == 0
        ))
    }
}

/** Filter people compatible with viewer's gender + orientation */
private fun compatiblePeople(viewer: UserProfile): List<Person> {
    return samplePeople.filter { p ->
        when (viewer.orientation) {
            Orientation.STRAIGHT -> when (viewer.gender) {
                Gender.MALE -> p.gender == Gender.FEMALE && p.orientation in listOf(Orientation.STRAIGHT, Orientation.BISEXUAL)
                Gender.FEMALE -> p.gender == Gender.MALE && p.orientation in listOf(Orientation.STRAIGHT, Orientation.BISEXUAL)
                Gender.OTHER -> true
            }
            Orientation.GAY -> p.gender == Gender.MALE && p.orientation in listOf(Orientation.GAY, Orientation.BISEXUAL)
            Orientation.LESBIAN -> p.gender == Gender.FEMALE && p.orientation in listOf(Orientation.LESBIAN, Orientation.BISEXUAL)
            Orientation.BISEXUAL -> when (viewer.gender) {
                Gender.MALE -> (p.gender == Gender.FEMALE && p.orientation in listOf(Orientation.STRAIGHT, Orientation.BISEXUAL)) ||
                        (p.gender == Gender.MALE && p.orientation in listOf(Orientation.GAY, Orientation.BISEXUAL))
                Gender.FEMALE -> (p.gender == Gender.MALE && p.orientation in listOf(Orientation.STRAIGHT, Orientation.BISEXUAL)) ||
                        (p.gender == Gender.FEMALE && p.orientation in listOf(Orientation.LESBIAN, Orientation.BISEXUAL))
                Gender.OTHER -> true
            }
            Orientation.OTHER -> true
        }
    }
}

// ─── Local persistence ────────────────────────────────────────────────────────
private const val PREFS = "fastlove_prefs"
private val gson = Gson()

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
        val normalized = loaded.copy(photos = durablePhotos, voiceIntro = loaded.voiceIntro.orEmpty())
        prefs(ctx).edit().putString("user", gson.toJson(normalized)).apply()
        normalized
    } catch (_: Exception) { UserProfile() }
}
private fun loadPlan(ctx: Context): SubscriptionPlan {
    val n = prefs(ctx).getString("plan", "FREE") ?: "FREE"
    return try { SubscriptionPlan.valueOf(n) } catch (_: Exception) { SubscriptionPlan.FREE }
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
    var currentUser by remember { mutableStateOf(loadUser(context)) }
    var subscriptionPlan by remember { mutableStateOf(loadPlan(context)) }
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
    val paidDirectChatIds = remember {
        mutableStateListOf<Int>().also { it.addAll(loadIntList(context, "paidIds")) }
    }

    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var chatPerson by remember { mutableStateOf<Person?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
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
            blockedIds.toList(), paidDirectChatIds.toList(),
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
        if (p !in likedPeople) likedPeople.add(p)
        if (p.likedYou && p !in matchedPeople) {
            matchedPeople.add(p)
            showMatchPopup = p
        }
        persist()
    }

    val orientationFiltered = remember(currentUser) { compatiblePeople(currentUser) }

    val filteredPeople = remember(filters, blockedIds, orientationFiltered) {
        orientationFiltered.filter { p ->
            p.id !in blockedIds &&
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
                        onSuccess = { phone, register ->
                            if (register) {
                                currentUser = UserProfile(phone = phone)
                                hasProfile = false
                            } else {
                                // Login: try restore by phone
                                val saved = loadUser(context)
                                if (saved.phone == phone && saved.name.isNotBlank()) {
                                    currentUser = saved
                                    hasProfile = loadHasProfile(context)
                                    subscriptionPlan = loadPlan(context)
                                } else {
                                    currentUser = UserProfile(phone = phone)
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
                        onComplete = { profile ->
                            currentUser = profile
                            hasProfile = true
                            persist()
                        }
                    )
                    showSubscription -> SubscriptionScreen(
                        currentPlan = subscriptionPlan,
                        messageCredits = paidMessageCredits,
                        onBack = { showSubscription = false },
                        onChoosePlan = { plan ->
                            subscriptionPlan = plan
                            showSubscription = false
                            persist()
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
                            likedPeople.clear()
                            matchedPeople.clear()
                            blockedIds.clear()
                            paidDirectChatIds.clear()
                            paidMessageCredits = 0
                        },
                        onDismiss = { showDeleteRequest = false }
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
                    selectedPerson != null -> PersonDetailScreen(
                        person = selectedPerson!!,
                        canSeeLikes = subscriptionPlan != SubscriptionPlan.FREE,
                        isMatched = selectedPerson in matchedPeople,
                        canDirectMessage = selectedPerson!!.id in paidDirectChatIds || selectedPerson in matchedPeople,
                        onBack = { selectedPerson = null },
                        onLike = {
                            if (canSwipe()) {
                                doLike(selectedPerson!!)
                                selectedPerson = null
                            }
                        },
                        onPass = {
                            if (canSwipe()) selectedPerson = null
                        },
                        onBlock = {
                            blockedIds.add(selectedPerson!!.id)
                            likedPeople.removeAll { it.id == selectedPerson!!.id }
                            matchedPeople.removeAll { it.id == selectedPerson!!.id }
                            selectedPerson = null
                            persist()
                        },
                        onChat = {
                            val p = selectedPerson!!
                            if (p in matchedPeople || p.id in paidDirectChatIds) {
                                chatPerson = p
                                selectedPerson = null
                            } else showPayToMessage = p
                        },
                        onReport = { showReport = selectedPerson }
                    )
                    chatPerson != null -> ChatScreen(person = chatPerson!!, onBack = { chatPerson = null })
                    showFilters -> FilterScreen(
                        current = filters,
                        onApply = { filters = it; showFilters = false; discoverIndex = 0 },
                        onClose = { showFilters = false }
                    )
                    else -> {
                        Column(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(1f)) {
                                when (tab) {
                                    0 -> DiscoverScreen(
                                        viewer = currentUser,
                                        person = filteredPeople.getOrNull(discoverIndex),
                                        onOpen = { selectedPerson = it },
                                        onLike = { p ->
                                            if (canSwipe()) {
                                                doLike(p)
                                                discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                            }
                                        },
                                        onPass = {
                                            if (canSwipe()) discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
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
                                    1 -> LikedByMeScreen(
                                        liked = likedPeople.filter { it.id !in blockedIds },
                                        matched = matchedPeople,
                                        paidIds = paidDirectChatIds,
                                        onOpen = { selectedPerson = it },
                                        onChat = { p ->
                                            if (p in matchedPeople || p.id in paidDirectChatIds) chatPerson = p
                                            else showPayToMessage = p
                                        }
                                    )
                                    2 -> if (subscriptionPlan == SubscriptionPlan.FREE) {
                                        UpgradeTeaserScreen(onUpgrade = { showSubscription = true })
                                    } else LikedMeScreen(
                                        people = orientationFiltered.filter { it.likedYou && it.id !in blockedIds },
                                        matched = matchedPeople,
                                        onLikeBack = { p -> doLike(p) },
                                        onOpen = { selectedPerson = it },
                                        onBlock = { blockedIds.add(it.id); persist() }
                                    )
                                    3 -> MatchesChatList(
                                        matches = (matchedPeople.filter { it.id !in blockedIds } +
                                            samplePeople.filter { it.id in paidDirectChatIds && it !in matchedPeople && it.id !in blockedIds }).distinctBy { it.id },
                                        onChat = { chatPerson = it }
                                    )
                                    4 -> MyProfileScreen(
                                        user = currentUser,
                                        blockedCount = blockedIds.size,
                                        plan = subscriptionPlan,
                                        matchCount = matchedPeople.size,
                                        likeCount = likedPeople.size,
                                        onUpgrade = { showSubscription = true },
                                        onEdit = { showEditProfile = true },
                                        onShowBlocked = { showBlockedList = true },
                                        onDeleteRequest = { showDeleteRequest = true },
                                        onLogout = {
                                            clearSession(context)
                                            isLoggedIn = false
                                            hasProfile = false
                                            currentUser = UserProfile()
                                            subscriptionPlan = SubscriptionPlan.FREE
                                            dailySwipes = 0
                                            swipeDay = currentDayKey()
                                            likedPeople.clear()
                                            matchedPeople.clear()
                                            blockedIds.clear()
                                            paidDirectChatIds.clear()
                                            paidMessageCredits = 0
                                            discoverIndex = 0
                                            tab = 0
                                        }
                                    )
                                }
                            }
                            BottomNav(tab = tab, onTab = { tab = it }, matchCount = matchedPeople.count { it.id !in blockedIds })
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
            Column {
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
    currentPlan: SubscriptionPlan,
    messageCredits: Int,
    onBack: () -> Unit,
    onChoosePlan: (SubscriptionPlan) -> Unit,
    onBuyMessages: (Int) -> Unit
) {
    var periodWeekly by remember { mutableStateOf(true) }
    val messagePackages = listOf(
        Triple(1, 1_000_000, 0),
        Triple(5, 4_500_000, 10),
        Triple(10, 8_000_000, 20),
        Triple(20, 14_000_000, 30)
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(DarkBg).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Text("اشتراک و پیام مستقیم", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        }
        Spacer(Modifier.height(10.dp))
        Text("خریدها در نسخه دمو شبیه‌سازی می‌شوند و پرداخت واقعی انجام نمی‌شود.", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(18.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(TinderPink.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MarkChatUnread, null, tint = TinderPink)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("اعتبار پیام مستقیم", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    Text("هر اعتبار برای شروع گفتگو با یک نفر", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                }
                Text("$messageCredits", color = TinderOrange, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Vazirmatn)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("بسته‌های پیام مستقیم", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = Vazirmatn)
        Text("قیمت پایه هر اعتبار ۱٬۰۰۰٬۰۰۰ تومان است؛ بسته‌های بزرگ‌تر تخفیف دارند.", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(8.dp))
        messagePackages.forEach { (count, price, discount) ->
            Card(
                onClick = { onBuyMessages(count) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(if (count == 10) 1.5.dp else 1.dp, if (count == 10) BrandViolet else Color.White.copy(alpha = 0.08f))
            ) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("$count اعتبار", color = TextWhite, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            if (discount > 0) Surface(color = SuccessGreen.copy(alpha = 0.14f), shape = RoundedCornerShape(20.dp)) {
                                Text("$discount٪ تخفیف", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontFamily = Vazirmatn)
                            }
                            if (count == 10) Surface(color = BrandViolet.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
                                Text("پیشنهاد ویژه", color = BrandViolet, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontFamily = Vazirmatn)
                            }
                        }
                        Text("هر اعتبار: ${formatToman(price / count)}", color = TextMuted, fontSize = 11.sp, fontFamily = Vazirmatn)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatToman(price), color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        Text("افزودن اعتبار", color = TinderPink, fontSize = 10.sp, fontFamily = Vazirmatn)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("اشتراک", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().background(DarkSurface, RoundedCornerShape(14.dp)).padding(4.dp)) {
            listOf(true to "هفتگی", false to "ماهانه").forEach { (weekly, label) ->
                val sel = periodWeekly == weekly
                Surface(onClick = { periodWeekly = weekly }, shape = RoundedCornerShape(10.dp), color = if (sel) TinderPink else Color.Transparent, modifier = Modifier.weight(1f)) {
                    Text(label, Modifier.padding(vertical = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = TextWhite, fontFamily = Vazirmatn, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        listOf(SubscriptionPlan.PRO to (if (periodWeekly) 99000 else 299000), SubscriptionPlan.VIP to (if (periodWeekly) 149000 else 449000)).forEach { (plan, price) ->
            val isCurrent = currentPlan == plan
            Card(onClick = { onChoosePlan(plan) }, Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCurrent) TinderPink.copy(alpha = 0.2f) else DarkSurface),
                border = if (isCurrent) BorderStroke(2.dp, TinderPink) else null) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text(plan.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                        Spacer(Modifier.weight(1f))
                        Text(formatToman(price), color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    }
                    Text(if (plan == SubscriptionPlan.PRO) "۵۰ سوایپ روزانه + دیدن لایک‌ها" else "سوایپ نامحدود + همه امکانات", color = TextGray, fontFamily = Vazirmatn, fontSize = 12.sp)
                }
            }
        }
        OutlinedButton(onClick = { onChoosePlan(SubscriptionPlan.FREE) }, modifier = Modifier.fillMaxWidth()) {
            Text("ادامه با پلن رایگان", fontFamily = Vazirmatn, color = TextGray)
        }
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

// ─── Auth ─────────────────────────────────────────────────────────────────────
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
private fun AuthFlow(onSuccess: (String, Boolean) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var registerMode by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableIntStateOf(120) }
    var resendToken by remember { mutableIntStateOf(0) }
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
        AnimatedAuthBackground()
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.size(88.dp).graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                    .clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(GradientTinder))
                    .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, null, tint = TextWhite, modifier = Modifier.size(43.dp))
                Box(Modifier.align(Alignment.BottomEnd).padding(9.dp).size(10.dp).background(Color.White.copy(alpha = 0.9f), CircleShape))
            }
            Spacer(Modifier.height(16.dp))
            Text("فست لاو", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite, fontFamily = Vazirmatn)
            Text("آشنایی‌های واقعی، از یک شروع ساده", fontSize = 13.sp, color = TextGray, fontFamily = Vazirmatn)
            Spacer(Modifier.height(26.dp))

            Card(
                Modifier.fillMaxWidth().widthIn(max = 500.dp).animateContentSize(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE8171F34)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(Modifier.padding(horizontal = 21.dp, vertical = 23.dp)) {
                    AuthStepIndicator(step)
                    Spacer(Modifier.height(23.dp))
                    Crossfade(targetState = step, label = "auth-step-content") { activeStep ->
                        Column {
                            when (activeStep) {
                                0 -> {
                                    Text("خوش اومدی! 👋", fontWeight = FontWeight.Bold, fontSize = 21.sp, color = TextWhite, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(6.dp))
                                    Text("برای شروع ماجراجویی بعدی‌ات، یکی از گزینه‌ها را انتخاب کن.", color = TextGray, fontSize = 13.sp, lineHeight = 22.sp, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(22.dp))
                                    AuthChoiceCard(
                                        title = "ساخت حساب جدید",
                                        subtitle = "چند قدم تا آشنایی‌های تازه",
                                        icon = Icons.Default.PersonAdd,
                                        primary = true
                                    ) { registerMode = true; step = 1; error = "" }
                                    Spacer(Modifier.height(12.dp))
                                    AuthChoiceCard(
                                        title = "ورود به حساب",
                                        subtitle = "خوشحالیم دوباره می‌بینیمت",
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
                                    Text(if (registerMode) "شماره‌ات رو وارد کن" else "دوباره خوش اومدی", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextWhite, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(6.dp))
                                    Text("کد تأیید برای ادامه به شماره موبایلت ارسال می‌شود.", color = TextGray, fontSize = 13.sp, fontFamily = Vazirmatn)
                                    Spacer(Modifier.height(20.dp))
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { if (it.length <= 11) phone = it.filter { c -> c.isDigit() } },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("شماره موبایل", fontFamily = Vazirmatn) },
                                        placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹", fontFamily = Vazirmatn) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = TinderPink) }
                                    )
                                    if (error.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(error, color = Color(0xFFFF879D), fontSize = 12.sp, fontFamily = Vazirmatn)
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    TinderButton(if (registerMode) "ادامه ثبت‌نام" else "ادامه ورود") {
                                        if (phone.length >= 10) { error = ""; step = 2 } else error = "لطفاً یک شماره موبایل معتبر وارد کن"
                                    }
                                    TextButton(onClick = { step = 0; error = "" }) {
                                        Text("بازگشت به انتخاب", color = TextGray, fontFamily = Vazirmatn)
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
                                        onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("کد تأیید", fontFamily = Vazirmatn) },
                                        placeholder = { Text("۱۲۳۴۵۶", fontFamily = Vazirmatn) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TinderPink) }
                                    )
                                    if (error.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(error, color = Color(0xFFFF879D), fontSize = 12.sp, fontFamily = Vazirmatn)
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    TinderButton("تأیید و ادامه") {
                                        if (secondsLeft <= 0) error = "کد منقضی شده است"
                                        else if (otp == "123456") { error = ""; onSuccess(phone, registerMode) }
                                        else error = "کد واردشده صحیح نیست؛ کد تست ۱۲۳۴۵۶ است"
                                    }
                                    TextButton(onClick = { if (secondsLeft <= 0) { otp = ""; secondsLeft = 120; resendToken++ } }) {
                                        Text("درخواست مجدد کد", color = if (secondsLeft <= 0) TinderPink else TextMuted, fontFamily = Vazirmatn)
                                    }
                                    TextButton(onClick = { step = 1; otp = ""; error = "" }) {
                                        Text("تغییر شماره موبایل", color = TextGray, fontFamily = Vazirmatn)
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
private fun ProfileSetupScreen(phone: String, onComplete: (UserProfile) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var birthYearPersian by remember { mutableIntStateOf(1380) }
    var birthMonth by remember { mutableIntStateOf(1) }
    var gender by remember { mutableStateOf(Gender.FEMALE) }
    var orientation by remember { mutableStateOf(Orientation.STRAIGHT) }
    var goal by remember { mutableStateOf(RelationGoal.FRIENDSHIP) }
    var job by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("تهران") }
    var about by remember { mutableStateOf("") }
    var voiceIntro by remember { mutableStateOf("") }
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
    val age = (currentPersianYear() - birthYearPersian).coerceAtLeast(18)
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

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

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("ساخت پروفایل", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i -> Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (i <= step) TinderPink else DarkCard)) }
            }
            Text(when (step) { 0 -> "اطلاعات پایه"; 1 -> "هویت و موقعیت"; 2 -> "عکس و علایق"; else -> "درباره تو" }, color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 24.dp)) {
            when (step) {
                0 -> {
                    SectionLabel("نام نمایشی")
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("مثلاً سارا", fontFamily = Vazirmatn) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors(), leadingIcon = { Icon(Icons.Default.Person, null, tint = TinderPink) })
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("ماه تولد")
                    var showMonthPicker by remember { mutableStateOf(false) }
                    Surface(onClick = { showMonthPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = TinderPink); Spacer(Modifier.width(12.dp)); Text(persianMonths[birthMonth - 1], color = TextWhite, fontFamily = Vazirmatn); Spacer(Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
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
                    SectionLabel("سال تولد (هجری شمسی)")
                    Surface(onClick = { showYearPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, null, tint = TinderPink); Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) { Text("$birthYearPersian هـ.ش", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 16.sp); Text("$age ساله", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn) }
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showYearPicker) YearPickerDialog(selected = birthYearPersian, minYear = currentPersianYear() - 80, maxYear = currentPersianYear() - 18, onSelect = { birthYearPersian = it; showYearPicker = false }, onDismiss = { showYearPicker = false })
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("جنسیت")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Gender.FEMALE to "زن", Gender.MALE to "مرد", Gender.OTHER to "دیگر").forEach { (g, label) ->
                            val sel = gender == g
                            FilterChip(selected = sel, onClick = { gender = g }, label = { Text(label, fontFamily = Vazirmatn) }, colors = chipColors(sel))
                        }
                    }
                }
                1 -> {
                    SectionLabel("گرایش جنسی")
                    Text("بر اساس جنسیت شما پیشنهاد شده", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 8.dp))
                    val suggestedOrientations = when (gender) {
                        Gender.MALE -> listOf(Orientation.STRAIGHT to "دگرجنس‌گرا", Orientation.GAY to "همجنس‌گرا", Orientation.BISEXUAL to "دوجنس‌گرا", Orientation.OTHER to "دیگر")
                        Gender.FEMALE -> listOf(Orientation.STRAIGHT to "دگرجنس‌گرا", Orientation.LESBIAN to "لزبین", Orientation.BISEXUAL to "دوجنس‌گرا", Orientation.OTHER to "دیگر")
                        Gender.OTHER -> listOf(Orientation.STRAIGHT to "دگرجنس‌گرا", Orientation.GAY to "همجنس‌گرا", Orientation.LESBIAN to "لزبین", Orientation.BISEXUAL to "دوجنس‌گرا", Orientation.OTHER to "دیگر")
                    }
                    LaunchedEffect(gender) { if (suggestedOrientations.none { it.first == orientation }) orientation = suggestedOrientations.first().first }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestedOrientations.forEach { (o, label) ->
                            val sel = orientation == o
                            Surface(onClick = { orientation = o }, shape = RoundedCornerShape(12.dp), color = if (sel) TinderPink.copy(alpha = 0.2f) else DarkSurface, border = BorderStroke(1.dp, if (sel) TinderPink else DarkCard), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = sel, onClick = { orientation = o }, colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                                    Text(label, color = TextWhite, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("شهر")
                    Surface(onClick = { showCityPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = TinderPink); Spacer(Modifier.width(12.dp)); Text(city, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showCityPicker) CityPickerDialog(cities = iranianCities, selected = city, onSelect = { city = it; showCityPicker = false }, onDismiss = { showCityPicker = false })
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("شغل")
                    Surface(onClick = { showJobPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Work, null, tint = TinderPink); Spacer(Modifier.width(12.dp)); Text(if (job.isBlank()) "انتخاب شغل" else job, Modifier.weight(1f), color = if (job.isBlank()) TextMuted else TextWhite, fontFamily = Vazirmatn); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showJobPicker) CityPickerDialog(cities = availableJobs, selected = job, title = "انتخاب شغل", onSelect = { job = it; showJobPicker = false }, onDismiss = { showJobPicker = false })
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("هدف از آشنایی")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RelationGoal.entries.forEach { g ->
                            val sel = goal == g
                            Surface(
                                onClick = { goal = g },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) TinderPink.copy(alpha = 0.2f) else DarkSurface,
                                border = BorderStroke(1.dp, if (sel) TinderPink else DarkCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = sel, onClick = { goal = g }, colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                                    Text(g.title, color = TextWhite, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    SectionLabel("عکس‌های واقعی (حداکثر ۵)")
                    Text("حداقل یک عکس • روی عکس بزن تا اصلی شود", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(12.dp))
                    // Modern photo grid
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(selectedPhotos) { i, uriStr ->
                            val isPrimary = i == primaryPhotoIndex
                            Box(Modifier.size(110.dp).clip(RoundedCornerShape(18.dp)).border(if (isPrimary) 2.5.dp else 0.dp, TinderPink, RoundedCornerShape(18.dp)).clickable { primaryPhotoIndex = i }) {
                                AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                if (isPrimary) Surface(shape = RoundedCornerShape(bottomStart = 10.dp), color = TinderPink, modifier = Modifier.align(Alignment.BottomEnd)) {
                                    Text("اصلی", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = TextWhite, fontSize = 11.sp, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(i) }; if (primaryPhotoIndex >= selectedPhotos.size) primaryPhotoIndex = (selectedPhotos.size - 1).coerceAtLeast(0) }, modifier = Modifier.align(Alignment.TopEnd).size(30.dp).background(Color.Black.copy(alpha = 0.55f), CircleShape)) {
                                    Icon(Icons.Default.Close, null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        if (selectedPhotos.size < 5) {
                            item {
                                Box(Modifier.size(110.dp).clip(RoundedCornerShape(18.dp)).background(DarkCard).border(1.5.dp, TinderPink.copy(alpha = 0.4f), RoundedCornerShape(18.dp)).clickable { showPhotoSheet = true }, contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, null, tint = TinderPink, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text("افزودن عکس", fontSize = 12.sp, color = TinderPink, fontFamily = Vazirmatn, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                    if (showPhotoSheet) ModernPhotoPickerSheet(
                        onGallery = { ensurePermissionsAndPick(false) },
                        onCamera = { ensurePermissionsAndPick(true) },
                        onDismiss = { showPhotoSheet = false }
                    )
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("علایق (حداقل ۳)")
                    Spacer(Modifier.height(8.dp))
                    InterestFlow(selectedInterests) { interest -> selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest }
                }
                3 -> {
                    SectionLabel("درباره من")
                    OutlinedTextField(value = about, onValueChange = { about = it }, modifier = Modifier.fillMaxWidth().height(140.dp), placeholder = { Text("اهل کافه و کتابم...", fontFamily = Vazirmatn) }, shape = RoundedCornerShape(14.dp), colors = fieldColors())
                    Spacer(Modifier.height(16.dp))
                    VoiceIntroEditor(path = voiceIntro, onPathChange = { voiceIntro = it })
                    Spacer(Modifier.height(20.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            if (selectedPhotos.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(110.dp)) {
                                    itemsIndexed(selectedPhotos) { idx, uriStr ->
                                        val isMain = idx == primaryPhotoIndex
                                        Box(Modifier.size(110.dp).clip(RoundedCornerShape(12.dp)).border(if (isMain) 2.dp else 0.dp, TinderPink, RoundedCornerShape(12.dp))) {
                                            AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            if (isMain) Text("اصلی", Modifier.align(Alignment.BottomCenter).background(TinderPink).padding(horizontal = 6.dp, vertical = 2.dp), color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
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
                    0 -> if (name.isBlank()) error = "نام را وارد کنید" else { error = ""; step = 1 }
                    1 -> if (job.isBlank()) error = "شغل را انتخاب کنید" else { error = ""; step = 2 }
                    2 -> when {
                        selectedPhotos.isEmpty() -> error = "حداقل یک عکس واقعی اضافه کنید"
                        selectedInterests.size < 3 -> error = "حداقل ۳ علاقه انتخاب کنید"
                        else -> { error = ""; step = 3 }
                    }
                    3 -> onComplete(UserProfile(name = name.trim(), birthYearPersian = birthYearPersian, birthMonth = birthMonth, gender = gender, orientation = orientation, goal = goal, job = job, city = city, about = about.trim().ifBlank { "هنوز چیزی ننوشتم 😊" }, interests = selectedInterests.toList(), photos = selectedPhotos, primaryPhotoIndex = primaryPhotoIndex.coerceIn(0, (selectedPhotos.size - 1).coerceAtLeast(0)), phone = phone, voiceIntro = voiceIntro))
                }
            }, modifier = Modifier.weight(if (step > 0) 1.5f else 1f).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = TinderPink)) {
                Text(if (step == 3) "شروع کن 🚀" else "بعدی", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
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
    var orientation by remember { mutableStateOf(user.orientation) }
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
        uri?.let { selected -> storeUriInAppFiles(context, selected, "profile_photos")?.let { local -> if (selectedPhotos.size < 5) selectedPhotos = selectedPhotos + local } }
        showPhotoSheet = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { captured -> storeUriInAppFiles(context, captured, "profile_photos")?.let { local -> if (selectedPhotos.size < 5) selectedPhotos = selectedPhotos + local } }
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
    val suggestedOrientations = when (user.gender) {
        Gender.MALE -> listOf(Orientation.STRAIGHT to "دگرجنس‌گرا", Orientation.GAY to "همجنس‌گرا", Orientation.BISEXUAL to "دوجنس‌گرا", Orientation.OTHER to "دیگر")
        Gender.FEMALE -> listOf(Orientation.STRAIGHT to "دگرجنس‌گرا", Orientation.LESBIAN to "لزبین", Orientation.BISEXUAL to "دوجنس‌گرا", Orientation.OTHER to "دیگر")
        else -> listOf(Orientation.STRAIGHT to "دگرجنس‌گرا", Orientation.GAY to "همجنس‌گرا", Orientation.LESBIAN to "لزبین", Orientation.BISEXUAL to "دوجنس‌گرا", Orientation.OTHER to "دیگر")
    }
    Column(Modifier.fillMaxSize().background(DarkBg)) {
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
            SectionLabel("گرایش")
            suggestedOrientations.forEach { (o, label) ->
                val sel = orientation == o
                Surface(onClick = { orientation = o }, shape = RoundedCornerShape(10.dp), color = if (sel) TinderPink.copy(alpha = 0.2f) else DarkSurface, border = BorderStroke(1.dp, if (sel) TinderPink else DarkCard), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sel, onClick = { orientation = o }, colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                        Text(label, color = TextWhite, fontFamily = Vazirmatn)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            SectionLabel("عکس‌ها")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(selectedPhotos) { i, uriStr ->
                    val isPrimary = i == primaryPhotoIndex
                    Box(Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).border(if (isPrimary) 2.dp else 0.dp, TinderPink, RoundedCornerShape(12.dp)).clickable { primaryPhotoIndex = i }) {
                        AsyncImage(model = Uri.parse(uriStr), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        if (isPrimary) Text("اصلی", Modifier.align(Alignment.BottomEnd).background(TinderPink).padding(4.dp), color = TextWhite, fontSize = 9.sp, fontFamily = Vazirmatn)
                        IconButton(onClick = { selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(i) }; if (primaryPhotoIndex >= selectedPhotos.size) primaryPhotoIndex = (selectedPhotos.size - 1).coerceAtLeast(0) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = TextWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (selectedPhotos.size < 5) item {
                    Box(Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(DarkCard).clickable { showPhotoSheet = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.AddAPhoto, null, tint = TinderPink) }
                }
            }
            if (showPhotoSheet) ModernPhotoPickerSheet(onGallery = { ensurePermissionsAndPick(false) }, onCamera = { ensurePermissionsAndPick(true) }, onDismiss = { showPhotoSheet = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("درباره من")
            OutlinedTextField(value = about, onValueChange = { about = it }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(14.dp), colors = fieldColors())
            Spacer(Modifier.height(14.dp))
            VoiceIntroEditor(path = voiceIntro, onPathChange = { voiceIntro = it })
            Spacer(Modifier.height(12.dp))
            SectionLabel("علایق")
            InterestFlow(selectedInterests) { interest -> selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest }
            Spacer(Modifier.height(24.dp))
        }
        Button(onClick = {
            onSave(user.copy(name = name.trim(), job = job, city = city, about = about.trim(), orientation = orientation, interests = selectedInterests.toList(), photos = selectedPhotos, primaryPhotoIndex = primaryPhotoIndex.coerceIn(0, (selectedPhotos.size - 1).coerceAtLeast(0)), voiceIntro = voiceIntro))
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("سال تولد هجری شمسی", fontFamily = Vazirmatn) }, text = {
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
        Column {
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

    Column(Modifier.fillMaxSize().background(DarkBg)) {
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
    onPass: () -> Unit,
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
    val rotation = (offsetX / 38f).coerceIn(-16f, 16f)
    val likeAlpha = (offsetX / 150f).coerceIn(0f, 1f)
    val passAlpha = (-offsetX / 150f).coerceIn(0f, 1f)

    fun animateSwipe(like: Boolean) {
        val current = person ?: return
        if (isAnimating) return
        isAnimating = true
        scope.launch {
            val target = if (like) 1500f else -1500f
            val anim = Animatable(offsetX)
            anim.animateTo(target, tween(260, easing = FastOutSlowInEasing)) { offsetX = value }
            if (like) onLike(current) else onPass()
            offsetX = 0f
            offsetY = 0f
            isAnimating = false
        }
    }

    fun returnCard() {
        scope.launch {
            val ax = Animatable(offsetX)
            val ay = Animatable(offsetY)
            launch { ax.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) { offsetX = value } }
            launch { ay.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) { offsetY = value } }
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
                        .graphicsLayer { rotationZ = rotation }
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
private fun MatchesChatList(matches: List<Person>, onChat: (Person) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("مچ‌ها و چت‌ها", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(12.dp))
        if (matches.isEmpty()) EmptyState("هنوز مچی نداری", "با لایک شدید مچ شو", "💬")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(matches) { p -> PersonRow(person = p, onClick = { onChat(p) }) { Icon(Icons.Default.Chat, null, tint = TinderPink) } }
        }
    }
}

@Composable
private fun ChatScreen(person: Person, onBack: () -> Unit) {
    val context = LocalContext.current
    val storedMessages = remember(person.id) { loadChatMessages(context, person.id) }
    val messages = remember(person.id) {
        mutableStateListOf<ChatMessage>().apply {
            if (storedMessages.isNotEmpty()) addAll(storedMessages)
            else {
                add(ChatMessage("سلام! خوشحالم که مچ شدیم 😊", false, "۱۰:۳۰"))
                add(ChatMessage("سلام، منم خوشحالم!", true, "۱۰:۳۲"))
            }
        }
    }
    var input by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingStartedAt by remember { mutableLongStateOf(0L) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }
    var voiceError by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

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

    Column(Modifier.fillMaxSize().background(DarkBg).imePadding()) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Box(Modifier.size(40.dp).clip(CircleShape).background(person.tint), contentAlignment = Alignment.Center) { Text(person.emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(10.dp))
            Column { Text(person.name, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn); Text("گفتگوی شما", color = SuccessGreen, fontSize = 12.sp, fontFamily = Vazirmatn) }
        }
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
                    messages.add(ChatMessage(input.trim(), true, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                    saveChatMessages(context, person.id, messages.toList())
                    input = ""; keyboard?.hide()
                }
            }, enabled = !isRecording, colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink)) {
                Icon(Icons.Default.Send, null)
            }
        }
    }
}

@Composable
private fun PersonDetailScreen(
    person: Person, canSeeLikes: Boolean, isMatched: Boolean, canDirectMessage: Boolean,
    onBack: () -> Unit, onLike: () -> Unit, onPass: () -> Unit, onBlock: () -> Unit, onChat: () -> Unit, onReport: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(320.dp).background(Brush.verticalGradient(listOf(person.tint, DarkBg)))) {
            Text(person.emoji, fontSize = 120.sp, modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            IconButton(onClick = onReport, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Default.Flag, "گزارش", tint = TextWhite) }
        }
        Column(Modifier.padding(20.dp)) {
            Text("${person.name}, ${person.age}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Text("${person.job} · ${person.city} · ${person.distanceKm} کیلومتر", color = TextGray, fontFamily = Vazirmatn)
            if (person.likedYou && canSeeLikes) {
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = TinderPink) {
                    Text("❤️ تو را لایک کرده", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TextWhite, fontFamily = Vazirmatn)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(person.about, color = TextWhite, fontFamily = Vazirmatn, lineHeight = 22.sp)
            Spacer(Modifier.height(16.dp))
            Text("علایق", fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                person.interests.forEach { tag -> AssistChip(onClick = {}, label = { Text(tag, fontFamily = Vazirmatn) }) }
            }
            Spacer(Modifier.height(24.dp))
            // Like + Dislike both present
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isMatched) {
                    OutlinedButton(onClick = onPass, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.5.dp, TextGray)) {
                        Icon(Icons.Default.Close, null, tint = TextGray); Spacer(Modifier.width(6.dp))
                        Text("رد کردن", fontFamily = Vazirmatn, color = TextGray)
                    }
                    Button(onClick = onLike, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = TinderPink), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Favorite, null); Spacer(Modifier.width(6.dp))
                        Text("لایک", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = onChat, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if (canDirectMessage) SuccessGreen else TinderOrange), shape = RoundedCornerShape(14.dp)) {
                Text(when { isMatched -> "چت"; canDirectMessage -> "چت (پرداخت‌شده)"; else -> "پیام مستقیم" }, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onBlock, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFFF6B6B))) {
                Text("بلاک", color = Color(0xFFFF6B6B), fontFamily = Vazirmatn)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Flag, null, tint = TextMuted, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text("گزارش تخلف به ادمین", color = TextMuted, fontFamily = Vazirmatn, fontSize = 13.sp)
            }
        }
    }
}

// ─── My Profile — modern redesign ─────────────────────────────────────────────
@Composable
private fun MyProfileScreen(
    user: UserProfile,
    blockedCount: Int,
    plan: SubscriptionPlan,
    matchCount: Int,
    likeCount: Int,
    onUpgrade: () -> Unit,
    onEdit: () -> Unit,
    onShowBlocked: () -> Unit,
    onDeleteRequest: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(DarkBg)) {
        // Hero header
        Box(Modifier.fillMaxWidth().height(200.dp).background(Brush.verticalGradient(listOf(Color(0xFF2A0A12), DarkBg)))) {
            Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(96.dp).clip(CircleShape).border(3.dp, TinderPink, CircleShape)) {
                    val primary = user.photos.getOrNull(user.primaryPhotoIndex)
                    if (primary != null) {
                        AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(primary)).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.fillMaxSize().background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                            Text(user.name.take(1).ifEmpty { "♡" }, fontSize = 40.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Text("${user.age} ساله • ${user.city}", color = TextGray, fontFamily = Vazirmatn, fontSize = 14.sp)
            }
            IconButton(onClick = onEdit, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(DarkCard.copy(alpha = 0.7f), CircleShape)) {
                Icon(Icons.Default.Edit, "ویرایش", tint = TextWhite)
            }
        }

        // Stats row
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("مچ", "$matchCount", Modifier.weight(1f))
            StatCard("لایک", "$likeCount", Modifier.weight(1f))
            StatCard("اشتراک", plan.title, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))

        // Photos gallery
        if (user.photos.isNotEmpty()) {
            Text("عکس‌ها", fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(10.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(user.photos) { idx, uriStr ->
                    val isMain = idx == user.primaryPhotoIndex
                    Box(Modifier.size(100.dp).clip(RoundedCornerShape(14.dp)).border(if (isMain) 2.dp else 0.dp, TinderPink, RoundedCornerShape(14.dp))) {
                        AsyncImage(model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        if (isMain) Text("اصلی", Modifier.align(Alignment.BottomCenter).background(TinderPink).padding(horizontal = 6.dp, vertical = 2.dp), color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // About card
        Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = TinderPink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("درباره من", fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                }
                Spacer(Modifier.height(10.dp))
                Text(user.about, color = TextGray, fontFamily = Vazirmatn, lineHeight = 22.sp, fontSize = 14.sp)
                if (user.voiceIntro.isNotBlank() && File(user.voiceIntro).exists()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("معرفی صوتی", color = BrandViolet, fontFamily = Vazirmatn, fontSize = 12.sp)
                        VoiceIntroPlayer(user.voiceIntro)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(Icons.Default.Work, user.job)
                    InfoChip(Icons.Default.LocationOn, user.city)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Interests
        if (user.interests.isNotEmpty()) {
            Text("علایق", fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.padding(horizontal = 20.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                user.interests.forEach {
                    Surface(shape = RoundedCornerShape(20.dp), color = TinderPink.copy(alpha = 0.15f), border = BorderStroke(1.dp, TinderPink.copy(alpha = 0.4f))) {
                        Text(it, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = TextWhite, fontFamily = Vazirmatn, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Actions
        Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp)) {
            Column {
                ProfileActionRow(Icons.Default.Star, "ارتقا اشتراک (${plan.title})", TinderOrange, onUpgrade)
                HorizontalDivider(color = DarkCard)
                ProfileActionRow(Icons.Default.Edit, "ویرایش پروفایل", TinderPink, onEdit)
                HorizontalDivider(color = DarkCard)
                ProfileActionRow(Icons.Default.Block, "بلاک‌شده‌ها: $blockedCount", TextMuted, onShowBlocked)
            }
        }
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onDeleteRequest, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("درخواست حذف حساب کاربری", color = Color(0xFFFF6B6B), fontFamily = Vazirmatn, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, TinderPink)) {
            Icon(Icons.Default.Logout, null, tint = TinderPink); Spacer(Modifier.width(8.dp))
            Text("خروج از حساب کاربری", color = TinderPink, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(32.dp))
    }
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
private fun BottomNav(tab: Int, onTab: (Int) -> Unit, matchCount: Int) {
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
            Triple(2, Icons.Default.Visibility, "لایک‌شده"),
            Triple(3, Icons.Default.Chat, "چت"),
            Triple(4, Icons.Default.Person, "من")
        )
        items.forEach { (idx, icon, label) ->
            NavigationBarItem(
                selected = tab == idx, onClick = { onTab(idx) },
                icon = {
                    if (idx == 3 && matchCount > 0) BadgedBox(badge = { Badge { Text("$matchCount") } }) { Icon(icon, label) }
                    else Icon(icon, label)
                },
                label = { Text(label, fontFamily = Vazirmatn, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = TinderPink, selectedTextColor = TinderPink, indicatorColor = TinderPink.copy(alpha = 0.15f), unselectedIconColor = TextMuted, unselectedTextColor = TextMuted)
            )
        }
    }
    }
}
