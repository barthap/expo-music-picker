import ExpoModulesCore

internal struct MusicPickerOptions: Record {
  @Field var allowMultipleSelection: Bool = false
  @Field var showCloudItems: Bool = true
  @Field var userPrompt: String? = nil
  @Field var includeArtworkImage: Bool = false
}
