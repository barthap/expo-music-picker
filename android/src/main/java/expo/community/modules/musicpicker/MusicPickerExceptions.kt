package expo.community.modules.musicpicker

import expo.modules.kotlin.exception.CodedException

internal class MissingIntentData :
    CodedException("Failed to resolve result intent from picker")

internal class MissingCurrentActivityException :
    CodedException("Activity which was provided during module initialization is no longer available")
