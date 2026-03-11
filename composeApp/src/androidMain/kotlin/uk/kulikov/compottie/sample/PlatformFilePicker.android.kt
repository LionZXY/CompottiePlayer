package uk.kulikov.compottie.sample

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun PlatformFilePicker(
    show: Boolean,
    onResult: (ByteArray?) -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            onResult(bytes)
        } else {
            onResult(null)
        }
    }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(
                arrayOf(
                    "application/json",
                    "application/zip",
                    "application/octet-stream",
                    "*/*"
                )
            )
        }
    }
}
