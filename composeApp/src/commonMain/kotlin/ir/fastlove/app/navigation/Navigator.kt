package ir.fastlove.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/** Top-level tabs. */
enum class Tab(val label: String) {
    DISCOVER("کشف"),
    LIKES("لایک‌ها"),
    MAP("نقشه"),
    CHATS("گفتگوها"),
    PROFILE("پروفایل من")
}

/** Detail destinations pushed on top of the current tab. */
sealed interface Screen {
    data class PersonDetail(val personId: Int) : Screen
    data class Chat(val personId: Int) : Screen
    data class ScheduledDateDetail(val personId: Int, val proposalId: String) : Screen
    data object ScheduledDates : Screen
    data object Filter : Screen
    data object EditProfile : Screen
    data object PrivacySettings : Screen
    data object Subscription : Screen
    data object PaymentHistory : Screen
    data object BlockedList : Screen
    data object PublicPreview : Screen
}

/**
 * Minimal, predictable navigator: one selected tab plus a detail back stack. Back pops the detail
 * stack first, then is handled by the host (Android system back / iOS swipe handled by the shell).
 */
class Navigator {
    var selectedTab = mutableStateOf(Tab.DISCOVER)
        private set

    val backStack: SnapshotStateList<Screen> = mutableStateListOf()

    val currentScreen: Screen? get() = backStack.lastOrNull()

    fun selectTab(tab: Tab) {
        if (backStack.isNotEmpty()) backStack.clear()
        selectedTab.value = tab
    }

    fun push(screen: Screen) {
        backStack.add(screen)
    }

    /** @return true if something was popped. */
    fun pop(): Boolean {
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    fun popToRoot() {
        backStack.clear()
    }
}
