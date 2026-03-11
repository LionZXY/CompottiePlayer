package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun DropTargetContainer(
    onFileDrop: (ByteArray) -> Unit,
    modifier: Modifier,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    content(false)
}
