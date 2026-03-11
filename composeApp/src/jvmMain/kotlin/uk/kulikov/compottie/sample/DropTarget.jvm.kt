package uk.kulikov.compottie.sample

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import java.io.File
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun DropTargetContainer(
    onFileDrop: (ByteArray) -> Unit,
    modifier: Modifier,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }
    val currentOnFileDrop by rememberUpdatedState(onFileDrop)

    val target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
                val dragData = event.dragData()
                if (dragData is DragData.FilesList) {
                    val uri = dragData.readFiles().firstOrNull() ?: return false
                    try {
                        val file = File(URI(uri))
                        currentOnFileDrop(file.readBytes())
                        return true
                    } catch (_: Exception) {
                        return false
                    }
                }
                return false
            }

            override fun onStarted(event: DragAndDropEvent) {
                isDragging = true
            }

            override fun onEntered(event: DragAndDropEvent) {
                isDragging = true
            }

            override fun onExited(event: DragAndDropEvent) {
                isDragging = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragging = false
            }
        }
    }

    Box(
        modifier = modifier.dragAndDropTarget(
            shouldStartDragAndDrop = { true },
            target = target,
        ),
    ) {
        content(isDragging)
    }
}
