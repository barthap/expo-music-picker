import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';
const NativeView = requireNativeViewManager('ExpoMusicPicker');
export default function ExpoMusicPickerView(props) {
    return React.createElement(NativeView, { name: props.name });
}
//# sourceMappingURL=ExpoMusicPickerView.js.map