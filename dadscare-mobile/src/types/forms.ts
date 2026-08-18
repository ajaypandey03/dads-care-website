// Mirrors dadscare-backend's CreateUnlockRequestRequest — see UnlockRequestController.

export interface StockLineInput {
  productMasterId: number;
  quantity: number;
}

export interface TruckEntryInput {
  source: string;
  productMasterId: number;
  vehicleNo: string;
  transporterMasterId: number;
  quantity: number;
  waitingSince?: string; // ISO instant
}

export interface CustomFieldInput {
  heading: string;
  value: string;
}

export interface CreateUnlockRequestPayload {
  commandType: "LOCK" | "UNLOCK";
  stockLines: StockLineInput[];
  truckEntries: TruckEntryInput[];
  laborCount?: number;
  remarks?: string;
  customFields: CustomFieldInput[];
}
