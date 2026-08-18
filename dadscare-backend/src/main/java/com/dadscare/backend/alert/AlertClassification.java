package com.dadscare.backend.alert;

/**
 * The outcome of {@link RulesEngineService}'s evaluation of one lock-state telemetry
 * event, in descending order of how alarming it is. See "Lock vs Shutter" in Confluence.
 */
public enum AlertClassification {
    /** Matched a {@link com.dadscare.backend.unlock.UnlockRequest} this backend itself relayed. Full priority, notified. */
    CONFIRMED,
    /** No matching UnlockRequest; secondary heuristic score &gt;=70. Full priority, notified. */
    UNEXPLAINED_HIGH,
    /** No matching UnlockRequest; secondary heuristic score 40-69. Low priority, notified. */
    UNEXPLAINED_VERIFY,
    /** No matching UnlockRequest; secondary heuristic score &lt;40. Log-only, never notified. */
    SUPPRESSED
}
