package ir.fastlove.app.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Primary call-to-action: brand gradient pill. */
@Composable
fun FastLovePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: ImageVector? = null
) {
    val alpha = if (enabled) 1f else 0.45f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(FastLoveColors.brandGradient.map { it.copy(alpha = alpha) }))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                Icon(leading, null, tint = FastLoveColors.TextPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(text, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Secondary button: outlined, transparent fill. */
@Composable
fun FastLoveSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(FastLoveColors.SurfaceElevated.copy(alpha = if (enabled) 1f else 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

/** Standard elevated card surface. */
@Composable
fun FastLoveCard(
    modifier: Modifier = Modifier,
    color: Color = FastLoveColors.SurfaceElevated,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, FastLoveColors.Stroke)
    ) { content() }
}

/** Selectable chip (interests, jobs, filters). */
@Composable
fun FastLoveChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) FastLoveColors.Coral.copy(alpha = 0.18f) else FastLoveColors.SurfaceElevated
    val border = if (selected) FastLoveColors.Coral else FastLoveColors.Stroke
    val fg = if (selected) FastLoveColors.Coral else FastLoveColors.TextSecondary
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = bg,
        shape = CircleShape,
        border = BorderStroke(1.dp, border)
    ) {
        Text(
            text,
            color = fg,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        )
    }
}

/** Small informational badge with an icon. */
@Composable
fun InfoChip(icon: ImageVector, text: String, tint: Color = FastLoveColors.Violet) {
    Surface(color = tint.copy(alpha = 0.14f), shape = CircleShape) {
        Row(
            Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(Spacing.xs))
            Text(text, color = tint, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** The "دمو" marker used wherever behaviour is simulated. */
@Composable
fun DemoBadge(modifier: Modifier = Modifier, label: String = "دمو") {
    Surface(modifier = modifier, color = FastLoveColors.Warning.copy(alpha = 0.18f), shape = CircleShape) {
        Text(
            label,
            color = FastLoveColors.Warning,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp)
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = FastLoveColors.TextMuted,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier.padding(vertical = Spacing.sm)
    )
}

/** Online/offline presence dot + label. */
@Composable
fun OnlineStatusBadge(isOnline: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (isOnline) FastLoveColors.Online else FastLoveColors.TextMuted))
        Spacer(Modifier.width(Spacing.xs))
        Text(
            if (isOnline) "آنلاین" else "آفلاین",
            color = if (isOnline) FastLoveColors.Online else FastLoveColors.TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, emoji: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(Spacing.md))
        Text(title, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Spacing.xs))
        Text(subtitle, color = FastLoveColors.TextMuted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().heightIn(min = 160.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FastLoveColors.Coral)
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(Spacing.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("😕", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(Spacing.sm))
        Text(message, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(Modifier.height(Spacing.md))
            FastLoveSecondaryButton("تلاش دوباره", onRetry, modifier = Modifier.width(180.dp))
        }
    }
}
