package expo.community.modules.musicpicker

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import expo.community.modules.musicpicker.records.ArtworkImage
import expo.community.modules.musicpicker.records.MusicMetadata
import expo.community.modules.musicpicker.records.MusicPickerOptions
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.exception.Exceptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class MusicMetadataResolver(
    private val appContext: AppContext
) {
  private val hasGrantedPermission: Boolean
    get() = appContext.permissions
        ?.hasGrantedPermissions(*Utilities.audioPermissions)
        ?: false

  private val androidContext
    get() = appContext.reactContext ?: throw Exceptions.ReactContextLost()

  /**
   * Tries best effort to get audio metadata from given content [uri].
   * See the [MusicPickerModule] comment to see possibilities.
   */
  suspend fun getMusicMetadata(uri: Uri, options: MusicPickerOptions?): MusicMetadata {
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

  private suspend fun getMediaStoreInfo(uri: Uri): MusicMetadata? = withContext(Dispatchers.IO) {
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
        MediaStore.Audio.AudioColumns.DATE_ADDED, // 10
    )

    androidContext.contentResolver.query(
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
          cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED)


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
            audioRelativePath
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

  private suspend fun getDirectUriInfo(uri: Uri): MusicMetadata? = withContext(Dispatchers.IO) {
    androidContext.contentResolver.query(
        uri, null, null, null, null
    )?.use { cursor ->
      if (!cursor.moveToFirst()) {
        return@use null
      }

      val fileNameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
      val fileName = cursor.getString(fileNameIndex)

      return@use MusicMetadata(
          uri,
          fileName = fileName,
      )
    }
  }

  private suspend fun retrieveFileMetadata(
      uri: Uri,
      metadataRetriever: MediaMetadataRetriever
  ): MusicMetadata = withContext(Dispatchers.IO) {
      with(metadataRetriever) {
          val artist = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
          val year = extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
          val track = extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
          val title = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
          val duration = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { it / 1000.0 }
          val album = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
          val dateAdded = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.toIntOrNull()

          MusicMetadata(
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
  }

  private suspend fun retrieveArtwork(
      metadataRetriever: MediaMetadataRetriever
  ): ArtworkImage? = withContext(Dispatchers.IO) {
    val (data, width, height) = metadataRetriever.embeddedPicture?.let {
      val bmp = BitmapFactory.decodeByteArray(it, 0, it.size) ?: return@let null
      val data = Base64.encodeToString(it, Base64.DEFAULT) ?: return@let null
      Triple(data, bmp.width, bmp.height)
    } ?: return@withContext null

    ArtworkImage(data, width, height)
  }
}
