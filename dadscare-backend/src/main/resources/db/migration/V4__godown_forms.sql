-- Phase 4: godown operational forms (Opening/Closing form data submitted alongside an
-- UnlockRequest) — inventory, trucks, labor, custom fields. See Godown Operational
-- Workflow in Confluence.

CREATE TABLE godown_forms (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    unlock_request_id     BIGINT          NOT NULL,
    organization_id       BIGINT          NOT NULL,
    labor_count           INT,
    remarks               VARCHAR(200),
    custom_fields_json    TEXT,
    created_at            TIMESTAMP(6)    NOT NULL,
    updated_at            TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_godown_forms_unlock_request_id (unlock_request_id),
    KEY idx_godown_forms_organization_id (organization_id),
    CONSTRAINT fk_godown_forms_unlock_request FOREIGN KEY (unlock_request_id) REFERENCES unlock_requests (id),
    CONSTRAINT fk_godown_forms_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE = InnoDB;

CREATE TABLE stock_lines (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    godown_form_id       BIGINT          NOT NULL,
    product_master_id    BIGINT          NOT NULL,
    quantity             INT             NOT NULL,
    unit                 VARCHAR(20)     NOT NULL,
    KEY idx_stock_lines_godown_form_id (godown_form_id),
    CONSTRAINT fk_stock_lines_godown_form FOREIGN KEY (godown_form_id) REFERENCES godown_forms (id),
    CONSTRAINT fk_stock_lines_product FOREIGN KEY (product_master_id) REFERENCES product_masters (id)
) ENGINE = InnoDB;

CREATE TABLE truck_entries (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    godown_form_id            BIGINT          NOT NULL,
    source                    VARCHAR(255)    NOT NULL,
    product_master_id         BIGINT          NOT NULL,
    vehicle_no                VARCHAR(30)     NOT NULL,
    transporter_master_id     BIGINT          NOT NULL,
    quantity                  INT             NOT NULL,
    waiting_since             TIMESTAMP(6),
    KEY idx_truck_entries_godown_form_id (godown_form_id),
    CONSTRAINT fk_truck_entries_godown_form FOREIGN KEY (godown_form_id) REFERENCES godown_forms (id),
    CONSTRAINT fk_truck_entries_product FOREIGN KEY (product_master_id) REFERENCES product_masters (id),
    CONSTRAINT fk_truck_entries_transporter FOREIGN KEY (transporter_master_id) REFERENCES transporter_masters (id)
) ENGINE = InnoDB;
