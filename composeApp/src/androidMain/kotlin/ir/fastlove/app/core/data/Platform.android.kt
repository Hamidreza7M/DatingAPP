package ir.fastlove.app.core.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/** Holds the application context so [provideSettings] can build a SharedPreferences store. */
object AndroidPlatform {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

actual fun provideSettings(): Settings {
    val prefs = AndroidPlatform.appContext.getSharedPreferences("fastlove_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
