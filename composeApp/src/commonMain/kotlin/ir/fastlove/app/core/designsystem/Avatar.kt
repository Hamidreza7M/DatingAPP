package ir.fastlove.app.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** ARGB Long (e.g. 0xFFFFE0E6) → Compose Color. */
fun Long.toColor(): Color = Color(this)

/**
 * Emoji avatar on a soft gradient. The demo uses generated avatars rather than real photos so it
 * needs no platform photo-picker / file IO; real photo upload is a documented platform adapter.
 */
@Composable
fun EmojiAvatar(
    emoji: String,
    tintArgb: Long,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    corner: Dp = 16.dp
) {
    val tint = tintArgb.toColor()
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(listOf(tint, FastLoveColors.SurfaceElevated))),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.42f).sp)
    }
}
