/**
 * Options
 */
export interface MusicPickerOptions {
  allowMultipleSelection?: boolean;
  includeArtworkImage?: boolean;
  /**
   * @platform ios
   */
  showCloudItems?: boolean;
  /**
   * @platform ios
   */
  userPrompt?: string;
}

/**
 * Represents single selection result
 */
export interface MusicItem {
  id: number;
  uri: string;
  title?: string;
  artist?: string;
  album?: string;
  durationSeconds: number;
  track?: number;
  dateAdded?: number;
  /**
   * @platform android
   */
  fileName: string;
  /**
   * @platform android
   */
  year?: number;
  artworkImage?: {
    width: number;
    height: number;
    base64Data: string;
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

export type PickerResult = PickerResultSuccess | PickerResultCanceled;
