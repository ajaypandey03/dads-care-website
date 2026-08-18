import { ProductMaster, TransporterMaster } from "@/types/models";
import { apiClient } from "./apiClient";

export async function listProductMasters(): Promise<ProductMaster[]> {
  const { data } = await apiClient.get<ProductMaster[]>("/api/v1/product-masters");
  return data;
}

export async function listTransporterMasters(): Promise<TransporterMaster[]> {
  const { data } = await apiClient.get<TransporterMaster[]>("/api/v1/transporter-masters");
  return data;
}
