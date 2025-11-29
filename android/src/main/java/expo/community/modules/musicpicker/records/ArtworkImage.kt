package expo.community.modules.musicpicker.records

import android.os.Bundle
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

data class ArtworkImage(
    @Field val base64Data: String,
    @Field val width: Int,
    @Field val height: Int
): Record {
  fun toBundle() = Bundle().apply {
    putInt("width", width)
    putInt("height", height)
    putString("base64Data", base64Data)
  }
}