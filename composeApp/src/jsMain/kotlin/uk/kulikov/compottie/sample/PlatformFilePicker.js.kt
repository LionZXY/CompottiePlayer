package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume

@Composable
actual fun PlatformFilePicker(
    show: Boolean,
    onResult: (ByteArray?) -> Unit,
) {
    LaunchedEffect(show) {
        if (!show) return@LaunchedEffect
        val bytes = pickFileWeb()
        onResult(bytes)
    }
}

private suspend fun pickFileWeb(): ByteArray? = suspendCancellableCoroutine { cont ->
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = ".json,.lottie,.zip"
    input.style.display = "none"
    document.body?.appendChild(input)

    input.onchange = {
        val file = input.files?.get(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val arrayBuffer = reader.result
                if (arrayBuffer != null) {
                    val buffer = arrayBuffer.asDynamic().unsafeCast<ArrayBuffer>()
                    val int8Array = Int8Array(buffer)
                    val bytes = int8Array.unsafeCast<ByteArray>()
                    cont.resume(bytes)
                } else {
                    cont.resume(null)
                }
                Unit
            }
            reader.readAsArrayBuffer(file)
        } else {
            cont.resume(null)
        }
        Unit
    }

    cont.invokeOnCancellation {
        document.body?.removeChild(input)
    }

    input.click()
}
