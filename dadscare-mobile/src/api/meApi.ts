import { User } from "@/types/models";
import { apiClient } from "./apiClient";

export async function getMe(): Promise<User> {
  const { data } = await apiClient.get<User>("/api/v1/me");
  return data;
}

export async function registerPushToken(token: string): Promise<void> {
  await apiClient.put("/api/v1/me/push-token", { token });
}
