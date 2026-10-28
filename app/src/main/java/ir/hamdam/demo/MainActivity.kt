package ir.hamdam.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// ─── Vazirmatn Font ───────────────────────────────────────────────────────────
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

// ─── Tinder-like Theme Colors ─────────────────────────────────────────────────
private val TinderPink = Color(0xFFFF4458)
private val TinderPinkDark = Color(0xFFE63950)
private val TinderOrange = Color(0xFFFF7854)
private val DarkBg = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkCard = Color(0xFF2A2A2A)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)
private val TextMuted = Color(0xFF777777)
private val SuccessGreen = Color(0xFF2ED573)
private val GradientTinder = listOf(Color(0xFFFF4458), Color(0xFFFF7854))

// ─── Data Models ──────────────────────────────────────────────────────────────
enum class Gender { MALE, FEMALE, OTHER }
enum class Orientation { STRAIGHT, GAY, LESBIAN, BISEXUAL, OTHER }

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.OTHER,
    val orientation: Orientation = Orientation.STRAIGHT,
    val job: String = "",
    val city: String = "",
    val about: String = "",
    val interests: List<String> = emptyList(),
    val photos: List<String> = emptyList(), // local emoji or uri string for demo
    val phone: String = ""
)

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
    val photoUrl: String,          // real-looking test photo
    val distanceKm: Int,
    val likedYou: Boolean = false
)

data class ChatMessage(val text: String, val isMe: Boolean, val time: String)

data class FilterState(
    val minAge: Int = 18,
    val maxAge: Int = 45,
    val jobs: Set<String> = emptySet(),
    val interests: Set<String> = emptySet(),
    val maxDistance: Int = 50
)

// ─── Sample People with realistic test photo URLs (pravatar / similar) ────────
private val samplePeople = listOf(
    Person(1, "رها", 26, Gender.FEMALE, Orientation.STRAIGHT, "طراح گرافیک", "تهران",
        "اهل کافه‌گردی و کتاب. دنبال آشنایی واقعی.", listOf("کتاب", "کافه", "سفر", "طراحی"),
        "https://i.pravatar.cc/600?img=5", 3, likedYou = true),
    Person(2, "سارا", 28, Gender.FEMALE, Orientation.STRAIGHT, "مهندس نرم‌افزار", "کرج",
        "طبیعت‌گردی و موسیقی حال من را خوب می‌کند.", listOf("طبیعت", "موسیقی", "عکاسی", "کدنویسی"),
        "https://i.pravatar.cc/600?img=9", 12, likedYou = false),
    Person(3, "نیلوفر", 25, Gender.FEMALE, Orientation.BISEXUAL, "دانشجوی معماری", "تهران",
        "کنجکاوم دنیا را بیشتر بشناسم.", listOf("هنر", "پیاده‌روی", "فیلم", "معماری"),
        "https://i.pravatar.cc/600?img=10", 5, likedYou = true),
    Person(4, "مهسا", 29, Gender.FEMALE, Orientation.STRAIGHT, "معلم زبان", "اصفهان",
        "رابطه‌ای می‌خواهم که در آن هر دو رشد کنیم.", listOf("سفر", "آشپزی", "فیلم", "یوگا"),
        "https://i.pravatar.cc/600?img=16", 28, likedYou = false),
    Person(5, "آتنا", 24, Gender.FEMALE, Orientation.STRAIGHT, "محتواساز", "شیراز",
        "عاشق ساخت ویدیو و داستان‌گویی.", listOf("ویدیو", "خنده", "سفر", "کافه"),
        "https://i.pravatar.cc/600?img=20", 8, likedYou = true),
    Person(6, "پریا", 27, Gender.FEMALE, Orientation.LESBIAN, "روانشناس", "مشهد",
        "گوش دادن خوب بلدم و عاشق گفت‌وگوهای عمیقم.", listOf("روانشناسی", "کتاب", "پیاده‌روی"),
        "https://i.pravatar.cc/600?img=25", 15, likedYou = false),
    Person(7, "آرمین", 27, Gender.MALE, Orientation.STRAIGHT, "برنامه‌نویس", "تهران",
        "عاشق کوهنوردی و قهوه.", listOf("کوهنوردی", "کافه", "کدنویسی", "فیلم"),
        "https://i.pravatar.cc/600?img=12", 4, likedYou = true),
    Person(8, "کاوه", 30, Gender.MALE, Orientation.GAY, "عکاس", "تهران",
        "زندگی را از پشت لنز می‌بینم.", listOf("عکاسی", "سفر", "هنر", "موسیقی"),
        "https://i.pravatar.cc/600?img=15", 7, likedYou = false),
    Person(9, "لیلا", 23, Gender.FEMALE, Orientation.BISEXUAL, "دانشجو", "کرج",
        "پر از انرژی و عاشق ماجراجویی.", listOf("رقص", "سفر", "موسیقی", "ورزش"),
        "https://i.pravatar.cc/600?img=32", 18, likedYou = true),
    Person(10, "نوید", 31, Gender.MALE, Orientation.STRAIGHT, "پزشک", "تهران",
        "به دنبال آرامش و ارتباط عمیق.", listOf("کتاب", "یوگا", "سفر", "آشپزی"),
        "https://i.pravatar.cc/600?img=33", 6, likedYou = false)
)

private val availableInterests = listOf(
    "کتاب", "کافه", "سفر", "موسیقی", "فیلم", "ورزش", "عکاسی", "آشپزی",
    "هنر", "طبیعت", "یوگا", "کدنویسی", "رقص", "بازی", "مد", "حیوانات", "کوهنوردی"
)
private val availableJobs = listOf("طراح گرافیک", "مهندس نرم‌افزار", "دانشجو", "معلم", "پزشک", "عکاس", "محتواساز", "برنامه‌نویس", "روانشناس", "آزاد")
private val demoPhotoEmojis = listOf("😊", "😎", "🥳", "😍", "🥰", "😇", "🤩", "😋", "🔥", "✨")

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
    var currentUser by remember { mutableStateOf(UserProfile()) }

    var tab by remember { mutableIntStateOf(0) }
    var discoverIndex by remember { mutableIntStateOf(0) }
    val likedPeople = remember { mutableStateListOf<Person>() }
    val matchedPeople = remember { mutableStateListOf<Person>() }
    val blockedIds = remember { mutableStateListOf<Int>() }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var chatPerson by remember { mutableStateOf<Person?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(FilterState()) }

    val filteredPeople = remember(filters, blockedIds) {
        samplePeople.filter { p ->
            p.id !in blockedIds &&
            p.age in filters.minAge..filters.maxAge &&
            p.distanceKm <= filters.maxDistance &&
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
            Surface(Modifier.fillMaxSize(), color = DarkBg) {
                when {
                    !isLoggedIn -> AuthFlow(onSuccess = { phone ->
                        currentUser = currentUser.copy(phone = phone)
                        isLoggedIn = true
                    })
                    !hasProfile -> ProfileSetupScreen(
                        phone = currentUser.phone,
                        onComplete = { profile ->
                            currentUser = profile
                            hasProfile = true
                        }
                    )
                    selectedPerson != null -> PersonDetailScreen(
                        person = selectedPerson!!,
                        onBack = { selectedPerson = null },
                        onLike = {
                            val p = selectedPerson!!
                            if (p !in likedPeople) likedPeople.add(p)
                            if (p.likedYou && p !in matchedPeople) matchedPeople.add(p)
                            selectedPerson = null
                        },
                        onBlock = {
                            blockedIds.add(selectedPerson!!.id)
                            likedPeople.removeAll { it.id == selectedPerson!!.id }
                            matchedPeople.removeAll { it.id == selectedPerson!!.id }
                            selectedPerson = null
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
                                            if (p !in likedPeople) likedPeople.add(p)
                                            if (p.likedYou && p !in matchedPeople) matchedPeople.add(p)
                                            discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                        },
                                        onPass = {
                                            discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                        },
                                        onFilter = { showFilters = true },
                                        onBlock = { p ->
                                            blockedIds.add(p.id)
                                            discoverIndex = (discoverIndex + 1).coerceAtMost(filteredPeople.size)
                                        }
                                    )
                                    1 -> LikedByMeScreen(
                                        liked = likedPeople.filter { it.id !in blockedIds },
                                        onOpen = { selectedPerson = it },
                                        onChat = { if (it in matchedPeople) chatPerson = it }
                                    )
                                    2 -> LikedMeScreen(
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
                                        matches = matchedPeople.filter { it.id !in blockedIds },
                                        onChat = { chatPerson = it }
                                    )
                                    4 -> MyProfileScreen(
                                        user = currentUser,
                                        blockedCount = blockedIds.size,
                                        onLogout = {
                                            isLoggedIn = false
                                            hasProfile = false
                                            currentUser = UserProfile()
                                            likedPeople.clear()
                                            matchedPeople.clear()
                                            blockedIds.clear()
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

// ─── Auth ─────────────────────────────────────────────────────────────────────
@Composable
private fun AuthFlow(onSuccess: (String) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0A0C), DarkBg)))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        // Heart Logo
        Box(
            Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(GradientTinder)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Favorite, null, tint = TextWhite, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("فست لاو", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = TinderPink, fontFamily = Vazirmatn)
        Text("مثل تیندر، فقط سریع‌تر 🔥", fontSize = 15.sp, color = TextGray, fontFamily = Vazirmatn)
        Spacer(Modifier.height(48.dp))

        if (step == 0) {
            Text("شماره موبایل", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextWhite, fontFamily = Vazirmatn)
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
            Spacer(Modifier.height(28.dp))
            TinderButton("دریافت کد تأیید") {
                if (phone.length >= 10) { error = ""; step = 1 } else error = "شماره معتبر وارد کنید"
            }
            Spacer(Modifier.height(12.dp))
            Text("نسخه دمو — پیامک واقعی ارسال نمی‌شود", fontSize = 12.sp, color = TextMuted, fontFamily = Vazirmatn)
        } else {
            Text("کد تأیید", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextWhite, fontFamily = Vazirmatn)
            Text("کد تست: 123456", fontSize = 14.sp, color = TinderPink, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 8.dp))
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
            TinderButton("تأیید و ورود") {
                if (otp == "123456") { error = ""; onSuccess(phone) } else error = "کد اشتباه است (تست: ۱۲۳۴۵۶)"
            }
            TextButton(onClick = { step = 0; otp = ""; error = "" }) {
                Text("تغییر شماره", color = TextGray, fontFamily = Vazirmatn)
            }
        }
    }
}

// ─── Profile Setup ────────────────────────────────────────────────────────────
@Composable
private fun ProfileSetupScreen(phone: String, onComplete: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.FEMALE) }
    var orientation by remember { mutableStateOf(Orientation.STRAIGHT) }
    var job by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var selectedPhotos by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .background(DarkBg)
            .padding(24.dp)
    ) {
        Text("ساخت پروفایل", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        Text("اطلاعاتت رو کامل کن", color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn)
        Spacer(Modifier.height(24.dp))

        // Photos
        Text("عکس‌ها (حداکثر ۵)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(selectedPhotos.size) { i ->
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedPhotos[i], fontSize = 36.sp)
                    IconButton(
                        onClick = { selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(i) } },
                        modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, tint = TinderPink, modifier = Modifier.size(20.dp))
                    }
                }
            }
            if (selectedPhotos.size < 5) {
                item {
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkCard)
                            .border(1.dp, TinderPink.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
                onGallery = {
                    // Demo: add random emoji as "gallery photo"
                    if (selectedPhotos.size < 5) selectedPhotos = selectedPhotos + demoPhotoEmojis.random()
                    showPhotoSheet = false
                },
                onCamera = {
                    if (selectedPhotos.size < 5) selectedPhotos = selectedPhotos + "📸"
                    showPhotoSheet = false
                },
                onDismiss = { showPhotoSheet = false }
            )
        }

        Spacer(Modifier.height(20.dp))
        ProfileField("نام", name, { name = it }, Icons.Default.Person)
        ProfileField("سن", age, { if (it.length <= 2) age = it.filter { c -> c.isDigit() } }, Icons.Default.Cake, KeyboardType.Number)

        // Gender
        Text("جنسیت", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextWhite, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Gender.entries.forEach { g ->
                val label = when (g) {
                    Gender.MALE -> "مرد"
                    Gender.FEMALE -> "زن"
                    Gender.OTHER -> "دیگر"
                }
                val selected = gender == g
                FilterChip(
                    selected = selected,
                    onClick = { gender = g },
                    label = { Text(label, fontFamily = Vazirmatn, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TinderPink,
                        selectedLabelColor = TextWhite,
                        containerColor = DarkCard,
                        labelColor = TextGray
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        // Orientation
        Text("گرایش جنسی", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextWhite, fontFamily = Vazirmatn, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                Orientation.STRAIGHT to "دگرجنس‌گرا",
                Orientation.GAY to "همجنس‌گرا",
                Orientation.LESBIAN to "لزبین",
                Orientation.BISEXUAL to "دوجنس‌گرا",
                Orientation.OTHER to "دیگر"
            ).forEach { (o, label) ->
                val selected = orientation == o
                FilterChip(
                    selected = selected,
                    onClick = { orientation = o },
                    label = { Text(label, fontFamily = Vazirmatn, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TinderPink,
                        selectedLabelColor = TextWhite,
                        containerColor = DarkCard,
                        labelColor = TextGray
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        ProfileField("شغل", job, { job = it }, Icons.Default.Work)
        ProfileField("شهر", city, { city = it }, Icons.Default.LocationOn)

        Text("درباره من", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            placeholder = { Text("چند خط درباره خودت...", fontFamily = Vazirmatn) },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors()
        )

        Spacer(Modifier.height(18.dp))
        Text("علایق (حداقل ۳)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(10.dp))
        InterestFlow(selectedInterests) { interest ->
            selectedInterests = if (interest in selectedInterests) selectedInterests - interest else selectedInterests + interest
        }

        if (error.isNotEmpty()) Text(error, color = TinderPink, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 14.dp))

        Spacer(Modifier.height(24.dp))
        TinderButton("شروع کن 🚀") {
            when {
                name.isBlank() -> error = "نام را وارد کنید"
                age.toIntOrNull() == null || age.toInt() !in 18..80 -> error = "سن معتبر (۱۸–۸۰)"
                job.isBlank() -> error = "شغل را وارد کنید"
                selectedInterests.size < 3 -> error = "حداقل ۳ علاقه انتخاب کنید"
                selectedPhotos.isEmpty() -> error = "حداقل یک عکس اضافه کنید"
                else -> {
                    error = ""
                    onComplete(
                        UserProfile(
                            name = name.trim(),
                            age = age.toInt(),
                            gender = gender,
                            orientation = orientation,
                            job = job.trim(),
                            city = city.trim().ifBlank { "تهران" },
                            about = about.trim().ifBlank { "هنوز چیزی ننوشتم 😊" },
                            interests = selectedInterests.toList(),
                            photos = selectedPhotos,
                            phone = phone
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PhotoSourceSheet(onGallery: () -> Unit, onCamera: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("افزودن عکس", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("در نسخه دمو عکس واقعی ذخیره نمی‌شود. یکی را انتخاب کنید:", color = TextGray, fontFamily = Vazirmatn, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onGallery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = TinderPink)
                    Spacer(Modifier.width(10.dp))
                    Text("انتخاب از گالری", color = TextWhite, fontFamily = Vazirmatn)
                }
                TextButton(
                    onClick = onCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = TinderPink)
                    Spacer(Modifier.width(10.dp))
                    Text("گرفتن با دوربین", color = TextWhite, fontFamily = Vazirmatn)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = TextMuted, fontFamily = Vazirmatn) }
        }
    )
}

// ─── Filter Screen ────────────────────────────────────────────────────────────
@Composable
private fun FilterScreen(current: FilterState, onApply: (FilterState) -> Unit, onClose: () -> Unit) {
    var minAge by remember { mutableFloatStateOf(current.minAge.toFloat()) }
    var maxAge by remember { mutableFloatStateOf(current.maxAge.toFloat()) }
    var maxDist by remember { mutableFloatStateOf(current.maxDistance.toFloat()) }
    var selectedJobs by remember { mutableStateOf(current.jobs) }
    var selectedInterests by remember { mutableStateOf(current.interests) }
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scroll)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = TextWhite) }
            Text("فیلترها", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
        }
        Spacer(Modifier.height(24.dp))

        Text("محدوده سن: ${minAge.toInt()} – ${maxAge.toInt()}", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold)
        RangeSlider(
            value = minAge..maxAge,
            onValueChange = { minAge = it.start; maxAge = it.endInclusive },
            valueRange = 18f..60f,
            colors = SliderDefaults.colors(thumbColor = TinderPink, activeTrackColor = TinderPink)
        )

        Spacer(Modifier.height(20.dp))
        Text("حداکثر فاصله: ${maxDist.toInt()} کیلومتر", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold)
        Slider(
            value = maxDist,
            onValueChange = { maxDist = it },
            valueRange = 1f..100f,
            colors = SliderDefaults.colors(thumbColor = TinderPink, activeTrackColor = TinderPink)
        )

        Spacer(Modifier.height(20.dp))
        Text("شغل", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        FlowChips(availableJobs, selectedJobs) { j ->
            selectedJobs = if (j in selectedJobs) selectedJobs - j else selectedJobs + j
        }

        Spacer(Modifier.height(20.dp))
        Text("علایق", color = TextWhite, fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        FlowChips(availableInterests, selectedInterests) { i ->
            selectedInterests = if (i in selectedInterests) selectedInterests - i else selectedInterests + i
        }

        Spacer(Modifier.height(32.dp))
        TinderButton("اعمال فیلتر") {
            onApply(
                FilterState(
                    minAge = minAge.toInt(),
                    maxAge = maxAge.toInt(),
                    jobs = selectedJobs,
                    interests = selectedInterests,
                    maxDistance = maxDist.toInt()
                )
            )
        }
        TextButton(onClick = {
            onApply(FilterState())
        }, modifier = Modifier.fillMaxWidth()) {
            Text("پاک کردن فیلترها", color = TextGray, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun FlowChips(items: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column {
        items.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    val sel = item in selected
                    FilterChip(
                        selected = sel,
                        onClick = { onToggle(item) },
                        label = { Text(item, fontFamily = Vazirmatn, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TinderPink,
                            selectedLabelColor = TextWhite,
                            containerColor = DarkCard,
                            labelColor = TextGray
                        )
                    )
                }
            }
        }
    }
}

// ─── Discover (Tinder-style card) ─────────────────────────────────────────────
@Composable
private fun DiscoverScreen(
    person: Person?,
    onOpen: (Person) -> Unit,
    onLike: (Person) -> Unit,
    onPass: () -> Unit,
    onFilter: () -> Unit,
    onBlock: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Favorite, null, tint = TinderPink, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text("فست لاو", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TinderPink, fontFamily = Vazirmatn)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onFilter) {
                Icon(Icons.Default.Tune, "فیلتر", tint = TextWhite)
            }
        }

        if (person == null) {
            EmptyState("دیگه کسی نیست!", "فیلترها رو تغییر بده یا بعداً بیا", "🎉")
        } else {
            // Full card like Tinder
            Card(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable { onOpen(person) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Photo
                    AsyncImage(
                        model = person.photoUrl,
                        contentDescription = person.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_menu_report_image)
                    )
                    // Gradient overlay at bottom
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )
                    // Info
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        if (person.likedYou) {
                            Surface(shape = RoundedCornerShape(20.dp), color = TinderPink) {
                                Text(
                                    "❤️ تو رو لایک کرده",
                                    Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Vazirmatn
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            "${person.name}, ${person.age}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontFamily = Vazirmatn
                        )
                        Text(
                            "${person.job} · ${person.distanceKm} کیلومتر",
                            color = TextGray,
                            fontSize = 14.sp,
                            fontFamily = Vazirmatn
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            person.about,
                            color = TextWhite.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = Vazirmatn
                        )
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

            // Action buttons (Tinder style)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pass
                FilledIconButton(
                    onClick = onPass,
                    modifier = Modifier.size(60.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkCard, contentColor = TextGray)
                ) {
                    Icon(Icons.Default.Close, "رد", modifier = Modifier.size(30.dp))
                }
                // Block
                FilledIconButton(
                    onClick = { onBlock(person) },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkCard, contentColor = Color(0xFFFF6B6B))
                ) {
                    Icon(Icons.Default.Block, "بلاک", modifier = Modifier.size(22.dp))
                }
                // Like
                FilledIconButton(
                    onClick = { onLike(person) },
                    modifier = Modifier.size(68.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink, contentColor = TextWhite)
                ) {
                    Icon(Icons.Default.Favorite, "لایک", modifier = Modifier.size(34.dp))
                }
            }
        }
    }
}

// ─── Liked by me ──────────────────────────────────────────────────────────────
@Composable
private fun LikedByMeScreen(liked: List<Person>, onOpen: (Person) -> Unit, onChat: (Person) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("لایک‌های من")
        if (liked.isEmpty()) EmptyState("هنوز کسی رو لایک نکردی", "برو کشف و قلب بزن!", "♡")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(liked, key = { it.id }) { p ->
                PersonRow(p, onClick = { onOpen(p) }) {
                    if (p.likedYou) TextButton(onClick = { onChat(p) }) {
                        Text("چت", color = TinderPink, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                    } else Text("در انتظار", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                }
            }
        }
    }
}

// ─── Liked me ─────────────────────────────────────────────────────────────────
@Composable
private fun LikedMeScreen(
    people: List<Person>,
    matched: List<Person>,
    onLikeBack: (Person) -> Unit,
    onOpen: (Person) -> Unit,
    onBlock: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("کسانی که تو رو لایک کردن")
        if (people.isEmpty()) EmptyState("هنوز کسی لایکت نکرده", "پروفایلت رو جذاب‌تر کن!", "💫")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(people, key = { it.id }) { p ->
                val isMatched = p in matched
                PersonRow(p, onClick = { onOpen(p) }) {
                    Row {
                        if (isMatched) {
                            Surface(shape = RoundedCornerShape(12.dp), color = TinderPink.copy(alpha = 0.2f)) {
                                Text("مچ 💕", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TinderPink, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                            }
                        } else {
                            Button(
                                onClick = { onLikeBack(p) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
                            ) { Text("لایک", fontSize = 12.sp, fontFamily = Vazirmatn) }
                        }
                        IconButton(onClick = { onBlock(p) }) {
                            Icon(Icons.Default.Block, "بلاک", tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Matches ──────────────────────────────────────────────────────────────────
@Composable
private fun MatchesChatList(matches: List<Person>, onChat: (Person) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("مچ‌ها و چت")
        if (matches.isEmpty()) EmptyState("هنوز مچی نداری", "با لایک متقابل مچ می‌شی و می‌تونی چت کنی.", "💬")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(matches, key = { it.id }) { p ->
                PersonRow(p, onClick = { onChat(p) }) {
                    Text("سلام! 👋", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
                }
            }
        }
    }
}

// ─── Chat ─────────────────────────────────────────────────────────────────────
@Composable
private fun ChatScreen(person: Person, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("سلام! خوشحالم باهات آشنا شدم 😊", false, "۱۰:۳۰"),
            ChatMessage("سلام، منم خوشحالم 👋", true, "۱۰:۳۲")
        )
    }

    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            Modifier.fillMaxWidth().background(DarkSurface).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
            AsyncImage(
                model = person.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(44.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(person.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextWhite, fontFamily = Vazirmatn)
                Text("آنلاین", color = SuccessGreen, fontSize = 12.sp, fontFamily = Vazirmatn)
            }
        }
        HorizontalDivider(color = DarkCard)

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = if (msg.isMe) 18.dp else 4.dp,
                            bottomEnd = if (msg.isMe) 4.dp else 18.dp
                        ),
                        color = if (msg.isMe) TinderPink else DarkCard
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(msg.text, color = TextWhite, fontSize = 15.sp, fontFamily = Vazirmatn)
                            Text(msg.time, color = TextWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = Vazirmatn, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
        Text("گفت‌وگوی نمایشی — پیام‌ها ذخیره نمی‌شوند", color = TextMuted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontFamily = Vazirmatn)

        Row(Modifier.fillMaxWidth().background(DarkSurface).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("پیامت را بنویس...", fontFamily = Vazirmatn) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = fieldColors()
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (message.isNotBlank()) {
                        messages.add(ChatMessage(message.trim(), true, "الان"))
                        message = ""
                    }
                },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = TinderPink)
            ) {
                Icon(Icons.Default.Send, null, tint = TextWhite)
            }
        }
    }
}

// ─── Detail ───────────────────────────────────────────────────────────────────
@Composable
private fun PersonDetailScreen(person: Person, onBack: () -> Unit, onLike: () -> Unit, onBlock: () -> Unit) {
    Column(Modifier.fillMaxSize().background(DarkBg)) {
        Box {
            AsyncImage(
                model = person.photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(360.dp),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(12.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = TextWhite)
            }
        }
        Column(Modifier.padding(20.dp).weight(1f)) {
            Text("${person.name}, ${person.age}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Text("${person.job} · ${person.city} · ${person.distanceKm} کیلومتر", color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn)
            val genderLabel = when (person.gender) {
                Gender.MALE -> "مرد"
                Gender.FEMALE -> "زن"
                Gender.OTHER -> "دیگر"
            }
            Text("جنسیت: $genderLabel", color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn, modifier = Modifier.padding(top = 4.dp))
            if (person.likedYou) {
                Surface(shape = RoundedCornerShape(12.dp), color = TinderPink.copy(alpha = 0.2f), modifier = Modifier.padding(top = 10.dp)) {
                    Text("❤️ این فرد تو را لایک کرده", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TinderPink, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Vazirmatn)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("درباره من", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite, fontFamily = Vazirmatn)
            Text(person.about, Modifier.padding(top = 6.dp), lineHeight = 22.sp, color = TextGray, fontFamily = Vazirmatn)
            Spacer(Modifier.height(14.dp))
            Text("علایق", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite, fontFamily = Vazirmatn)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                person.interests.forEach { tag ->
                    Surface(shape = RoundedCornerShape(14.dp), color = DarkCard) {
                        Text(tag, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TinderPink, fontSize = 13.sp, fontFamily = Vazirmatn)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBlock,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                    border = BorderStroke(1.dp, Color(0xFFFF6B6B))
                ) {
                    Icon(Icons.Default.Block, null)
                    Spacer(Modifier.width(6.dp))
                    Text("بلاک", fontFamily = Vazirmatn)
                }
                Button(
                    onClick = onLike,
                    modifier = Modifier.weight(1.5f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TinderPink)
                ) {
                    Icon(Icons.Default.Favorite, null)
                    Spacer(Modifier.width(6.dp))
                    Text("لایک", fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── My Profile ───────────────────────────────────────────────────────────────
@Composable
private fun MyProfileScreen(user: UserProfile, blockedCount: Int, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(DarkBg)) {
        AppHeader("پروفایل من")
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (user.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(user.photos) { emoji ->
                        Box(Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(DarkCard), contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 36.sp)
                        }
                    }
                }
            } else {
                Box(Modifier.size(100.dp).clip(CircleShape).background(DarkCard), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = TinderPink, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite, fontFamily = Vazirmatn)
            Text("${user.age} ساله · ${user.job}", color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn)
            val gLabel = when (user.gender) {
                Gender.MALE -> "مرد"
                Gender.FEMALE -> "زن"
                Gender.OTHER -> "دیگر"
            }
            val oLabel = when (user.orientation) {
                Orientation.STRAIGHT -> "دگرجنس‌گرا"
                Orientation.GAY -> "همجنس‌گرا"
                Orientation.LESBIAN -> "لزبین"
                Orientation.BISEXUAL -> "دوجنس‌گرا"
                Orientation.OTHER -> "دیگر"
            }
            Text("$gLabel · $oLabel · ${user.city}", color = TextMuted, fontSize = 13.sp, fontFamily = Vazirmatn)
            Spacer(Modifier.height(8.dp))
            Text(user.about, textAlign = TextAlign.Center, color = TextGray, fontSize = 14.sp, fontFamily = Vazirmatn)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                user.interests.forEach { tag ->
                    Surface(shape = RoundedCornerShape(14.dp), color = DarkCard) {
                        Text(tag, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TinderPink, fontSize = 12.sp, fontFamily = Vazirmatn)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            InfoRow("شماره موبایل", user.phone)
            InfoRow("افراد بلاک‌شده", "$blockedCount نفر")
            InfoRow("نسخه", "فست لاو ۲.۰")
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TinderPink),
                border = BorderStroke(1.5.dp, TinderPink)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("خروج از حساب", fontFamily = Vazirmatn)
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), color = TextMuted, fontSize = 14.sp, fontFamily = Vazirmatn)
            Text(value, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFamily = Vazirmatn)
        }
    }
}

// ─── Shared UI ────────────────────────────────────────────────────────────────
@Composable
private fun AppHeader(subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Favorite, null, tint = TinderPink, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text("فست لاو", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TinderPink, fontFamily = Vazirmatn)
            Text(subtitle, fontSize = 12.sp, color = TextMuted, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun PersonRow(person: Person, onClick: () -> Unit, action: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = person.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${person.name}, ${person.age}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite, fontFamily = Vazirmatn)
                Text("${person.job} · ${person.distanceKm} کیلومتر", color = TextMuted, fontSize = 12.sp, fontFamily = Vazirmatn)
            }
            action()
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String, emoji: String) {
    Column(Modifier.fillMaxSize().padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(18.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite, textAlign = TextAlign.Center, fontFamily = Vazirmatn)
        Text(description, Modifier.padding(top = 10.dp), color = TextMuted, textAlign = TextAlign.Center, lineHeight = 22.sp, fontSize = 14.sp, fontFamily = Vazirmatn)
    }
}

@Composable
private fun BottomNav(tab: Int, onTab: (Int) -> Unit, matchCount: Int) {
    NavigationBar(containerColor = DarkSurface, tonalElevation = 0.dp) {
        NavItem(0, tab, onTab, Icons.Default.Home, Icons.Outlined.Home, "کشف")
        NavItem(1, tab, onTab, Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, "لایک‌ها")
        NavItem(2, tab, onTab, Icons.Default.FavoriteBorder, Icons.Outlined.FavoriteBorder, "لایک‌شده")
        NavigationBarItem(
            selected = tab == 3,
            onClick = { onTab(3) },
            icon = {
                BadgedBox(badge = { if (matchCount > 0) Badge(containerColor = TinderPink) { Text("$matchCount") } }) {
                    Icon(if (tab == 3) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline, null)
                }
            },
            label = { Text("چت", fontSize = 11.sp, fontFamily = Vazirmatn) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = TinderPink, selectedTextColor = TinderPink, indicatorColor = TinderPink.copy(alpha = 0.15f))
        )
        NavItem(4, tab, onTab, Icons.Default.Person, Icons.Outlined.Person, "من")
    }
}

@Composable
private fun RowScope.NavItem(
    index: Int, tab: Int, onTab: (Int) -> Unit,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    NavigationBarItem(
        selected = tab == index,
        onClick = { onTab(index) },
        icon = { Icon(if (tab == index) selectedIcon else unselectedIcon, null) },
        label = { Text(label, fontSize = 11.sp, fontFamily = Vazirmatn) },
        colors = NavigationBarItemDefaults.colors(selectedIconColor = TinderPink, selectedTextColor = TinderPink, indicatorColor = TinderPink.copy(alpha = 0.15f))
    )
}

@Composable
private fun TinderButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            Modifier.fillMaxSize().background(Brush.horizontalGradient(GradientTinder), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Vazirmatn)
        }
    }
}

@Composable
private fun ProfileField(
    label: String, value: String, onChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboard: KeyboardType = KeyboardType.Text
) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextWhite, fontFamily = Vazirmatn)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            leadingIcon = { Icon(icon, null, tint = TinderPink, modifier = Modifier.size(20.dp)) },
            colors = fieldColors()
        )
    }
}

@Composable
private fun InterestFlow(selected: Set<String>, onToggle: (String) -> Unit) {
    Column {
        availableInterests.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { interest ->
                    val isSelected = interest in selected
                    Surface(
                        onClick = { onToggle(interest) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) TinderPink else DarkCard
                    ) {
                        Text(
                            interest,
                            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = if (isSelected) TextWhite else TextGray,
                            fontSize = 12.sp,
                            fontFamily = Vazirmatn
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TinderPink,
    unfocusedBorderColor = DarkCard,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    cursorColor = TinderPink,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)
