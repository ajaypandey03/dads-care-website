import { secureStorage } from "@/utils/secureStorage";
import axios from "axios";
import Constants from "expo-constants";
import { Platform } from "react-native";

const TOKEN_KEY = "dadscare_access_token";

/**
 * Resolves the backend base URL for dev/prod. Mirrors velosyss-mobile's dev-host
 * auto-detection (LAN IP from the Expo dev server + Android emulator special-case),
 * simplified since we don't yet have a deployed prod API — see EXPO_PUBLIC_API_URL.
 */
function resolveBaseUrl(): string {
  const configured = process.env.EXPO_PUBLIC_API_URL;
  if (configured) {
    return configured;
  }

  const devHost = Constants.expoConfig?.hostUri?.split(":")[0];
  if (devHost) {
    // Android emulators can't reach the host machine via its LAN IP the same way a
    // physical device or the iOS simulator can — 10.0.2.2 is the documented alias.
    const host = Platform.OS === "android" && devHost === "localhost" ? "10.0.2.2" : devHost;
    return `http://${host}:8090`;
  }

  return "http://localhost:8090";
}

export const apiClient = axios.create({
  baseURL: resolveBaseUrl(),
  timeout: 15000,
});

let cachedToken: string | null = null;
let sessionExpiredHandler: (() => void) | null = null;

export function setSessionExpiredHandler(handler: () => void) {
  sessionExpiredHandler = handler;
}

export async function setToken(token: string | null) {
  cachedToken = token;
  if (token) {
    await secureStorage.setItem(TOKEN_KEY, token);
  } else {
    await secureStorage.removeItem(TOKEN_KEY);
  }
}

export async function loadStoredToken(): Promise<string | null> {
  if (cachedToken !== null) {
    return cachedToken;
  }
  cachedToken = await secureStorage.getItem(TOKEN_KEY);
  return cachedToken;
}

apiClient.interceptors.request.use(async (config) => {
  const token = await loadStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// dadscare-backend issues a single access token with a fixed TTL — there is no refresh
// endpoint yet (unlike velosyss-mobile, which has one). A 401 here always means the
// session needs a fresh login, so we clear the stored token and let AuthContext redirect
// to the login screen, rather than attempting a refresh that doesn't exist server-side.
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      await setToken(null);
      sessionExpiredHandler?.();
    }
    return Promise.reject(error);
  },
);
