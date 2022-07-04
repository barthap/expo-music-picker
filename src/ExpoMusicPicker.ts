import { PermissionResponse, UnavailabilityError } from "expo-modules-core";
import { MusicPickerOptions, PickerResult } from "./ExpoMusicPicker.types";

import ExpoMusicPicker from "./MusicPickerModule";

export async function getPermissionsAsync(): Promise<PermissionResponse> {
  if (!ExpoMusicPicker.getPermissionsAsync) {
    throw new UnavailabilityError("ExpoMusicPicker", "getPermissionsAsync");
  }

  return await ExpoMusicPicker.getPermissionsAsync();
}

export async function requestPermissionsAsync(): Promise<PermissionResponse> {
  if (!ExpoMusicPicker.requestPermissionsAsync) {
    throw new UnavailabilityError("ExpoMusicPicker", "requestPermissionsAsync");
  }

  return await ExpoMusicPicker.requestPermissionsAsync();
}

export async function openMusicLibraryAsync(
  options: MusicPickerOptions = {}
): Promise<PickerResult> {
  if (!ExpoMusicPicker.openMusicLibraryAsync) {
    throw new UnavailabilityError("ExpoMusicPicker", "openMusicLibraryAsync");
  }

  return await ExpoMusicPicker.openMusicLibraryAsync(options);
}

export * from "./ExpoMusicPicker.types";
