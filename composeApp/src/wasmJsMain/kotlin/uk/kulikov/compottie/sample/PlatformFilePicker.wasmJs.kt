package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
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

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsUint8Array(buffer: JsAny): JsAny = js("new Uint8Array(buffer)")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsArrayLength(arr: JsAny): Int = js("arr.length")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsArrayGet(arr: JsAny, index: Int): Int = js("arr[index]")

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
                val result = reader.result
                if (result != null) {
                    val uint8Array = jsUint8Array(result)
                    val length = jsArrayLength(uint8Array)
                    val bytes = ByteArray(length) { i -> jsArrayGet(uint8Array, i).toByte() }
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
