package expo.community.modules.musicpicker.records

import android.net.Uri
import android.os.Bundle
import expo.modules.kotlin.records.Field

data class MusicMetadata(
    @Field val uri: Uri,
    @Field val artist: String? = null,
    @Field val year: Int? = null,
    @Field val track: Int? = null,
    @Field val title: String? = null,
    @Field val fileName: String? = null,
    @Field val duration: Number? = null,
    @Field val album: String? = null,
    @Field val albumId: Long? = null,
    @Field val albumFolderName: String? = null,
    @Field val id: Long? = null,
    @Field val dateAdded: Int? = null,
    @Field val composer: String? = null,
    @Field var artworkImage: ArtworkImage? = null
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