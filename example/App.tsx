import { StyleSheet, Text, View } from 'react-native';

import * as ExpoMusicPicker from 'expo-music-picker';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>{ExpoMusicPicker.hello()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
