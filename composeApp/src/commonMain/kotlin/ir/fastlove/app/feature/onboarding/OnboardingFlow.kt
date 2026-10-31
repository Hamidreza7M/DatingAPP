package ir.fastlove.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.fastlove.app.core.designsystem.DemoBadge
import ir.fastlove.app.core.designsystem.FastLoveCard
import ir.fastlove.app.core.designsystem.FastLoveChip
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLovePrimaryButton
import ir.fastlove.app.core.designsystem.FastLoveSecondaryButton
import ir.fastlove.app.core.designsystem.SectionLabel
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.Gender
import ir.fastlove.app.core.model.LookingForGender
import ir.fastlove.app.core.model.RelationGoal
import ir.fastlove.app.core.model.UserProfile
import ir.fastlove.app.core.util.latinDigits
import ir.fastlove.app.core.util.persianDigits

private enum class Step { AUTH, AGE, GENDER, LOOKING, GOAL, BASICS, INTERESTS, JOB, PROMPTS, PREVIEW }

/**
 * Stepwise demo onboarding. No real OTP/SMS is sent — login is simulated. Produces a [UserProfile]
 * and hands it to [onComplete].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingFlow(onComplete: (UserProfile) -> Unit) {
    var step by remember { mutableStateOf(Step.AUTH) }
    var draft by remember { mutableStateOf(UserProfile(birthYearPersian = FastLoveClock.currentPersianYear() - 25)) }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(FastLoveColors.Background, FastLoveColors.Surface)))
            .safeDrawingPadding()
            .padding(horizontal = Spacing.gutter)
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(Spacing.xl))
            Header(step)
            Spacer(Modifier.height(Spacing.xl))

            AnimatedContent(
                targetState = step,
                transitionSpec = { (fadeIn() togetherWith fadeOut()) },
                label = "onboarding-step"
            ) { s ->
                when (s) {
                    Step.AUTH -> AuthStep(
                        onLoginDemo = { draft = draft.copy(phone = "۰۹۱۲۰۰۰۰۰۰۰"); onComplete(demoUser()) },
                        onCreate = { phone -> draft = draft.copy(phone = phone); step = Step.AGE }
                    )
                    Step.AGE -> AgeStep(onConfirm = { step = Step.GENDER })
                    Step.GENDER -> GenderStep(draft.gender) { draft = draft.copy(gender = it); step = Step.LOOKING }
                    Step.LOOKING -> LookingStep(draft.lookingFor) { draft = draft.copy(lookingFor = it); step = Step.GOAL }
                    Step.GOAL -> GoalStep(draft.goal) { draft = draft.copy(goal = it); step = Step.BASICS }
                    Step.BASICS -> BasicsStep(draft, onNext = { draft = it; step = Step.INTERESTS })
                    Step.INTERESTS -> InterestsStep(draft.interests.toSet()) {
                        draft = draft.copy(interests = it.toList()); step = Step.JOB
                    }
                    Step.JOB -> JobStep(draft) { draft = it; step = Step.PROMPTS }
                    Step.PROMPTS -> PromptsStep(draft) { draft = it; step = Step.PREVIEW }
                    Step.PREVIEW -> PreviewStep(draft, onFinish = { onComplete(draft) }, onEdit = { step = Step.BASICS })
                }
            }
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}

private fun demoUser(): UserProfile = UserProfile(
    name = "کاربر دمو",
    birthYearPersian = FastLoveClock.currentPersianYear() - 27,
    gender = Gender.OTHER,
    lookingFor = LookingForGender.BOTH,
    goal = RelationGoal.FRIENDSHIP,
    job = "آزاد",
    city = "تهران",
    about = "اینجا برای کشف و آشنایی هستم.",
    interests = listOf("کافه", "موسیقی", "سفر"),
    idealFirstDate = "یک کافه دنج و گفت‌وگوی طولانی.",
    makesMeSmile = "موسیقی خوب و آدم‌های مهربان.",
    relationshipValue = "صداقت و احترام.",
    phone = "۰۹۱۲۰۰۰۰۰۰۰"
)

@Composable
private fun Header(step: Step) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).clip(CircleShape)
                .background(Brush.linearGradient(FastLoveColors.warmGradient)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Favorite, null, tint = FastLoveColors.TextPrimary) }
        Spacer(Modifier.width(Spacing.md))
        Column {
            Text("فست‌لاو", color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("آشنایی هدفمند", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.weight(1f))
        DemoBadge()
    }
    Spacer(Modifier.height(Spacing.lg))
    val progress = (step.ordinal + 1f) / Step.entries.size
    Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(FastLoveColors.SurfaceElevated)) {
        Box(Modifier.fillMaxWidth(progress).height(6.dp).clip(CircleShape).background(Brush.horizontalGradient(FastLoveColors.brandGradient)))
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Text(title, color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(Spacing.xs))
    Text(subtitle, color = FastLoveColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(Spacing.xl))
}

@Composable
private fun AuthStep(onLoginDemo: () -> Unit, onCreate: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    Column {
        StepTitle("خوش آمدی", "برای شروع وارد شو یا پروفایل جدید بساز. در این دمو پیامکی ارسال نمی‌شود.")
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = latinDigits(it).filter { c -> c.isDigit() }.take(11) },
            label = { Text("شماره موبایل (اختیاری در دمو)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.lg))
        FastLovePrimaryButton("ساخت پروفایل جدید", { onCreate(persianDigits(phone.ifBlank { "09120000000" })) }, leading = Icons.Filled.ArrowForward)
        Spacer(Modifier.height(Spacing.md))
        FastLoveSecondaryButton("ورود سریع با حساب دمو", onLoginDemo)
    }
}

@Composable
private fun AgeStep(onConfirm: () -> Unit) {
    Column {
        StepTitle("تأیید سن", "فست‌لاو فقط برای افراد بالای ۱۸ سال است.")
        FastLoveCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg)) {
                Text("با ادامه تأیید می‌کنی که ۱۸ سال یا بیشتر داری.", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(Spacing.lg))
        FastLovePrimaryButton("۱۸ سال به بالا هستم", onConfirm)
    }
}

@Composable
private fun GenderStep(selected: Gender, onSelect: (Gender) -> Unit) {
    Column {
        StepTitle("جنسیت تو", "این اطلاعات برای پیشنهادهای بهتر استفاده می‌شود.")
        val options = listOf(Gender.FEMALE to "زن", Gender.MALE to "مرد", Gender.OTHER to "دیگر")
        options.forEach { (g, label) ->
            SelectRow(label, selected == g) { onSelect(g) }
            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

@Composable
private fun LookingStep(selected: LookingForGender?, onSelect: (LookingForGender) -> Unit) {
    Column {
        StepTitle("دنبال آشنایی با چه کسی هستی؟", "می‌توانی بعداً تغییرش دهی.")
        LookingForGender.entries.forEach { lf ->
            SelectRow(lf.title, selected == lf) { onSelect(lf) }
            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

@Composable
private fun GoalStep(selected: RelationGoal, onSelect: (RelationGoal) -> Unit) {
    Column {
        StepTitle("هدف تو از رابطه", "صادقانه انتخاب کن؛ به مچ بهتر کمک می‌کند.")
        RelationGoal.entries.forEach { goal ->
            SelectRow(goal.title, selected == goal) { onSelect(goal) }
            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicsStep(draft: UserProfile, onNext: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf(draft.name) }
    var city by remember { mutableStateOf(draft.city) }
    var birthYear by remember { mutableStateOf(draft.birthYearPersian) }
    Column {
        StepTitle("کمی درباره‌ی تو", "نام، سال تولد و شهر.")
        OutlinedTextField(name, { name = it }, label = { Text("نام") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(Spacing.md))
        SectionLabel("سال تولد (شمسی)")
        Row(verticalAlignment = Alignment.CenterVertically) {
            FastLoveSecondaryButton("−", { birthYear-- }, modifier = Modifier.width(64.dp))
            Text(
                persianDigits(birthYear.toString()),
                color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
            FastLoveSecondaryButton("+", { birthYear++ }, modifier = Modifier.width(64.dp))
        }
        Spacer(Modifier.height(Spacing.md))
        SectionLabel("شهر")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FastLoveData.iranianCities.take(12).forEach { c ->
                FastLoveChip(c, city == c, { city = c })
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        FastLovePrimaryButton(
            "ادامه",
            { onNext(draft.copy(name = name.trim(), city = city, birthYearPersian = birthYear)) },
            enabled = name.isNotBlank() && city.isNotBlank()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsStep(selected: Set<String>, onNext: (Set<String>) -> Unit) {
    var set by remember { mutableStateOf(selected) }
    Column {
        StepTitle("علاقه‌هایت", "حداقل سه مورد برای مچ بهتر انتخاب کن.")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FastLoveData.availableInterests.forEach { i ->
                FastLoveChip(i, i in set, { set = if (i in set) set - i else set + i })
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        FastLovePrimaryButton("ادامه (${persianDigits(set.size.toString())})", { onNext(set) }, enabled = set.size >= 3)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobStep(draft: UserProfile, onNext: (UserProfile) -> Unit) {
    var job by remember { mutableStateOf(draft.job) }
    var about by remember { mutableStateOf(draft.about) }
    Column {
        StepTitle("شغل و معرفی", "یک معرفی کوتاه و صمیمی بنویس.")
        SectionLabel("شغل")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FastLoveData.availableJobs.forEach { j -> FastLoveChip(j, job == j, { job = j }) }
        }
        Spacer(Modifier.height(Spacing.md))
        OutlinedTextField(about, { about = it.take(240) }, label = { Text("درباره‌ی من") }, modifier = Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(Spacing.xl))
        FastLovePrimaryButton("ادامه", { onNext(draft.copy(job = job, about = about.trim())) }, enabled = job.isNotBlank())
    }
}

@Composable
private fun PromptsStep(draft: UserProfile, onNext: (UserProfile) -> Unit) {
    var ideal by remember { mutableStateOf(draft.idealFirstDate) }
    var smile by remember { mutableStateOf(draft.makesMeSmile) }
    var value by remember { mutableStateOf(draft.relationshipValue) }
    Column {
        StepTitle("سه جمله‌ی کوتاه", "این‌ها پروفایلت را جذاب‌تر می‌کنند (اختیاری).")
        OutlinedTextField(ideal, { ideal = it.take(120) }, label = { Text(FastLoveData.profilePrompts[0]) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(Spacing.md))
        OutlinedTextField(smile, { smile = it.take(120) }, label = { Text(FastLoveData.profilePrompts[1]) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(Spacing.md))
        OutlinedTextField(value, { value = it.take(120) }, label = { Text(FastLoveData.profilePrompts[2]) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(Spacing.xl))
        FastLovePrimaryButton("دیدن پیش‌نمایش", {
            onNext(draft.copy(idealFirstDate = ideal.trim(), makesMeSmile = smile.trim(), relationshipValue = value.trim()))
        })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewStep(draft: UserProfile, onFinish: () -> Unit, onEdit: () -> Unit) {
    Column {
        StepTitle("پیش‌نمایش پروفایل", "این چیزی است که دیگران می‌بینند.")
        FastLoveCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg)) {
                Text(
                    "${draft.name}، ${persianDigits(draft.ageFor(FastLoveClock.currentPersianYear()).toString())}",
                    color = FastLoveColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge
                )
                Text("${draft.job} · ${draft.city}", color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Spacing.sm))
                Text(draft.about, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Spacing.md))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    draft.interests.forEach { FastLoveChip(it, true, {}) }
                }
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        FastLovePrimaryButton("ورود به کشف", onFinish)
        Spacer(Modifier.height(Spacing.sm))
        FastLoveSecondaryButton("ویرایش اطلاعات", onEdit)
    }
}

@Composable
private fun SelectRow(label: String, selected: Boolean, onClick: () -> Unit) {
    FastLoveCard(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) FastLoveColors.Coral.copy(alpha = 0.16f) else FastLoveColors.SurfaceElevated
    ) {
        Row(Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                color = if (selected) FastLoveColors.Coral else FastLoveColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.size(22.dp).clip(CircleShape).background(if (selected) FastLoveColors.Coral else FastLoveColors.SurfaceHigh))
        }
    }
}
