import { Subscription } from 'expo-modules-core';
import ExpoMusicPickerView, { ExpoMusicPickerViewProps } from './ExpoMusicPickerView';
export declare const PI: any;
export declare function hello(): string;
export declare function setValueAsync(value: string): Promise<any>;
export declare type ChangeEventPayload = {
    value: string;
};
export declare function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription;
export { ExpoMusicPickerView, ExpoMusicPickerViewProps };
