package com.dadscare.backend.unlock;

/**
 * Mirrors Velosyss's real command lifecycle exactly (see §6.4 of the Integration Guide)
 * plus two states of our own for the part of the flow Velosyss never sees.
 */
public enum UnlockRequestStatus {
    /** Created, not yet sent to Velosyss (normally sub-second — this is our own pre-call state). */
    PENDING,
    /** Velosyss accepted the command, waiting to be sent to the lock. Not terminal. */
    QUEUED,
    /** Sent to the lock, awaiting its response. Not terminal. */
    DISPATCHED,
    /** Velosyss could not dispatch — the lock isn't currently connected. Terminal. */
    DEVICE_OFFLINE,
    /** The lock replied — check {@link UnlockRequest#getSucceeded()} for the actual outcome. Terminal. */
    RESPONDED,
    /** No response within Velosyss's timeout window. Terminal. */
    EXPIRED,
    /** We never got as far as Velosyss accepting it — network error, misconfiguration, or a 4xx/5xx
     *  on the initial POST (see {@link UnlockRequest#getFailureReason()}). Terminal. */
    FAILED
}
