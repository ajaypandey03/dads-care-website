-- Phase 3 follow-up: lets a small set of Dad's Care's own staff onboard new customer
-- organizations. A platform admin is still a normal `users` row belonging to one
-- Organization (see the "Dad's Care Internal" org seeded below) — this flag just
-- grants access to the cross-tenant /api/v1/platform/** endpoints on top of that.
ALTER TABLE users ADD COLUMN is_platform_admin BOOLEAN NOT NULL DEFAULT FALSE;
