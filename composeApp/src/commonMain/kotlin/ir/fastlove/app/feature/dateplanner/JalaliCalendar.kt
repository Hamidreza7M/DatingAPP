package ir.fastlove.app.feature.dateplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.DateLogic
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.domain.Jalali
import ir.fastlove.app.core.domain.JalaliDate
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.util.persianDigits

/**
 * Persian (Jalali) month grid. Lets the user move between months (no past days, up to +90 days)
 * and pick a day. Emits the chosen [JalaliDate].
 */
@Composable
fun JalaliCalendar(selected: JalaliDate?, onSelect: (JalaliDate) -> Unit) {
    val today = remember { FastLoveClock.currentJalali() }
    val todayEpoch = remember { Jalali.toGregorian(today.year, today.month, today.day).toEpochDays() }
    var viewYear by remember { mutableStateOf(selected?.year ?: today.year) }
    var viewMonth by remember { mutableStateOf(selected?.month ?: today.month) }

    fun stepMonth(delta: Int) {
        var m = viewMonth + delta
        var y = viewYear
        if (m < 1) { m = 12; y-- }
        if (m > 12) { m = 1; y++ }
        viewYear = y; viewMonth = m
    }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconBox(Icons.Filled.ChevronRight) { stepMonth(-1) }
            Text(
                "${FastLoveData.persianMonths[viewMonth - 1]} ${persianDigits(viewYear.toString())}",
                color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconBox(Icons.Filled.ChevronLeft) { stepMonth(1) }
        }
        Spacer(Modifier.size(Spacing.sm))
        Row(Modifier.fillMaxWidth()) {
            FastLoveData.persianWeekdays.forEach { wd ->
                Text(wd.take(1), color = FastLoveColors.TextMuted, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.size(Spacing.xs))

        val daysInMonth = Jalali.daysInMonth(viewYear, viewMonth)
        val leading = persianLeadingBlanks(viewYear, viewMonth)

        val cells = leading + (1..daysInMonth).toList().map { it }
        val rows = (cells + List((7 - cells.size % 7) % 7) { 0 }).chunked(7)
        rows.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (day == 0) {
                            // blank
                        } else {
                            val epoch = Jalali.toGregorian(viewYear, viewMonth, day).toEpochDays()
                            val selectable = DateLogic.isSelectableDay(epoch, todayEpoch)
                            val isSel = selected?.year == viewYear && selected.month == viewMonth && selected.day == day
                            Box(
                                Modifier.size(38.dp).clip(CircleShape)
                                    .background(if (isSel) FastLoveColors.Coral else FastLoveColors.SurfaceElevated.copy(alpha = if (selectable) 1f else 0.3f))
                                    .clickable(enabled = selectable) { onSelect(JalaliDate(viewYear, viewMonth, day)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    persianDigits(day.toString()),
                                    color = when {
                                        isSel -> FastLoveColors.TextPrimary
                                        !selectable -> FastLoveColors.TextMuted
                                        else -> FastLoveColors.TextSecondary
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Number of empty leading cells so day 1 lands under the correct Persian weekday column. */
private fun persianLeadingBlanks(year: Int, month: Int): List<Int> {
    val firstDate = Jalali.toGregorian(year, month, 1)
    val iso = firstDate.dayOfWeek.ordinal + 1 // Mon=1..Sun=7
    val persianIndex = (iso + 1) % 7           // Sat=0..Fri=6
    return List(persianIndex) { 0 }
}

@Composable
private fun IconBox(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(FastLoveColors.SurfaceElevated).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = FastLoveColors.TextPrimary)
    }
}
