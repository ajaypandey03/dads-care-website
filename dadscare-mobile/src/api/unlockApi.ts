import { CreateUnlockRequestPayload } from "@/types/forms";
import { UnlockRequest } from "@/types/models";
import { apiClient } from "./apiClient";

export async function submitUnlockRequest(
  deviceId: number,
  payload: CreateUnlockRequestPayload,
): Promise<UnlockRequest> {
  const { data } = await apiClient.post<UnlockRequest>(`/api/v1/devices/${deviceId}/unlock-requests`, payload);
  return data;
}
