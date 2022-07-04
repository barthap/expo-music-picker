import { UnavailabilityError } from "expo-modules-core";
import ExpoMusicPicker from "./MusicPickerModule";
export async function getPermissionsAsync() {
    if (!ExpoMusicPicker.getPermissionsAsync) {
        throw new UnavailabilityError("ExpoMusicPicker", "getPermissionsAsync");
    }
    return await ExpoMusicPicker.getPermissionsAsync();
}
export async function requestPermissionsAsync() {
    if (!ExpoMusicPicker.requestPermissionsAsync) {
        throw new UnavailabilityError("ExpoMusicPicker", "requestPermissionsAsync");
    }
    return await ExpoMusicPicker.requestPermissionsAsync();
}
export async function openMusicLibraryAsync(options = {}) {
    if (!ExpoMusicPicker.openMusicLibraryAsync) {
        throw new UnavailabilityError("ExpoMusicPicker", "openMusicLibraryAsync");
    }
    return await ExpoMusicPicker.openMusicLibraryAsync(options);
}
export * from "./ExpoMusicPicker.types";
//# sourceMappingURL=ExpoMusicPicker.js.map