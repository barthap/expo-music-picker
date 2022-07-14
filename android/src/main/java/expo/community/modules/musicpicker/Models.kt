package expo.community.modules.musicpicker

import android.net.Uri
import android.os.Bundle

data class MusicMetadata(
    val uri: Uri,
    val artist: String? = null,
    val year: Int? = null,
    val track: Int? = null,
    val title: String? = null,
    val fileName: String? = null,
    val duration: Number? = null,
    val album: String? = null,
    val albumId: Long? = null,
    val albumFolderName: String? = null,
    val id: Long? = null,
    val dateAdded: Int? = null,
    val composer: String? = null,
    var artworkImage: ArtworkImage? = null
) {
  fun toBundle() = Bundle().apply {
    putLong("id", id ?: -1)
    putString("uri", uri.toString())
    putString("artist", artist ?: composer)
    putString("title", title)
    putString("album", album ?: albumFolderName)
    putLong("durationSeconds", duration?.toLong() ?: -1)
    putBundle("artworkImage", artworkImage?.toBundle())
    track?.let { putInt("track", it) }
    dateAdded?.let { putInt("dateAdded", it) }

    // putLong("albumId", albumId ?: -1) // not usable
    // putString("composer", composer) // API 30+

    // Android only fields
    year?.let { putInt("year", it) }
    putString("fileName", fileName)
  }
}

data class ArtworkImage(
    val base64Data: String,
    val width: Int,
    val height: Int
) {
  fun toBundle() = Bundle().apply {
    putInt("width", width)
    putInt("height", height)
    putString("base64Data", base64Data)
  }
}
