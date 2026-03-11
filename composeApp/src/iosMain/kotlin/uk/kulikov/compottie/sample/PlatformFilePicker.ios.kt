package uk.kulikov.compottie.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun PlatformFilePicker(
    show: Boolean,
    onResult: (ByteArray?) -> Unit,
) {
    LaunchedEffect(show) {
        if (!show) return@LaunchedEffect
        showIOSFilePicker(onResult)
    }
}

private fun showIOSFilePicker(onResult: (ByteArray?) -> Unit) {
    val types = listOf(UTTypeJSON, UTTypeData)
    val picker = UIDocumentPickerViewController(forOpeningContentTypes = types)
    picker.allowsMultipleSelection = false

    val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>
        ) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            if (url != null) {
                url.startAccessingSecurityScopedResource()
                val data = NSData.dataWithContentsOfURL(url)
                url.stopAccessingSecurityScopedResource()
                if (data != null) {
                    onResult(data.toByteArray())
                } else {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            onResult(null)
        }
    }

    picker.delegate = delegate

    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVC?.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}
