/**
 * Options
 */
export interface MusicPickerOptions {
  allowMultipleSelection?: boolean;
  showCloudItems?: boolean;
  userPrompt?: string;
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
