package ir.fastlove.app.feature.dateplanner

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.EmojiAvatar
import ir.fastlove.app.core.designsystem.EmptyState
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.Jalali
import ir.fastlove.app.navigation.Screen

@Composable
fun ScheduledDatesScreen() {
    val repo = repository()
    val nav = navigator()
    val dates = remember { repo.allActiveDates() }

    Column(Modifier.fillMaxSize()) {
        FastLoveTopBar("قرارهای برنامه‌ریزی‌شده", onBack = { nav.pop() })
        if (dates.isEmpty()) {
            EmptyState("هنوز قراری نداری", "از صفحه‌ی چت یا پروفایل یک قرار پیشنهاد بده.", "📅")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(dates.size) { i ->
                    val (person, proposal) = dates[i]
                    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().clickable { nav.push(Screen.ScheduledDateDetail(person.id, proposal.id)) }) {
                        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            EmojiAvatar(person.emoji, person.tintArgb, size = 48.dp)
                            Spacer(Modifier.padding(Spacing.sm))
                            Column(Modifier.weight(1f)) {
                                Text("${proposal.kind.emoji} ${proposal.place}", color = FastLoveColors.TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                Text("${person.name} · ${Jalali.labelFromMillis(proposal.scheduledAtMillis)}", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                            }
                            Text(proposal.status.title, color = FastLoveColors.Violet, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
