-- Storage for the tabular "Closing Report" the customer sends over WhatsApp today by
-- hand (see the pasted template: one row per stock-movement metric, one column per
-- product) — this migration only adds the data model; the WhatsApp message format itself
-- is a separate, not-yet-decided follow-up (see ClosingReportService javadoc).
--
-- One ClosingReport header per (site, report_date) — the day-level fields (labour count,
-- truck counts, remarks) — with one ClosingReportLine per ProductMaster hanging off it,
-- carrying the seven per-product metrics from the template.

CREATE TABLE closing_reports (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT          NOT NULL,
    site_id             BIGINT          NOT NULL,
    report_date         DATE            NOT NULL,
    labour_count        INT,
    completed_trucks    INT,
    standing_trucks     INT,
    remarks             VARCHAR(500),
    submitted_by_user_id BIGINT,
    created_at          TIMESTAMP(6)    NOT NULL,
    updated_at          TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_closing_reports_site_date (site_id, report_date),
    KEY idx_closing_reports_org_date (organization_id, report_date),
    CONSTRAINT fk_closing_reports_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_closing_reports_site FOREIGN KEY (site_id) REFERENCES sites (id),
    CONSTRAINT fk_closing_reports_user FOREIGN KEY (submitted_by_user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE closing_report_lines (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    closing_report_id          BIGINT          NOT NULL,
    product_master_id          BIGINT          NOT NULL,
    opening_stock               DECIMAL(12, 3)  NOT NULL DEFAULT 0,
    sale_qty                    DECIMAL(12, 3)  NOT NULL DEFAULT 0,
    received_qty                DECIMAL(12, 3)  NOT NULL DEFAULT 0,
    closing_stock                DECIMAL(12, 3)  NOT NULL DEFAULT 0,
    advance_billed_qty          DECIMAL(12, 3)  NOT NULL DEFAULT 0,
    aging_over_30_days_qty      DECIMAL(12, 3)  NOT NULL DEFAULT 0,
    waiting_trucks               INT             NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP(6)    NOT NULL,
    updated_at                  TIMESTAMP(6)    NOT NULL,
    UNIQUE KEY uk_closing_report_lines_report_product (closing_report_id, product_master_id),
    CONSTRAINT fk_closing_report_lines_report FOREIGN KEY (closing_report_id) REFERENCES closing_reports (id),
    CONSTRAINT fk_closing_report_lines_product FOREIGN KEY (product_master_id) REFERENCES product_masters (id)
) ENGINE = InnoDB;
