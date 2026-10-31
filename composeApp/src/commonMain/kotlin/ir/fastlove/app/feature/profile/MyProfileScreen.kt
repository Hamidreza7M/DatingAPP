package ir.fastlove.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.util.persianDigits
import ir.fastlove.app.navigation.Screen

@Composable
fun MyProfileScreen() {
    val repo = repository()
    val nav = navigator()
    val user by repo.user.collectAsState()
    val sub by repo.subscription.collectAsState()
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Text("پروفایل من", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            DemoBadge()
        }

        Box(
            Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter).height(180.dp).clip(MaterialTheme.shapes.extraLarge)
                .background(Brush.verticalGradient(FastLoveColors.brandGradient))
        ) {
            Column(Modifier.align(Alignment.BottomStart).padding(Spacing.lg)) {
                Text("${user.name}، ${persianDigits(user.ageFor(FastLoveClock.currentPersianYear()).toString())}", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                Text("${user.job} · ${user.city}", color = FastLoveColors.TextPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            }
            Surface(color = FastLoveColors.Background.copy(alpha = 0.6f), shape = CircleShape, modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.md)) {
                Text("پلن: ${sub.plan.title}", color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs))
            }
        }

        Column(Modifier.padding(Spacing.gutter)) {
            // completeness meter
            val pct = (user.completeness * 100).toInt()
            Text("تکمیل پروفایل: ${persianDigits(pct.toString())}٪", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.xs))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(FastLoveColors.SurfaceElevated)) {
                Box(Modifier.fillMaxWidth(user.completeness).height(8.dp).clip(CircleShape).background(Brush.horizontalGradient(FastLoveColors.brandGradient)))
            }
            Spacer(Modifier.height(Spacing.lg))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FastLovePrimaryButton("ویرایش پروفایل", { nav.push(Screen.EditProfile) }, modifier = Modifier.weight(1f), leading = Icons.Filled.Edit)
            }
            Spacer(Modifier.height(Spacing.sm))
            FastLoveSecondaryButton("پیش‌نمایش از دید دیگران", { nav.push(Screen.PublicPreview) })

            Spacer(Modifier.height(Spacing.lg))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, null, tint = FastLoveColors.Violet)
                Spacer(Modifier.height(Spacing.xs))
                Text("  کد معرف تو: ${user.referralCode}", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(Spacing.lg))

            ActionRow(Icons.Filled.Star, "اشتراک و ارتقا") { nav.push(Screen.Subscription) }
            ActionRow(Icons.Filled.CalendarMonth, "قرارهای برنامه‌ریزی‌شده") { nav.push(Screen.ScheduledDates) }
            ActionRow(Icons.Filled.Lock, "حریم خصوصی") { nav.push(Screen.PrivacySettings) }
            ActionRow(Icons.Filled.Block, "کاربران بلاک‌شده") { nav.push(Screen.BlockedList) }
            ActionRow(Icons.Filled.Receipt, "تاریخچه پرداخت (دمو)") { nav.push(Screen.PaymentHistory) }
            ActionRow(Icons.Filled.RestartAlt, "بازنشانی دمو (Reset Demo)", tint = FastLoveColors.Error) { confirmReset = true }
            Spacer(Modifier.height(Spacing.huge))
        }
    }

    if (confirmReset) {
        Dialog(onDismissRequest = { confirmReset = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, color = FastLoveColors.Surface) {
                Column(Modifier.padding(Spacing.xl)) {
                    Text("بازنشانی دمو", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Spacing.sm))
                    Text("همه‌ی لایک‌ها، مچ‌ها، چت‌ها، قرارها و تنظیمات پاک و به حالت اولیه برمی‌گردد.", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.lg))
                    FastLovePrimaryButton("بله، بازنشانی کن", { repo.resetDemo(); confirmReset = false; nav.selectTab(ir.fastlove.app.navigation.Tab.DISCOVER) })
                    Spacer(Modifier.height(Spacing.sm))
                    FastLoveSecondaryButton("انصراف", { confirmReset = false })
                }
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, tint: androidx.compose.ui.graphics.Color = FastLoveColors.TextPrimary, onClick: () -> Unit) {
    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint)
            Spacer(Modifier.height(Spacing.sm))
            Text("  $title", color = tint, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}
