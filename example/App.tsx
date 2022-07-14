import {
  Image,
  StyleSheet,
  Text,
  View,
  Button,
  FlatList,
  ScrollView,
} from "react-native";

import * as MusicPicker from "expo-music-picker";
import { useState } from "react";
import { MusicItem } from "expo-music-picker";
import { Platform } from "expo-modules-core";

function ItemElement({ item, index }: { item: MusicItem; index: number }) {
  const coverImgSrc = item.artworkImage
    ? { uri: `data:image/jpeg;base64,${item.artworkImage.base64Data}` }
    : require("./assets/album-placeholder.png");
  return (
    <View style={styles.listItem} key={item.id ?? index}>
      <Image
        style={styles.artworkImage}
        resizeMode="contain"
        source={coverImgSrc}
      />

      <Text numberOfLines={1} style={styles.title}>
        {item.title || "Untitled"}
      </Text>
      <Text numberOfLines={1} style={styles.artistName}>
        {item.artist || "Unknown artist"}
      </Text>
      <Text numberOfLines={1} style={styles.albumName}>
        {item.album || "Unknown album"}
      </Text>
    </View>
  );
}

export default function App() {
  const [items, setItems] = useState<MusicPicker.MusicItem[]>([]);

  const pickMediaAsync = async () => {
    try {
      const permissionResult = await MusicPicker.requestPermissionsAsync();

      if (!permissionResult.granted) {
        console.warn("No permission. Open settings and grant please");
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

  const strippedItems = items.map((it) => {
    const { artworkImage, ...rest } = it;
    return {
      ...rest,
      artworkImage: artworkImage
        ? {
            ...artworkImage,
            base64Data: artworkImage.base64Data.substring(0, 20).concat("..."),
          }
        : null,
    };
  });

  return (
    <View style={styles.container}>
      <Button title="Open music library" onPress={pickMediaAsync} />
      <FlatList
        style={styles.list}
        horizontal
        data={items}
        renderItem={(props) => <ItemElement {...props} />}
        ListEmptyComponent={
          <View style={styles.container}>
            <Text>No song selected.</Text>
            <Text>Open the picker and select one. Or more.</Text>
          </View>
        }
        contentContainerStyle={styles.listContainer}
      />
      <Text style={styles.title}>Raw result:</Text>
      <ScrollView style={styles.resultBox}>
        <Text style={styles.monoText}>
          {JSON.stringify(strippedItems, null, 2)}
        </Text>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
    width: "100%",
  },
  list: {
    maxHeight: 250,
  },
  listContainer: {
    backgroundColor: "#fff",
  },
  listItem: {
    flex: 1,
    alignItems: "center",
    padding: 10,
    width: 180,
    height: 220,
    backgroundColor: "#f0f0f5",
    margin: 10,
    borderRadius: 5,
  },
  artworkImage: {
    width: 150,
    height: 150,
    marginBottom: 5,
    borderRadius: 5,
  },
  title: {
    fontWeight: "bold",
  },
  artistName: {},
  albumName: {
    fontStyle: "italic",
  },
  resultBox: {
    width: "90%",
    padding: 10,
    maxHeight: 250,
    height: 250,
    backgroundColor: "#eee",
  },
  monoText: {
    fontFamily: Platform.OS === "ios" ? "Courier" : "monospace",
  },
});
