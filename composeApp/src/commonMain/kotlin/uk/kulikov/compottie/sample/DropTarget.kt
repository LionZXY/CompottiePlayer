package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DropTargetContainer(
    onFileDrop: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (isDragging: Boolean) -> Unit,
)
