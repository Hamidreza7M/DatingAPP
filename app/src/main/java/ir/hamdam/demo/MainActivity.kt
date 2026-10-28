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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Modern Youth Theme ───────────────────────────────────────────────────────
private val Primary = Color(0xFFFF2D55)          // vibrant pink-red
private val PrimaryDark = Color(0xFFE91E63)
private val Secondary = Color(0xFF7C4DFF)        // soft purple
private val Accent = Color(0xFFFF6B9D)
private val Background = Color(0xFFFFF5F7)
private val Surface = Color(0xFFFFFFFF)
private val Ink = Color(0xFF1A1A2E)
private val Muted = Color(0xFF8E8E9A)
private val SoftPink = Color(0xFFFFE4EC)
private val SoftPurple = Color(0xFFF0E6FF)
private val Success = Color(0xFF00C853)
private val GradientStart = Color(0xFFFF2D55)
private val GradientEnd = Color(0xFFFF6B9D)

// ─── Data Models ──────────────────────────────────────────────────────────────
data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val job: String = "",
    val city: String = "",
    val about: String = "",
    val interests: List<String> = emptyList(),
    val photos: List<String> = emptyList(),   // emoji placeholders for demo
    val phone: String = ""
)

data class Person(
    val id: Int,
    val name: String,
    val age: Int,
    val job: String,
    val city: String,
    val about: String,
    val interests: List<String>,
    val emoji: String,
    val tint: Color,
    val likedYou: Boolean = false
)

data class ChatMessage(val text: String, val isMe: Boolean, val time: String)

// ─── Sample Data ──────────────────────────────────────────────────────────────
private val samplePeople = listOf(
    Person(1, "رها", 26, "طراح گرافیک", "تهران",
        "اهل کافه‌گردی، کتاب و گفت‌وگوهای طولانی. دنبال یک آشنایی واقعی و محترمانه‌ام.",
        listOf("کتاب", "کافه", "سفر", "طراحی"), "🌷", Color(0xFFFFE0E6), likedYou = true),
    Person(2, "سارا", 28, "مهندس نرم‌افزار", "کرج",
        "طبیعت‌گردی و موسیقی حال من را خوب می‌کند. صداقت برایم از هر چیزی مهم‌تر است.",
        listOf("طبیعت", "موسیقی", "عکاسی", "کدنویسی"), "🌿", Color(0xFFE0F2E9), likedYou = false),
    Person(3, "نیلوفر", 25, "دانشجوی معماری", "تهران",
        "کنجکاوم دنیا را بیشتر بشناسم؛ از نمایشگاه هنری تا یک پیاده‌روی ساده.",
        listOf("هنر", "پیاده‌روی", "فیلم", "معماری"), "🌼", Color(0xFFFFF0D6), likedYou = true),
    Person(4, "مهسا", 29, "معلم زبان", "اصفهان",
        "آدم آرامی هستم و رابطه‌ای می‌خواهم که در آن هر دو رشد کنیم.",
        listOf("سفر", "آشپزی", "فیلم", "یوگا"), "🪻", Color(0xFFEDE0FF), likedYou = false),
    Person(5, "آتنا", 24, "محتواساز", "شیراز",
        "عاشق ساخت ویدیو و داستان‌گویی. دنبال کسی هستم که باهاش بخندم.",
        listOf("ویدیو", "خنده", "سفر", "کافه"), "✨", Color(0xFFFFE8D6), likedYou = true),
    Person(6, "پریا", 27, "روانشناس", "مشهد",
        "گوش دادن خوب بلدم و عاشق گفت‌وگوهای عمیقم.",
        listOf("روانشناسی", "کتاب", "پیاده‌روی", "موسیقی"), "🌙", Color(0xFFE0E8FF), likedYou = false)
)

private val availableInterests = listOf(
    "کتاب", "کافه", "سفر", "موسیقی", "فیلم", "ورزش", "عکاسی", "آشپزی",
    "هنر", "طبیعت", "یوگا", "کدنویسی", "رقص", "بازی", "مد", "حیوانات"
)

private val photoEmojis = listOf("😊", "😎", "🥳", "😍", "🥰", "😇", "🤩", "😋")

// ─── App Entry ────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FastLoveApp() }
    }
}

@Composable
private fun FastLoveApp() {
    // Auth & Profile state
    var isLoggedIn by remember { mutableStateOf(false) }
    var hasProfile by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(UserProfile()) }

    // Main app state
    var tab by remember { mutableIntStateOf(0) }
    var discoverIndex by remember { mutableIntStateOf(0) }
    val likedPeople = remember { mutableStateListOf<Person>() }
    val matchedPeople = remember { mutableStateListOf<Person>() }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var chatPerson by remember { mutableStateOf<Person?>(null) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            secondary = Secondary,
            background = Background,
            surface = Surface,
            onPrimary = Color.White,
            onBackground = Ink,
            onSurface = Ink
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(Modifier.fillMaxSize(), color = Background) {
                when {
                    !isLoggedIn -> AuthFlow(
                        onSuccess = { phone ->
                            currentUser = currentUser.copy(phone = phone)
                            isLoggedIn = true
                        }
                    )
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
                        }
                    )
                    chatPerson != null -> ChatScreen(
                        person = chatPerson!!,
                        onBack = { chatPerson = null }
                    )
                    else -> {
                        Column(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(1f)) {
                                when (tab) {
                                    0 -> DiscoverScreen(
                                        person = samplePeople.getOrNull(discoverIndex),
                                        onOpen = { selectedPerson = it },
                                        onLike = { p ->
                                            if (p !in likedPeople) likedPeople.add(p)
                                            if (p.likedYou && p !in matchedPeople) matchedPeople.add(p)
                                            discoverIndex = (discoverIndex + 1).coerceAtMost(samplePeople.size)
                                        },
                                        onPass = {
                                            discoverIndex = (discoverIndex + 1).coerceAtMost(samplePeople.size)
                                        }
                                    )
                                    1 -> LikedByMeScreen(
                                        liked = likedPeople,
                                        onOpen = { selectedPerson = it },
                                        onChat = { if (it in matchedPeople) chatPerson = it }
                                    )
                                    2 -> LikedMeScreen(
                                        people = samplePeople.filter { it.likedYou },
                                        matched = matchedPeople,
                                        onLikeBack = { p ->
                                            if (p !in likedPeople) likedPeople.add(p)
                                            if (p !in matchedPeople) matchedPeople.add(p)
                                        },
                                        onOpen = { selectedPerson = it }
                                    )
                                    3 -> MatchesChatList(
                                        matches = matchedPeople,
                                        onChat = { chatPerson = it }
                                    )
                                    4 -> MyProfileScreen(
                                        user = currentUser,
                                        onLogout = {
                                            isLoggedIn = false
                                            hasProfile = false
                                            currentUser = UserProfile()
                                            likedPeople.clear()
                                            matchedPeople.clear()
                                            discoverIndex = 0
                                            tab = 0
                                        }
                                    )
                                }
                            }
                            BottomNav(tab = tab, onTab = { tab = it }, matchCount = matchedPeople.size)
                        }
                    }
                }
            }
        }
    }
}

// ─── 1. Auth Flow (Phone + OTP) ───────────────────────────────────────────────
@Composable
private fun AuthFlow(onSuccess: (String) -> Unit) {
    var step by remember { mutableIntStateOf(0) } // 0=phone, 1=otp
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFFF0F3), Background))
            )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        // Logo
        Box(
            Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
            contentAlignment = Alignment.Center
        ) {
            Text("💕", fontSize = 48.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "فست لاو",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Primary
        )
        Text(
            "عشق سریع، ارتباط واقعی",
            fontSize = 15.sp,
            color = Muted,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.height(48.dp))

        if (step == 0) {
            Text("شماره موبایل خود را وارد کنید", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 11) phone = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SoftPink,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface
                ),
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary) }
            )
            if (error.isNotEmpty()) {
                Text(error, color = Primary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(28.dp))
            GradientButton(
                text = "دریافت کد تأیید",
                onClick = {
                    if (phone.length >= 10) {
                        error = ""
                        step = 1
                    } else {
                        error = "شماره موبایل معتبر وارد کنید"
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "نسخه نمایشی — نیازی به پیامک واقعی نیست",
                fontSize = 12.sp,
                color = Muted,
                textAlign = TextAlign.Center
            )
        } else {
            Text("کد تأیید را وارد کنید", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
            Text(
                "کد تست: 123456",
                fontSize = 14.sp,
                color = Primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("۱۲۳۴۵۶") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SoftPink,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface
                ),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) }
            )
            if (error.isNotEmpty()) {
                Text(error, color = Primary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(28.dp))
            GradientButton(
                text = "تأیید و ورود",
                onClick = {
                    if (otp == "123456") {
                        error = ""
                        onSuccess(phone)
                    } else {
                        error = "کد تأیید اشتباه است (تست: ۱۲۳۴۵۶)"
                    }
                }
            )
            TextButton(onClick = { step = 0; otp = ""; error = "" }) {
                Text("تغییر شماره", color = Muted)
            }
        }
    }
}

// ─── 2. Profile Setup ─────────────────────────────────────────────────────────
@Composable
private fun ProfileSetupScreen(phone: String, onComplete: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var selectedPhotos by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp)
    ) {
        Text("ساخت پروفایل", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
        Text("پروفایلت رو کامل کن تا بقیه بتونن باهات آشنا بشن", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))

        // Photos (max 5)
        Text("عکس‌ها (حداکثر ۵)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(selectedPhotos.size) { i ->
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftPink),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedPhotos[i], fontSize = 32.sp)
                    IconButton(
                        onClick = { selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(i) } },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (selectedPhotos.size < 5) {
                item {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftPink)
                            .clickable {
                                val next = photoEmojis.random()
                                selectedPhotos = selectedPhotos + next
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
        Text("برای دمو روی + بزن تا ایموجی اضافه بشه", fontSize = 11.sp, color = Muted, modifier = Modifier.padding(top = 6.dp))

        Spacer(Modifier.height(24.dp))
        ProfileField("نام", name, { name = it }, Icons.Default.Person)
        ProfileField("سن", age, { if (it.length <= 2) age = it.filter { c -> c.isDigit() } }, Icons.Default.Cake, KeyboardType.Number)
        ProfileField("شغل", job, { job = it }, Icons.Default.Work)
        ProfileField("شهر", city, { city = it }, Icons.Default.LocationOn)
        Spacer(Modifier.height(8.dp))
        Text("درباره من", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            placeholder = { Text("چند خط درباره خودت بنویس...") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SoftPink,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            )
        )

        Spacer(Modifier.height(20.dp))
        Text("علایق (حداقل ۳ تا انتخاب کن)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        FlowRow(interests = availableInterests, selected = selectedInterests) { interest ->
            selectedInterests = if (interest in selectedInterests)
                selectedInterests - interest
            else
                selectedInterests + interest
        }

        if (error.isNotEmpty()) {
            Text(error, color = Primary, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(Modifier.height(28.dp))
        GradientButton(
            text = "شروع کن 🚀",
            onClick = {
                when {
                    name.isBlank() -> error = "نام را وارد کنید"
                    age.toIntOrNull() == null || age.toInt() !in 18..80 -> error = "سن معتبر وارد کنید (۱۸ تا ۸۰)"
                    job.isBlank() -> error = "شغل را وارد کنید"
                    selectedInterests.size < 3 -> error = "حداقل ۳ علاقه انتخاب کنید"
                    selectedPhotos.isEmpty() -> error = "حداقل یک عکس اضافه کنید"
                    else -> {
                        error = ""
                        onComplete(
                            UserProfile(
                                name = name.trim(),
                                age = age.toInt(),
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
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboard: KeyboardType = KeyboardType.Text
) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            leadingIcon = { Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SoftPink,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            )
        )
    }
}

@Composable
private fun FlowRow(interests: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column {
        interests.chunked(4).forEach { row ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { interest ->
                    val isSelected = interest in selected
                    Surface(
                        onClick = { onToggle(interest) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Primary else SoftPink,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            interest,
                            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Primary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ─── Bottom Navigation ────────────────────────────────────────────────────────
@Composable
private fun BottomNav(tab: Int, onTab: (Int) -> Unit, matchCount: Int) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 12.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        NavItem(0, tab, onTab, Icons.Default.Home, Icons.Outlined.Home, "کشف")
        NavItem(1, tab, onTab, Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, "لایک‌ها")
        NavItem(2, tab, onTab, Icons.Default.FavoriteBorder, Icons.Outlined.FavoriteBorder, "لایک‌شده")
        NavigationBarItem(
            selected = tab == 3,
            onClick = { onTab(3) },
            icon = {
                BadgedBox(badge = {
                    if (matchCount > 0) Badge(containerColor = Primary) { Text("$matchCount") }
                }) {
                    Icon(
                        if (tab == 3) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        null
                    )
                }
            },
            label = { Text("چت", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = SoftPink
            )
        )
        NavItem(4, tab, onTab, Icons.Default.Person, Icons.Outlined.Person, "من")
    }
}

@Composable
private fun RowScope.NavItem(
    index: Int,
    tab: Int,
    onTab: (Int) -> Unit,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    NavigationBarItem(
        selected = tab == index,
        onClick = { onTab(index) },
        icon = { Icon(if (tab == index) selectedIcon else unselectedIcon, null) },
        label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Primary,
            selectedTextColor = Primary,
            indicatorColor = SoftPink
        )
    )
}

// ─── Discover Screen ──────────────────────────────────────────────────────────
@Composable
private fun DiscoverScreen(
    person: Person?,
    onOpen: (Person) -> Unit,
    onLike: (Person) -> Unit,
    onPass: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("کشف افراد جدید")
        if (person == null) {
            EmptyState("دیگه کسی نیست!", "بعداً دوباره سر بزن", "🎉")
        } else {
            Card(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clickable { onOpen(person) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Brush.verticalGradient(listOf(person.tint, Color(0xFFFFF5F7)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(person.emoji, fontSize = 120.sp)
                        if (person.likedYou) {
                            Surface(
                                Modifier.align(Alignment.TopStart).padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Primary
                            ) {
                                Text(
                                    "❤️ تو رو لایک کرده",
                                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            "لمس کن برای جزئیات",
                            Modifier.align(Alignment.BottomCenter).padding(12.dp),
                            color = Ink.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${person.name}، ${person.age}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = SoftPurple) {
                                Text(person.job, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, color = Secondary)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.LocationOn, null, tint = Muted, modifier = Modifier.size(16.dp))
                            Text(person.city, color = Muted, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(person.about, color = Ink, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            person.interests.take(3).forEach { tag ->
                                Surface(shape = RoundedCornerShape(16.dp), color = SoftPink) {
                                    Text(tag, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Primary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onPass,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Surface, contentColor = Muted)
                ) {
                    Icon(Icons.Default.Close, "رد", modifier = Modifier.size(32.dp))
                }
                FilledIconButton(
                    onClick = { onLike(person) },
                    modifier = Modifier.size(76.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Favorite, "لایک", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

// ─── 3. History of people I liked ─────────────────────────────────────────────
@Composable
private fun LikedByMeScreen(
    liked: List<Person>,
    onOpen: (Person) -> Unit,
    onChat: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("افرادی که لایک کردی")
        if (liked.isEmpty()) {
            EmptyState("هنوز کسی رو لایک نکردی", "برو بخش کشف و قلب بزن!", "♡")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(liked, key = { it.id }) { p ->
                    PersonRow(p, onClick = { onOpen(p) }) {
                        if (p.likedYou) {
                            TextButton(onClick = { onChat(p) }) {
                                Text("چت", color = Primary, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("در انتظار", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── 4. People who liked me ───────────────────────────────────────────────────
@Composable
private fun LikedMeScreen(
    people: List<Person>,
    matched: List<Person>,
    onLikeBack: (Person) -> Unit,
    onOpen: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("کسانی که تو رو لایک کردن")
        if (people.isEmpty()) {
            EmptyState("هنوز کسی لایکت نکرده", "پروفایلت رو جذاب‌تر کن!", "💫")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(people, key = { it.id }) { p ->
                    val isMatched = p in matched
                    PersonRow(p, onClick = { onOpen(p) }) {
                        if (isMatched) {
                            Surface(shape = RoundedCornerShape(12.dp), color = SoftPink) {
                                Text("مچ شدید 💕", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onLikeBack(p) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("لایک کن", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── 5. Matches & Chat List ───────────────────────────────────────────────────
@Composable
private fun MatchesChatList(matches: List<Person>, onChat: (Person) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("مچ‌ها و گفت‌وگوها")
        if (matches.isEmpty()) {
            EmptyState(
                "هنوز مچی نداری",
                "وقتی کسی رو لایک کنی که تو رو هم لایک کرده باشه، اینجا ظاهر می‌شه و می‌تونی چت کنی.",
                "💬"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(matches, key = { it.id }) { p ->
                    PersonRow(p, onClick = { onChat(p) }) {
                        Text("سلام! 👋", color = Muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ─── Chat Screen ──────────────────────────────────────────────────────────────
@Composable
private fun ChatScreen(person: Person, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("سلام! خوشحالم باهات آشنا شدم 😊", false, "۱۰:۳۰"),
            ChatMessage("سلام، منم خوشحالم 👋", true, "۱۰:۳۲")
        )
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "بازگشت")
            }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(person.tint),
                contentAlignment = Alignment.Center
            ) {
                Text(person.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(person.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("آنلاین", color = Success, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = SoftPink)

        // Messages
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (msg.isMe) 18.dp else 4.dp,
                            bottomEnd = if (msg.isMe) 4.dp else 18.dp
                        ),
                        color = if (msg.isMe) Primary else Surface,
                        shadowElevation = 1.dp
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(msg.text, color = if (msg.isMe) Color.White else Ink, fontSize = 15.sp)
                            Text(
                                msg.time,
                                color = if (msg.isMe) Color.White.copy(alpha = 0.7f) else Muted,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            "این گفت‌وگو نمایشی است و پیام‌ها ذخیره نمی‌شوند",
            color = Muted,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        // Input
        Row(
            Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("پیامت را بنویس...") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SoftPink,
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background
                )
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
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Send, "ارسال", tint = Color.White)
            }
        }
    }
}

// ─── Person Detail ────────────────────────────────────────────────────────────
@Composable
private fun PersonDetailScreen(person: Person, onBack: () -> Unit, onLike: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت") }
            Text("پروفایل", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(person.tint),
            contentAlignment = Alignment.Center
        ) {
            Text(person.emoji, fontSize = 110.sp)
        }
        Column(Modifier.padding(24.dp).weight(1f)) {
            Text("${person.name}، ${person.age}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("${person.job} · ${person.city}", color = Muted, fontSize = 14.sp)
            if (person.likedYou) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftPink,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        "❤️ این فرد تو را لایک کرده",
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("درباره من", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(person.about, Modifier.padding(top = 8.dp), lineHeight = 24.sp, color = Ink)
            Spacer(Modifier.height(18.dp))
            Text("علایق", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Row(
                Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                person.interests.forEach { tag ->
                    Surface(shape = RoundedCornerShape(16.dp), color = SoftPink) {
                        Text(tag, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Primary, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            GradientButton(text = "لایک کردن ❤️", onClick = onLike)
        }
    }
}

// ─── My Profile ───────────────────────────────────────────────────────────────
@Composable
private fun MyProfileScreen(user: UserProfile, onLogout: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppHeader("پروفایل من")
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photos
            if (user.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(user.photos) { emoji ->
                        Box(
                            Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SoftPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 36.sp)
                        }
                    }
                }
            } else {
                Box(
                    Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SoftPink),
                    contentAlignment = Alignment.Center
                ) {
                    Text("😊", fontSize = 48.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("${user.age} ساله · ${user.job}", color = Muted, fontSize = 14.sp)
            Text("📍 ${user.city}", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(user.about, textAlign = TextAlign.Center, color = Ink, fontSize = 14.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                user.interests.forEach { tag ->
                    Surface(shape = RoundedCornerShape(16.dp), color = SoftPink) {
                        Text(tag, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Primary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            InfoCard("شماره موبایل", user.phone)
            InfoCard("نسخه اپ", "فست لاو ۱.۰ — دمو")
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                border = BorderStroke(1.5.dp, Primary)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("خروج از حساب")
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "نسخه نمایشی — داده‌ها فقط روی دستگاه ذخیره می‌شوند",
                color = Muted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, Modifier.weight(1f), color = Muted, fontSize = 14.sp)
            Text(value, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// ─── Shared Components ────────────────────────────────────────────────────────
@Composable
private fun AppHeader(subtitle: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "فست لاو",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Primary
            )
            Text(subtitle, fontSize = 13.sp, color = Muted)
        }
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
            contentAlignment = Alignment.Center
        ) {
            Text("💕", fontSize = 22.sp)
        }
    }
}

@Composable
private fun PersonRow(person: Person, onClick: () -> Unit, action: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(person.tint),
                contentAlignment = Alignment.Center
            ) {
                Text(person.emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${person.name}، ${person.age}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                Text("${person.job} · ${person.city}", color = Muted, fontSize = 12.sp)
            }
            action()
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String, emoji: String) {
    Column(
        Modifier.fillMaxSize().padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
        Text(
            description,
            Modifier.padding(top = 10.dp),
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun GradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
