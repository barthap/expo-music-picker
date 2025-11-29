package expo.community.modules.musicpicker.records

import android.os.Bundle

sealed class PickerResult {
    abstract fun toBundle(): Bundle

    class Canceled(): PickerResult() {
        override fun toBundle() = Bundle().apply {
            putBoolean("cancelled", true)
        }
    }

    class Picked(private val pickedItems: List<MusicMetadata>): PickerResult() {
        override fun toBundle() = Bundle().apply {
            val items = pickedItems.map(MusicMetadata::toBundle)

            putBoolean("cancelled", true)
            putParcelableArrayList("items", ArrayList(items))
        }
    }
}