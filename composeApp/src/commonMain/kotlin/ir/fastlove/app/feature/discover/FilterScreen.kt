package ir.fastlove.app.feature.discover

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.FilterState
import ir.fastlove.app.core.model.RelationGoal
import ir.fastlove.app.core.util.persianDigits

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterScreen() {
    val repo = repository()
    val nav = navigator()
    val current by repo.filter.collectAsState()
    var draft by remember { mutableStateOf(current) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar("فیلترها", onBack = { nav.pop() })
        Column(Modifier.padding(Spacing.gutter)) {
            SectionLabel("بازه سنی: ${persianDigits(draft.minAge.toString())} تا ${persianDigits(draft.maxAge.toString())}")
            RangeSlider(
                value = draft.minAge.toFloat()..draft.maxAge.toFloat(),
                onValueChange = { draft = draft.copy(minAge = it.start.toInt(), maxAge = it.endInclusive.toInt()) },
                valueRange = 18f..70f,
                colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = FastLoveColors.Coral, activeTrackColor = FastLoveColors.Coral)
            )

            SectionLabel("حداکثر فاصله: ${persianDigits(draft.maxDistance.toString())} کیلومتر")
            Slider(
                value = draft.maxDistance.toFloat(),
                onValueChange = { draft = draft.copy(maxDistance = it.toInt()) },
                valueRange = 1f..100f,
                colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = FastLoveColors.Coral, activeTrackColor = FastLoveColors.Coral)
            )

            SectionLabel("هدف رابطه")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                RelationGoal.entries.forEach { g ->
                    FastLoveChip(g.title, g in draft.goals, {
                        draft = draft.copy(goals = if (g in draft.goals) draft.goals - g else draft.goals + g)
                    })
                }
            }

            SectionLabel("شهرها")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FastLoveData.iranianCities.take(12).forEach { c ->
                    FastLoveChip(c, c in draft.cities, {
                        draft = draft.copy(cities = if (c in draft.cities) draft.cities - c else draft.cities + c)
                    })
                }
            }

            SectionLabel("علاقه‌ها")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FastLoveData.availableInterests.forEach { i ->
                    FastLoveChip(i, i in draft.interests, {
                        draft = draft.copy(interests = if (i in draft.interests) draft.interests - i else draft.interests + i)
                    })
                }
            }

            Spacer(Modifier.height(Spacing.xl))
            FastLovePrimaryButton("اعمال فیلتر", { repo.applyFilter(draft); nav.pop() })
            Spacer(Modifier.height(Spacing.sm))
            FastLoveSecondaryButton("پاک‌کردن همه", { draft = FilterState() })
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}
