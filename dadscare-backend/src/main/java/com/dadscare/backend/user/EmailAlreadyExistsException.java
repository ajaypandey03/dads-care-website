package com.dadscare.backend.user;

/** Thrown when inviting a user whose email is already registered (emails are globally unique). */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("A user with email " + email + " already exists");
    }
}
