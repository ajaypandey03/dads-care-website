package com.dadscare.backend.site;

/** Thrown when mapping a device to a shutter unit that already has a different device mapped to it. */
public class ShutterUnitAlreadyMappedException extends RuntimeException {
    public ShutterUnitAlreadyMappedException(Long shutterUnitId) {
        super("Shutter unit " + shutterUnitId + " already has a device mapped to it — unassign it first");
    }
}
