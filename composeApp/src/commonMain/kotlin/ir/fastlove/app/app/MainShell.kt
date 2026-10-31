package ir.fastlove.app.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.platform.PlatformBackHandler
import ir.fastlove.app.feature.chat.ChatScreen
import ir.fastlove.app.feature.chat.ChatsListScreen
import ir.fastlove.app.feature.dateplanner.ScheduledDateDetailScreen
import ir.fastlove.app.feature.dateplanner.ScheduledDatesScreen
import ir.fastlove.app.feature.discover.DiscoverScreen
import ir.fastlove.app.feature.discover.FilterScreen
import ir.fastlove.app.feature.discover.PersonDetailScreen
import ir.fastlove.app.feature.likes.LikesScreen
import ir.fastlove.app.feature.map.MapScreen
import ir.fastlove.app.feature.profile.EditProfileScreen
import ir.fastlove.app.feature.profile.MyProfileScreen
import ir.fastlove.app.feature.profile.PublicPreviewScreen
import ir.fastlove.app.feature.settings.BlockedListScreen
import ir.fastlove.app.feature.settings.PrivacySettingsScreen
import ir.fastlove.app.feature.subscription.PaymentHistoryScreen
import ir.fastlove.app.feature.subscription.SubscriptionScreen
import ir.fastlove.app.navigation.Screen
import ir.fastlove.app.navigation.Tab

@Composable
fun MainShell() {
    val nav = navigator()
    val repo = repository()
    val interactions by repo.interactions.collectAsState()
    val detail = nav.currentScreen

    PlatformBackHandler(enabled = detail != null) { nav.pop() }

    Box(Modifier.fillMaxSize().background(FastLoveColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxSize()) {
                when (nav.selectedTab.value) {
                    Tab.DISCOVER -> DiscoverScreen()
                    Tab.LIKES -> LikesScreen()
                    Tab.MAP -> MapScreen()
                    Tab.CHATS -> ChatsListScreen()
                    Tab.PROFILE -> MyProfileScreen()
                }
            }
            BottomBar(
                selected = nav.selectedTab.value,
                matchCount = interactions.matchedIds.size,
                onSelect = { nav.selectTab(it) }
            )
        }

        if (detail != null) {
            Box(Modifier.fillMaxSize().background(FastLoveColors.Background)) {
                DetailHost(detail)
            }
        }

        val session by repo.session.collectAsState()
        if (!session.tourSeen && detail == null && nav.selectedTab.value == Tab.DISCOVER) {
            ir.fastlove.app.feature.onboarding.ProductTour(onFinish = { repo.markTourSeen() })
        }
    }
}

@Composable
private fun DetailHost(screen: Screen) {
    when (screen) {
        is Screen.PersonDetail -> PersonDetailScreen(screen.personId)
        is Screen.Chat -> ChatScreen(screen.personId)
        is Screen.ScheduledDateDetail -> ScheduledDateDetailScreen(screen.personId, screen.proposalId)
        Screen.ScheduledDates -> ScheduledDatesScreen()
        Screen.Filter -> FilterScreen()
        Screen.EditProfile -> EditProfileScreen()
        Screen.PrivacySettings -> PrivacySettingsScreen()
        Screen.Subscription -> SubscriptionScreen()
        Screen.PaymentHistory -> PaymentHistoryScreen()
        Screen.BlockedList -> BlockedListScreen()
        Screen.PublicPreview -> PublicPreviewScreen()
    }
}

@Composable
private fun BottomBar(selected: Tab, matchCount: Int, onSelect: (Tab) -> Unit) {
    NavigationBar(
        containerColor = FastLoveColors.Surface,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Tab.entries.forEach { tab ->
            val icon = when (tab) {
                Tab.DISCOVER -> Icons.Filled.Explore
                Tab.LIKES -> Icons.Filled.Favorite
                Tab.MAP -> Icons.Filled.Map
                Tab.CHATS -> Icons.Filled.Chat
                Tab.PROFILE -> Icons.Filled.Person
            }
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FastLoveColors.Coral,
                    selectedTextColor = FastLoveColors.Coral,
                    indicatorColor = FastLoveColors.Coral.copy(alpha = 0.14f),
                    unselectedIconColor = FastLoveColors.TextMuted,
                    unselectedTextColor = FastLoveColors.TextMuted
                )
            )
        }
    }
}
