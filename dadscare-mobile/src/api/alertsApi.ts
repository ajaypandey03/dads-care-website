import { AlertItem } from "@/types/models";
import { apiClient } from "./apiClient";

export async function listAlerts(): Promise<AlertItem[]> {
  const { data } = await apiClient.get<AlertItem[]>("/api/v1/alerts");
  return data;
}

export async function submitFeedback(alertId: number, wasCorrect: boolean, comment?: string): Promise<void> {
  await apiClient.post(`/api/v1/alerts/${alertId}/feedback`, { wasCorrect, comment });
}
