package expo.community.modules.musicpicker

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import java.util.ArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Checks whether this [Uri] is a `com.android.providers.media.documents` provider uri
 */
val Uri.isMediaProviderUri
  get() = this.authority == "com.android.providers.media.documents"

/**
 * Checks whether this [Uri] is a `com.android.providers.downloads.documents` provider uri
 */
val Uri.isDownloadsProviderUri
  get() = this.authority == "com.android.providers.downloads.documents"

/**
 * Checks whether this [Uri] points to a media asset that can be queried using
 * ```
 * ContentResolver.query(
 *   MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
 *   projection,
 *   BaseColumns._ID + "=?",
 *   arrayOf(assetIdFromThisUri),
 *   null
 * )
 * ```
 * to get media metadata
 */
val Uri.supportsExternalContentQuery
  get() = isMediaProviderUri || (
      isDownloadsProviderUri
          && DocumentsContract
          .getDocumentId(this)
          .startsWith("msf:")
      )

/**
 * Special implementation of the [use] extension for [Lazy] objects
 * that will call [AutoCloseable.close] only when [Lazy.isInitialized]
 * is true
 */
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

/**
 * [Iterator] implementation for [ClipData] items
 */
val ClipData.items: Iterator<ClipData.Item>
  get() = object : Iterator<ClipData.Item> {
    var index = 0
    val count = itemCount

    override fun hasNext(): Boolean = index < count

    override fun next(): ClipData.Item = getItemAt(index++)
  }

inline fun <reified T> Intent.getListExtra(name: String): List<T>? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    getParcelableArrayListExtra(name, T::class.java)
  } else {
    getParcelableArrayListExtra(name)
  }
}

object Utilities {
  val audioPermissions: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
      arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
