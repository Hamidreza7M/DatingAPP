package ir.fastlove.app.feature.dateplanner

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.domain.Jalali
import ir.fastlove.app.core.domain.JalaliDate
import ir.fastlove.app.core.domain.TimeSlot
import ir.fastlove.app.core.model.DateKind
import ir.fastlove.app.core.model.DateProposal
import ir.fastlove.app.core.model.DateProposalStatus
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.util.Ids
import ir.fastlove.app.core.util.persianDigits

private enum class DateStep { KIND, PLACE, DATE, TIME, SUMMARY }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateProposalDialog(person: Person, onDismiss: () -> Unit) {
    val repo = repository()
    var step by remember { mutableStateOf(DateStep.KIND) }
    var kind by remember { mutableStateOf<DateKind?>(null) }
    var place by remember { mutableStateOf<DatePlace?>(null) }
    var date by remember { mutableStateOf<JalaliDate?>(null) }
    var slot by remember { mutableStateOf<TimeSlot?>(null) }
    var hour by remember { mutableStateOf(18) }
    var minute by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = FastLoveColors.Surface, modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 620.dp)) {
            Column(Modifier.padding(Spacing.xl).verticalScroll(rememberScrollState())) {
                Row {
                    Text("پیشنهاد قرار با ${person.name}", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    DemoBadge()
                }
                Spacer(Modifier.height(Spacing.lg))

                when (step) {
                    DateStep.KIND -> {
                        SectionLabel("نوع قرار")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            DateKind.entries.forEach { k ->
                                FastLoveChip("${k.emoji} ${k.title}", kind == k, { kind = k })
                            }
                        }
                        Spacer(Modifier.height(Spacing.xl))
                        FastLovePrimaryButton("ادامه", { step = DateStep.PLACE }, enabled = kind != null)
                    }
                    DateStep.PLACE -> {
                        SectionLabel("انتخاب مکان")
                        DatePlaces.forKind(kind!!).forEach { p ->
                            PlaceRow(p.name, place?.name == p.name) { place = p }
                        }
                        Spacer(Modifier.height(Spacing.xl))
                        FastLovePrimaryButton("ادامه", { step = DateStep.DATE }, enabled = place != null)
                        BackLine { step = DateStep.KIND }
                    }
                    DateStep.DATE -> {
                        SectionLabel("انتخاب تاریخ (شمسی)")
                        JalaliCalendar(date) { date = it }
                        Spacer(Modifier.height(Spacing.lg))
                        FastLovePrimaryButton("ادامه", { step = DateStep.TIME }, enabled = date != null)
                        BackLine { step = DateStep.PLACE }
                    }
                    DateStep.TIME -> {
                        SectionLabel("بازه‌ی زمانی")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            TimeSlot.entries.forEach { s ->
                                FastLoveChip("${s.title} (${persianDigits(s.startHour.toString())}–${persianDigits(s.endHour.toString())})", slot == s, {
                                    slot = s; hour = s.startHour; minute = 0
                                })
                            }
                        }
                        Spacer(Modifier.height(Spacing.lg))
                        SectionLabel("ساعت دقیق")
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Stepper(persianDigits(hour.toString().padStart(2, '0'))) { delta -> hour = (hour + delta + 24) % 24 }
                            Text(" : ", color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.headlineMedium)
                            Stepper(persianDigits(minute.toString().padStart(2, '0'))) { delta -> minute = (minute + delta * 15 + 60) % 60 }
                        }
                        Spacer(Modifier.height(Spacing.xl))
                        FastLovePrimaryButton("ادامه", { step = DateStep.SUMMARY }, enabled = slot != null)
                        BackLine { step = DateStep.DATE }
                    }
                    DateStep.SUMMARY -> {
                        val millis = Jalali.toEpochMillis(date!!.year, date!!.month, date!!.day, hour, minute)
                        SectionLabel("خلاصه")
                        SummaryRow("نوع", "${kind!!.emoji} ${kind!!.title}")
                        SummaryRow("مکان", place!!.name)
                        SummaryRow("تاریخ", Jalali.label(date!!))
                        SummaryRow("ساعت", persianDigits("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"))
                        Spacer(Modifier.height(Spacing.sm))
                        Text("پذیرش طرف مقابل در این دمو شبیه‌سازی می‌شود.", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(Spacing.lg))
                        FastLovePrimaryButton("ارسال پیشنهاد", {
                            repo.proposeDate(
                                DateProposal(
                                    id = Ids.proposalId(),
                                    personId = person.id,
                                    kind = kind!!,
                                    place = place!!.name,
                                    scheduledAtMillis = millis,
                                    proposerIsMe = true,
                                    status = DateProposalStatus.PENDING,
                                    createdAtMillis = FastLoveClock.nowMillis(),
                                    locationLatitude = place!!.latitude,
                                    locationLongitude = place!!.longitude
                                )
                            )
                            onDismiss()
                        })
                        BackLine { step = DateStep.TIME }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) FastLoveColors.Coral.copy(alpha = 0.16f) else FastLoveColors.SurfaceElevated,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable(onClick = onClick)
    ) {
        Text(name, color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.md))
    }
}

@Composable
private fun Stepper(value: String, onStep: (Int) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        FastLoveSecondaryButton("−", { onStep(-1) }, modifier = Modifier.width(56.dp))
        Text(value, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.width(72.dp))
        FastLoveSecondaryButton("+", { onStep(1) }, modifier = Modifier.width(56.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = FastLoveColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(72.dp))
        Text(value, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BackLine(onBack: () -> Unit) {
    Spacer(Modifier.height(Spacing.sm))
    Text("بازگشت", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable(onClick = onBack).padding(Spacing.sm))
}
