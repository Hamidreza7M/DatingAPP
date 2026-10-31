package ir.fastlove.app.core.platform

import android.content.Intent
import ir.fastlove.app.core.data.AndroidPlatform

actual fun shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(send, "اشتراک‌گذاری قرار").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AndroidPlatform.appContext.startActivity(chooser)
}
