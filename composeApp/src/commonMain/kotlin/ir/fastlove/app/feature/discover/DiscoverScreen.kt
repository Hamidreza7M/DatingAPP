package ir.fastlove.app.feature.discover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.designsystem.toColor
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.domain.Matching
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.util.oneDecimalPersian
import ir.fastlove.app.core.util.persianDigits
import ir.fastlove.app.navigation.Screen
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen() {
    val repo = repository()
    val nav = navigator()
    val user by repo.user.collectAsState()
    val interactions by repo.interactions.collectAsState()
    val filter by repo.filter.collectAsState()
    val year = remember { FastLoveClock.currentPersianYear() }

    val candidates = remember(user, interactions, filter) {
        Matching.compatiblePeople(user, repo.people).filter { p ->
            p.id !in interactions.likedIds &&
                p.id !in interactions.dislikedIds &&
                p.id !in interactions.blockedIds &&
                p.age in filter.minAge..filter.maxAge &&
                p.distanceKm <= filter.maxDistance &&
                (filter.cities.isEmpty() || p.city in filter.cities) &&
                (filter.goals.isEmpty() || p.goal in filter.goals) &&
                (filter.interests.isEmpty() || p.interests.any { it in filter.interests })
        }
    }

    var matchPerson by remember { mutableStateOf<Person?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("کشف", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            DemoBadge()
            Spacer(Modifier.width(Spacing.sm))
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(FastLoveColors.SurfaceElevated).clickable { nav.push(Screen.Filter) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Tune, "فیلتر", tint = FastLoveColors.TextPrimary) }
        }

        Box(Modifier.weight(1f).fillMaxSize().padding(horizontal = Spacing.gutter), contentAlignment = Alignment.Center) {
            val top = candidates.firstOrNull()
            if (top == null) {
                ir.fastlove.app.core.designsystem.EmptyState(
                    "فعلاً کسی نمانده",
                    "فیلترها را بازتر کن یا بعداً دوباره سر بزن.",
                    "🔍"
                )
            } else {
                // Peek of the next card
                candidates.getOrNull(1)?.let { next ->
                    DiscoverCard(next, user, year, Modifier.fillMaxSize().padding(Spacing.sm), onTap = {})
                }
                SwipeableCard(
                    person = top,
                    user = user,
                    year = year,
                    onLike = {
                        if (repo.like(top.id)) matchPerson = top
                    },
                    onDislike = { repo.dislike(top.id) },
                    onTap = { nav.push(Screen.PersonDetail(top.id)) }
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(Spacing.lg),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            candidates.firstOrNull()?.let { top ->
                RoundAction(Icons.Filled.Close, FastLoveColors.TextSecondary) { repo.dislike(top.id) }
                Spacer(Modifier.width(Spacing.xxl))
                RoundAction(Icons.Filled.Favorite, FastLoveColors.Coral, big = true) {
                    if (repo.like(top.id)) matchPerson = top
                }
            }
        }
    }

    matchPerson?.let { p ->
        MatchPopup(
            person = p,
            onChat = { matchPerson = null; nav.push(Screen.Chat(p.id)) },
            onDismiss = { matchPerson = null }
        )
    }
}

@Composable
private fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color, big: Boolean = false, onClick: () -> Unit) {
    val s = if (big) 68.dp else 56.dp
    Box(
        Modifier.size(s).clip(CircleShape).background(FastLoveColors.SurfaceElevated).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(if (big) 32.dp else 26.dp)) }
}

@Composable
private fun SwipeableCard(
    person: Person,
    user: ir.fastlove.app.core.model.UserProfile,
    year: Int,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onTap: () -> Unit
) {
    var offsetX by remember(person.id) { mutableStateOf(0f) }
    val animatedX by animateFloatAsState(offsetX, label = "swipe")
    val threshold = 280f

    Box(
        Modifier
            .fillMaxSize()
            .offset { IntOffset(animatedX.roundToInt(), 0) }
            .pointerInput(person.id) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > threshold -> onLike()
                            offsetX < -threshold -> onDislike()
                            else -> offsetX = 0f
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                }
            }
    ) {
        DiscoverCard(person, user, year, Modifier.fillMaxSize(), onTap = onTap)

        // Like / Nope stamps
        val likeAlpha = (offsetX / threshold).coerceIn(0f, 1f)
        val nopeAlpha = (-offsetX / threshold).coerceIn(0f, 1f)
        if (likeAlpha > 0.05f) Stamp("لایک ♥", FastLoveColors.Online, Alignment.TopStart, likeAlpha)
        if (nopeAlpha > 0.05f) Stamp("رد ✕", FastLoveColors.Error, Alignment.TopEnd, nopeAlpha)
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.Stamp(text: String, color: androidx.compose.ui.graphics.Color, align: Alignment, alpha: Float) {
    Surface(
        color = color.copy(alpha = 0.18f * alpha + 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.align(align).padding(Spacing.xl)
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoverCard(
    person: Person,
    user: ir.fastlove.app.core.model.UserProfile,
    year: Int,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val match = remember(person.id, user) { Matching.evaluate(user, person, year) }
    Box(
        modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.verticalGradient(listOf(person.tintArgb.toColor(), FastLoveColors.SurfaceElevated)))
            .clickable(onClick = onTap)
    ) {
        // scrim bottom
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, FastLoveColors.Scrim))))
        Text(person.emoji, modifier = Modifier.align(Alignment.Center).padding(bottom = 120.dp), style = MaterialTheme.typography.displayLarge)

        // compatibility badge
        Surface(color = FastLoveColors.Background.copy(alpha = 0.7f), shape = CircleShape, modifier = Modifier.align(Alignment.TopCenter).padding(Spacing.lg)) {
            Text("${oneDecimalPersian(match.score)}/۱۰", color = FastLoveColors.Coral, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs))
        }

        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(Spacing.xl)) {
            Text(
                "${person.name}، ${persianDigits(person.age.toString())}",
                color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "${person.job} · ${person.city} · ${persianDigits(person.distanceKm.toString())} کیلومتر",
                color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(Spacing.xs))
            Text("هدف: ${person.goal.title}", color = FastLoveColors.Violet, style = MaterialTheme.typography.labelLarge)
            if (person.idealFirstDate.isNotBlank()) {
                Spacer(Modifier.height(Spacing.sm))
                Text("«${person.idealFirstDate}»", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                match.reasons.take(3).forEach { reason ->
                    Surface(color = FastLoveColors.SurfaceElevated.copy(alpha = 0.85f), shape = CircleShape) {
                        Text(reason, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp))
                    }
                }
            }
        }
    }
}
