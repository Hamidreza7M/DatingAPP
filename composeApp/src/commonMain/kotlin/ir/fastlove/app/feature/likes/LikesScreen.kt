package ir.fastlove.app.feature.likes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.EmptyState
import ir.fastlove.app.core.designsystem.EmojiAvatar
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.util.persianDigits
import ir.fastlove.app.navigation.Screen

private enum class LikesTab(val title: String) { RECEIVED("دریافتی"), SENT("ارسالی") }

@Composable
fun LikesScreen() {
    val repo = repository()
    val nav = navigator()
    val interactions by repo.interactions.collectAsState()
    val sub by repo.subscription.collectAsState()
    var tab by remember { mutableStateOf(LikesTab.RECEIVED) }

    val received = remember(interactions) {
        repo.people.filter {
            it.likedYou && it.id !in interactions.blockedIds && it.id !in interactions.dislikedIds
        }
    }
    val sent = remember(interactions) { repo.people.filter { it.id in interactions.likedIds } }
    val canSeeReceived = sub.plan != ir.fastlove.app.core.model.SubscriptionPlan.FREE

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Text("لایک‌ها", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            DemoBadge()
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            LikesTab.entries.forEach { t ->
                val count = if (t == LikesTab.RECEIVED) received.size else sent.size
                Surface(
                    color = if (tab == t) FastLoveColors.Coral.copy(alpha = 0.18f) else FastLoveColors.SurfaceElevated,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f).clickable { tab = t }
                ) {
                    Text(
                        "${t.title} (${persianDigits(count.toString())})",
                        color = if (tab == t) FastLoveColors.Coral else FastLoveColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = Spacing.md).fillMaxWidth()
                    )
                }
            }
        }
        Spacer(Modifier.padding(Spacing.xs))

        val list = if (tab == LikesTab.RECEIVED) received else sent
        when {
            tab == LikesTab.RECEIVED && !canSeeReceived -> LockedReceived(received.size) { nav.push(Screen.Subscription) }
            list.isEmpty() -> EmptyState(
                if (tab == LikesTab.RECEIVED) "هنوز لایکی دریافت نکرده‌ای" else "هنوز کسی را لایک نکرده‌ای",
                "در صفحه‌ی کشف فعال باش.",
                "💛"
            )
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(list, key = { it.id }) { p ->
                    PersonRow(p, matched = p.id in interactions.matchedIds) { nav.push(Screen.PersonDetail(p.id)) }
                }
            }
        }
    }
}

@Composable
private fun LockedReceived(count: Int, onUpgrade: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(Spacing.gutter), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.padding(Spacing.xxl))
        Text("🔒", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.padding(Spacing.sm))
        Text("${persianDigits(count.toString())} نفر تو را لایک کرده‌اند", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("برای دیدن لایک‌های دریافتی به پلاس ارتقا بده (نمایشی).", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.padding(Spacing.md))
        ir.fastlove.app.core.designsystem.FastLovePrimaryButton("ارتقا به پلاس", onUpgrade)
    }
}

@Composable
private fun PersonRow(person: Person, matched: Boolean, onClick: () -> Unit) {
    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(person.emoji, person.tintArgb, size = 52.dp)
            Spacer(Modifier.padding(Spacing.sm))
            Column(Modifier.weight(1f)) {
                Text("${person.name}، ${persianDigits(person.age.toString())}", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${person.job} · ${person.city}", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
            }
            if (matched) Text("مچ 💖", color = FastLoveColors.Coral, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}
