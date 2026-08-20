package com.dadscare.backend.platform;

/** Thrown when an authenticated-but-not-platform-admin caller hits a /api/v1/platform/** endpoint. */
public class PlatformAdminRequiredException extends RuntimeException {
    public PlatformAdminRequiredException() {
        super("This endpoint is restricted to Dad's Care platform admins");
    }
}
