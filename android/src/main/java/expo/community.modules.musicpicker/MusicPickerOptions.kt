package expo.community.modules.musicpicker

import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

internal class MusicPickerOptions : Record {
    @Field
    var allowMultipleSelection: Boolean = false

    @Field
    var includeArtworkImage: Boolean = false
}
