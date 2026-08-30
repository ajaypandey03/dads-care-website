package com.dadscare.backend.user;

/**
 * Self-service {@code PUT /api/v1/me/phone} — this is the number WhatsApp alerts go to
 * (see NotificationDispatcher), so letting a user set their own rather than requiring an
 * ORG_ADMIN to do it via the Team page matters for anyone who isn't one. Blank/null clears
 * it, which opts the user out of WhatsApp alerts entirely (see NotificationDispatcher#hasPhone).
 */
public record UpdatePhoneRequest(String phone) {}
