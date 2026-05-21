package com.example.iotalarmcopilot.device.application;

import com.example.iotalarmcopilot.device.domain.model.Device;
import com.example.iotalarmcopilot.device.domain.model.DeviceCode;
import com.example.iotalarmcopilot.device.domain.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备查询应用服务
 */
@Service
public class DeviceQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final DeviceRepository deviceRepository;

    public DeviceQueryApplicationService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceVO get(String deviceCode) {
        return deviceRepository.findByDeviceCode(new DeviceCode(deviceCode))
                .map(this::toVO)
                .orElse(null);
    }

    public List<DeviceVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return deviceRepository.findRecent(safeLimit).stream()
                .map(this::toVO)
                .toList();
    }

    public DeviceVO toVO(Device device) {
        return new DeviceVO(
                device.id(),
                device.deviceCode().value(),
                device.productCode().value(),
                device.deviceName(),
                device.groupCodeValue(),
                device.status().name(),
                device.shadowVersion(),
                device.shadowDocument(),
                device.shadowUpdatedAt(),
                device.registeredAt(),
                device.statusChangedAt(),
                device.createdAt(),
                device.updatedAt());
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
