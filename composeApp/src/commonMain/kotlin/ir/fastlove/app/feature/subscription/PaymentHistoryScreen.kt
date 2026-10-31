package ir.fastlove.app.feature.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.EmptyState
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.Jalali
import ir.fastlove.app.core.util.formatToman

@Composable
fun PaymentHistoryScreen() {
    val repo = repository()
    val nav = navigator()
    val sub by repo.subscription.collectAsState()

    Column(Modifier.fillMaxSize()) {
        FastLoveTopBar("تاریخچه پرداخت (دمو)", onBack = { nav.pop() })
        if (sub.payments.isEmpty()) {
            EmptyState("پرداختی ثبت نشده", "خریدهای نمایشی اینجا نمایش داده می‌شوند.", "🧾")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(sub.payments.size) { i ->
                    val r = sub.payments[i]
                    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(Spacing.lg)) {
                            Column(Modifier.weight(1f)) {
                                Text(r.planTitle, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(Jalali.labelFromMillis(r.purchasedAtMillis), color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                            }
                            Text(formatToman(r.priceToman), color = FastLoveColors.Coral, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
