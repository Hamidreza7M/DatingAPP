package ir.fastlove.app.core.data

import com.russhwolf.settings.Settings

/** Platform-provided key-value store (SharedPreferences on Android, NSUserDefaults on iOS). */
expect fun provideSettings(): Settings
