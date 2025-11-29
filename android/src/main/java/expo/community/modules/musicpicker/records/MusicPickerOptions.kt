package expo.community.modules.musicpicker.records

import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

internal class MusicPickerOptions : Record {
  @Field
  val allowMultipleSelection: Boolean = false

  @Field
  val includeArtworkImage: Boolean = false
}