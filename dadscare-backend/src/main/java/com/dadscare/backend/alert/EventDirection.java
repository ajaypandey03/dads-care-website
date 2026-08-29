package com.dadscare.backend.alert;

public enum EventDirection {
    OPEN,
    CLOSE,
    /** An ALARM webhook event (§4.2) — shackle cut, tamper, etc. Always alert-worthy, never scored. */
    ALARM
}
