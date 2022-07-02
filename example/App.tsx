import {
  Image,
  StyleSheet,
  Text,
  TouchableHighlight,
  View,
} from "react-native";

import * as MusicPicker from "../src/ExpoMusicPicker";
import { useState } from "react";

export default function App() {
  const [items, setItems] = useState<MusicPicker.MusicItem[]>([]);

  const pickMediaAsync = async () => {
    try {
      const permissionResult = await MusicPicker.requestPermissionsAsync();

      console.log(permissionResult);
      if (!permissionResult.granted && !permissionResult.canAskAgain) {
        console.warn("No permission");
        return;
      }

      const result = await MusicPicker.openMusicLibraryAsync({
        allowMultipleSelection: false,
        userPrompt: "Select some songs",
        includeArtworkImage: true,
      });

      if (!result.cancelled) {
        setItems(result.items);
      }
    } catch (e) {
      console.warn("Exception occurred when picking music:", e);
    }
  };

  const finalItmes = items.map((it) => {
    const { artworkImage, ...rest } = it;
    console.log(artworkImage?.width, artworkImage?.height);
    return artworkImage?.base64Data ? rest : it;
  });

  return (
    <View style={styles.container}>
      <TouchableHighlight onPress={pickMediaAsync}>
        <Text>Open music library</Text>
      </TouchableHighlight>
      <Text>{JSON.stringify(finalItmes, null, 2)}</Text>
      {items[0]?.artworkImage?.base64Data && (
        <Image
          source={{
            uri: `data:image/jpeg;base64,${items[0].artworkImage.base64Data}`,
          }}
          style={{ width: 200, height: 200 }}
          resizeMode="contain"
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});
