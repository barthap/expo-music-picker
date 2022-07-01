import { StyleSheet, Text, TouchableHighlight, View } from "react-native";

import * as MusicPicker from "../src/ExpoMusicPicker";
import { useState } from "react";

export default function App() {
  const [items, setItems] = useState<MusicPicker.MusicItem[]>([]);

  const pickMediaAsync = async () => {
    try {
      const result = await MusicPicker.openMusicLibraryAsync({
        allowMultipleSelection: false,
      });

      if (!result.cancelled) {
        setItems(result.items);
      }
    } catch (e) {
      console.warn("Exception occurred when picking music:", e);
    }
  };

  return (
    <View style={styles.container}>
      <TouchableHighlight onPress={pickMediaAsync}>
        <Text>Open music library</Text>
      </TouchableHighlight>
      <Text>{JSON.stringify(items, null, 2)}</Text>
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
