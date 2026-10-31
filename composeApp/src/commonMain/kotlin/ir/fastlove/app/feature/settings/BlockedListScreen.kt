package ir.fastlove.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.EmojiAvatar
import ir.fastlove.app.core.designsystem.EmptyState
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.Spacing

@Composable
fun BlockedListScreen() {
    val repo = repository()
    val nav = navigator()
    val interactions by repo.interactions.collectAsState()
    val blocked = repo.people.filter { it.id in interactions.blockedIds }

    Column(Modifier.fillMaxSize()) {
        FastLoveTopBar("کاربران بلاک‌شده", onBack = { nav.pop() })
        if (blocked.isEmpty()) {
            EmptyState("لیست بلاک خالی است", "هر کاربری که بلاک کنی اینجا نمایش داده می‌شود.", "🚫")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(blocked, key = { it.id }) { p ->
                    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            EmojiAvatar(p.emoji, p.tintArgb, size = 44.dp)
                            Spacer(Modifier.padding(Spacing.sm))
                            Text(p.name, color = FastLoveColors.TextPrimary, modifier = Modifier.weight(1f))
                            Surface(
                                color = FastLoveColors.SurfaceHigh,
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.clickable { repo.unblock(p.id) }
                            ) {
                                Text("رفع بلاک", color = FastLoveColors.Coral, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm))
                            }
                        }
                    }
                }
            }
        }
    }
}
