import { requireNativeModule } from "expo-modules-core";
const ExpoMusicPicker = requireNativeModule("ExpoMusicPicker");
export async function getPermissionsAsync() {
    return await ExpoMusicPicker.getPermissionsAsync();
}
export async function requestPermissionsAsync() {
    return await ExpoMusicPicker.requestPermissionsAsync();
}
export async function openMusicLibraryAsync(options = {}) {
    return await ExpoMusicPicker.openMusicLibraryAsync(options);
}
export * from "./ExpoMusicPicker.types";
//# sourceMappingURL=ExpoMusicPicker.js.map