package ir.fastlove.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** When true, screens should reduce animation intensity (accessibility). */
val LocalReduceMotion = staticCompositionLocalOf { false }

private val FastLoveColorScheme = darkColorScheme(
    primary = FastLoveColors.Coral,
    onPrimary = FastLoveColors.TextPrimary,
    secondary = FastLoveColors.Violet,
    onSecondary = FastLoveColors.TextPrimary,
    tertiary = FastLoveColors.Peach,
    background = FastLoveColors.Background,
    onBackground = FastLoveColors.TextPrimary,
    surface = FastLoveColors.Surface,
    onSurface = FastLoveColors.TextPrimary,
    surfaceVariant = FastLoveColors.SurfaceElevated,
    onSurfaceVariant = FastLoveColors.TextSecondary,
    error = FastLoveColors.Error,
    outline = FastLoveColors.TextMuted
)

/**
 * Root theme: dark FastLove scheme, Vazirmatn typography, soft shapes, and full RTL.
 */
@Composable
fun FastLoveTheme(
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalReduceMotion provides reduceMotion
    ) {
        MaterialTheme(
            colorScheme = FastLoveColorScheme,
            typography = fastLoveTypography(),
            shapes = FastLoveShapes,
            content = content
        )
    }
}
