package ir.fastlove.app.feature.discover

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.OnlineStatusBadge
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.designsystem.toColor
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.feature.dateplanner.DateProposalDialog
import ir.fastlove.app.navigation.Screen
import ir.fastlove.app.core.util.persianDigits

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonDetailScreen(personId: Int) {
    val repo = repository()
    val nav = navigator()
    val person = remember(personId) { repo.people.firstOrNull { it.id == personId } } ?: run {
        FastLoveTopBar("پروفایل", onBack = { nav.pop() }); return
    }
    val interactions by repo.interactions.collectAsState()
    var showReport by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    val isMatched = personId in interactions.matchedIds

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar(person.name, onBack = { nav.pop() })
        Box(
            Modifier.fillMaxWidth().height(320.dp)
                .background(Brush.verticalGradient(listOf(person.tintArgb.toColor(), FastLoveColors.SurfaceElevated)))
        ) {
            Text(person.emoji, Modifier.align(Alignment.Center), style = MaterialTheme.typography.displayLarge)
            Column(Modifier.align(Alignment.BottomStart).padding(Spacing.xl)) {
                Text("${person.name}، ${persianDigits(person.age.toString())}", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                Text("${person.job} · ${person.city}", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                if (person.showOnlineStatus) { Spacer(Modifier.height(Spacing.xs)); OnlineStatusBadge(person.isOnline) }
            }
        }

        Column(Modifier.padding(Spacing.gutter)) {
            SectionLabel("درباره")
            Text(person.about, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)

            SectionLabel("علاقه‌ها")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                person.interests.forEach { FastLoveChip(it, true, {}) }
            }

            if (person.idealFirstDate.isNotBlank()) Prompt(FastLoveData.profilePrompts[0], person.idealFirstDate)
            if (person.makesMeSmile.isNotBlank()) Prompt(FastLoveData.profilePrompts[1], person.makesMeSmile)
            if (person.relationshipValue.isNotBlank()) Prompt(FastLoveData.profilePrompts[2], person.relationshipValue)

            Spacer(Modifier.height(Spacing.xl))
            if (isMatched) {
                FastLovePrimaryButton("گفتگو", { nav.pop(); nav.push(Screen.Chat(person.id)) }, leading = Icons.Filled.Chat)
                Spacer(Modifier.height(Spacing.sm))
                FastLoveSecondaryButton("پیشنهاد قرار اول", { showDate = true })
            } else {
                FastLovePrimaryButton("لایک ♥", {
                    repo.like(person.id); nav.pop()
                })
            }
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ActionPill("بلاک", Icons.Filled.Block, Modifier.weight(1f)) { repo.block(person.id); nav.pop() }
                ActionPill("گزارش", Icons.Filled.Flag, Modifier.weight(1f)) { showReport = true }
            }
            Spacer(Modifier.height(Spacing.huge))
        }
    }

    if (showReport) ReportDialog(person.name, onDismiss = { showReport = false }, onSubmit = { showReport = false })
    if (showDate) DateProposalDialog(person, onDismiss = { showDate = false })
}

@Composable
private fun Prompt(label: String, value: String) {
    SectionLabel(label)
    Surface(color = FastLoveColors.SurfaceElevated, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text("«$value»", color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.lg))
    }
}

@Composable
private fun ActionPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = FastLoveColors.SurfaceElevated,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            androidx.compose.material3.Icon(icon, null, tint = FastLoveColors.TextSecondary, modifier = Modifier.height(18.dp))
            Text(" $text", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
