package com.dadscare.backend.unlock;

public enum UnlockRequestStatus {
    /** Created, not yet relayed to Velosyss. */
    PENDING,
    /** Relayed successfully — Velosyss accepted the command for delivery. This is the
     *  status {@link com.dadscare.backend.alert.RulesEngineService} matches against. */
    RELAYED,
    /** Velosyss rejected the command outright (e.g. LOCK/UNLOCK not yet supported, or
     *  the device is offline with no queueing — see Velosyss-side prerequisites). */
    FAILED
}
