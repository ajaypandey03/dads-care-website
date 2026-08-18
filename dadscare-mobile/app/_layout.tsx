import "../global.css";
import { AuthProvider } from "@/context/AuthContext";
import { useFlushPendingSubmissions } from "@/utils/useFlushPendingSubmissions";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useState } from "react";
import { SafeAreaProvider } from "react-native-safe-area-context";

export default function RootLayout() {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: { queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false } },
      }),
  );

  useFlushPendingSubmissions();

  return (
    <SafeAreaProvider>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <StatusBar style="dark" />
          <Stack screenOptions={{ headerTitleStyle: { fontWeight: "600" } }}>
            <Stack.Screen name="index" options={{ headerShown: false }} />
            <Stack.Screen name="(auth)" options={{ headerShown: false }} />
            <Stack.Screen name="(app)" options={{ headerShown: false }} />
          </Stack>
        </AuthProvider>
      </QueryClientProvider>
    </SafeAreaProvider>
  );
}
