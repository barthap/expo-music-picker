import { requireNativeModule, NativeModulesProxy, EventEmitter } from 'expo-modules-core';
import ExpoMusicPickerView from './ExpoMusicPickerView';
// It loads the native module object from the JSI or falls back to
// the bridge module (from NativeModulesProxy) if the remote debugger is on.
const ExpoMusicPicker = requireNativeModule('ExpoMusicPicker');
// Get the native constant value.
export const PI = ExpoMusicPicker.PI;
export function hello() {
    return ExpoMusicPicker.hello();
}
export async function setValueAsync(value) {
    return await ExpoMusicPicker.setValueAsync(value);
}
// For now the events are not going through the JSI, so we have to use its bridge equivalent.
// This will be fixed in the stable release and built into the module object.
const emitter = new EventEmitter(NativeModulesProxy.ExpoMusicPicker);
export function addChangeListener(listener) {
    return emitter.addListener('onChange', listener);
}
export { ExpoMusicPickerView };
//# sourceMappingURL=ExpoMusicPicker.js.map