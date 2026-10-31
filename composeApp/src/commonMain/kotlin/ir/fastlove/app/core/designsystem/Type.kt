package ir.fastlove.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.fastlove.app.resources.Res
import ir.fastlove.app.resources.vazirmatn_bold
import ir.fastlove.app.resources.vazirmatn_medium
import ir.fastlove.app.resources.vazirmatn_regular
import ir.fastlove.app.resources.vazirmatn_semibold

/** Vazirmatn family loaded from Compose Multiplatform resources (shared across Android & iOS). */
@Composable
fun vazirmatnFamily(): FontFamily = FontFamily(
    Font(Res.font.vazirmatn_regular, FontWeight.Normal),
    Font(Res.font.vazirmatn_medium, FontWeight.Medium),
    Font(Res.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(Res.font.vazirmatn_bold, FontWeight.Bold)
)

@Composable
fun fastLoveTypography(): Typography {
    val f = vazirmatnFamily()
    fun style(size: Int, weight: FontWeight, lineHeight: Int) =
        TextStyle(fontFamily = f, fontWeight = weight, fontSize = size.sp, lineHeight = lineHeight.sp)
    return Typography(
        displayLarge = style(34, FontWeight.Bold, 42),
        headlineLarge = style(28, FontWeight.Bold, 36),
        headlineMedium = style(24, FontWeight.Bold, 32),
        titleLarge = style(20, FontWeight.SemiBold, 28),
        titleMedium = style(17, FontWeight.SemiBold, 24),
        titleSmall = style(15, FontWeight.Medium, 22),
        bodyLarge = style(16, FontWeight.Normal, 24),
        bodyMedium = style(14, FontWeight.Normal, 22),
        bodySmall = style(12, FontWeight.Normal, 18),
        labelLarge = style(14, FontWeight.SemiBold, 20),
        labelMedium = style(12, FontWeight.Medium, 16),
        labelSmall = style(10, FontWeight.Medium, 14)
    )
}
