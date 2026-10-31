package ir.fastlove.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import ir.fastlove.app.core.data.FastLoveRepository
import ir.fastlove.app.core.data.MockFastLoveRepository
import ir.fastlove.app.core.designsystem.FastLoveTheme
import ir.fastlove.app.feature.onboarding.OnboardingFlow
import ir.fastlove.app.navigation.Navigator

/**
 * Application root. Chooses onboarding vs. the main shell based on the persisted session, and
 * provides the repository + navigator to the whole tree.
 */
@Composable
fun App(
    repository: FastLoveRepository = remember { MockFastLoveRepository() },
    reduceMotion: Boolean = false
) {
    val navigator = remember { Navigator() }
    FastLoveTheme(reduceMotion = reduceMotion) {
        CompositionLocalProvider(
            LocalRepository provides repository,
            LocalNavigator provides navigator
        ) {
            val session by repository.session.collectAsState()
            if (!session.hasProfile) {
                OnboardingFlow(onComplete = { repository.completeProfile(it) })
            } else {
                MainShell()
            }
        }
    }
}
