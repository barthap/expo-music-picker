package expo.community.modules.musicpicker

import android.net.Uri
import android.provider.DocumentsContract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Checks whether provided [Uri] is a `com.android.providers.media.documents` provider uri
 */
val Uri.isMediaProviderUri
    get() = this.authority == "com.android.providers.media.documents"

val Uri.isDownloadsProviderUri
    get() = this.authority == "com.android.providers.downloads.document"

val Uri.supportsExternalContentQuery
    get() = isMediaProviderUri || (
            isDownloadsProviderUri
                    && DocumentsContract
                    .getDocumentId(this)
                    .startsWith("msf:")
            )

@OptIn(ExperimentalContracts::class)
inline fun <T : AutoCloseable, R> Lazy<T>.useLazy(block: (() -> T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    try {
        val valueGetter = { this.value }
        return block(valueGetter)
    } finally {
        if (isInitialized()) {
            value.close()
        }
    }
}