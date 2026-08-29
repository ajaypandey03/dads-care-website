package com.dadscare.backend.velosyss;

import com.dadscare.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * A single named cursor for platform-level polling against Velosyss's read APIs — today
 * just {@link #EVENTS_CURSOR}, for {@code GET /locks/events?since=} reconciliation (see
 * VelosyssPollingService). Platform-level, not per-Organization: Velosyss's events feed
 * isn't scoped that way either (§5), and events are attributed to an Organization via
 * their device on ingest, same as the webhook path.
 */
@Getter
@Setter
@Entity
@Table(name = "integration_cursors")
public class IntegrationCursor extends BaseEntity {

    public static final String EVENTS_CURSOR = "velosyss-events";

    @Column(name = "cursor_name", nullable = false, unique = true)
    private String cursorName;

    @Column(name = "cursor_value", nullable = false)
    private Instant cursorValue;
}
