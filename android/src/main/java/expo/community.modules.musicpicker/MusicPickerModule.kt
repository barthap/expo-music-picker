package expo.community.modules.musicpicker

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import expo.modules.core.utilities.ifNull
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import java.io.File


const val TAG = "expo-music-picker"
private const val INTENT_REQUEST_ID = 2317

/*
3 providers:
- com.android.providers.media.documents/document/audio:56
- com.android.providers.downloads.documents/document/msf:56 - new androids 29, media store compatible
- com.android.providers.downloads.documents/document/59
- com.android.externalstorage.documents/document/primary:Download:filename.mp3 - this is path

 useful: https://android.googlesource.com/platform/packages/providers/DownloadProvider/+/refs/heads/master/src/com/android/providers/downloads/MediaStoreDownloadsHelper.java
 */

class MusicPickerModule : Module() {
  private var currentPickingContext: PickingContext? = null

  private val hasGrantedPermission: Boolean
    get() = appContext.permissions
            ?.hasGrantedPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            ?: false

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
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
    get() = appContext.activityProvider?.currentActivity ?: throw MissingCurrentActivityException()

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
          val audioInfo = getAudioInfo(intent.data!!)
          items.add(audioInfo.toBundle())
        } else if (intent.clipData != null) {
          // multiple items
          val clipData: ClipData = intent.clipData!!
          for (i in 0 until clipData.itemCount) {
            val item: ClipData.Item = clipData.getItemAt(i)
            val audioInfo = getAudioInfo(item.uri)
            items.add(audioInfo.toBundle())
          }
        }

        if (intent.hasExtra("uris")) {
          val paths = intent.getParcelableArrayListExtra<Uri>("uris")
          for (i in 0 until paths!!.size) {
            val audioInfo = getAudioInfo(paths[i])
            items.add(audioInfo.toBundle())
          }
        }

        // For Xiaomi Phones
        if (items.size == 0 && intent.hasExtra("pick-result-data")) {
          val paths = intent.getParcelableArrayListExtra<Uri>("pick-result-data")
          for (i in 0 until paths!!.size) {
            val audioInfo = getAudioInfo(paths[i])
            items.add(audioInfo.toBundle())
          }
        }

        putParcelableArrayList("items", items)
      }
    }
    currentPickingContext?.promise?.resolve(resultBundle)
    currentPickingContext = null

  }

  private fun getMediaStoreInfo(uri: Uri): Music? {
    val rawId = DocumentsContract.getDocumentId(uri)
    val assetId = if (rawId.contains(':')) rawId.split(':')[1] else rawId

    val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME
    } else {
      MediaStore.Audio.AudioColumns.DATA
    }

    val projection = arrayOf(
            MediaStore.Audio.AudioColumns.ARTIST, // 0
            MediaStore.Audio.AudioColumns.YEAR, // 1
            MediaStore.Audio.AudioColumns.TRACK, // 2
            MediaStore.Audio.AudioColumns.TITLE, // 3
            MediaStore.Audio.AudioColumns.DISPLAY_NAME, // 4,
            MediaStore.Audio.AudioColumns.DURATION, //5,
            MediaStore.Audio.AudioColumns.ALBUM, // 6
            MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
            pathColumn, // 8
            MediaStore.Audio.AudioColumns._ID, // 9
            MediaStore.MediaColumns.DATE_MODIFIED // 10
    )

    return appContext.reactContext!!.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            BaseColumns._ID + "=?",
            arrayOf(assetId),
            null
    )?.use { cursor ->
      if (!cursor.moveToFirst()) {
        return@use null
      }

      val artistIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
      val yearIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR)
      val trackIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
      val titleIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
      val displayNameIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
      val durationIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
      val albumIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
      val albumIdIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
      val relativePathIndex =
              cursor.getColumnIndexOrThrow(pathColumn)
      val idIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
      val dateAddedIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)


      val audioId = cursor.getLong(idIndex)
      val audioArtist = cursor.getString(artistIndex)
      val audioYear = cursor.getInt(yearIndex)
      val audioTrack = cursor.getInt(trackIndex)
      val audioTitle = cursor.getString(titleIndex)
      val audioDisplayName = cursor.getString(displayNameIndex)
      val audioDuration = cursor.getLong(durationIndex) / 1000.0
      val audioAlbum = cursor.getString(albumIndex)
      val albumId = cursor.getLong(albumIdIndex)
      val audioRelativePath = cursor.getString(relativePathIndex)
      val audioDateAdded = cursor.getInt(dateAddedIndex)

      val audioFolderName =
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                audioRelativePath ?: "/"
              } else {
                File(audioRelativePath).parentFile?.name?.takeIf { it != "0" } ?: "/"
              }

      return@use Music(
              uri = uri,
              artist = audioArtist,
              year = audioYear,
              track = audioTrack,
              title = audioTitle,
              displayName = audioDisplayName,
              duration = audioDuration,
              album = audioAlbum,
              albumId = albumId,
              relativePath = audioFolderName,
              id = audioId,
              dateAdded = audioDateAdded,
              artworkImage = null
      )
    }
  }

  private fun getDirectUriInfo(uri: Uri): Music? {
    val projection = arrayOf(
//            MediaStore.Audio.AudioColumns._ID,
//            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )

    return appContext.reactContext!!.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
    )?.use { cursor ->
      if (!cursor.moveToFirst()) {
        return@use null
      }

      val fileNameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
      val lastModifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

      val fileName = cursor.getString(fileNameIndex)
      val lastModified = cursor.getInt(lastModifiedIndex)

      return@use Music(
              uri,
              displayName = fileName,
              dateAdded = lastModified
      )
    }
  }

  private fun getAudioInfo(uri: Uri): Music {
    val metadataRetriever = lazy {
      MediaMetadataRetriever().apply { setDataSource(appContext.reactContext!!, uri) }
    }

    return metadataRetriever.useLazy { getRetriever ->
      val result = if (hasGrantedPermission && uri.supportsExternalContentQuery) {
        getMediaStoreInfo(uri)
      } else {
        null
      } ?: run {
        val fileName = getDirectUriInfo(uri)?.displayName
        retrieveFileMetadata(uri, getRetriever()).copy(displayName = fileName)
      }

      if (currentPickingContext?.options?.includeArtworkImage == true) {
        result.artworkImage = retrieveArtwork(getRetriever())
      }

      return@useLazy result
    }
  }

  private fun retrieveFileMetadata(uri: Uri, metadataRetriever: MediaMetadataRetriever): Music {
    val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    val year = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
    val track = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
    val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    val duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { it / 1000.0 }
    val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    val dateAdded = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.toIntOrNull()

    return Music(
            uri,
            artist = artist,
            year = year,
            track = track,
            title = title,
            duration = duration,
            album = album,
            dateAdded = dateAdded
    )
  }

  private fun retrieveArtwork(metadataRetriever: MediaMetadataRetriever): ArtworkImage? {
    val (data, width, height) = metadataRetriever.embeddedPicture?.let {
      val bmp = BitmapFactory.decodeByteArray(it, 0, it.size) ?: return@let null
      val data = Base64.encodeToString(it, Base64.DEFAULT) ?: return@let null
      Triple(data, bmp.width, bmp.height)
    } ?: return null

    return ArtworkImage(data, width, height)
  }

  private data class PickingContext(
          val promise: Promise,
          val options: MusicPickerOptions
  )
}
