package com.dadscare.backend.site;

/**
 * A shutter's last-known open/closed state, derived from its device's most recent
 * LOCK_OPEN vs LOCK_CLOSE telemetry (see {@link SiteService}) — not a stored field, since
 * it's inferred the same way the rest of this platform infers shutter state from lock
 * events (see "Lock vs Shutter" in Confluence). {@code UNKNOWN} means no lock events have
 * been received yet, or the shutter has no device mapped to it at all.
 */
public enum ShutterState {
    OPEN,
    CLOSED,
    UNKNOWN
}
