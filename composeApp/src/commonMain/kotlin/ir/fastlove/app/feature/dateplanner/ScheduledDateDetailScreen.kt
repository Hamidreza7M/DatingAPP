package ir.fastlove.app.feature.dateplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.DateLogic
import ir.fastlove.app.core.domain.Jalali
import ir.fastlove.app.core.model.DateProposalStatus
import ir.fastlove.app.core.platform.shareText
import ir.fastlove.app.core.util.persianDigits

@Composable
fun ScheduledDateDetailScreen(personId: Int, proposalId: String) {
    val repo = repository()
    val nav = navigator()
    val person = remember(personId) { repo.people.firstOrNull { it.id == personId } }
    val proposals by repo.dates(personId).collectAsState()
    val proposal = proposals.firstOrNull { it.id == proposalId }
    var showCounter by remember { mutableStateOf(false) }

    if (person == null || proposal == null) {
        Column { FastLoveTopBar("جزئیات قرار", onBack = { nav.pop() }); Text("این قرار دیگر در دسترس نیست.", color = FastLoveColors.TextMuted, modifier = Modifier.padding(Spacing.gutter)) }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar("جزئیات قرار", onBack = { nav.pop() }, trailing = {
            Icon(Icons.Filled.Share, "اشتراک‌گذاری", tint = FastLoveColors.TextSecondary,
                modifier = Modifier.clickable {
                    shareText("قرار من در فست‌لاو: ${proposal.kind.title} در ${proposal.place} (دمو).")
                }.padding(Spacing.sm).height(22.dp))
        })
        Column(Modifier.padding(Spacing.gutter)) {
            Surface(color = FastLoveColors.SurfaceElevated, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.lg)) {
                    Text("${proposal.kind.emoji} ${proposal.kind.title} با ${person.name}", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    Info("مکان", proposal.place)
                    Info("تاریخ", Jalali.label(Jalali.fromEpochMillis(proposal.scheduledAtMillis)))
                    Info("ساعت", ir.fastlove.app.core.domain.FastLoveClock.timeLabel(proposal.scheduledAtMillis))
                    Info("وضعیت", proposal.status.title)
                    Info("پیشنهاددهنده", if (proposal.proposerIsMe) "من" else person.name)
                }
            }

            SectionLabel("محل قرار روی نقشه")
            Box(Modifier.fillMaxWidth().height(160.dp).background(FastLoveColors.Surface, MaterialTheme.shapes.large), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Place, null, tint = FastLoveColors.Coral, modifier = Modifier.height(40.dp))
                    Text(proposal.place, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.labelMedium)
                    proposal.locationLatitude?.let { lat ->
                        Text(persianDigits("$lat, ${proposal.locationLongitude}"), color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
            if (proposal.status == DateProposalStatus.PENDING || proposal.status == DateProposalStatus.COUNTER) {
                FastLovePrimaryButton("شبیه‌سازی پذیرش طرف مقابل", { repo.updateDate(personId, DateLogic.accept(proposal)) })
                Spacer(Modifier.height(Spacing.sm))
            }
            if (DateLogic.canCounter(proposal)) {
                FastLoveSecondaryButton("پیشنهاد تغییر زمان", { repo.updateDate(personId, proposal.copy(status = DateProposalStatus.COUNTER)); showCounter = true })
                Spacer(Modifier.height(Spacing.sm))
            }
            if (DateLogic.canCancel(proposal)) {
                FastLoveSecondaryButton("لغو قرار", { repo.updateDate(personId, DateLogic.cancel(proposal)) })
                Spacer(Modifier.height(Spacing.sm))
            }
            FastLoveSecondaryButton("اشتراک کارت قرار با فرد مورد اعتماد", {
                shareText("قرار من در فست‌لاو: ${proposal.kind.title} در ${proposal.place}، ${Jalali.label(Jalali.fromEpochMillis(proposal.scheduledAtMillis))} ساعت ${ir.fastlove.app.core.domain.FastLoveClock.timeLabel(proposal.scheduledAtMillis)} (دمو).")
            })
            Spacer(Modifier.height(Spacing.md))
            DemoBadge()
            Spacer(Modifier.height(Spacing.huge))
        }
    }

    if (showCounter) DateProposalDialog(person, onDismiss = { showCounter = false })
}

@Composable
private fun Info(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = FastLoveColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
        Text(value, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
