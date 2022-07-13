import { PermissionStatus, Platform, } from "expo-modules-core";
import { parseBlob, selectCover, } from "music-metadata-browser";
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
        // SSR guard
        if (!Platform.isDOMAvailable) {
            return { cancelled: true };
        }
        return await openFileBrowserAsync(options);
    },
};
function openFileBrowserAsync({ allowMultipleSelection = false, includeArtworkImage = false, }) {
    const input = document.createElement("input");
    input.style.display = "none";
    input.setAttribute("type", "file");
    input.setAttribute("accept", "audio/*");
    // input.setAttribute('id', v4());
    if (allowMultipleSelection) {
        input.setAttribute("multiple", "multiple");
    }
    document.body.appendChild(input);
    return new Promise((resolve, reject) => {
        input.addEventListener("change", async () => {
            if (input.files) {
                if (!allowMultipleSelection) {
                    const item = await readFile(input.files[0], {
                        includeArtworkImage,
                    });
                    resolve({
                        cancelled: false,
                        items: [item],
                    });
                }
                else {
                    const songs = await Promise.all(Array.from(input.files).map((file) => readFile(file, { includeArtworkImage })));
                    resolve({
                        cancelled: false,
                        items: songs,
                    });
                }
            }
            document.body.removeChild(input);
        });
        const event = new MouseEvent("click");
        input.dispatchEvent(event);
    });
}
async function readFile(targetFile, options) {
    const metadata = await parseBlob(targetFile, {
        duration: true,
        skipCovers: !options.includeArtworkImage,
    });
    const url = window.URL.createObjectURL(targetFile);
    const { title, track, album, artist, year } = metadata.common;
    const picture = selectCover(metadata.common.picture);
    console.log(picture);
    const cover = await parseCoverImage(picture);
    return {
        uri: url,
        id: -1,
        durationSeconds: metadata.format.duration ?? -1,
        album,
        artist,
        title,
        track: track.no ?? undefined,
        year,
        artworkImage: cover ?? undefined,
    };
}
async function parseCoverImage(image) {
    if (!image)
        return null;
    const { data, type } = image;
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onerror = () => {
            reject(new Error(`Failed to read the selected media because the operation failed.`));
        };
        reader.onload = ({ target }) => {
            const uri = target.result;
            const fail = () => resolve(null);
            if (typeof uri === "string") {
                const image = new Image();
                // for some reason, the library returns image base64 data,
                // but with application/octet-stream mime type
                image.src = uri.replace("application/octet-stream", type ?? "image/jpeg");
                image.onload = () => resolve({
                    width: image.naturalWidth ?? image.width,
                    height: image.naturalHeight ?? image.height,
                    // The blob's result cannot be directly decoded as Base64 without
                    // first removing the Data-URL declaration preceding the
                    // Base64-encoded data. To retrieve only the Base64 encoded string,
                    // first remove data:*/*;base64, from the result.
                    // https://developer.mozilla.org/en-US/docs/Web/API/FileReader/readAsDataURL
                    base64Data: uri.substr(uri.indexOf(",") + 1),
                });
                image.onerror = () => fail;
            }
            else {
                fail();
            }
        };
        reader.readAsDataURL(new Blob([new Uint8Array(data)]));
    });
}
//# sourceMappingURL=MusicPickerModule.web.js.map