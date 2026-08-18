import { registerPushToken } from "@/api/meApi";
import Constants, { ExecutionEnvironment } from "expo-constants";
import * as Notifications from "expo-notifications";
import { useEffect } from "react";
import { Platform } from "react-native";

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

const isExpoGo = Constants.executionEnvironment === ExecutionEnvironment.StoreClient;

/** Registers for push and syncs the Expo push token to dadscare-backend. Mirrors velosyss-mobile's registration effect. */
export function usePushNotifications(enabled: boolean) {
  useEffect(() => {
    if (!enabled || isExpoGo) {
      // Expo Go doesn't support remote push since SDK 53 — a real build is needed to test this.
      return;
    }

    (async () => {
      if (Platform.OS === "android") {
        await Notifications.setNotificationChannelAsync("default", {
          name: "default",
          importance: Notifications.AndroidImportance.HIGH,
        });
      }

      const { status: existingStatus } = await Notifications.getPermissionsAsync();
      let finalStatus = existingStatus;
      if (existingStatus !== "granted") {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
      }
      if (finalStatus !== "granted") {
        return;
      }

      const projectId = Constants.expoConfig?.extra?.eas?.projectId;
      const { data: token } = await Notifications.getExpoPushTokenAsync(
        projectId ? { projectId } : undefined,
      );

      try {
        await registerPushToken(token);
      } catch {
        // Non-fatal — the app still works without push; the next login attempt will retry.
      }
    })();
  }, [enabled]);
}
