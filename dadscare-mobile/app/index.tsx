import { useAuth } from "@/context/AuthContext";
import { Redirect } from "expo-router";
import { ActivityIndicator, View } from "react-native";

/** The root route just redirects — Expo Router needs an index.tsx even when it's this thin. */
export default function Index() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View className="flex-1 items-center justify-center bg-white">
        <ActivityIndicator size="large" color="#1E40AF" />
      </View>
    );
  }

  return <Redirect href={user ? "/(app)" : "/(auth)/login"} />;
}
