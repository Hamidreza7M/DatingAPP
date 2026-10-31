package ir.fastlove.app.core.platform

import androidx.compose.runtime.Composable

/** iOS has no hardware back button; navigation back is handled by the in-app top-bar control. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // no-op
}
