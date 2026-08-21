package com.dadscare.backend.unlock;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UnlockRequestController {

    private final UnlockRequestService unlockRequestService;

    /** VIEWER is deliberately excluded — a read-only role must never be able to operate a physical shutter. */
    @PreAuthorize("hasAnyRole('ORG_ADMIN','SITE_MANAGER','OPERATOR')")
    @PostMapping("/devices/{deviceId}/unlock-requests")
    public UnlockRequestDto create(
            @PathVariable Long deviceId, @Valid @RequestBody CreateUnlockRequestRequest request) {
        return unlockRequestService.create(deviceId, request);
    }

    @GetMapping("/unlock-requests")
    public List<UnlockRequestDto> list() {
        return unlockRequestService.listForOrganization();
    }
}
