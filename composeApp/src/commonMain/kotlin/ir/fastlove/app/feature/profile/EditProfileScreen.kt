package ir.fastlove.app.feature.profile

import androidx.compose.foundation.ExperimentalLayoutApi
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
import androidx.compose.material3.OutlinedTextField
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
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.LookingForGender
import ir.fastlove.app.core.model.RelationGoal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen() {
    val repo = repository()
    val nav = navigator()
    val user by repo.user.collectAsState()
    var draft by remember { mutableStateOf(user) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FastLoveTopBar("ویرایش پروفایل", onBack = { nav.pop() })
        Column(Modifier.padding(Spacing.gutter)) {
            OutlinedTextField(draft.name, { draft = draft.copy(name = it) }, label = { Text("نام") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(draft.job, { draft = draft.copy(job = it) }, label = { Text("شغل") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(draft.about, { draft = draft.copy(about = it.take(240)) }, label = { Text("درباره‌ی من") }, modifier = Modifier.fillMaxWidth().height(110.dp))

            SectionLabel("شهر")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FastLoveData.iranianCities.take(12).forEach { c -> FastLoveChip(c, draft.city == c, { draft = draft.copy(city = c) }) }
            }

            SectionLabel("هدف رابطه")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                RelationGoal.entries.forEach { g -> FastLoveChip(g.title, draft.goal == g, { draft = draft.copy(goal = g) }) }
            }

            SectionLabel("دنبال آشنایی با")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LookingForGender.entries.forEach { lf -> FastLoveChip(lf.title, draft.lookingFor == lf, { draft = draft.copy(lookingFor = lf) }) }
            }

            SectionLabel("علاقه‌ها")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FastLoveData.availableInterests.forEach { i ->
                    FastLoveChip(i, i in draft.interests, {
                        draft = draft.copy(interests = if (i in draft.interests) draft.interests - i else draft.interests + i)
                    })
                }
            }

            SectionLabel("شبکه‌های اجتماعی (اختیاری)")
            OutlinedTextField(draft.instagram, { draft = draft.copy(instagram = it) }, label = { Text("اینستاگرام") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(draft.spotify, { draft = draft.copy(spotify = it) }, label = { Text("اسپاتیفای") }, modifier = Modifier.fillMaxWidth())

            SectionLabel("سه جمله‌ی کوتاه")
            OutlinedTextField(draft.idealFirstDate, { draft = draft.copy(idealFirstDate = it.take(120)) }, label = { Text(FastLoveData.profilePrompts[0]) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(draft.makesMeSmile, { draft = draft.copy(makesMeSmile = it.take(120)) }, label = { Text(FastLoveData.profilePrompts[1]) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(draft.relationshipValue, { draft = draft.copy(relationshipValue = it.take(120)) }, label = { Text(FastLoveData.profilePrompts[2]) }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(Spacing.xl))
            FastLovePrimaryButton("ذخیره", { repo.updateProfile(draft); nav.pop() })
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}
