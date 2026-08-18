package com.dadscare.backend.alert;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceCalibrationRepository extends JpaRepository<DeviceCalibration, Long> {

    Optional<DeviceCalibration> findByDeviceId(Long deviceId);
}
