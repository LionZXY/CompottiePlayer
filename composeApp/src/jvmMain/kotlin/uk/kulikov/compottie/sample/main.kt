package uk.kulikov.compottie.sample

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Lottie Player",
    ) {
        CompositionLocalProvider(LocalWindow provides window) {
            App()
        }
    }
}
