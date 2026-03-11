package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformFilePicker(
    show: Boolean,
    onResult: (ByteArray?) -> Unit,
)
