package ir.fastlove.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.Spacing

@Composable
fun PrivacySettingsScreen() {
    val repo = repository()
    val nav = navigator()
    val user by repo.user.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar("حریم خصوصی", onBack = { nav.pop() }, trailing = { DemoBadge() })
        Column(Modifier.padding(Spacing.gutter)) {
            SwitchRow("نمایش وضعیت آنلاین", user.showOnlineStatus) { repo.updateProfile(user.copy(showOnlineStatus = it)) }
            SwitchRow("نمایش آخرین بازدید", user.showLastSeen) { repo.updateProfile(user.copy(showLastSeen = it)) }
            SwitchRow("نمایش تقریبی من روی نقشه", user.showOnMap) { repo.updateProfile(user.copy(showOnMap = it)) }
            SwitchRow("مخفی‌شدن کامل از نقشه", user.hiddenFromMap) { repo.updateProfile(user.copy(hiddenFromMap = it)) }
            Spacer(Modifier.height(Spacing.lg))
            Text("راهنمای قرار امن", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                "برای قرار اول جای عمومی را انتخاب کن، به یک فرد مورد اعتماد اطلاع بده، و با وسیله‌ی نقلیه‌ی خودت برو. امکانات امنیتی پایه همیشه رایگان‌اند.",
                color = FastLoveColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xs)
            )
        }
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
