package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.DragEvent
import org.w3c.dom.events.Event
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
actual fun DropTargetContainer(
    onFileDrop: (ByteArray) -> Unit,
    modifier: Modifier,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }
    val currentOnFileDrop by rememberUpdatedState(onFileDrop)

    DisposableEffect(Unit) {
        val dragOver: (Event) -> Unit = { e ->
            e.preventDefault()
            isDragging = true
        }
        val dragLeave: (Event) -> Unit = { e ->
            e.preventDefault()
            isDragging = false
        }
        val drop: (Event) -> Unit = { e ->
            e.preventDefault()
            isDragging = false
            val dragEvent = e as DragEvent
            val file = dragEvent.dataTransfer?.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val result = reader.result
                    if (result != null) {
                        val buffer = result.asDynamic().unsafeCast<ArrayBuffer>()
                        val int8Array = Int8Array(buffer)
                        val bytes = int8Array.unsafeCast<ByteArray>()
                        currentOnFileDrop(bytes)
                    }
                    Unit
                }
                reader.readAsArrayBuffer(file)
            }
        }

        val body = document.body ?: return@DisposableEffect onDispose {}
        body.addEventListener("dragover", dragOver)
        body.addEventListener("dragleave", dragLeave)
        body.addEventListener("drop", drop)

        onDispose {
            body.removeEventListener("dragover", dragOver)
            body.removeEventListener("dragleave", dragLeave)
            body.removeEventListener("drop", drop)
        }
    }

    content(isDragging)
}
