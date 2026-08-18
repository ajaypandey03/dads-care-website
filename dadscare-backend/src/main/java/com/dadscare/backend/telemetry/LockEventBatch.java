package com.dadscare.backend.telemetry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Velosyss's cron push sends a batch of events accumulated since its last successful run. */
public record LockEventBatch(@NotEmpty @Valid List<LockEventPayload> events) {
}
