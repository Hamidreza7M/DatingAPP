package ir.fastlove.app.core.platform

import androidx.compose.runtime.Composable

/** Handles the hardware/system back gesture where the platform has one (Android). No-op on iOS. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
