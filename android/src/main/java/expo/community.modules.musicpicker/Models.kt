package expo.community.modules.musicpicker

import android.net.Uri
import android.os.Bundle

data class Music(
        val uri: Uri,
        val artist: String? = null,
        val year: Int? = null,
        val track: Int? = null,
        val title: String? = null,
        val displayName: String? = null,
        val duration: Number? = null,
        val album: String? = null,
        val albumId: Long? = null,
        val relativePath: String? = null,
        val id: Long? = null,
        val dateAdded: Int? = null,
        var artworkImage: ArtworkImage? = null
) {
    fun toBundle() = Bundle().apply {
        putLong("id", id ?: -1)
        putString("uri", uri.toString())
        putString("artist", artist)
        putString("title", title)
        putString("album", album ?: relativePath)
        putString("displayName", displayName)
        putLong("durationSeconds", duration?.toLong() ?: 0)
        putBundle("artworkImage", artworkImage?.toBundle())

        //
        putInt("year", year ?: -1)
        putInt("track", track ?: -1)
        putLong("albumId", albumId ?: -1)
        putInt("dateAdded", dateAdded ?: -1)
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
