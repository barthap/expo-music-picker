package expo.community.modules.musicpicker

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition


const val TAG = "expo-music-picker"
private const val INTENT_REQUEST_ID = 2317

/**
 * Depending on user selection, picker can result with content URI/URIs that are pointing
 * to one of three content providers:
 * - `com.android.providers.media.documents/document/audio:56`
 *   points to MediaDocumentsProvider - this is the best option, because we can query
 *   MediaStore external content database for metadata by extracting the ID from the uri
 *   This however requires the READ_EXTERNAL_STORAGE permission
 *   This is the option when user browses 'Audio' section of the picker
 * - `com.android.providers.downloads.documents/document/msf:56 - API 29+`,
 *   `com.android.providers.downloads.documents/document/59 - API < 29`
 *   both point to DownloadStorageProvider which on API29+ has ID compatible with MediaStore database
 *   so we can use the same strategy as above. On older APIs, IDs are different and
 *   we need to get metadata by querying URI directly and using MediaMetadataRetriever
 *
 * - `com.android.externalstorage.documents/document/primary:Download:filename.mp3`
 *   This is the option when user selects music by browsing the filesystem
 *   We must use the MediaMetadataRetriever here
 *
 * TODO: Rename displayName to fileName, check folder/album name in last option - take it from uri
 *
 */
class MusicPickerModule : Module() {
  private var currentPickingContext: PickingContext? = null

  override fun definition() = ModuleDefinition {
    Name("ExpoMusicPicker")

    AsyncFunction("requestPermissionsAsync") { promise: Promise ->
      askForPermissionsWithPermissionsManager(
          appContext.permissions,
          promise,
          Manifest.permission.READ_EXTERNAL_STORAGE
      )
    }

    AsyncFunction("getPermissionsAsync") { promise: Promise ->
      getPermissionsWithPermissionsManager(
          appContext.permissions,
          promise,
          Manifest.permission.READ_EXTERNAL_STORAGE
      )
    }

    AsyncFunction("openMusicLibraryAsync") { options: MusicPickerOptions, promise: Promise ->
      val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "audio/*"
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (options.allowMultipleSelection) {
          putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        // putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/ogg", "application/x-ogg"))
      }

      currentPickingContext = PickingContext(promise, options)
      currentActivity.startActivityForResult(
          Intent.createChooser(intent, "Select Audio"),
          INTENT_REQUEST_ID,
          null
      )
    }

    OnActivityResult { _, (requestCode, resultCode, data) ->
      when (requestCode) {
        INTENT_REQUEST_ID -> processPickerResult(resultCode, data)
      }
    }
  }

  private val currentActivity
    get() = appContext.activityProvider?.currentActivity
        ?: throw MissingCurrentActivityException()

  private val musicMetadataResolver by lazy { MusicMetadataResolver(appContext) }

  private fun getMusicMetadata(uri: Uri) =
      musicMetadataResolver.getMusicMetadata(uri, currentPickingContext?.options)

  /**
   * Processes activity result of music picker.
   */
  private fun processPickerResult(resultCode: Int, intent: Intent?) {
    val resultBundle: Bundle = Bundle().apply {
      if (resultCode == RESULT_CANCELED) {
        putBoolean("cancelled", true)
      } else {
        if (intent == null) throw MissingIntentData()

        putBoolean("cancelled", false)
        val items = arrayListOf<Bundle>()

        if (intent.data != null) {
          // single item
          val metadata = getMusicMetadata(intent.data!!)
          items.add(metadata.toBundle())
        } else if (intent.clipData != null) {
          // multiple items
          for (item in intent.clipData!!.items) {
            val metadata = getMusicMetadata(item.uri)
            items.add(metadata.toBundle())
          }
        }

        if (intent.hasExtra("uris")) {
          val paths = intent.getParcelableArrayListExtra<Uri>("uris") ?: emptyList()
          for (uri in paths) {
            val metadata = getMusicMetadata(uri)
            items.add(metadata.toBundle())
          }
        }

        // For Xiaomi Phones
        if (items.size == 0 && intent.hasExtra("pick-result-data")) {
          val paths = intent.getParcelableArrayListExtra<Uri>("pick-result-data")
              ?: emptyList()
          for (uri in paths) {
            val metadata = getMusicMetadata(uri)
            items.add(metadata.toBundle())
          }
        }

        putParcelableArrayList("items", items)
      }
    }
    currentPickingContext?.promise?.resolve(resultBundle)
    currentPickingContext = null
  }

  private data class PickingContext(
      val promise: Promise,
      val options: MusicPickerOptions
  )
}
