package expo.community.modules.musicpicker

import android.Manifest
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import expo.modules.kotlin.AppContext
import java.io.File

internal class MusicMetadataResolver(
    private val appContext: AppContext
) {
  private val hasGrantedPermission: Boolean
    get() = appContext.permissions
        ?.hasGrantedPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
        ?: false

  private val androidContext
    get() = requireNotNull(appContext.reactContext) {
      "React context is null"
    }

  /**
   * Tries best effort to get audio metadata from given content [uri].
   * See the [MusicPickerModule] comment to see possibilities.
   */
  fun getMusicMetadata(uri: Uri, options: MusicPickerOptions?): MusicMetadata {
    val metadataRetriever = lazy {
      MediaMetadataRetriever().apply { setDataSource(androidContext, uri) }
    }

    return metadataRetriever.useLazy { getRetriever ->
      val result = if (hasGrantedPermission && uri.supportsExternalContentQuery) {
        // most accurate metadata, from MediaStore provider
        // requires READ_EXTERNAL_STORAGE permission
        runCatching { getMediaStoreInfo(uri) }.getOrNull()
      } else {
        null
      } ?: run {
        // if unavailable or failed,
        val fileName = getDirectUriInfo(uri)?.fileName
        retrieveFileMetadata(uri, getRetriever()).copy(fileName = fileName)
      }

      if (options?.includeArtworkImage == true) {
        result.artworkImage = retrieveArtwork(getRetriever())
      }

      return@useLazy result
    }
  }

  private fun getMediaStoreInfo(uri: Uri): MusicMetadata? {
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
        MediaStore.MediaColumns.DATE_MODIFIED, // 10
    )

    return androidContext.contentResolver.query(
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
            audioRelativePath ?: null
          } else {
            File(audioRelativePath).parentFile?.name?.takeIf { it != "0" }
          }

      return@use MusicMetadata(
          uri = uri,
          artist = audioArtist,
          year = audioYear,
          track = audioTrack,
          title = audioTitle,
          fileName = audioDisplayName,
          duration = audioDuration,
          album = audioAlbum,
          albumId = albumId,
          albumFolderName = audioFolderName,
          id = audioId,
          dateAdded = audioDateAdded,
          artworkImage = null
      )
    }
  }

  private fun getDirectUriInfo(uri: Uri): MusicMetadata? {
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

      return@use MusicMetadata(
          uri,
          fileName = fileName,
          dateAdded = lastModified
      )
    }
  }

  private fun retrieveFileMetadata(uri: Uri, metadataRetriever: MediaMetadataRetriever): MusicMetadata {
    val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    val year = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
    val track = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
    val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    val duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { it / 1000.0 }
    val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    val dateAdded = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.toIntOrNull()

    return MusicMetadata(
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
}
