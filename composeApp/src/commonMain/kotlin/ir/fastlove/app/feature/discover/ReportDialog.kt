package ir.fastlove.app.feature.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.ReportReason

@Composable
fun ReportDialog(name: String, onSubmit: (ReportReason) -> Unit, onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf<ReportReason?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = FastLoveColors.Surface) {
            Column(Modifier.fillMaxWidth().padding(Spacing.xl)) {
                Text("گزارش $name", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.xs))
                DemoBadge()
                Spacer(Modifier.height(Spacing.md))
                FastLoveData.reportReasons.forEach { reason ->
                    Surface(
                        color = if (selected == reason) FastLoveColors.Coral.copy(alpha = 0.16f) else FastLoveColors.SurfaceElevated,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { selected = reason }
                    ) {
                        Text(reason.title, color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.md))
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
                FastLovePrimaryButton("ارسال گزارش", { selected?.let(onSubmit) ?: onDismiss() }, enabled = selected != null)
            }
        }
    }
}
