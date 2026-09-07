-- Per-organization values for the WhatsApp alert template's variables that have no
-- backing data elsewhere in the model (see RmlConnectWhatsAppSender): the salutation
-- used in "Dear <...>," and the CFA name used in "CFA-<...>". Editable by Dad's Care
-- platform admins via PUT /api/v1/platform/organizations/{id}.
ALTER TABLE organizations
    ADD COLUMN whatsapp_salutation VARCHAR(100) NOT NULL DEFAULT 'Customer';

ALTER TABLE organizations
    ADD COLUMN whatsapp_cfa_name VARCHAR(255) NOT NULL DEFAULT '-';
