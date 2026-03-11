package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import java.io.File
import java.net.URI
import javax.swing.JFrame

val LocalWindow = staticCompositionLocalOf<JFrame?> { null }

@Composable
actual fun DropTargetContainer(
    onFileDrop: (ByteArray) -> Unit,
    modifier: Modifier,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }
    val currentOnFileDrop by rememberUpdatedState(onFileDrop)
    val window = LocalWindow.current

    DisposableEffect(window) {
        if (window == null) return@DisposableEffect onDispose {}

        val listener = object : DropTargetListener {
            override fun dragEnter(dtde: DropTargetDragEvent) {
                isDragging = true
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            }

            override fun dragOver(dtde: DropTargetDragEvent) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            }

            override fun dropActionChanged(dtde: DropTargetDragEvent) {}

            override fun dragExit(dte: DropTargetEvent) {
                isDragging = false
            }

            override fun drop(dtde: DropTargetDropEvent) {
                isDragging = false
                dtde.acceptDrop(DnDConstants.ACTION_COPY)
                try {
                    val file = extractFile(dtde.transferable)
                    if (file != null) {
                        currentOnFileDrop(file.readBytes())
                    }
                    dtde.dropComplete(true)
                } catch (e: Exception) {
                    dtde.dropComplete(false)
                }
            }
        }

        val dropTarget = DropTarget(window, DnDConstants.ACTION_COPY, listener, true)
        onDispose {
            dropTarget.removeDropTargetListener(listener)
        }
    }

    content(isDragging)
}

private val uriListFlavor = DataFlavor("text/uri-list;class=java.lang.String")

private fun extractFile(transferable: Transferable): File? {
    // Windows / macOS: Java file list flavor
    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        @Suppress("UNCHECKED_CAST")
        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
        return files.firstOrNull()
    }

    // Linux (Nautilus, Dolphin, Thunar, etc.): text/uri-list flavor
    if (transferable.isDataFlavorSupported(uriListFlavor)) {
        val uriList = transferable.getTransferData(uriListFlavor) as String
        val uri = uriList.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && !it.startsWith("#") }
            ?: return null
        return File(URI(uri))
    }

    return null
}
