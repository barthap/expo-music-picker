import { PermissionResponse } from "expo-modules-core";
import { MusicPickerOptions, PickerResult } from "./ExpoMusicPicker";
declare const _default: {
    getPermissionsAsync(): Promise<PermissionResponse>;
    requestPermissionsAsync(): Promise<PermissionResponse>;
    openMusicLibraryAsync(options: MusicPickerOptions): Promise<PickerResult>;
};
export default _default;
