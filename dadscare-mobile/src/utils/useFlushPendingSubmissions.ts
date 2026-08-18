import NetInfo from "@react-native-community/netinfo";
import { useEffect } from "react";
import { flushPendingSubmissions } from "./pendingSubmissionQueue";

/** Flushes the queue once on mount and again every time connectivity is regained — mirrors DriverLeadFlowContext. */
export function useFlushPendingSubmissions() {
  useEffect(() => {
    flushPendingSubmissions();

    const unsubscribe = NetInfo.addEventListener((state) => {
      if (state.isConnected) {
        flushPendingSubmissions();
      }
    });

    return unsubscribe;
  }, []);
}
