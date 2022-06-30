import {
  requireNativeModule,
  NativeModulesProxy,
  EventEmitter,
  Subscription,
} from "expo-modules-core";

// It loads the native module object from the JSI or falls back to
// the bridge module (from NativeModulesProxy) if the remote debugger is on.
const ExpoMusicPicker = requireNativeModule("ExpoMusicPicker");

// Get the native constant value.
export const PI = ExpoMusicPicker.PI;

export function hello(): string {
  return ExpoMusicPicker.hello();
}

export async function setValueAsync(value: string) {
  return await ExpoMusicPicker.setValueAsync(value);
}

// For now the events are not going through the JSI, so we have to use its bridge equivalent.
// This will be fixed in the stable release and built into the module object.
const emitter = new EventEmitter(NativeModulesProxy.ExpoMusicPicker);

export type ChangeEventPayload = {
  value: string;
};

export function addChangeListener(
  listener: (event: ChangeEventPayload) => void
): Subscription {
  return emitter.addListener<ChangeEventPayload>("onChange", listener);
}
