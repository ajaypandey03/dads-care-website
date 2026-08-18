// Mirrors the DTOs returned by dadscare-backend — keep these in sync by hand for now.
// (Velosyss's own mobile apps do the same; there's no shared-types package convention
// to follow here, see the Implementation Tracker notes on velosyss-mobile conventions.)

export type Role = "ORG_ADMIN" | "SITE_MANAGER" | "VIEWER" | "OPERATOR";

export interface User {
  id: number;
  organizationId: number;
  name: string;
  email: string;
  phone: string | null;
  role: Role;
}

export interface Site {
  id: number;
  name: string;
  godownCode: string;
  address: string | null;
  status: string;
}

export type DeviceType = "DIGITAL_LOCK";

export interface Device {
  id: number;
  velosyssDeviceRef: string;
  type: DeviceType;
  status: string;
  online: boolean;
  lastSeenAt: string | null;
  lastBatteryPct: number | null;
}

export interface ShutterUnit {
  id: number;
  siteId: number;
  label: string;
  status: string;
  device: Device | null;
}

export interface ProductMaster {
  id: number;
  name: string;
  unit: string;
  active: boolean;
}

export interface TransporterMaster {
  id: number;
  name: string;
  code: string | null;
  active: boolean;
}

export type CommandType = "LOCK" | "UNLOCK";
export type UnlockRequestStatus = "PENDING" | "RELAYED" | "FAILED";

export interface UnlockRequest {
  id: number;
  deviceId: number;
  commandType: CommandType;
  status: UnlockRequestStatus;
  createdAt: string;
}

export type AlertDirection = "OPEN" | "CLOSE";
export type AlertClassification = "CONFIRMED" | "UNEXPLAINED_HIGH" | "UNEXPLAINED_VERIFY" | "SUPPRESSED";

export interface AlertItem {
  id: number;
  deviceId: number;
  direction: AlertDirection;
  classification: AlertClassification;
  confidenceScore: number | null;
  sequenceCode: string | null;
  createdAt: string;
}
