package ir.hamdam.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Rose = Color(0xFFD94D72)
private val Ink = Color(0xFF29242A)
private val Muted = Color(0xFF817780)
private val Canvas = Color(0xFFFFF8F6)
private val PaleRose = Color(0xFFFFE5EB)

private data class Profile(val name: String, val age: Int, val city: String, val about: String, val interests: List<String>, val emoji: String, val tint: Color)
private val profiles = listOf(
    Profile("رها", 26, "تهران", "اهل کافه‌گردی، کتاب و گفت‌وگوهای طولانی. دنبال یک آشنایی واقعی و محترمانه‌ام.", listOf("کتاب", "کافه", "سفر"), "🌷", Color(0xFFF7DCE3)),
    Profile("سارا", 28, "کرج", "طبیعت‌گردی و موسیقی حال من را خوب می‌کند. صداقت برایم از هر چیزی مهم‌تر است.", listOf("طبیعت", "موسیقی", "عکاسی"), "🌿", Color(0xFFDDEBE0)),
    Profile("نیلوفر", 25, "تهران", "کنجکاوم دنیا را بیشتر بشناسم؛ از نمایشگاه هنری تا یک پیاده‌روی ساده.", listOf("هنر", "پیاده‌روی", "فیلم"), "🌼", Color(0xFFF4E8C9)),
    Profile("مهسا", 29, "اصفهان", "آدم آرامی هستم و رابطه‌ای می‌خواهم که در آن هر دو رشد کنیم.", listOf("سفر", "آشپزی", "فیلم"), "🪻", Color(0xFFE8DFF5))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HamdamApp() }
    }
}

@Composable
private fun HamdamApp() {
    var tab by remember { mutableStateOf(0) }
    var index by remember { mutableIntStateOf(0) }
    val liked = remember { mutableStateListOf<Profile>() }
    val passed = remember { mutableStateListOf<Profile>() }
    var selected by remember { mutableStateOf<Profile?>(null) }
    var chatProfile by remember { mutableStateOf<Profile?>(null) }
    MaterialTheme(colorScheme = lightColorScheme(primary = Rose, background = Canvas, surface = Color.White)) {
        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
                Column(Modifier.fillMaxSize()) {
                    if (selected != null) {
                        ProfileDetail(selected!!, onBack = { selected = null }, onLike = {
                            if (selected !in liked) liked.add(selected!!)
                            selected = null
                        })
                    } else if (chatProfile != null) {
                        ChatScreen(chatProfile!!, onBack = { chatProfile = null })
                    } else {
                        when (tab) {
                            0 -> DiscoverScreen(profile = profiles.getOrNull(index), onOpen = { selected = it }, onLike = { p -> if (p !in liked) liked.add(p); index = (index + 1).coerceAtMost(profiles.size) }, onPass = { p -> passed.add(p); index = (index + 1).coerceAtMost(profiles.size) })
                            1 -> LikesScreen(liked, onOpen = { selected = it }, onChat = { chatProfile = it })
                            2 -> ChatsScreen(liked, onChat = { chatProfile = it })
                            else -> MyProfileScreen()
                        }
                        NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                            NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("کشف") })
                            NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.FavoriteBorder, null) }, label = { Text("لایک‌ها") })
                            NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.ChatBubbleOutline, null) }, label = { Text("گفت‌وگو") })
                            NavigationBarItem(selected = tab == 3, onClick = { tab = 3 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("پروفایل") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(subtitle: String = "آشنایی‌های واقعی، از همین‌جا") {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("همدم", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Rose)
            Text(subtitle, fontSize = 12.sp, color = Muted)
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(PaleRose), contentAlignment = Alignment.Center) { Text("♥", fontSize = 23.sp, color = Rose) }
    }
}

@Composable
private fun DiscoverScreen(profile: Profile?, onOpen: (Profile) -> Unit, onLike: (Profile) -> Unit, onPass: (Profile) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header()
        if (profile == null) {
            Spacer(Modifier.weight(1f))
            Text("فعلاً همین‌قدر آشنایی!", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("بعداً دوباره سر بزن و افراد تازه را ببین.", modifier = Modifier.fillMaxWidth().padding(18.dp), textAlign = TextAlign.Center, color = Muted)
            Button(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("پروفایل‌های جدید به‌زودی") }
            Spacer(Modifier.weight(1f))
        } else {
            Card(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp, vertical = 4.dp).clickable { onOpen(profile) }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(0.95f).background(Brush.verticalGradient(listOf(profile.tint, Color(0xFFFFF2F3)))), contentAlignment = Alignment.Center) {
                        Text(profile.emoji, fontSize = 116.sp)
                        Surface(Modifier.align(Alignment.TopStart).padding(14.dp), shape = RoundedCornerShape(30.dp), color = Color.White.copy(alpha = .9f)) { Text("● فعال در همدم", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontSize = 11.sp, color = Color(0xFF39825C)) }
                        Text("برای دیدن جزئیات لمس کن", modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), color = Ink.copy(alpha = .65f), fontSize = 12.sp)
                    }
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${profile.name}، ${profile.age}", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Spacer(Modifier.width(8.dp))
                            Text("✓", color = Color.White, modifier = Modifier.clip(CircleShape).background(Color(0xFF4D9A75)).padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = Muted, modifier = Modifier.size(16.dp)); Text(profile.city, color = Muted, fontSize = 13.sp) }
                        Spacer(Modifier.height(10.dp))
                        Text(profile.about, color = Ink, fontSize = 14.sp, lineHeight = 22.sp, maxLines = 3)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { profile.interests.forEach { tag -> Surface(shape = RoundedCornerShape(20.dp), color = PaleRose) { Text(tag, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = Rose, fontSize = 12.sp) } } }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(onClick = { onPass(profile) }, modifier = Modifier.size(60.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = Muted)) { Icon(Icons.Default.Close, "رد کردن", modifier = Modifier.size(30.dp)) }
                FilledIconButton(onClick = { onLike(profile) }, modifier = Modifier.size(72.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Rose, contentColor = Color.White)) { Icon(Icons.Default.Favorite, "پسندیدن", modifier = Modifier.size(32.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileDetail(profile: Profile, onBack: () -> Unit, onLike: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت") }; Text("پروفایل", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        Box(Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 18.dp).clip(RoundedCornerShape(24.dp)).background(profile.tint), contentAlignment = Alignment.Center) { Text(profile.emoji, fontSize = 100.sp) }
        Column(Modifier.padding(24.dp)) {
            Text("${profile.name}، ${profile.age}", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("📍 ${profile.city}", color = Muted)
            Spacer(Modifier.height(20.dp)); Text("درباره من", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(profile.about, modifier = Modifier.padding(top = 8.dp), lineHeight = 25.sp, color = Ink)
            Spacer(Modifier.height(18.dp)); Text("علاقه‌مندی‌ها", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { profile.interests.forEach { Surface(color = PaleRose, shape = RoundedCornerShape(20.dp)) { Text(it, Modifier.padding(12.dp), color = Rose) } } }
            Spacer(Modifier.weight(1f)); Button(onClick = onLike, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Favorite, null); Spacer(Modifier.width(8.dp)); Text("پسندیدن پروفایل") }
        }
    }
}

@Composable
private fun LikesScreen(liked: List<Profile>, onOpen: (Profile) -> Unit, onChat: (Profile) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header("افرادی که پسندیده‌ای")
        if (liked.isEmpty()) EmptyState("هنوز لایکی ثبت نکرده‌ای", "در بخش کشف، پروفایل‌هایی را که دوست داری پسند کن.", "♡")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(liked) { p -> ProfileRow(p, onClick = { onOpen(p) }, action = { TextButton(onClick = { onChat(p) }) { Text("گفت‌وگو") } }) } }
    }
}

@Composable
private fun ChatsScreen(liked: List<Profile>, onChat: (Profile) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header("گفت‌وگوهای شما")
        if (liked.isEmpty()) EmptyState("هنوز گفت‌وگویی نداری", "وقتی یک پروفایل را پسندیدی، می‌توانی گفت‌وگوی نمایشی را باز کنی.", "💬")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(liked) { p -> ProfileRow(p, onClick = { onChat(p) }, action = { Text("سلام! 👋", color = Muted, fontSize = 12.sp) }) } }
    }
}

@Composable
private fun ProfileRow(profile: Profile, onClick: () -> Unit, action: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(CircleShape).background(profile.tint), contentAlignment = Alignment.Center) { Text(profile.emoji, fontSize = 29.sp) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("${profile.name}، ${profile.age}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink); Text(profile.city, color = Muted, fontSize = 12.sp) }
            action()
        }
    }
}

@Composable
private fun ChatScreen(profile: Profile, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("سلام! خوشحالم باهات آشنا شدم 😊") }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت") }; Text("${profile.name}  ${profile.emoji}", fontWeight = FontWeight.Bold, fontSize = 19.sp) }
        Column(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.Bottom) {
            messages.forEachIndexed { i, msg -> Row(Modifier.fillMaxWidth(), horizontalArrangement = if (i % 2 == 0) Arrangement.Start else Arrangement.End) { Surface(color = if (i % 2 == 0) Color.White else PaleRose, shape = RoundedCornerShape(18.dp)) { Text(msg, Modifier.padding(13.dp), color = Ink) } }; Spacer(Modifier.height(8.dp)) }
            Text("این گفت‌وگو نمونه‌ای است و پیام‌ها روی سرور ذخیره نمی‌شوند.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        }
        Row(Modifier.fillMaxWidth().background(Color.White).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.weight(1f), placeholder = { Text("پیامت را بنویس...") }, shape = RoundedCornerShape(18.dp), singleLine = true)
            Spacer(Modifier.width(8.dp)); Button(onClick = { if (message.isNotBlank()) { messages.add(message.trim()); message = "" } }, shape = RoundedCornerShape(16.dp)) { Text("ارسال") }
        }
    }
}

@Composable
private fun MyProfileScreen() {
    Column(Modifier.fillMaxSize()) {
        Header("صفحه شخصی تو")
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(112.dp).clip(CircleShape).background(PaleRose), contentAlignment = Alignment.Center) { Text("🙂", fontSize = 58.sp) }
            Spacer(Modifier.height(14.dp)); Text("کاربر مهمان", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("پروفایل نمایشی", color = Muted)
            Spacer(Modifier.height(26.dp))
            listOf("ویرایش اطلاعات", "شهر: تهران", "حریم خصوصی و امنیت", "درباره همدم").forEach { label -> Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Text(label, modifier = Modifier.weight(1f), color = Ink); Text("‹", color = Muted, fontSize = 22.sp) } } }
            Spacer(Modifier.height(12.dp)); Text("نسخه نمایشی ۱.۰ — بدون حساب کاربری واقعی", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String, emoji: String) {
    Column(Modifier.fillMaxSize().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(emoji, fontSize = 58.sp); Spacer(Modifier.height(18.dp)); Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
        Text(description, modifier = Modifier.padding(top = 10.dp), color = Muted, textAlign = TextAlign.Center, lineHeight = 23.sp)
    }
}
