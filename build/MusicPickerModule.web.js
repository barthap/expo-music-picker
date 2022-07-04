import { PermissionStatus } from "expo-modules-core";
export default {
    async getPermissionsAsync() {
        return {
            status: PermissionStatus.GRANTED,
            expires: "never",
            granted: true,
            canAskAgain: true,
        };
    },
    async requestPermissionsAsync() {
        return {
            status: PermissionStatus.GRANTED,
            expires: "never",
            granted: true,
            canAskAgain: true,
        };
    },
    async openMusicLibraryAsync(options) {
        throw new Error("Not implemented");
    },
};
//# sourceMappingURL=MusicPickerModule.web.js.map