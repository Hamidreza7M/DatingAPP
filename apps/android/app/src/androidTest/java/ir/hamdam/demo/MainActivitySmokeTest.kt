package ir.hamdam.demo

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Device-level launch check for the first-run authentication screen. */
class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * The auth screen intentionally contains infinite decorative transitions.
     * Compose's default test clock auto-advances while waiting for idleness, which
     * can make a smoke test wait on animations that are never expected to finish.
     * Freeze the Compose clock; Android can still perform its normal layout/draw.
     */
    @Before
    fun freezeInfiniteAnimations() {
        composeRule.mainClock.autoAdvance = false
    }

    @Test
    fun firstLaunchShowsFastLoveAuthenticationScreen() {
        composeRule.waitForIdle()

        composeRule.onNodeWithText("فست لاو", substring = false).assertExists()
        composeRule.onNodeWithText(
            "آشنایی‌های واقعی، از یک شروع ساده",
            substring = false
        ).assertExists()
        composeRule.onNodeWithText("ساخت حساب جدید", substring = false).assertExists()
    }
}
