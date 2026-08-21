package com.dadscare.backend.user;

/** Thrown by self-service password change when the supplied current password doesn't match. */
public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Current password is incorrect");
    }
}
