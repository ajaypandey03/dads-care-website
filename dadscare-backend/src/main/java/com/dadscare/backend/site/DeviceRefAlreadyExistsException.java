package com.dadscare.backend.site;

/** Thrown when registering/editing a device to a velosyssDeviceRef another device already has. */
public class DeviceRefAlreadyExistsException extends RuntimeException {
    public DeviceRefAlreadyExistsException(String ref) {
        super("A device with reference \"" + ref + "\" already exists");
    }
}
