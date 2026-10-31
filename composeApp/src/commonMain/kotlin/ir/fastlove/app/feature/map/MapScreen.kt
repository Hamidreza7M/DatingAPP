package ir.fastlove.app.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.designsystem.toColor
import ir.fastlove.app.core.domain.MapInteractionFilter
import ir.fastlove.app.core.domain.MapLogic
import ir.fastlove.app.core.model.InteractionState
import ir.fastlove.app.core.util.persianDigits
import ir.fastlove.app.navigation.Screen

// Tehran approximate bounds used for the schematic projection.
private const val LAT_MIN = 35.565
private const val LAT_MAX = 35.830
private const val LON_MIN = 51.185
private const val LON_MAX = 51.630

@Composable
fun MapScreen() {
    val repo = repository()
    val nav = navigator()
    val user by repo.user.collectAsState()
    val interactions by repo.interactions.collectAsState()
    var filter by remember { mutableStateOf(MapInteractionFilter.ALL) }
    var zoom by remember { mutableStateOf(12.0) }
    var showSelf by remember { mutableStateOf(false) }

    val markers = remember(user, interactions, filter) {
        MapLogic.markers(
            viewer = user,
            people = repo.people.filter { it.latitude != 0.0 },
            blockedIds = interactions.blockedIds,
            likedIds = interactions.likedIds,
            dislikedIds = interactions.dislikedIds,
            filter = filter
        )
    }
    val clustered = MapLogic.shouldCluster(zoom)

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Text("نقشه", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            DemoBadge(label = "تهران · دمو")
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            MapInteractionFilter.entries.forEach { f ->
                FastLoveChip(f.title, filter == f, { filter = f })
            }
        }
        Spacer(Modifier.size(Spacing.sm))

        BoxWithConstraints(
            Modifier.weight(1f).fillMaxWidth().padding(Spacing.gutter).clip(MaterialTheme.shapes.extraLarge)
                .background(Brush.verticalGradient(listOf(FastLoveColors.Surface, FastLoveColors.SurfaceElevated)))
        ) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()

            if (clustered) {
                Box(Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
                    Surface(color = FastLoveColors.Coral.copy(alpha = 0.2f), shape = CircleShape) {
                        Column(Modifier.padding(Spacing.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(persianDigits(markers.size.toString()), color = FastLoveColors.Coral, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineLarge)
                            Text("نفر در این محدوده", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.labelMedium)
                            Text("برای دیدن تک‌تک، زوم کن", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                markers.forEach { m ->
                    val x = ((m.roundedLongitude - LON_MIN) / (LON_MAX - LON_MIN)).coerceIn(0.03, 0.9).toFloat() * w
                    val y = (1.0 - (m.roundedLatitude - LAT_MIN) / (LAT_MAX - LAT_MIN)).coerceIn(0.03, 0.9).toFloat() * h
                    MapMarkerPin(
                        emoji = m.person.emoji,
                        tintArgb = m.person.tintArgb,
                        interaction = m.interaction,
                        online = m.person.isOnline,
                        modifier = Modifier.offset { androidx.compose.ui.unit.IntOffset(x.toInt(), y.toInt()) }.clickable { nav.push(Screen.PersonDetail(m.person.id)) }
                    )
                }
                if (showSelf) {
                    MapMarkerPin("🧍", 0xFF9B8CFF, InteractionState.NONE, true, Modifier.align(Alignment.Center), isSelf = true)
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter)) {
            Text("زوم", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
            Slider(value = zoom.toFloat(), onValueChange = { zoom = it.toDouble() }, valueRange = 9f..15f,
                colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = FastLoveColors.Coral, activeTrackColor = FastLoveColors.Coral))
            Row(Modifier.fillMaxWidth().padding(bottom = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Text("نمایش موقعیت من روی نقشه", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = showSelf, onCheckedChange = { showSelf = it })
            }
            Text("موقعیت‌ها تقریبی‌اند و مختصات دقیق افراد نمایش داده نمی‌شود.", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.size(Spacing.sm))
        }
    }
}

@Composable
private fun MapMarkerPin(
    emoji: String,
    tintArgb: Long,
    interaction: InteractionState,
    online: Boolean,
    modifier: Modifier = Modifier,
    isSelf: Boolean = false
) {
    Box(modifier.size(46.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(tintArgb.toColor()), contentAlignment = Alignment.Center) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        // interaction badge
        val badge = when (interaction) {
            InteractionState.LIKED -> "♥" to FastLoveColors.Coral
            InteractionState.DISLIKED -> "✕" to FastLoveColors.TextMuted
            InteractionState.NONE -> null
        }
        badge?.let { (sym, color) ->
            Surface(color = FastLoveColors.Background, shape = CircleShape, modifier = Modifier.align(Alignment.TopStart)) {
                Text(sym, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
        if (!isSelf) {
            Box(Modifier.align(Alignment.TopEnd).size(10.dp).clip(CircleShape).background(if (online) FastLoveColors.Online else FastLoveColors.TextMuted))
        }
    }
}
