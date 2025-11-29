import { PermissionResponse } from "expo-modules-core";
import { MusicPickerOptions, PickerResult } from "./ExpoMusicPicker.types";
export declare function getPermissionsAsync(): Promise<PermissionResponse>;
export declare function requestPermissionsAsync(): Promise<PermissionResponse>;
export declare function openMusicLibraryAsync(options?: MusicPickerOptions): Promise<PickerResult>;
export * from "./ExpoMusicPicker.types";
//# sourceMappingURL=ExpoMusicPicker.d.ts.map