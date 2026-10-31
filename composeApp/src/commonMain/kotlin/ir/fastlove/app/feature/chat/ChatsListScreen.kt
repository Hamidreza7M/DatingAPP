package ir.fastlove.app.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.EmptyState
import ir.fastlove.app.core.designsystem.EmojiAvatar
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.OnlineStatusBadge
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.navigation.Screen

@Composable
fun ChatsListScreen() {
    val repo = repository()
    val nav = navigator()
    val interactions by repo.interactions.collectAsState()
    val matches = repo.people.filter { it.id in interactions.matchedIds && it.id !in interactions.blockedIds }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Text("گفتگوها", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            DemoBadge()
        }
        if (matches.isEmpty()) {
            EmptyState("هنوز گفتگویی نداری", "وقتی با کسی مچ شوی، اینجا می‌توانی گفتگو را شروع کنی.", "💬")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(matches, key = { it.id }) { p ->
                    val messages by repo.messages(p.id).collectAsState()
                    val last = messages.lastOrNull()?.text ?: "گفتگو را شروع کن…"
                    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().clickable { nav.push(Screen.Chat(p.id)) }) {
                        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            EmojiAvatar(p.emoji, p.tintArgb, size = 52.dp)
                            Spacer(Modifier.padding(Spacing.sm))
                            Column(Modifier.weight(1f)) {
                                Text(p.name, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(last, color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                            if (p.showOnlineStatus) OnlineStatusBadge(p.isOnline)
                        }
                    }
                }
            }
        }
    }
}
