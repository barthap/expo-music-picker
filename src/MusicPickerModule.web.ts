import { PermissionResponse, PermissionStatus } from "expo-modules-core";
import { MusicPickerOptions, PickerResult } from "./ExpoMusicPicker";

export default {
  async getPermissionsAsync(): Promise<PermissionResponse> {
    return {
      status: PermissionStatus.GRANTED,
      expires: "never",
      granted: true,
      canAskAgain: true,
    };
  },
  async requestPermissionsAsync(): Promise<PermissionResponse> {
    return {
      status: PermissionStatus.GRANTED,
      expires: "never",
      granted: true,
      canAskAgain: true,
    };
  },
  async openMusicLibraryAsync(
    options: MusicPickerOptions
  ): Promise<PickerResult> {
    throw new Error("Not implemented");
  },
};
