-- Aligns the schema with the real Velosyss Partner Integration Layer contract (see the
-- "Velosyss Lock Integration Guide" in Confluence), which differs from the assumptions
-- V1/V2 were built against:
--   * devices are correlated on webhook events by a `terminalId`, distinct from the
--     numeric lock id used in REST command/read paths (already stored as
--     velosyss_device_ref) — so devices gets a second identifier column.
--   * commands are relayed with Velosyss issuing the requestId (not us), and go through
--     a real async status lifecycle (QUEUED/DISPATCHED/DEVICE_OFFLINE/RESPONDED/EXPIRED)
--     reported via the COMMAND_RESULT webhook — so velosyss_request_id must be nullable
--     until the first successful API response, and unlock_requests needs outcome columns.
--   * `GET /locks/positions` gives live lat/lng/sealed/shackle/battery-mV in one poll —
--     devices gets columns to cache the latest poll for the dashboard.
--   * raw_events gets columns for the three real webhook event types (ALARM / SEAL_STATE
--     mapped to LOCK_OPEN|LOCK_CLOSE / COMMAND_RESULT) in place of the invented
--     TAMPER/MOTION/HEARTBEAT vocabulary that Velosyss never actually sends.

ALTER TABLE devices
    ADD COLUMN velosyss_terminal_id VARCHAR(64) NULL AFTER velosyss_device_ref,
    ADD COLUMN last_latitude        DOUBLE      NULL,
    ADD COLUMN last_longitude       DOUBLE      NULL,
    ADD COLUMN last_sealed          BOOLEAN     NULL,
    ADD COLUMN last_shackle_closed  BOOLEAN     NULL,
    ADD COLUMN last_battery_mv      INT         NULL,
    ADD UNIQUE KEY uk_devices_velosyss_terminal_id (velosyss_terminal_id);

ALTER TABLE unlock_requests
    MODIFY COLUMN velosyss_request_id VARCHAR(100) NULL,
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN succeeded     BOOLEAN      NULL,
    ADD COLUMN message       VARCHAR(500) NULL,
    ADD COLUMN dispatched_at TIMESTAMP(6) NULL,
    ADD COLUMN responded_at  TIMESTAMP(6) NULL,
    ADD COLUMN expired_at    TIMESTAMP(6) NULL;

ALTER TABLE raw_events
    ADD COLUMN alarm_code          VARCHAR(40)  NULL,
    ADD COLUMN alarm_description   VARCHAR(200) NULL,
    ADD COLUMN sealed              BOOLEAN      NULL,
    ADD COLUMN shackle_closed      BOOLEAN      NULL,
    ADD COLUMN command_request_id  VARCHAR(100) NULL,
    ADD COLUMN command_action      VARCHAR(20)  NULL,
    ADD COLUMN command_status      VARCHAR(20)  NULL,
    ADD COLUMN command_succeeded   BOOLEAN      NULL,
    ADD COLUMN command_message     VARCHAR(500) NULL;

-- Reconciliation cursor for the /locks/events polling safety net (see
-- VelosyssPollingService) — a single platform-level row, since Velosyss's events feed
-- isn't per-Organization.
CREATE TABLE integration_cursors (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cursor_name     VARCHAR(60)  NOT NULL,
    cursor_value    TIMESTAMP(6) NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    UNIQUE KEY uk_integration_cursors_name (cursor_name)
) ENGINE = InnoDB;
