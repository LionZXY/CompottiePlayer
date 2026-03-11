package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun PlatformFilePicker(
    show: Boolean,
    onResult: (ByteArray?) -> Unit,
) {
    LaunchedEffect(show) {
        if (!show) return@LaunchedEffect
        val chooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter(
                "Lottie files (*.json, *.lottie, *.zip)",
                "json", "lottie", "zip"
            )
        }
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            onResult(chooser.selectedFile.readBytes())
        } else {
            onResult(null)
        }
    }
}
