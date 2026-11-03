package ir.hamdam.demo

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/** Basic device-level launch check for the first-run authentication screen. */
class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun firstLaunchShowsFastLoveAuthenticationScreen() {
        composeRule.onNodeWithText("فست لاو").assertExists()
        composeRule.onNodeWithText("آشنایی‌های واقعی، از یک شروع ساده").assertExists()
    }
}
