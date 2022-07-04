import { Image, StyleSheet, Text, View, Button } from "react-native";

import * as MusicPicker from "expo-music-picker";
import { useState } from "react";

export default function App() {
  const [items, setItems] = useState<MusicPicker.MusicItem[]>([]);

  const pickMediaAsync = async () => {
    try {
      const permissionResult = await MusicPicker.requestPermissionsAsync();

      if (!permissionResult.granted) {
        console.warn("No permission");
        return;
      }

      const result = await MusicPicker.openMusicLibraryAsync({
        allowMultipleSelection: true,
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
      <Button title="Open music library" onPress={pickMediaAsync} />
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
