package ir.fastlove.app.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * FastLove color tokens — a layered midnight surface palette with a coral→violet brand ramp.
 * These preserve the original FastLove identity while being organised as a reusable token set.
 */
object FastLoveColors {
    // Brand ramp
    val Coral = Color(0xFFFF648C)
    val CoralSoft = Color(0xFFFF8A83)
    val Peach = Color(0xFFFF9A78)
    val Violet = Color(0xFF9B8CFF)

    // Surfaces (deep midnight, multi-layer)
    val Background = Color(0xFF0B1020)
    val Surface = Color(0xFF151B2D)
    val SurfaceElevated = Color(0xFF20283D)
    val SurfaceHigh = Color(0xFF2A3350)

    // Text
    val TextPrimary = Color(0xFFF9F8FF)
    val TextSecondary = Color(0xFFB9C1D8)
    val TextMuted = Color(0xFF818BA7)

    // Semantic
    val Success = Color(0xFF52D6A5)
    val Warning = Color(0xFFFFC266)
    val Error = Color(0xFFFF6B6B)
    val Online = Color(0xFF43E1A6)

    // Overlays / strokes
    val Stroke = Color(0x22FFFFFF)
    val StrokeStrong = Color(0x33FFFFFF)
    val Scrim = Color(0x990B1020)

    val brandGradient = listOf(Coral, CoralSoft, Violet)
    val warmGradient = listOf(Coral, Peach)
}
