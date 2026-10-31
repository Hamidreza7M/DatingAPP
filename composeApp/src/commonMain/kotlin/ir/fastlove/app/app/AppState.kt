package ir.fastlove.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import ir.fastlove.app.core.data.FastLoveRepository
import ir.fastlove.app.navigation.Navigator

/** Repository available to all features without prop-drilling. */
val LocalRepository = staticCompositionLocalOf<FastLoveRepository> {
    error("LocalRepository not provided")
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator not provided")
}

@Composable
fun repository(): FastLoveRepository = LocalRepository.current

@Composable
fun navigator(): Navigator = LocalNavigator.current
