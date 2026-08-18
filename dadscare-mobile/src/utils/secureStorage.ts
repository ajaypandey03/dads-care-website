import * as SecureStore from "expo-secure-store";
import { Platform } from "react-native";

/**
 * expo-secure-store has no Keychain/Keystore equivalent on web (browsers don't expose
 * one), so SecureStore's own web shim throws rather than silently no-op'ing. On native
 * (the actual target platforms — see app.json) this is the real Keychain/Keystore.
 * On web, this falls back to localStorage — fine for local dev/preview, NOT a claim
 * that token storage is secure there; a real web deployment of this app doesn't exist.
 */
export const secureStorage = {
  async getItem(key: string): Promise<string | null> {
    if (Platform.OS === "web") {
      return typeof localStorage !== "undefined" ? localStorage.getItem(key) : null;
    }
    return SecureStore.getItemAsync(key);
  },

  async setItem(key: string, value: string): Promise<void> {
    if (Platform.OS === "web") {
      if (typeof localStorage !== "undefined") {
        localStorage.setItem(key, value);
      }
      return;
    }
    await SecureStore.setItemAsync(key, value);
  },

  async removeItem(key: string): Promise<void> {
    if (Platform.OS === "web") {
      if (typeof localStorage !== "undefined") {
        localStorage.removeItem(key);
      }
      return;
    }
    await SecureStore.deleteItemAsync(key);
  },
};
