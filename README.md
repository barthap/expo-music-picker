> ❗ Work in progress. This library is not yet published to npm. Instructions below will not work.

# Expo Music Picker

A music picker library for React Native. Provides access to the system's UI for selecting songs from the phone's music library.

> Add screenshot/demo here

## Supported platforms

| Android Device | Android Emulator | iOS Device | iOS Simulator | Web | Expo Go |
| -------------- | ---------------- | ---------- | ------------- | --- | ------- |
| ✅             | ✅               | ✅         | ❌            | ✅  | ❌      |

- ✅ You can use this library with [Expo Development Builds](https://docs.expo.dev/development/introduction/). It includes a config plugin.
- ❌ This library can't be used in the "Expo Go" app because it [requires custom native code](https://docs.expo.dev/workflow/customizing/).

> This library requires Expo SDK 45 or newer

## Installation

```sh
npx expo install expo-music-picker
```

Once the library is installed, [create a new Development Build](https://docs.expo.dev/development/build/).

If you're installing this in a bare React Native app, you will need to have the [`expo` package installed and configured](https://docs.expo.dev/bare/installing-expo-modules/).

## Configuration in app.json / app.config.js

Add `expo-music-picker` to the plugins array of your **app.json** or **app.config.js** and then [create a new Development Build](https://docs.expo.dev/development/build/) to apply the changes.

```json
{
  "expo": {
    "plugins": [
      "expo-music-picker",
      { "musicLibraryPermission": "The app accesses your music to play it" }
    ]
  }
}
```

The config plugin has the following options:

<table>
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>Explanation</th>
    <th>Required</th>
    <th>Default Value</th>
  </td>
  <tr>
    <td><code>musicLibraryPermission</code></td>
    <td><code>string</code></td>
    <td>🍏 <b>iOS only</b> A string to set the <code>NSAppleMusicUsageDescription</code> permission message.</td>
    <td>❌</td>
    <td><code>"Allow $(PRODUCT_NAME) to access your music library"</code></td>
  </tr>
</table>

## Configuration for iOS 🍏

> This is only required for usage in bare React Native apps.

Add `NSAppleMusicUsageDescription` key to your `Info.plist`:

```xml
<key>NSAppleMusicUsageDescription</key>
<string>Give $(PRODUCT_NAME) permission to access your music library</string>
```

Run `npx pod-install` after installing the npm package.

## Configuration for Android 🤖

> This is only required for usage in bare React Native apps.

This package automatically adds the `READ_EXTERNAL_STORAGE` permission. It is used when picking music from the phone's library.

```xml
<!-- Added permissions -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

## Usage

```ts
import { useState } from "react";
import { Image, Text, View, Button } from "react-native";

import * as MusicPicker from "expo-music-picker";

export default function App() {
  const [item, setItem] = useState<MusicPicker.MusicItem | null>();

  const pickMediaAsync = async () => {
    try {
      // Request permissions
      const permissionResult = await MusicPicker.requestPermissionsAsync();
      if (!permissionResult.granted) {
        console.warn("No permission");
        return;
      }

      // Open the music picker
      const result = await MusicPicker.openMusicLibraryAsync({
        allowMultipleSelection: true,
        includeArtworkImage: true,
      });

      // Process the result
      console.log(result);
      if (!result.cancelled) {
        const [firstItem] = result.items;
        setItem(firstItem ?? null);
      }
    } catch (e) {
      console.warn("Exception occurred when picking music:", e);
    }
  };

  const artworkData = item?.artworkImage?.base64Data;

  return (
    <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
      <Button title="Open music library" onPress={pickMediaAsync} />
      <Text>
        {item ? `Song: ${item.artist} - ${item.title}` : "No song selected"}
      </Text>
      {artworkData && (
        <Image
          source={{ uri: `data:image/jpeg;base64,${artworkData}` }}
          style={{ width: 200, height: 200 }}
          resizeMode="contain"
        />
      )}
    </View>
  );
}
```

## API

```js
import * as MusicPicker from `expo-music-picker`;
```

### `requestPermissionsAsync(): Promise<PermissionResponse>`

Asks the user to grant permissions for accessing user's music library.

- 🍏 **On iOS** this is required to open the picker.
- 🤖 **On Android** this is not required, but recommended to get the most accurate results. Only basic metadata will be retrieved without that permission.
- 🌐 **On Web** this does nothing - the permission is always granted.

**Returns:** A promise that fulfills with [Expo standard permission response object](https://docs.expo.dev/versions/latest/sdk/imagepicker/#permissionresponse).

### `getPermissionsAsync(): Promise<PermissionResponse>`

Checks user's permissions for accessing music library.

On Web, this does nothing - the permission is always granted.

**Returns:** A promise that fulfills with [Expo standard permission response object](https://docs.expo.dev/versions/latest/sdk/imagepicker/#permissionresponse).

### `openMusicLibraryAsync(options?: MusicPickerOptions): Promise<PickerResult>`

### `MusicPickerOptions`

Options for the picker:

<table>
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>Explanation</th>
    <th>Required</th>
    <th>Default Value</th>
  </td>
  <tr>
    <td><code>allowMultipleSelection</code></td>
    <td><code>boolean</code></td>
    <td>Whether or not to allow selecting multiple media files at once.</td>
    <td>❌</td>
    <td><code>false</code></td>
  </tr>
  <tr>
    <td><code>includeArtworkImage</code></td>
    <td><code>boolean</code></td>
    <td>Whether to also include the artwork image dimensions and its data in Base64 format.</td>
    <td>❌</td>
    <td><code>false</code></td>
  </tr>
  <tr>
    <td><code>showCloudItems</code></td>
    <td><code>boolean</code></td>
    <td>🍏 <b>iOS only</b> When set to <code>true</code>, the picker shows available iCloud Music Library items, including purchased items, imported content, and Apple Music subscription content. When set to <code>false</code>, the picker only shows content downloaded to the device.</td>
    <td>❌</td>
    <td><code>true</code></td>
  </tr>
  <tr>
    <td><code>userPrompt</code></td>
    <td><code>string | undefined</code></td>
    <td>🍏 <b>iOS only</b> A prompt, for the user, that appears above the navigation bar buttons on the picker screen.</td>
    <td>❌</td>
    <td><code>undefined</code></td>
  </tr>
</table>

### `PickerResult`

A picking result resolved by `openMusicLibraryAsync`.

```ts
type PickerResult =
  | { cancelled: true }
  | { cancelled: false; items: MusicItem[] };
```

When the `cancelled` property is `false`, the picked music items are available under the `items` property.

### `MusicItem`

Represents a single picked music item.

<table>
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>Description</th>
  </td>
  <tr>
    <td><code>id</code></td>
    <td><code>number</code></td>
    <td>Unique asset ID. On Android, this value can be used to access the asset with <code>expo-media-library</code>. If the ID cannot be obtained, the value is <code>-1</code>.</td>
  </tr>
  <tr>
    <td><code>uri</code></td>
    <td><code>string</code></td>
    <td>URI of the asset. It can be used to play selected media with <code>expo-av</code>.</td>
  </tr>
  <tr>
    <td><code>title</code></td>
    <td><code>string?</code></td>
    <td>The title or name of the music item.</td>
  </tr>
  <tr>
    <td><code>artist</code></td>
    <td><code>string?</code></td>
    <td>The performing artist the music item.</td>
  </tr>
  <tr>
    <td><code>album</code></td>
    <td><code>string?</code></td>
    <td>The title of an album.</td>
  </tr>
  <tr>
    <td><code>durationSeconds</code></td>
    <td><code>number</code></td>
    <td>The duration of the music item in seconds. Returns <code>-1</code> if unavailable.</td>
  </tr>
    <tr>
    <td><code>track</code></td>
    <td><code>number?</code></td>
    <td>The track number of the media item, for a media item that is part of an album.</td>
  </tr>
  <tr>
    <td><code>year</code></td>
    <td><code>number?</code></td>
    <td>🤖 🌐 <b>Android & Web only</b>. Year of the song.</td>
  </tr>
  <tr>
    <td><code>fileName</code></td>
    <td><code>string?</code></td>
    <td>🤖 <b>Android only</b>. The filename of the media. Example: <i>toto_africa.mp3</i></td>
  </tr>
  <tr>
    <td><code>artworkImage</code></td>
    <td><code>ArtworkImage?</code></td>
    <td>When <code>options.includeArtworkImage</code> is <code>true</code> and the music item has attached artwork image, it can be accessed by this property. See <code>ArtworkImage</code> type below.</td>
  </tr>
</table>

### `ArtworkImage`

<table>
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>Description</th>
  </td>
  <tr>
    <td><code>width</code></td>
    <td><code>number</code></td>
    <td>Whether or not to allow selecting multiple media files at once.</td>
  </tr>
  <tr>
    <td><code>height</code></td>
    <td><code>number</code></td>
    <td>Whether to also include the artwork image dimensions and its data in Base64 format.</td>
  </tr>
  <tr>
    <td><code>base64Data</code></td>
    <td><code>string</code></td>
    <td>Base64-encoded image data. Does NOT include the <code>data:...;base64,</code> prefix.</td>
  </tr>

</table>
