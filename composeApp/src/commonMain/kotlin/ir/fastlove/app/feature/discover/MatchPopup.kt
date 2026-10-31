package ir.fastlove.app.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.Person

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchPopup(person: Person, onChat: () -> Unit, onDismiss: () -> Unit) {
    val repo = repository()
    val user = repo.user
    val shared = remember(person.id) {
        user.value.interests.toSet().intersect(person.interests.toSet()).toList()
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = FastLoveColors.Surface) {
            Column(
                Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(FastLoveColors.Coral.copy(alpha = 0.18f), FastLoveColors.Surface))).padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("مچ شد! 🎉", color = FastLoveColors.Coral, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text("تو و ${person.name} همدیگر را پسندیدید.", color = FastLoveColors.TextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                if (shared.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.md))
                    Text("علاقه‌های مشترک", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        shared.forEach { FastLoveChip(it, true, {}) }
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
                Text("پیشنهاد برای شروع گفتگو:", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(Spacing.xs))
                FastLoveData.conversationStarters.take(3).forEach {
                    Text("• $it", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
                }
                Spacer(Modifier.height(Spacing.lg))
                FastLovePrimaryButton("شروع گفتگو", onChat)
                Spacer(Modifier.height(Spacing.sm))
                FastLoveSecondaryButton("بعداً", onDismiss)
            }
        }
    }
}
