import { apiClient } from "./apiClient";

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  organizationId: number;
  role: string;
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>("/api/v1/auth/login", { email, password });
  return data;
}
