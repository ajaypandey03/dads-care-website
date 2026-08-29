// Mirrors dadscare-backend's REST DTOs (see dadscare-backend/src/main/java/com/dadscare/backend/**).
// Kept as plain hand-written types (no codegen) — same approach as dadscare-mobile/src/types.

export type Role = "ORG_ADMIN" | "SITE_MANAGER" | "VIEWER" | "OPERATOR";

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  organizationId: number;
  role: Role;
  platformAdmin: boolean;
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
  /** Velosyss's numeric lock id — used server-side for REST command/read calls. */
  velosyssDeviceRef: string;
  /** Velosyss's terminalId — the join key for inbound webhook events. Null until linked. */
  velosyssTerminalId: string | null;
  type: DeviceType;
  status: string;
  online: boolean;
  lastSeenAt: string | null;
  lastBatteryPct: number | null;
  lastLatitude: number | null;
  lastLongitude: number | null;
  lastSealed: boolean | null;
  lastShackleClosed: boolean | null;
}

export type ShutterState = "OPEN" | "CLOSED" | "UNKNOWN";

export interface ShutterUnit {
  id: number;
  siteId: number;
  label: string;
  status: string;
  device: Device | null;
  currentState: ShutterState;
  lastOpenedAt: string | null;
  lastClosedAt: string | null;
}

export type EventDirection = "OPEN" | "CLOSE" | "ALARM";

export type AlertClassification =
  | "CONFIRMED"
  | "UNEXPLAINED_HIGH"
  | "UNEXPLAINED_VERIFY"
  | "SUPPRESSED";

export interface Alert {
  id: number;
  deviceId: number;
  deviceRef: string;
  siteId: number | null;
  siteName: string | null;
  direction: EventDirection;
  classification: AlertClassification;
  confidenceScore: number | null;
  sequenceCode: string | null;
  createdAt: string;
  /** null = no "Was this correct?" answer yet; true/false = the most recent answer. */
  feedbackCorrect: boolean | null;
}

export type CommandType = "LOCK" | "UNLOCK";
/** Mirrors Velosyss's real command lifecycle (§6.4 of the Integration Guide) plus our own PENDING/FAILED pre/error states. */
export type UnlockRequestStatus =
  | "PENDING"
  | "QUEUED"
  | "DISPATCHED"
  | "DEVICE_OFFLINE"
  | "RESPONDED"
  | "EXPIRED"
  | "FAILED";

export interface UnlockRequest {
  id: number;
  deviceId: number;
  commandType: CommandType;
  status: UnlockRequestStatus;
  /** Only meaningful once status is RESPONDED — the lock's own reported success/failure. */
  succeeded: boolean | null;
  /** Detail from Velosyss (command response or COMMAND_RESULT webhook), or our own failure reason. */
  message: string | null;
  createdAt: string;
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

export interface UserAdmin {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  role: Role;
  status: string;
}

export interface CreateUserResponse {
  user: UserAdmin;
  /** null when the admin set the password manually instead of letting one be generated. */
  temporaryPassword: string | null;
}

export interface Me {
  id: number;
  organizationId: number;
  name: string;
  email: string;
  phone: string | null;
  role: Role;
  platformAdmin: boolean;
}

export interface Organization {
  id: number;
  name: string;
  slug: string;
  codePrefix: string;
  active: boolean;
  createdAt: string;
}

export interface CreateOrganizationResponse {
  organization: Organization;
  adminUser: UserAdmin;
  /** null when the platform admin set the first admin's password manually. */
  temporaryPassword: string | null;
}

export interface ResetPasswordResponse {
  user: UserAdmin;
  temporaryPassword: string;
}
