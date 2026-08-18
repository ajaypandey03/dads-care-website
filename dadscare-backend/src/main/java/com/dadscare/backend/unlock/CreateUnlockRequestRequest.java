package com.dadscare.backend.unlock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * The app-submitted form data goes here, alongside the command — see the Godown
 * Operational Workflow page in Confluence. All form fields are optional at the API level
 * (some tenants may not enforce the form-gate — see Open Decisions) but
 * {@code UnlockRequestService} persists whatever is present as a {@link
 * com.dadscare.backend.forms.GodownForm}.
 */
public record CreateUnlockRequestRequest(
        @NotNull CommandType commandType,
        List<@Valid StockLineRequest> stockLines,
        List<@Valid TruckEntryRequest> truckEntries,
        Integer laborCount,
        @Size(max = 200) String remarks,
        @Size(max = 10) List<@Valid CustomFieldRequest> customFields) {

    public record StockLineRequest(@NotNull Long productMasterId, @NotNull @Positive Integer quantity) {
    }

    public record TruckEntryRequest(
            @NotBlank String source,
            @NotNull Long productMasterId,
            @NotBlank String vehicleNo,
            @NotNull Long transporterMasterId,
            @NotNull @Positive Integer quantity,
            Instant waitingSince) {
    }

    public record CustomFieldRequest(@NotBlank String heading, @NotBlank String value) {
    }
}
