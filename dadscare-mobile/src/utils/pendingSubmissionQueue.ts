import { submitUnlockRequest } from "@/api/unlockApi";
import { CreateUnlockRequestPayload } from "@/types/forms";
import AsyncStorage from "@react-native-async-storage/async-storage";

/**
 * Queues an unlock-request submission that failed due to a network error (not a
 * validation/auth error — those aren't retried), and flushes the queue when
 * connectivity returns. Mirrors velosyss-mobile-driver's driverSettlementQueue pattern
 * — see the Implementation Tracker's notes on velosyss-mobile conventions.
 *
 * IMPORTANT: unlike the driver app's settlement queue, queuing here does NOT mean the
 * godown gets unlocked immediately — lock control is fully server-mediated (see the
 * Godown Operational Workflow page), so the physical action only happens once this
 * queue actually flushes and the backend accepts the request. The UI must make that
 * delay obvious to the operator (see OpenCloseForm) rather than implying an instant
 * local unlock the way the driver app's settlement queue can imply an instant local
 * state change.
 */
const STORAGE_KEY = "dadscare_pending_unlock_requests";

interface QueuedSubmission {
  deviceId: number;
  payload: CreateUnlockRequestPayload;
  queuedAt: string;
}

export async function queueSubmission(deviceId: number, payload: CreateUnlockRequestPayload): Promise<void> {
  const queue = await readQueue();
  queue.push({ deviceId, payload, queuedAt: new Date().toISOString() });
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(queue));
}

export async function pendingCount(): Promise<number> {
  return (await readQueue()).length;
}

/** Attempts every queued submission; keeps only the ones that still fail (network errors), same as the driver app's flush. */
export async function flushPendingSubmissions(): Promise<{ flushed: number; remaining: number }> {
  const queue = await readQueue();
  if (queue.length === 0) {
    return { flushed: 0, remaining: 0 };
  }

  const stillPending: QueuedSubmission[] = [];
  let flushed = 0;

  for (const item of queue) {
    try {
      await submitUnlockRequest(item.deviceId, item.payload);
      flushed++;
    } catch {
      stillPending.push(item);
    }
  }

  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(stillPending));
  return { flushed, remaining: stillPending.length };
}

async function readQueue(): Promise<QueuedSubmission[]> {
  const raw = await AsyncStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : [];
}
