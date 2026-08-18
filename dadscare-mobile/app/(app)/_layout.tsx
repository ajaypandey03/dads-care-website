import { OfflineBanner } from "@/components/OfflineBanner";
import { useAuth } from "@/context/AuthContext";
import { usePushNotifications } from "@/utils/usePushNotifications";
import { Redirect, Stack } from "expo-router";
import { Text, TouchableOpacity } from "react-native";

export default function AppLayout() {
  const { user, logout } = useAuth();
  usePushNotifications(!!user);

  if (!user) {
    return <Redirect href="/(auth)/login" />;
  }

  return (
    <>
      <OfflineBanner />
      <Stack
        screenOptions={{
          headerTitleStyle: { fontWeight: "600" },
          headerRight: () => (
            <TouchableOpacity onPress={logout}>
              <Text className="text-brand font-medium">Log out</Text>
            </TouchableOpacity>
          ),
        }}
      >
        <Stack.Screen name="index" options={{ title: "Your Godowns" }} />
        <Stack.Screen name="sites/[siteId]" options={{ title: "Shutters" }} />
        <Stack.Screen name="devices/[deviceId]/[mode]" options={{ title: "Godown Form" }} />
        <Stack.Screen name="alerts/index" options={{ title: "Alerts" }} />
      </Stack>
    </>
  );
}
