-- Phase 4: mobile app support. Push token registration for dadscare-mobile
-- (PUT /api/v1/me/push-token) — no new tables needed for Site/Device listing, those
-- reuse existing tables from V1.

ALTER TABLE users
    ADD COLUMN push_token VARCHAR(255) NULL AFTER status;
