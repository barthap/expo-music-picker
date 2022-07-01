import { requireNativeModule } from "expo-modules-core";
import { MusicPickerOptions, PickerResult } from "./ExpoMusicPicker.types";

const ExpoMusicPicker = requireNativeModule("ExpoMusicPicker");

export async function openMusicLibraryAsync(
  options: MusicPickerOptions = {}
): Promise<PickerResult> {
  return await ExpoMusicPicker.openMusicLibraryAsync(options);
}

export * from "./ExpoMusicPicker.types";
