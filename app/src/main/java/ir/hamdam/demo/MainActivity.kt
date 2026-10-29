package ir.hamdam.demo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Vazirmatn Font ───────────────────────────────────────────────────────────
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

// ─── Tinder-like Theme ────────────────────────────────────────────────────────
private val TinderPink = Color(0xFFFF4458)
private val TinderOrange = Color(0xFFFF7854)
private val DarkBg = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkCard = Color(0xFF2A2A2A)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)
private val TextMuted = Color(0xFF777777)
private val SuccessGreen = Color(0xFF2ED573)
private val GradientTinder = listOf(Color(0xFFFF4458), Color(0xFFFF7854))

// ─── Data ─────────────────────────────────────────────────────────────────────
enum class Gender { MALE, FEMALE, OTHER }
enum class Orientation { STRAIGHT, GAY, LESBIAN, BISEXUAL, OTHER }
enum class MsgType { TEXT, IMAGE, VOICE }

enum class SubscriptionPlan(val title: String, val dailySwipes: Int?) {
    FREE("رایگان", 10), PRO("پرو", 50), VIP("وی‌آی‌پی", null)
}

private fun formatToman(amount: Int): String =
    "%,d".format(amount).replace(',', '٬') + " تومان"

data class UserProfile(
    val name: String = "",
    val birthYear: Int = 2000,
    val birthMonth: Int = 1,
    val gender: Gender = Gender.OTHER,
    val orientation: Orientation = Orientation.STRAIGHT,
    val job: String = "",
    val city: String = "",
    val about: String = "",
    val interests: List<String> = emptyList(),
    /** URI strings of real photos selected from gallery/camera */
    val photos: List<String> = emptyList(),
    val primaryPhotoIndex: Int = 0,
    val phone: String = ""
) {
    val age: Int get() = Calendar.getInstance().get(Calendar.YEAR) - birthYear
    val primaryPhoto: String?
        get() = photos.getOrNull(primaryPhotoIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0)))
}

data class Person(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: Gender,
    val orientation: Orientation,
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
    val city: String = ""
)

private val persianMonths = listOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")

private val iranianCities = listOf(
    "تهران", "کرج", "مشهد", "اصفهان", "شیراز", "تبریز", "اهواز", "قم",
    "کرمانشاه", "ارومیه", "رشت", "زاهدان", "همدان", "کرمان", "یزد",
    "اردبیل", "بندرعباس", "اراک", "اسلام‌شهر", "زنجان", "سنندج", "قزوین",
    "خرم‌آباد", "گرگان", "ساری", "بوشهر", "بیرجند", "ایلام", "سمنان", "یاسوج",
    "بجنورد", "شهرکرد", "کیش", "قشم"
)

private val samplePeople = listOf(
    Person(1, "رها", 26, Gender.FEMALE, Orientation.STRAIGHT, "طراح گرافیک", "تهران",
        "اهل کافه‌گردی و کتاب. دنبال آشنایی واقعی.", listOf("کتاب", "کافه", "سفر", "طراحی"),
        "🌷", Color(0xFFFFE0E6), 3, likedYou = true),
    Person(2, "سارا", 28, Gender.FEMALE, Orientation.STRAIGHT, "مهندس نرم‌افزار", "کرج",
        "طبیعت‌گردی و موسیقی حال من را خوب می‌کند.", listOf("طبیعت", "موسیقی", "عکاسی", "کدنویسی"),
        "🌿", Color(0xFFE0F2E9), 12, likedYou = false),
    Person(3, "نیلوفر", 25, Gender.FEMALE, Orientation.BISEXUAL, "دانشجوی معماری", "تهران",
        "کنجکاوم دنیا را بیشتر بشناسم.", listOf("هنر", "پیاده‌روی", "فیلم", "معماری"),
        "🌼", Color(0xFFFFF0D6), 5, likedYou = true),
    Person(4, "مهسا", 29, Gender.FEMALE, Orientation.STRAIGHT, "معلم زبان", "اصفهان",
        "رابطه‌ای می‌خواهم که در آن هر دو رشد کنیم.", listOf("سفر", "آشپزی", "فیلم", "یوگا"),
        "🪻", Color(0xFFEDE0FF), 28, likedYou = false),
    Person(5, "آتنا", 24, Gender.FEMALE, Orientation.STRAIGHT, "محتواساز", "شیراز",
        "عاشق ساخت ویدیو و داستان‌گویی.", listOf("ویدیو", "خنده", "سفر", "کافه"),
        "✨", Color(0xFFFFE8D6), 8, likedYou = true),
    Person(6, "پریا", 27, Gender.FEMALE, Orientation.LESBIAN, "روانشناس", "مشهد",
        "گوش دادن خوب بلدم و عاشق گفت‌وگوهای عمیقم.", listOf("روانشناسی", "کتاب", "پیاده‌روی"),
        "🌙", Color(0xFFE0E8FF), 15, likedYou = false),
    Person(7, "آرمین", 27, Gender.MALE, Orientation.STRAIGHT, "برنامه‌نویس", "تهران",
        "عاشق کوهنوردی و قهوه.", listOf("کوهنوردی", "کافه", "کدنویسی", "فیلم"),
        "🏔️", Color(0xFFD6EAF8), 4, likedYou = true),
    Person(8, "کاوه", 30, Gender.MALE, Orientation.GAY, "عکاس", "تهران",
        "زندگی را از پشت لنز می‌بینم.", listOf("عکاسی", "سفر", "هنر", "موسیقی"),
        "📷", Color(0xFFF5D6E0), 7, likedYou = false),
    Person(9, "لیلا", 23, Gender.FEMALE, Orientation.BISEXUAL, "دانشجو", "کرج",
        "پر از انرژی و عاشق ماجراجویی.", listOf("رقص", "سفر", "موسیقی", "ورزش"),
        "🔥", Color(0xFFFFE0CC), 18, likedYou = true),
    Person(10, "نوید", 31, Gender.MALE, Orientation.STRAIGHT, "پزشک", "تهران",
        "به دنبال آرامش و ارتباط عمیق.", listOf("کتاب", "یوگا", "سفر", "آشپزی"),
        "🩺", Color(0xFFD5F5E3), 6, likedYou = false)
)

private val availableInterests = listOf(
    "کتاب", "کافه", "سفر", "موسیقی", "فیلم", "ورزش", "عکاسی", "آشپزی",
    "هنر", "طبیعت", "یوگا", "کدنویسی", "رقص", "بازی", "مد", "حیوانات", "کوهنوردی"
)
private val availableJobs = listOf("طراح گرافیک", "مهندس نرم‌افزار", "دانشجو", "معلم", "پزشک", "عکاس", "محتواساز", "برنامه‌نویس", "روانشناس", "آزاد")

// ─── Entry ────────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FastLoveApp() }
    }
}

@Composable
private fun FastLoveApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var hasProfile by remember { mutableStateOf(false) }
    var registrationFlow by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf(UserProfile()) }

    var tab by remember { mutableIntStateOf(0) }
    var discoverIndex by remember { mutableIntStateOf(0) }
    val likedPeople = remember { mutableStateListOf<Person>() }
    val matchedPeople = remember { mutableStateListOf<Person>() }
    val blockedIds = remember { mutableStateListOf<Int>() }
    /** ids of people the user paid to message without match */
    val paidDirectChatIds = remember { mutableStateListOf<Int>() }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var chatPerson by remember { mutableStateOf<Person?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showPayToMessage by remember { mutableStateOf<Person?>(null) }
    var subscriptionPlan by remember { mutableStateOf(SubscriptionPlan.FREE) }
    var filters by remember { mutableStateOf(FilterState()) }
    fun currentDayKey(): String = Calendar.getInstance().let {
        "%04d-%02d-%02d".format(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
    }
    var swipeDay by remember { mutableStateOf(currentDayKey()) }
    var dailySwipes by remember { mutableIntStateOf(0) }
    fun refreshDailySwipes() {
        val nowKey = currentDayKey()
        if (swipeDay != nowKey) { swipeDay = nowKey; dailySwipes = 0 }
    }
    fun canSwipe(): Boolean {
        refreshDailySwipes()
        val limit = subscriptionPlan.dailySwipes
        if (limit != null && dailySwipes >= limit) { showSubscription = true; return false }
        dailySwipes += 1
        return true
    }

    val filteredPeople = remember(filters, blockedIds) {
        samplePeople.filter { p ->
            p.id !in blockedIds &&
            p.age in filters.minAge..filters.maxAge &&
            p.distanceKm <= filters.maxDistance &&
            (filters.city.isBlank() || p.city == filters.city) &&
            (filters.jobs.isEmpty() || p.job in filters.jobs) &&
            (filters.interests.isEmpty() || p.interests.any { it in filters.interests })
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = TinderPink,
            secondary = TinderOrange,
            background = DarkBg,
            surface = DarkSurface,
            onPrimary = TextWhite,
            onBackground = TextWhite,
            onSurface = TextWhite
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
                    !isLoggedIn -> AuthFlow(onSuccess = { phone, register ->
                        currentUser = currentUser.copy(phone = phone)
                        registrationFlow = register
                        isLoggedIn = true
                        if (!register) hasProfile = true
                    })
                    !hasProfile -> ProfileSetupScreen(
                        phone = currentUser.phone,
                        onComplete = { profile ->
                            currentUser = profile
                            hasProfile = true
                        }
                    )
                    showSubscription -> SubscriptionScreen(
                        currentPlan = subscriptionPlan,
                        onBack = { showSubscription = false },
                        onChoosePlan = { plan ->
                            subscriptionPlan = plan
                            showSubscription = false
                        }
                    )
                    showEditProfile -> EditProfileScreen(
                        user = currentUser,
                        onSave = { updated ->
                            currentUser = updated
                            showEditProfile = false
                        },
                        onBack = { showEditProfile = false }
                    )
                    showPayToMessage != null -> PayToMessageDialog(
                        person = showPayToMessage!!,
                        onConfirm = {
                            val p = showPayToMessage!!
                            if (p.id !in paidDirectChatIds) paidDirectChatIds.add(p.id)
                            chatPerson = p
                            showPayToMessage = null
                        },
                        onDismiss = { showPayToMessage = null }
                    )
                    selectedPerson != null -> PersonDetailScreen(
                        person = selectedPerson!!,
                        canSeeLikes = subscriptionPlan != SubscriptionPlan.FREE,
                        isMatched = selectedPerson in matchedPeople,
                        canDirectMessage = selectedPerson!!.id in paidDirectChatIds || selectedPerson in matchedPeople,
                        onBack = { selectedPerson = null },
                        onLike = {
                            if (canSwipe()) {
                                val p = selectedPerson!!
                                if (p !in likedPeople) likedPeople.add(p)
                                if (p.likedYou && p !in matchedPeople) matchedPeople.add(p)
                                selectedPerson = null
                            }
                        },
                        onBlock = {
                            blockedIds.add(selectedPerson!!.id)
                            likedPeople.removeAll { it.id == selectedPerson!!.id }
                            matchedPeople.removeAll { it.id == selectedPerson!!.id }
                            selectedPerson = null
                        },
                        onChat = {
                            val p = selectedPerson!!
                            if (p in matchedPeople || p.id in paidDirectChatIds) {
                                chatPerson = p
                                selectedPerson = null
                            } else {
                                showPayToMessage = p
                            }
                        }
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
                                        person = filteredPeople.getOrNull(discoverIndex),
                                        onOpen = { selectedPerson = it },
                                        onLike = { p ->
                                            if (canSwipe()) {
                                                if (p !in likedPeople) likedPeople.add(p)
                                                if (p.likedYou && p !in matchedPeople) matchedPeople.add(p)
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
                                        }
                                    )
                                    1 -> LikedByMeScreen(
                                        liked = likedPeople.filter { it.id !in blockedIds },
                                        matched = matchedPeople,
                                        paidIds = paidDirectChatIds,
                                        onOpen = { selectedPerson = it },
                                        onChat = { p ->
                                            if (p in matchedPeople || p.id in paidDirectChatIds) {
                                                chatPerson = p
                                            } else {
                                                showPayToMessage = p
                                            }
                                        }
                                    )
                                    2 -> if (subscriptionPlan == SubscriptionPlan.FREE) {
                                        UpgradeTeaserScreen(onUpgrade = { showSubscription = true })
                                    } else LikedMeScreen(
                                        people = samplePeople.filter { it.likedYou && it.id !in blockedIds },
                                        matched = matchedPeople,
                                        onLikeBack = { p ->
                                            if (p !in likedPeople) likedPeople.add(p)
                                            if (p !in matchedPeople) matchedPeople.add(p)
                                        },
                                        onOpen = { selectedPerson = it },
                                        onBlock = { blockedIds.add(it.id) }
                                    )
                                    3 -> MatchesChatList(
                                        matches = matchedPeople.filter { it.id !in blockedIds } +
                                            samplePeople.filter { it.id in paidDirectChatIds && it !in matchedPeople && it.id !in blockedIds },
                                        onChat = { chatPerson = it }
                                    )
                                    4 -> MyProfileScreen(
                                        user = currentUser,
                                        blockedCount = blockedIds.size,
                                        plan = subscriptionPlan,
                                        onUpgrade = { showSubscription = true },
                                        onEdit = { showEditProfile = true },
                                        onLogout = {
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

// ─── Subscription ─────────────────────────────────────────────────────────────
@Composable
private fun SubscriptionScreen(
    currentPlan: SubscriptionPlan,
    onBack: () -> Unit,
    onChoosePlan: (SubscriptionPlan) -> Unit
) {
    var periodWeekly by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(DarkBg).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Text("اشتراک", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        }
        Spacer(Modifier.height(16.dp))
        Text("خرید صرفاً نمایشی است و پرداخت واقعی انجام نمی‌شود.", color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().background(DarkSurface, RoundedCornerShape(14.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(true to "هفتگی", false to "ماهانه").forEach { (weekly, label) ->
                val sel = periodWeekly == weekly
                Surface(
                    onClick = { periodWeekly = weekly },
                    shape = RoundedCornerShape(10.dp),
                    color = if (sel) TinderPink else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, Modifier.padding(vertical = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = TextWhite, fontFamily = Vazirmatn, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        listOf(
            SubscriptionPlan.PRO to (if (periodWeekly) 99000 else 299000),
            SubscriptionPlan.VIP to (if (periodWeekly) 149000 else 449000)
        ).forEach { (plan, price) ->
            val isCurrent = currentPlan == plan
            Card(
                onClick = { onChoosePlan(plan) },
                Modifier.fillMaxWidth().padding(vertical = 7.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCurrent) TinderPink.copy(alpha = 0.25f) else DarkSurface),
                border = if (isCurrent) BorderStroke(2.dp, TinderPink) else null
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(plan.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                        Spacer(Modifier.weight(1f))
                        Text(formatToman(price), color = TinderOrange, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when (plan) {
                            SubscriptionPlan.PRO -> "۵۰ سوایپ روزانه + دیدن لایک‌ها"
                            SubscriptionPlan.VIP -> "سوایپ نامحدود + همه امکانات"
                            else -> ""
                        },
                        color = TextGray, fontFamily = Vazirmatn, fontSize = 13.sp
                    )
                    if (isCurrent) {
                        Spacer(Modifier.height(8.dp))
                        Text("پلن فعلی شما", color = SuccessGreen, fontSize = 12.sp, fontFamily = Vazirmatn)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
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
        Text("با ارتقا به پرو یا وی‌آی‌پی می‌توانی ببینی چه کسانی به تو علاقه دارند.", color = TextGray, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        TinderButton("مشاهده پلن‌ها") { onUpgrade() }
    }
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
@Composable
private fun AuthFlow(onSuccess: (String, Boolean) -> Unit) {
    var step by remember { mutableIntStateOf(0) } // 0=choose, 1=phone, 2=otp
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var registerMode by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableIntStateOf(120) }
    LaunchedEffect(step) {
        if (step == 2) {
            secondsLeft = 120
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0A0C), DarkBg)))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier.size(100.dp).clip(CircleShape).background(Brush.linearGradient(GradientTinder)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Favorite, null, tint = TextWhite, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("فست لاو", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = TinderPink, fontFamily = Vazirmatn)
        Text("عشق اول و آخرت رو پیدا کن", fontSize = 15.sp, color = TextGray, fontFamily = Vazirmatn)
        Spacer(Modifier.height(30.dp))

        when (step) {
            0 -> {
                Text("خوش آمدید", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextWhite, fontFamily = Vazirmatn)
                Text("لطفاً یکی را انتخاب کنید", color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { registerMode = true; step = 1; error = "" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
                ) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("ثبت نام", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { registerMode = false; step = 1; error = "" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, TinderPink),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("ورود به حساب کاربری", fontFamily = Vazirmatn, fontSize = 16.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text("نسخه دمو — پیامک واقعی ارسال نمی‌شود", fontSize = 12.sp, color = TextMuted, fontFamily = Vazirmatn)
            }
            1 -> {
                Text(if (registerMode) "ثبت نام با شماره موبایل" else "ورود با شماره موبایل", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextWhite, fontFamily = Vazirmatn)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 11) phone = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹", fontFamily = Vazirmatn) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = TinderPink) }
                )
                if (error.isNotEmpty()) Text(error, color = TinderPink, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(20.dp))
                TinderButton(if (registerMode) "ادامه ثبت نام" else "ادامه ورود") {
                    if (phone.length >= 10) { error = ""; step = 2 } else error = "شماره معتبر وارد کنید"
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { step = 0; phone = ""; error = "" }) {
                    Text("بازگشت", color = TextGray, fontFamily = Vazirmatn)
                }
            }
            else -> {
                Text("کد تأیید", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextWhite, fontFamily = Vazirmatn)
                Text("کد تست: 123456", fontSize = 14.sp, color = TinderPink, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
                Text(
                    if (secondsLeft > 0) "زمان باقی‌مانده: ${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')}"
                    else "کد منقضی شده؛ دوباره درخواست بده",
                    color = if (secondsLeft > 0) TextGray else TinderPink,
                    fontFamily = Vazirmatn,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("۱۲۳۴۵۶", fontFamily = Vazirmatn) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = TinderPink) }
                )
                if (error.isNotEmpty()) Text(error, color = TinderPink, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(28.dp))
                TinderButton("تأیید و ادامه") {
                    if (secondsLeft <= 0) error = "کد منقضی شده؛ درخواست کد جدید بده"
                    else if (otp == "123456") { error = ""; onSuccess(phone, registerMode) }
                    else error = "کد اشتباه است (تست: ۱۲۳۴۵۶)"
                }
                TextButton(onClick = { if (secondsLeft <= 0) { otp = ""; secondsLeft = 120 } }) {
                    Text("درخواست مجدد کد", color = if (secondsLeft <= 0) TinderPink else TextMuted, fontFamily = Vazirmatn)
                }
                TextButton(onClick = { step = 1; otp = ""; error = "" }) {
                    Text("بازگشت", color = TextGray, fontFamily = Vazirmatn)
                }
            }
        }
    }
}

// ─── Profile Setup ────────────────────────────────────────────────────────────
@Composable
private fun ProfileSetupScreen(phone: String, onComplete: (UserProfile) -> Unit) {
    var step by remember { mutableIntStateOf(0) } // 0=basic, 1=identity, 2=photos+interests, 3=about
    var name by remember { mutableStateOf("") }
    var birthYear by remember { mutableIntStateOf(2000) }
    var birthMonth by remember { mutableIntStateOf(1) }
    var gender by remember { mutableStateOf(Gender.FEMALE) }
    var orientation by remember { mutableStateOf(Orientation.STRAIGHT) }
    var job by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("تهران") }
    var about by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var selectedPhotos by remember { mutableStateOf(listOf<String>()) } // Uri strings
    var primaryPhotoIndex by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showJobPicker by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val age = currentYear - birthYear
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Camera temp file
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createImageUri(ctx: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: ctx.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (selectedPhotos.size < 5) {
                selectedPhotos = selectedPhotos + it.toString()
                if (selectedPhotos.size == 1) primaryPhotoIndex = 0
            }
        }
        showPhotoSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                if (selectedPhotos.size < 5) {
                    selectedPhotos = selectedPhotos + uri.toString()
                    if (selectedPhotos.size == 1) primaryPhotoIndex = 0
                }
            }
        }
        showPhotoSheet = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val cameraGranted = results[Manifest.permission.CAMERA] == true
        val galleryGranted = results[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        // After permission, user can open sheet again
    }

    fun ensurePermissionsAndPick(fromCamera: Boolean) {
        val perms = mutableListOf<String>()
        if (fromCamera) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.CAMERA)
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
        if (perms.isNotEmpty()) {
            permissionLauncher.launch(perms.toTypedArray())
            return
        }
        if (fromCamera) {
            val uri = createImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("ساخت پروفایل", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (i <= step) TinderPink else DarkCard)
                    )
                }
            }
            Text(
                when (step) {
                    0 -> "اطلاعات پایه"
                    1 -> "هویت و موقعیت"
                    2 -> "عکس و علایق"
                    else -> "درباره تو"
                },
                color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 24.dp)
        ) {
            when (step) {
                0 -> {
                    SectionLabel("نام نمایشی")
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثلاً سارا", fontFamily = Vazirmatn) },
                        singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors(),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = TinderPink) }
                    )
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("ماه تولد")
                    var showMonthPicker by remember { mutableStateOf(false) }
                    Surface(onClick = { showMonthPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface,
                        border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = TinderPink)
                            Spacer(Modifier.width(12.dp)); Text(persianMonths[birthMonth - 1], color = TextWhite, fontFamily = Vazirmatn)
                            Spacer(Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showMonthPicker) AlertDialog(
                        onDismissRequest = { showMonthPicker = false },
                        title = { Text("ماه تولد", fontFamily = Vazirmatn) },
                        text = {
                            Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                                persianMonths.forEachIndexed { index, month ->
                                    TextButton(onClick = { birthMonth = index + 1; showMonthPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                        Text(month, color = if (birthMonth == index + 1) TinderPink else TextWhite, fontFamily = Vazirmatn)
                                    }
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showMonthPicker = false }) { Text("بستن", fontFamily = Vazirmatn) } },
                        containerColor = DarkSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("سال تولد")
                    Surface(
                        onClick = { showYearPicker = true },
                        shape = RoundedCornerShape(14.dp), color = DarkSurface,
                        border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, null, tint = TinderPink)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("$birthYear", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text("$age ساله", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                            }
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showYearPicker) {
                        YearPickerDialog(
                            selected = birthYear,
                            minYear = currentYear - 80,
                            maxYear = currentYear - 18,
                            onSelect = { birthYear = it; showYearPicker = false },
                            onDismiss = { showYearPicker = false }
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("جنسیت")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Gender.FEMALE to "زن", Gender.MALE to "مرد", Gender.OTHER to "دیگر").forEach { (g, label) ->
                            val sel = gender == g
                            FilterChip(
                                selected = sel, onClick = { gender = g },
                                label = { Text(label, fontFamily = Vazirmatn) },
                                colors = chipColors(sel)
                            )
                        }
                    }
                }
                1 -> {
                    SectionLabel("گرایش جنسی")
                    Text("بر اساس جنسیت شما پیشنهاد شده", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 8.dp))
                    val suggestedOrientations = when (gender) {
                        Gender.MALE -> listOf(
                            Orientation.STRAIGHT to "دگرجنس‌گرا",
                            Orientation.GAY to "همجنس‌گرا",
                            Orientation.BISEXUAL to "دوجنس‌گرا",
                            Orientation.OTHER to "دیگر"
                        )
                        Gender.FEMALE -> listOf(
                            Orientation.STRAIGHT to "دگرجنس‌گرا",
                            Orientation.LESBIAN to "لزبین",
                            Orientation.BISEXUAL to "دوجنس‌گرا",
                            Orientation.OTHER to "دیگر"
                        )
                        Gender.OTHER -> listOf(
                            Orientation.STRAIGHT to "دگرجنس‌گرا",
                            Orientation.GAY to "همجنس‌گرا",
                            Orientation.LESBIAN to "لزبین",
                            Orientation.BISEXUAL to "دوجنس‌گرا",
                            Orientation.OTHER to "دیگر"
                        )
                    }
                    LaunchedEffect(gender) {
                        if (suggestedOrientations.none { it.first == orientation }) {
                            orientation = suggestedOrientations.first().first
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestedOrientations.forEach { (o, label) ->
                            val sel = orientation == o
                            Surface(
                                onClick = { orientation = o },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) TinderPink.copy(alpha = 0.2f) else DarkSurface,
                                border = BorderStroke(1.dp, if (sel) TinderPink else DarkCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = sel, onClick = { orientation = o }, colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                                    Text(label, color = TextWhite, fontFamily = Vazirmatn)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("شهر")
                    Surface(
                        onClick = { showCityPicker = true },
                        shape = RoundedCornerShape(14.dp), color = DarkSurface,
                        border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = TinderPink)
                            Spacer(Modifier.width(12.dp))
                            Text(city, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showCityPicker) {
                        CityPickerDialog(
                            cities = iranianCities, selected = city,
                            onSelect = { city = it; showCityPicker = false },
                            onDismiss = { showCityPicker = false }
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("شغل")
                    Surface(
                        onClick = { showJobPicker = true },
                        shape = RoundedCornerShape(14.dp), color = DarkSurface,
                        border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Work, null, tint = TinderPink)
                            Spacer(Modifier.width(12.dp))
                            Text(if (job.isBlank()) "انتخاب شغل" else job, Modifier.weight(1f), color = if (job.isBlank()) TextMuted else TextWhite, fontFamily = Vazirmatn)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                    }
                    if (showJobPicker) {
                        CityPickerDialog(
                            cities = availableJobs, selected = job, title = "انتخاب شغل",
                            onSelect = { job = it; showJobPicker = false },
                            onDismiss = { showJobPicker = false }
                        )
                    }
                }
                2 -> {
                    SectionLabel("عکس‌های واقعی (حداکثر ۵)")
                    Text("حداقل یک عکس از گالری یا دوربین • روی عکس بزن تا اصلی شود", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(selectedPhotos) { i, uriStr ->
                            val isPrimary = i == primaryPhotoIndex
                            Box(
                                Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(if (isPrimary) 2.dp else 0.dp, TinderPink, RoundedCornerShape(16.dp))
                                    .clickable { primaryPhotoIndex = i }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isPrimary) {
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        color = TinderPink,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text("اصلی", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = TextWhite, fontSize = 10.sp, fontFamily = Vazirmatn)
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(i) }
                                        if (primaryPhotoIndex >= selectedPhotos.size) primaryPhotoIndex = (selectedPhotos.size - 1).coerceAtLeast(0)
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        if (selectedPhotos.size < 5) {
                            item {
                                Box(
                                    Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(DarkCard)
                                        .border(1.dp, TinderPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .clickable { showPhotoSheet = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, null, tint = TinderPink, modifier = Modifier.size(28.dp))
                                        Text("افزودن", fontSize = 11.sp, color = TinderPink, fontFamily = Vazirmatn)
                                    }
                                }
                            }
                        }
                    }
                    if (showPhotoSheet) {
                        PhotoSourceSheet(
                            onGallery = { ensurePermissionsAndPick(fromCamera = false) },
                            onCamera = { ensurePermissionsAndPick(fromCamera = true) },
                            onDismiss = { showPhotoSheet = false }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("علایق (حداقل ۳)")
                    Spacer(Modifier.height(8.dp))
                    InterestFlow(selectedInterests) { interest ->
                        selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest
                    }
                }
                3 -> {
                    SectionLabel("درباره من")
                    Text("چند خط بنویس تا بقیه بهتر بشناسنت", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = about, onValueChange = { about = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        placeholder = { Text("اهل کافه و کتابم، عاشق سفرهای کوتاه...", fontFamily = Vazirmatn) },
                        shape = RoundedCornerShape(14.dp), colors = fieldColors()
                    )
                    Spacer(Modifier.height(20.dp))
                    // Summary with photo slider
                    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            if (selectedPhotos.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(110.dp)) {
                                    itemsIndexed(selectedPhotos) { idx, uriStr ->
                                        val isMain = idx == primaryPhotoIndex
                                        Box(
                                            Modifier
                                                .size(110.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(if (isMain) 2.dp else 0.dp, TinderPink, RoundedCornerShape(12.dp))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            if (isMain) {
                                                Text(
                                                    "اصلی",
                                                    Modifier.align(Alignment.BottomCenter).background(TinderPink).padding(horizontal = 6.dp, vertical = 2.dp),
                                                    color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(GradientTinder)), contentAlignment = Alignment.Center) {
                                    val primary = selectedPhotos.getOrNull(primaryPhotoIndex)
                                    if (primary != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(Uri.parse(primary)).build(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text("♡", fontSize = 28.sp, color = TextWhite)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (name.isBlank()) "نام شما" else name, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 18.sp, fontFamily = Vazirmatn)
                                    Text("$age ساله  •  $city", color = TextGray, fontFamily = Vazirmatn, fontSize = 13.sp)
                                    Text(if (job.isBlank()) "شغل ثبت نشده" else job, color = TextMuted, fontFamily = Vazirmatn, fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            if (about.isNotBlank()) Text(about, color = TextGray, fontSize = 13.sp, lineHeight = 21.sp, fontFamily = Vazirmatn, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text("${selectedPhotos.size} عکس", fontFamily = Vazirmatn) }, leadingIcon = { Icon(Icons.Default.Photo, null) })
                                AssistChip(onClick = {}, label = { Text("${selectedInterests.size} علاقه", fontFamily = Vazirmatn) }, leadingIcon = { Icon(Icons.Default.Favorite, null) })
                                AssistChip(onClick = {}, label = { Text(persianMonths[birthMonth - 1], fontFamily = Vazirmatn) }, leadingIcon = { Icon(Icons.Default.Cake, null) })
                            }
                        }
                    }
                }
            }
            if (error.isNotEmpty()) {
                Text(error, color = TinderPink, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 16.dp))
            }
            Spacer(Modifier.height(24.dp))
        }

        // Bottom buttons
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { step--; error = "" },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TextMuted)
                ) {
                    Text("قبلی", fontFamily = Vazirmatn, color = TextWhite)
                }
            }
            Button(
                onClick = {
                    when (step) {
                        0 -> {
                            when {
                                name.isBlank() -> error = "نام را وارد کنید"
                                else -> { error = ""; step = 1 }
                            }
                        }
                        1 -> {
                            when {
                                job.isBlank() -> error = "شغل را انتخاب کنید"
                                else -> { error = ""; step = 2 }
                            }
                        }
                        2 -> {
                            when {
                                selectedPhotos.isEmpty() -> error = "حداقل یک عکس واقعی اضافه کنید"
                                selectedInterests.size < 3 -> error = "حداقل ۳ علاقه انتخاب کنید"
                                else -> { error = ""; step = 3 }
                            }
                        }
                        3 -> {
                            error = ""
                            onComplete(
                                UserProfile(
                                    name = name.trim(),
                                    birthYear = birthYear,
                                    birthMonth = birthMonth,
                                    gender = gender,
                                    orientation = orientation,
                                    job = job,
                                    city = city,
                                    about = about.trim().ifBlank { "هنوز چیزی ننوشتم 😊" },
                                    interests = selectedInterests.toList(),
                                    photos = selectedPhotos,
                                    primaryPhotoIndex = primaryPhotoIndex.coerceIn(0, (selectedPhotos.size - 1).coerceAtLeast(0)),
                                    phone = phone
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.weight(if (step > 0) 1.5f else 1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
            ) {
                Text(if (step == 3) "شروع کن 🚀" else "بعدی", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Edit Profile (gender & age locked) ───────────────────────────────────────
@Composable
private fun EditProfileScreen(user: UserProfile, onSave: (UserProfile) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var job by remember { mutableStateOf(user.job) }
    var city by remember { mutableStateOf(user.city) }
    var about by remember { mutableStateOf(user.about) }
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
        uri?.let {
            if (selectedPhotos.size < 5) {
                selectedPhotos = selectedPhotos + it.toString()
            }
        }
        showPhotoSheet = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                if (selectedPhotos.size < 5) selectedPhotos = selectedPhotos + uri.toString()
            }
        }
        showPhotoSheet = false
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    fun ensurePermissionsAndPick(fromCamera: Boolean) {
        val perms = mutableListOf<String>()
        if (fromCamera) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                perms.add(Manifest.permission.CAMERA)
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                    perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (perms.isNotEmpty()) {
            permissionLauncher.launch(perms.toTypedArray())
            return
        }
        if (fromCamera) {
            val uri = createImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else galleryLauncher.launch("image/*")
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
            Spacer(Modifier.height(8.dp))
            Text("${user.gender.name} • ${user.age} ساله", color = TextGray, fontFamily = Vazirmatn)
            Spacer(Modifier.height(16.dp))
            SectionLabel("نام")
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors())
            Spacer(Modifier.height(12.dp))
            SectionLabel("شهر")
            Surface(onClick = { showCityPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(city, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn)
                    Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                }
            }
            if (showCityPicker) CityPickerDialog(cities = iranianCities, selected = city, onSelect = { city = it; showCityPicker = false }, onDismiss = { showCityPicker = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("شغل")
            Surface(onClick = { showJobPicker = true }, shape = RoundedCornerShape(14.dp), color = DarkSurface, border = BorderStroke(1.dp, DarkCard), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(job, Modifier.weight(1f), color = TextWhite, fontFamily = Vazirmatn)
                    Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                }
            }
            if (showJobPicker) CityPickerDialog(cities = availableJobs, selected = job, title = "شغل", onSelect = { job = it; showJobPicker = false }, onDismiss = { showJobPicker = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("گرایش جنسی")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                suggestedOrientations.forEach { (o, label) ->
                    val sel = orientation == o
                    Surface(onClick = { orientation = o }, shape = RoundedCornerShape(10.dp), color = if (sel) TinderPink.copy(alpha = 0.2f) else DarkSurface, border = BorderStroke(1.dp, if (sel) TinderPink else DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sel, onClick = { orientation = o }, colors = RadioButtonDefaults.colors(selectedColor = TinderPink))
                            Text(label, color = TextWhite, fontFamily = Vazirmatn)
                        }
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
                        IconButton(onClick = {
                            selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(i) }
                            if (primaryPhotoIndex >= selectedPhotos.size) primaryPhotoIndex = (selectedPhotos.size - 1).coerceAtLeast(0)
                        }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = TextWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (selectedPhotos.size < 5) {
                    item {
                        Box(Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(DarkCard).clickable { showPhotoSheet = true }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddAPhoto, null, tint = TinderPink)
                        }
                    }
                }
            }
            if (showPhotoSheet) PhotoSourceSheet(onGallery = { ensurePermissionsAndPick(false) }, onCamera = { ensurePermissionsAndPick(true) }, onDismiss = { showPhotoSheet = false })
            Spacer(Modifier.height(12.dp))
            SectionLabel("درباره من")
            OutlinedTextField(value = about, onValueChange = { about = it }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(14.dp), colors = fieldColors())
            Spacer(Modifier.height(12.dp))
            SectionLabel("علایق")
            InterestFlow(selectedInterests) { interest ->
                selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest
            }
            Spacer(Modifier.height(24.dp))
        }
        Button(
            onClick = {
                onSave(
                    user.copy(
                        name = name.trim(),
                        job = job,
                        city = city,
                        about = about.trim(),
                        orientation = orientation,
                        interests = selectedInterests.toList(),
                        photos = selectedPhotos,
                        primaryPhotoIndex = primaryPhotoIndex.coerceIn(0, (selectedPhotos.size - 1).coerceAtLeast(0))
                        // gender & birthYear locked
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
        ) {
            Text("ذخیره تغییرات", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Pay to message (1M toman) ────────────────────────────────────────────────
@Composable
private fun PayToMessageDialog(person: Person, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیام مستقیم", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("هنوز با ${person.name} مچ نشده‌اید.", fontFamily = Vazirmatn, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("با پرداخت ۱٬۰۰۰٬۰۰۰ تومان می‌توانید بدون مچ مستقیماً پیام بفرستید.", fontFamily = Vazirmatn, color = TextWhite)
                Spacer(Modifier.height(8.dp))
                Text("این پرداخت در نسخه دمو صرفاً نمایشی است.", fontSize = 12.sp, color = TextMuted, fontFamily = Vazirmatn)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = TinderPink)) {
                Text("پرداخت ۱ میلیون تومان", fontFamily = Vazirmatn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", fontFamily = Vazirmatn) }
        },
        containerColor = DarkSurface
    )
}

// ─── Helpers UI ───────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, color = TextWhite, fontSize = 15.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun TinderButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
    ) {
        Text(text, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TinderPink,
    unfocusedBorderColor = DarkCard,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    cursorColor = TinderPink,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)

@Composable
private fun chipColors(selected: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = TinderPink.copy(alpha = 0.3f),
    selectedLabelColor = TextWhite,
    containerColor = DarkSurface,
    labelColor = TextGray
)

@Composable
private fun PhotoSourceSheet(onGallery: () -> Unit, onCamera: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب عکس", fontFamily = Vazirmatn) },
        text = {
            Column {
                Text("مجوز دوربین و گالری در صورت نیاز درخواست می‌شود.", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onGallery, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = TinderPink)
                    Spacer(Modifier.width(8.dp))
                    Text("گالری", fontFamily = Vazirmatn, color = TextWhite)
                }
                TextButton(onClick = onCamera, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CameraAlt, null, tint = TinderPink)
                    Spacer(Modifier.width(8.dp))
                    Text("دوربین", fontFamily = Vazirmatn, color = TextWhite)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن", fontFamily = Vazirmatn) } },
        containerColor = DarkSurface
    )
}

@Composable
private fun InterestFlow(selected: Set<String>, onToggle: (String) -> Unit) {
    Column {
        availableInterests.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { interest ->
                    val sel = interest in selected
                    FilterChip(
                        selected = sel,
                        onClick = { onToggle(interest) },
                        label = { Text(interest, fontFamily = Vazirmatn, fontSize = 13.sp) },
                        colors = chipColors(sel),
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun YearPickerDialog(selected: Int, minYear: Int, maxYear: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سال تولد", fontFamily = Vazirmatn) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                (maxYear downTo minYear).forEach { y ->
                    TextButton(onClick = { onSelect(y) }, modifier = Modifier.fillMaxWidth()) {
                        Text("$y", color = if (y == selected) TinderPink else TextWhite, fontFamily = Vazirmatn)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن", fontFamily = Vazirmatn) } },
        containerColor = DarkSurface
    )
}

@Composable
private fun CityPickerDialog(cities: List<String>, selected: String, title: String = "انتخاب شهر", onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = cities.filter { it.contains(query) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = Vazirmatn) },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("جستجو...", fontFamily = Vazirmatn) }, singleLine = true, colors = fieldColors(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    filtered.forEach { c ->
                        TextButton(onClick = { onSelect(c) }, modifier = Modifier.fillMaxWidth()) {
                            Text(c, color = if (c == selected) TinderPink else TextWhite, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن", fontFamily = Vazirmatn) } },
        containerColor = DarkSurface
    )
}

@Composable
private fun FilterScreen(current: FilterState, onApply: (FilterState) -> Unit, onClose: () -> Unit) {
    var minAge by remember { mutableIntStateOf(current.minAge) }
    var maxAge by remember { mutableIntStateOf(current.maxAge) }
    var maxDistance by remember { mutableIntStateOf(current.maxDistance) }
    var city by remember { mutableStateOf(current.city) }
    var jobs by remember { mutableStateOf(current.jobs) }
    var interests by remember { mutableStateOf(current.interests) }
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().background(DarkBg).verticalScroll(scroll).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = TextWhite) }
            Text("فیلترها", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        }
        Spacer(Modifier.height(16.dp))
        Text("سن: $minAge – $maxAge", color = TextWhite, fontFamily = Vazirmatn)
        RangeSlider(
            value = minAge.toFloat()..maxAge.toFloat(),
            onValueChange = { minAge = it.start.roundToInt(); maxAge = it.endInclusive.roundToInt() },
            valueRange = 18f..60f,
            colors = SliderDefaults.colors(thumbColor = TinderPink, activeTrackColor = TinderPink)
        )
        Spacer(Modifier.height(12.dp))
        Text("حداکثر فاصله: $maxDistance کیلومتر", color = TextWhite, fontFamily = Vazirmatn)
        Slider(value = maxDistance.toFloat(), onValueChange = { maxDistance = it.roundToInt() }, valueRange = 1f..100f, colors = SliderDefaults.colors(thumbColor = TinderPink, activeTrackColor = TinderPink))
        Spacer(Modifier.height(12.dp))
        SectionLabel("شهر")
        OutlinedTextField(value = city, onValueChange = { city = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("همه شهرها", fontFamily = Vazirmatn) }, singleLine = true, colors = fieldColors(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        TinderButton("اعمال فیلتر") {
            onApply(FilterState(minAge, maxAge, jobs, interests, maxDistance, city.trim()))
        }
    }
}

// ─── Discover with drag-to-swipe ──────────────────────────────────────────────
@Composable
private fun DiscoverScreen(
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val rotation = (offsetX / 40f).coerceIn(-15f, 15f)
    val likeAlpha = (offsetX / 200f).coerceIn(0f, 1f)
    val passAlpha = (-offsetX / 200f).coerceIn(0f, 1f)

    fun resetCard() {
        offsetX = 0f
        offsetY = 0f
    }

    fun animateOut(toRight: Boolean, onEnd: () -> Unit) {
        scope.launch {
            val target = if (toRight) 1200f else -1200f
            val anim = Animatable(offsetX)
            anim.animateTo(target, animationSpec = tween(280, easing = FastOutSlowInEasing)) {
                offsetX = value
            }
            onEnd()
            resetCard()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, null, tint = TinderPink, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text("فست لاو", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TinderPink, fontFamily = Vazirmatn)
            Spacer(Modifier.weight(1f))
            if (swipesRemaining != null) {
                Text("سوایپ روزانه: $swipesRemaining", color = if (swipesRemaining <= 2) TinderOrange else TextGray, fontSize = 11.sp, fontFamily = Vazirmatn)
                IconButton(onClick = onUpgrade) { Icon(Icons.Default.Star, "ارتقا", tint = TinderOrange) }
            }
            IconButton(onClick = onFilter) { Icon(Icons.Default.Tune, "فیلتر", tint = TextWhite) }
        }

        if (person == null) {
            EmptyState("دیگه کسی نیست!", "فیلترها رو تغییر بده یا بعداً بیا", "🎉")
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Card(
                    Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .graphicsLayer { rotationZ = rotation }
                        .pointerInput(person.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    when {
                                        offsetX > 180f -> animateOut(true) { onLike(person) }
                                        offsetX < -180f -> animateOut(false) { onPass() }
                                        else -> {
                                            scope.launch {
                                                val ax = Animatable(offsetX)
                                                val ay = Animatable(offsetY)
                                                launch { ax.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) { offsetX = value } }
                                                launch { ay.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) { offsetY = value } }
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y * 0.3f
                                }
                            )
                        }
                        .clickable { onOpen(person) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(person.tint, DarkCard))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(person.emoji, fontSize = 140.sp)
                        }
                        // LIKE / NOPE overlays
                        if (likeAlpha > 0.05f) {
                            Text(
                                "LIKE",
                                Modifier
                                    .align(Alignment.TopStart)
                                    .padding(24.dp)
                                    .rotate(-20f)
                                    .border(3.dp, SuccessGreen, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                color = SuccessGreen.copy(alpha = likeAlpha),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (passAlpha > 0.05f) {
                            Text(
                                "NOPE",
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(24.dp)
                                    .rotate(20f)
                                    .border(3.dp, TinderPink, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                color = TinderPink.copy(alpha = passAlpha),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            Modifier.fillMaxWidth().fillMaxHeight(0.4f).align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
                        )
                        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            if (person.likedYou && canSeeLikes) {
                                Surface(shape = RoundedCornerShape(20.dp), color = TinderPink) {
                                    Text("❤️ تو رو لایک کرده", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            Text("${person.name}, ${person.age}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                            Text("${person.job} · ${person.distanceKm} کیلومتر · ${person.city}", color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn)
                            Spacer(Modifier.height(6.dp))
                            Text(person.about, color = TextWhite.copy(alpha = 0.9f), fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontFamily = Vazirmatn)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                person.interests.take(3).forEach { tag ->
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                        Text(tag, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = TextWhite, fontSize = 12.sp, fontFamily = Vazirmatn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Action buttons still available
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { animateOut(false) { onPass() } },
                    modifier = Modifier.size(60.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkCard, contentColor = TextGray)
                ) {
                    Icon(Icons.Default.Close, "رد", modifier = Modifier.size(30.dp))
                }
                FilledIconButton(
                    onClick = { onBlock(person) },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkCard, contentColor = Color(0xFFFF6B6B))
                ) {
                    Icon(Icons.Default.Block, "بلاک", modifier = Modifier.size(22.dp))
                }
                FilledIconButton(
                    onClick = { animateOut(true) { onLike(person) } },
                    modifier = Modifier.size(60.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink, contentColor = TextWhite)
                ) {
                    Icon(Icons.Default.Favorite, "لایک", modifier = Modifier.size(30.dp))
                }
            }
            Text("کارت را به چپ یا راست بکشید", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun LikedByMeScreen(
    liked: List<Person>,
    matched: List<Person>,
    paidIds: List<Int>,
    onOpen: (Person) -> Unit,
    onChat: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("لایک‌های من", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(12.dp))
        if (liked.isEmpty()) EmptyState("هنوز کسی را لایک نکردی", "برو به کشف و سوایپ کن", "💔")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(liked) { p ->
                val isMatched = p in matched
                val canChat = isMatched || p.id in paidIds
                PersonRow(person = p, onClick = { onOpen(p) }) {
                    if (canChat) {
                        TextButton(onClick = { onChat(p) }) {
                            Text(if (isMatched) "مچ 💕" else "چت (پرداخت‌شده)", color = TinderPink, fontSize = 12.sp, fontFamily = Vazirmatn)
                        }
                    } else {
                        TextButton(onClick = { onChat(p) }) {
                            Text("پیام (۱ میلیون)", color = TinderOrange, fontSize = 11.sp, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LikedMeScreen(
    people: List<Person>,
    matched: List<Person>,
    onLikeBack: (Person) -> Unit,
    onOpen: (Person) -> Unit,
    onBlock: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("کسانی که تو را لایک کرده‌اند", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(12.dp))
        if (people.isEmpty()) EmptyState("هنوز کسی لایکت نکرده", "", "🌸")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(people) { p ->
                val isMatched = p in matched
                PersonRow(person = p, onClick = { onOpen(p) }) {
                    if (isMatched) {
                        Surface(shape = RoundedCornerShape(12.dp), color = TinderPink.copy(alpha = 0.2f)) {
                            Text("مچ 💕", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TinderPink, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                        }
                    } else {
                        TextButton(onClick = { onLikeBack(p) }) {
                            Text("لایک متقابل", color = SuccessGreen, fontFamily = Vazirmatn, fontSize = 12.sp)
                        }
                    }
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
        if (matches.isEmpty()) EmptyState("هنوز مچی نداری", "با لایک متقابل مچ شو یا پیام مستقیم بخر", "💬")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(matches) { p ->
                PersonRow(person = p, onClick = { onChat(p) }) {
                    Icon(Icons.Default.Chat, null, tint = TinderPink)
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(person: Person, onBack: () -> Unit) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("سلام! خوشحالم که مچ شدیم 😊", false, "۱۰:۳۰"),
            ChatMessage("سلام، منم خوشحالم!", true, "۱۰:۳۲")
        )
    }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    Column(Modifier.fillMaxSize().background(DarkBg).imePadding()) {
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            Box(Modifier.size(40.dp).clip(CircleShape).background(person.tint), contentAlignment = Alignment.Center) {
                Text(person.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(person.name, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Text("آنلاین", color = SuccessGreen, fontSize = 12.sp, fontFamily = Vazirmatn)
            }
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(messages) { msg ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isMe) Arrangement.Start else Arrangement.End) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (msg.isMe) TinderPink else DarkCard,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        when (msg.type) {
                            MsgType.TEXT -> Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(msg.text, color = TextWhite, fontFamily = Vazirmatn, fontSize = 15.sp)
                                Text(msg.time, color = TextWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.align(Alignment.End))
                            }
                            else -> Text(msg.text, Modifier.padding(12.dp), color = TextWhite, fontFamily = Vazirmatn)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(8.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("پیام بنویس...", fontFamily = Vazirmatn) },
                shape = RoundedCornerShape(24.dp),
                colors = fieldColors(),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        messages.add(ChatMessage(input.trim(), true, SimpleDateFormat("HH:mm", Locale("fa")).format(Date())))
                        input = ""
                        keyboard?.hide()
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink)
            ) {
                Icon(Icons.Default.Send, null)
            }
        }
    }
}

@Composable
private fun PersonDetailScreen(
    person: Person,
    canSeeLikes: Boolean,
    isMatched: Boolean,
    canDirectMessage: Boolean,
    onBack: () -> Unit,
    onLike: () -> Unit,
    onBlock: () -> Unit,
    onChat: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(320.dp).background(Brush.verticalGradient(listOf(person.tint, DarkBg)))) {
            Text(person.emoji, fontSize = 120.sp, modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Default.ArrowBack, null, tint = TextWhite)
            }
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
                person.interests.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag, fontFamily = Vazirmatn) })
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isMatched) {
                    Button(onClick = onLike, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = TinderPink), shape = RoundedCornerShape(14.dp)) {
                        Text("لایک", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = onChat,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (canDirectMessage) SuccessGreen else TinderOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        when {
                            isMatched -> "چت"
                            canDirectMessage -> "چت (پرداخت‌شده)"
                            else -> "پیام (۱ میلیون)"
                        },
                        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBlock, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFFF6B6B))) {
                Text("بلاک", color = Color(0xFFFF6B6B), fontFamily = Vazirmatn)
            }
        }
    }
}

@Composable
private fun MyProfileScreen(
    user: UserProfile,
    blockedCount: Int,
    plan: SubscriptionPlan,
    onUpgrade: () -> Unit,
    onEdit: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("پروفایل من", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "ویرایش", tint = TinderPink) }
        }
        Spacer(Modifier.height(16.dp))
        if (user.photos.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(user.photos) { idx, uriStr ->
                    val isMain = idx == user.primaryPhotoIndex
                    Box(Modifier.size(120.dp).clip(RoundedCornerShape(14.dp)).border(if (isMain) 2.dp else 0.dp, TinderPink, RoundedCornerShape(14.dp))) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(Uri.parse(uriStr)).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isMain) Text("اصلی", Modifier.align(Alignment.BottomCenter).background(TinderPink).padding(4.dp), color = TextWhite, fontSize = 11.sp, fontFamily = Vazirmatn)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Text("${user.age} ساله • ${user.city} • ${user.job}", color = TextGray, fontFamily = Vazirmatn)
        val oLabel = when (user.orientation) {
            Orientation.STRAIGHT -> "دگرجنس‌گرا"
            Orientation.GAY -> "همجنس‌گرا"
            Orientation.LESBIAN -> "لزبین"
            Orientation.BISEXUAL -> "دوجنس‌گرا"
            Orientation.OTHER -> "دیگر"
        }
        Text(oLabel, color = TextMuted, fontFamily = Vazirmatn, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Text(user.about, color = TextWhite, fontFamily = Vazirmatn, lineHeight = 22.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            user.interests.forEach { AssistChip(onClick = {}, label = { Text(it, fontFamily = Vazirmatn) }) }
        }
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("اشتراک: ${plan.title}", fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onUpgrade) { Text("ارتقا یا تغییر پلن", color = TinderOrange, fontFamily = Vazirmatn) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("بلاک‌شده‌ها: $blockedCount", color = TextMuted, fontFamily = Vazirmatn, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, TinderPink)) {
            Text("خروج از حساب", color = TinderPink, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun PersonRow(person: Person, onClick: () -> Unit, action: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(person.tint), contentAlignment = Alignment.Center) {
                Text(person.emoji, fontSize = 26.sp)
            }
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
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(emoji, fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = TextMuted, fontFamily = Vazirmatn, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BottomNav(tab: Int, onTab: (Int) -> Unit, matchCount: Int) {
    NavigationBar(containerColor = DarkSurface, contentColor = TextWhite) {
        val items = listOf(
            Triple(0, Icons.Default.Explore, "کشف"),
            Triple(1, Icons.Default.FavoriteBorder, "لایک‌ها"),
            Triple(2, Icons.Default.Visibility, "لایک‌شده"),
            Triple(3, Icons.Default.Chat, "چت"),
            Triple(4, Icons.Default.Person, "من")
        )
        items.forEach { (idx, icon, label) ->
            NavigationBarItem(
                selected = tab == idx,
                onClick = { onTab(idx) },
                icon = {
                    if (idx == 3 && matchCount > 0) {
                        BadgedBox(badge = { Badge { Text("$matchCount") } }) {
                            Icon(icon, label)
                        }
                    } else Icon(icon, label)
                },
                label = { Text(label, fontFamily = Vazirmatn, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TinderPink,
                    selectedTextColor = TinderPink,
                    indicatorColor = TinderPink.copy(alpha = 0.15f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
