-- Phase 2: UnlockRequest + Velosyss command relay, Rules & Alerts Engine, per-device
-- calibration, feedback loop, and notification dispatch.

CREATE TABLE unlock_requests (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id        BIGINT          NOT NULL,
    device_id              BIGINT          NOT NULL,
    requested_by_user_id   BIGINT          NOT NULL,
    command_type           VARCHAR(20)     NOT NULL,
    status                 VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    velosyss_request_id    VARCHAR(100)    NOT NULL,
    relayed_at             TIMESTAMP(6),
    failure_reason         VARCHAR(500),
    created_at             TIMESTAMP(6)    NOT NULL,
    updated_at             TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_unlock_requests_velosyss_request_id (velosyss_request_id),
    KEY idx_unlock_requests_org_created (organization_id, created_at),
    -- Backs RulesEngineService's correlation-candidate lookup: same device, same
    -- command type, RELAYED, within a time window.
    KEY idx_unlock_requests_correlation (device_id, command_type, status, relayed_at),
    CONSTRAINT fk_unlock_requests_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_unlock_requests_device FOREIGN KEY (device_id) REFERENCES devices (id),
    CONSTRAINT fk_unlock_requests_user FOREIGN KEY (requested_by_user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE device_calibrations (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id                       BIGINT          NOT NULL,
    tamper_motion_weight            INT             NOT NULL DEFAULT 35,
    duration_weight                 INT             NOT NULL DEFAULT 25,
    escalate_threshold              INT             NOT NULL DEFAULT 70,
    verify_threshold                INT             NOT NULL DEFAULT 40,
    quick_reclose_window_seconds    INT             NOT NULL DEFAULT 3,
    created_at                      TIMESTAMP(6)    NOT NULL,
    updated_at                      TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_device_calibrations_device_id (device_id),
    CONSTRAINT fk_device_calibrations_device FOREIGN KEY (device_id) REFERENCES devices (id)
) ENGINE = InnoDB;

CREATE TABLE alerts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT          NOT NULL,
    device_id           BIGINT          NOT NULL,
    raw_event_id        BIGINT          NOT NULL,
    unlock_request_id   BIGINT,
    direction           VARCHAR(10)     NOT NULL,
    classification      VARCHAR(20)     NOT NULL,
    confidence_score    INT,
    sequence_code       VARCHAR(30),
    created_at          TIMESTAMP(6)    NOT NULL,
    updated_at          TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_alerts_raw_event_id (raw_event_id),
    KEY idx_alerts_org_created (organization_id, created_at),
    CONSTRAINT fk_alerts_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_alerts_device FOREIGN KEY (device_id) REFERENCES devices (id),
    CONSTRAINT fk_alerts_raw_event FOREIGN KEY (raw_event_id) REFERENCES raw_events (id),
    CONSTRAINT fk_alerts_unlock_request FOREIGN KEY (unlock_request_id) REFERENCES unlock_requests (id)
) ENGINE = InnoDB;

CREATE TABLE feedback_entries (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id                BIGINT          NOT NULL,
    submitted_by_user_id    BIGINT          NOT NULL,
    was_correct             BOOLEAN         NOT NULL,
    comment                 VARCHAR(500),
    created_at              TIMESTAMP(6)    NOT NULL,
    updated_at              TIMESTAMP(6)    NOT NULL,
    KEY idx_feedback_entries_alert_id (alert_id),
    CONSTRAINT fk_feedback_entries_alert FOREIGN KEY (alert_id) REFERENCES alerts (id),
    CONSTRAINT fk_feedback_entries_user FOREIGN KEY (submitted_by_user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE notifications (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT          NOT NULL,
    alert_id            BIGINT          NOT NULL,
    channel             VARCHAR(20)     NOT NULL,
    recipient           VARCHAR(255)    NOT NULL,
    body                TEXT            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    sent_at             TIMESTAMP(6),
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMP(6)    NOT NULL,
    updated_at          TIMESTAMP(6)    NOT NULL,
    KEY idx_notifications_alert_id (alert_id),
    CONSTRAINT fk_notifications_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_notifications_alert FOREIGN KEY (alert_id) REFERENCES alerts (id)
) ENGINE = InnoDB;
