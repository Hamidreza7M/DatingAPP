package ir.fastlove.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.util.persianDigits

private data class TourStep(val emoji: String, val title: String, val body: String)

private val steps = listOf(
    TourStep("♥", "لایک", "کارت را به سمت راست بکش یا دکمه‌ی قلب را بزن تا کسی را لایک کنی."),
    TourStep("✕", "رد کردن", "کارت را به سمت چپ بکش یا دکمه‌ی رد را بزن تا رد کنی."),
    TourStep("👤", "مشاهده‌ی جزئیات", "روی کارت ضربه بزن تا پروفایل کامل، علاقه‌ها و دلایل سازگاری را ببینی."),
    TourStep("🚫", "بلاک", "از صفحه‌ی پروفایل می‌توانی کاربر را بلاک کنی؛ از کشف و نقشه حذف می‌شود.")
)

/** Lightweight coach-mark overlay shown once over the Discover screen. One concept per step. */
@Composable
fun ProductTour(onFinish: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    val step = steps[index]

    Box(Modifier.fillMaxSize().background(FastLoveColors.Scrim).clickable(enabled = false) {}, contentAlignment = Alignment.BottomCenter) {
        Surface(color = FastLoveColors.Surface, shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth().padding(Spacing.gutter)) {
            Column(Modifier.padding(Spacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(step.emoji, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(Spacing.sm))
                Text(step.title, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.xs))
                Text(step.body, color = FastLoveColors.TextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Spacing.md))
                Text("${persianDigits((index + 1).toString())} از ${persianDigits(steps.size.toString())}", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(Spacing.lg))
                FastLovePrimaryButton(if (index == steps.lastIndex) "شروع کشف" else "بعدی", {
                    if (index == steps.lastIndex) onFinish() else index++
                })
                Spacer(Modifier.height(Spacing.sm))
                FastLoveSecondaryButton("رد کردن آموزش", onFinish)
            }
        }
    }
}
