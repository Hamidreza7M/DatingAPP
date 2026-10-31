package ir.fastlove.app

import androidx.compose.ui.window.ComposeUIViewController
import ir.fastlove.app.app.App
import platform.UIKit.UIViewController

/** iOS entry point consumed by SwiftUI (see iosApp/iosApp/ContentView.swift). */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}
