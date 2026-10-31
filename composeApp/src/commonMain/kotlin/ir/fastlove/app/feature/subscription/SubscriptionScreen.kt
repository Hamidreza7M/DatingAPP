package ir.fastlove.app.feature.subscription

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.model.SubscriptionPlan
import ir.fastlove.app.core.util.formatToman

private data class Plan(val plan: SubscriptionPlan, val priceToman: Int, val features: List<String>)

private val plans = listOf(
    Plan(SubscriptionPlan.FREE, 0, listOf("ساخت پروفایل", "سهمیه‌ی محدود لایک", "چت پس از مچ", "پیشنهاد دیت", "ابزارهای پایه‌ی امنیتی")),
    Plan(SubscriptionPlan.PLUS, 149000, listOf("دیدن لایک‌های دریافتی", "فیلترهای پیشرفته", "سهمیه‌ی لایک بیشتر", "بازگرداندن دیسلایک", "تغییر موقعیت جست‌وجو (نمایشی)")),
    Plan(SubscriptionPlan.VIP, 299000, listOf("تمام امکانات پلاس", "حالت ناشناس (Incognito)", "Boost نمایشی", "اولویت نمایش (نمایشی)", "فیلترهای بیشتر"))
)

@Composable
fun SubscriptionScreen() {
    val repo = repository()
    val nav = navigator()
    val sub by repo.subscription.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar("اشتراک", onBack = { nav.pop() }, trailing = { DemoBadge() })
        Column(Modifier.padding(Spacing.gutter)) {
            Text("پلن فعلی: ${sub.plan.title}", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text("خریدها در این دمو شبیه‌سازی می‌شوند؛ هیچ پرداخت واقعی انجام نمی‌شود. ابزارهای امنیتی همیشه رایگان‌اند.", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = Spacing.sm))
            Spacer(Modifier.height(Spacing.sm))
            plans.forEach { p ->
                val active = sub.plan == p.plan
                Surface(
                    color = FastLoveColors.SurfaceElevated,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm)
                        .border(1.dp, if (active) FastLoveColors.Coral else FastLoveColors.Stroke, RoundedCornerShape(20.dp))
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        Row {
                            Text(p.plan.title, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(if (p.priceToman == 0) "رایگان" else "${formatToman(p.priceToman)}/ماه", color = FastLoveColors.Coral, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        p.features.forEach { f -> Text("• $f", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 1.dp)) }
                        if (p.plan != SubscriptionPlan.FREE) {
                            Spacer(Modifier.height(Spacing.md))
                            FastLovePrimaryButton(
                                if (active) "پلن فعال" else "ارتقا (نمایشی)",
                                { repo.purchase(p.plan, p.priceToman, 30) },
                                enabled = !active
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}
