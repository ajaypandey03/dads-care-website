-- Phase 1 foundation schema: tenants, sites/devices, users, master data, sequence
-- counters, and raw telemetry. Phase 2 (forms, unlock requests, alerts, notifications)
-- adds its own migrations on top of this one — do not add those tables here.

CREATE TABLE organizations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255)    NOT NULL,
    slug          VARCHAR(255)    NOT NULL,
    code_prefix   VARCHAR(10)     NOT NULL,
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP(6)    NOT NULL,
    updated_at    TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_organizations_slug (slug)
) ENGINE = InnoDB;

CREATE TABLE sites (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id  BIGINT          NOT NULL,
    name             VARCHAR(255)    NOT NULL,
    godown_code      VARCHAR(50)     NOT NULL,
    address          VARCHAR(500),
    latitude         DOUBLE,
    longitude        DOUBLE,
    timezone         VARCHAR(50)     NOT NULL DEFAULT 'Asia/Kolkata',
    status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP(6)    NOT NULL,
    updated_at       TIMESTAMP(6)    NOT NULL,
    KEY idx_sites_organization_id (organization_id),
    CONSTRAINT fk_sites_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE = InnoDB;

CREATE TABLE shutter_units (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_id       BIGINT          NOT NULL,
    label         VARCHAR(255)    NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP(6)    NOT NULL,
    updated_at    TIMESTAMP(6)    NOT NULL,
    KEY idx_shutter_units_site_id (site_id),
    CONSTRAINT fk_shutter_units_site FOREIGN KEY (site_id) REFERENCES sites (id)
) ENGINE = InnoDB;

CREATE TABLE devices (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id       BIGINT          NOT NULL,
    shutter_unit_id       BIGINT,
    velosyss_device_ref   VARCHAR(100)    NOT NULL,
    type                  VARCHAR(30)     NOT NULL DEFAULT 'DIGITAL_LOCK',
    status                VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_online             BOOLEAN         NOT NULL DEFAULT FALSE,
    last_seen_at          TIMESTAMP(6),
    last_battery_pct      INT,
    created_at            TIMESTAMP(6)    NOT NULL,
    updated_at            TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_devices_velosyss_device_ref (velosyss_device_ref),
    KEY idx_devices_organization_id (organization_id),
    KEY idx_devices_shutter_unit_id (shutter_unit_id),
    CONSTRAINT fk_devices_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_devices_shutter_unit FOREIGN KEY (shutter_unit_id) REFERENCES shutter_units (id)
) ENGINE = InnoDB;

CREATE TABLE users (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id  BIGINT          NOT NULL,
    name             VARCHAR(255)    NOT NULL,
    email            VARCHAR(255)    NOT NULL,
    phone            VARCHAR(30),
    password_hash    VARCHAR(255)    NOT NULL,
    role             VARCHAR(30)     NOT NULL DEFAULT 'VIEWER',
    mfa_enabled      BOOLEAN         NOT NULL DEFAULT FALSE,
    status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP(6)    NOT NULL,
    updated_at       TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_organization_id (organization_id),
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE = InnoDB;

CREATE TABLE user_site_access (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT          NOT NULL,
    site_id       BIGINT          NOT NULL,
    role          VARCHAR(30)     NOT NULL,
    created_at    TIMESTAMP(6)    NOT NULL,
    updated_at    TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_user_site_access_user_site (user_id, site_id),
    KEY idx_user_site_access_site_id (site_id),
    CONSTRAINT fk_user_site_access_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_site_access_site FOREIGN KEY (site_id) REFERENCES sites (id)
) ENGINE = InnoDB;

-- Backing store for SequenceCounterService's atomic per-org reference-code generator.
-- Rows are managed via INSERT ... ON DUPLICATE KEY UPDATE, never plain JPA save().
CREATE TABLE sequence_counters (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id   BIGINT          NOT NULL,
    counter_type      VARCHAR(50)     NOT NULL,
    next_value        BIGINT          NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sequence_counters_org_type (organization_id, counter_type)
) ENGINE = InnoDB;

CREATE TABLE product_masters (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id  BIGINT          NOT NULL,
    name             VARCHAR(255)    NOT NULL,
    unit             VARCHAR(20)     NOT NULL,
    active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP(6)    NOT NULL,
    updated_at       TIMESTAMP(6)    NOT NULL,
    KEY idx_product_masters_organization_id (organization_id),
    CONSTRAINT fk_product_masters_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE = InnoDB;

CREATE TABLE transporter_masters (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id  BIGINT          NOT NULL,
    name             VARCHAR(255)    NOT NULL,
    code             VARCHAR(30),
    active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP(6)    NOT NULL,
    updated_at       TIMESTAMP(6)    NOT NULL,
    KEY idx_transporter_masters_organization_id (organization_id),
    CONSTRAINT fk_transporter_masters_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE = InnoDB;

-- Immutable ledger of every event received from Velosyss's outbound push. See
-- WebhookService — rows are inserted once and never updated.
CREATE TABLE raw_events (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT          NOT NULL,
    device_id           BIGINT          NOT NULL,
    velosyss_event_id   VARCHAR(100)    NOT NULL,
    event_type          VARCHAR(30)     NOT NULL,
    lock_status         VARCHAR(30),
    latitude            DOUBLE,
    longitude           DOUBLE,
    speed               DOUBLE,
    battery_pct         INT,
    motion_magnitude    DOUBLE,
    tamper_flag         BOOLEAN,
    source_sensor       VARCHAR(50),
    event_timestamp     TIMESTAMP(6)    NOT NULL,
    received_at         TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_raw_events_velosyss_event_id (velosyss_event_id),
    KEY idx_raw_events_organization_id (organization_id, event_timestamp),
    KEY idx_raw_events_device_id (device_id, event_timestamp),
    CONSTRAINT fk_raw_events_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_raw_events_device FOREIGN KEY (device_id) REFERENCES devices (id)
) ENGINE = InnoDB;
