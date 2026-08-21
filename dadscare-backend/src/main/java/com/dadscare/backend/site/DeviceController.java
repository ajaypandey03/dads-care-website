package com.dadscare.backend.site;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final SiteService siteService;

    @GetMapping
    public List<DeviceDto> list() {
        return siteService.listDevices();
    }

    @PostMapping
    public DeviceDto create(@Valid @RequestBody CreateDeviceRequest request) {
        return siteService.createDevice(request);
    }

    @PutMapping("/{id}")
    public DeviceDto update(@PathVariable Long id, @Valid @RequestBody UpdateDeviceRequest request) {
        return siteService.updateDevice(id, request);
    }
}
