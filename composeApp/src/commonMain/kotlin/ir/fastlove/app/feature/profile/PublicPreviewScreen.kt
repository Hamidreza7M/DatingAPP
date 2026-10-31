package ir.fastlove.app.feature.profile

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.util.persianDigits

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicPreviewScreen() {
    val repo = repository()
    val nav = navigator()
    val user by repo.user.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar("پیش‌نمایش عمومی", onBack = { nav.pop() }, trailing = { DemoBadge() })
        Box(Modifier.fillMaxWidth().padding(Spacing.gutter).height(260.dp).clip(MaterialTheme.shapes.extraLarge).background(Brush.verticalGradient(FastLoveColors.brandGradient))) {
            Column(Modifier.align(Alignment.BottomStart).padding(Spacing.lg)) {
                Text("${user.name}، ${persianDigits(user.ageFor(FastLoveClock.currentPersianYear()).toString())}", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                Text("${user.job} · ${user.city} · هدف: ${user.goal.title}", color = FastLoveColors.TextPrimary.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(Modifier.padding(horizontal = Spacing.gutter)) {
            if (user.about.isNotBlank()) { SectionLabel("درباره"); Text(user.about, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium) }
            SectionLabel("علاقه‌ها")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                user.interests.forEach { FastLoveChip(it, true, {}) }
            }
            listOf(
                FastLoveData.profilePrompts[0] to user.idealFirstDate,
                FastLoveData.profilePrompts[1] to user.makesMeSmile,
                FastLoveData.profilePrompts[2] to user.relationshipValue
            ).filter { it.second.isNotBlank() }.forEach { (label, value) ->
                SectionLabel(label)
                Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Text("«$value»", color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.lg))
                }
            }
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}
