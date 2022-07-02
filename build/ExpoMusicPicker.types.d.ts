/**
 * Options
 */
export interface MusicPickerOptions {
    allowMultipleSelection?: boolean;
    showCloudItems?: boolean;
    userPrompt?: string;
    includeArtworkImage?: boolean;
}
/**
 * Represents single selection result
 */
export interface MusicItem {
    id: number;
    displayName: string;
    uri?: string;
    title?: string;
    artist?: string;
    album?: string;
    durationSeconds: number;
    artworkImage?: {
        width: number;
        height: number;
        base64Data: string | null;
    };
}
interface PickerResultBase {
    cancelled: boolean;
}
interface PickerResultCanceled extends PickerResultBase {
    cancelled: true;
}
interface PickerResultSuccess extends PickerResultBase {
    cancelled: false;
    items: MusicItem[];
}
export declare type PickerResult = PickerResultSuccess | PickerResultCanceled;
export {};
