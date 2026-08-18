import { ShutterUnit, Site } from "@/types/models";
import { apiClient } from "./apiClient";

export async function listSites(): Promise<Site[]> {
  const { data } = await apiClient.get<Site[]>("/api/v1/sites");
  return data;
}

export async function listShutterUnits(siteId: number): Promise<ShutterUnit[]> {
  const { data } = await apiClient.get<ShutterUnit[]>(`/api/v1/sites/${siteId}/shutter-units`);
  return data;
}
