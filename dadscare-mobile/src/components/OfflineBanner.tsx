import { useNetInfo } from "@react-native-community/netinfo";
import { Text, View } from "react-native";

/** Mirrors velosyss-mobile's OfflineBanner. */
export function OfflineBanner() {
  const netInfo = useNetInfo();

  if (netInfo.isConnected !== false) {
    return null;
  }

  return (
    <View className="bg-red-600 px-4 py-2">
      <Text className="text-center text-white text-sm font-medium">
        No internet connection — forms can still be drafted, but submitting requires connectivity
      </Text>
    </View>
  );
}
