package com.dadscare.backend.platform;

/** Thrown when onboarding a new organization whose slug is already taken (slugs are globally unique). */
public class SlugAlreadyExistsException extends RuntimeException {
    public SlugAlreadyExistsException(String slug) {
        super("An organization with slug \"" + slug + "\" already exists");
    }
}
