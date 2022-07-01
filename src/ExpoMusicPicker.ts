import { PermissionResponse, requireNativeModule } from "expo-modules-core";
import { MusicPickerOptions, PickerResult } from "./ExpoMusicPicker.types";

const ExpoMusicPicker = requireNativeModule("ExpoMusicPicker");

export async function getPermissionsAsync(): Promise<PermissionResponse> {
  return await ExpoMusicPicker.getPermissionsAsync();
}

export async function requestPermissionsAsync(): Promise<PermissionResponse> {
  return await ExpoMusicPicker.requestPermissionsAsync();
}

export async function openMusicLibraryAsync(
  options: MusicPickerOptions = {}
): Promise<PickerResult> {
  return await ExpoMusicPicker.openMusicLibraryAsync(options);
}

export * from "./ExpoMusicPicker.types";
