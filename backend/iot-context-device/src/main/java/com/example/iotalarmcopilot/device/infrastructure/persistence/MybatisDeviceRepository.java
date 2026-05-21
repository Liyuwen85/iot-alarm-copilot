package com.example.iotalarmcopilot.device.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.device.domain.model.Device;
import com.example.iotalarmcopilot.device.domain.model.DeviceCode;
import com.example.iotalarmcopilot.device.domain.repository.DeviceRepository;
import com.example.iotalarmcopilot.device.domain.repository.DeviceSaveResult;
import com.example.iotalarmcopilot.device.domain.repository.DeviceUpdateResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓储实现
 */
@Repository
public class MybatisDeviceRepository implements DeviceRepository {

    private final DeviceMapper deviceMapper;

    public MybatisDeviceRepository(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    @Override
    public DeviceSaveResult saveIfAbsent(Device device) {
        DeviceRecord record = DeviceRecord.fromDomain(device);
        int insertedRows = deviceMapper.insertIgnore(record);
        DeviceRecord savedRecord = deviceMapper.selectByDeviceCode(device.deviceCode().value());
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load device. deviceCode=" + device.deviceCode().value());
        }
        return new DeviceSaveResult(savedRecord.toDomain(), insertedRows == 1);
    }

    @Override
    public DeviceUpdateResult updateIfUnchanged(Device current, Device target) {
        DeviceRecord currentRecord = DeviceRecord.fromDomain(current);
        DeviceRecord targetRecord = DeviceRecord.fromDomain(target);
        int updatedRows = deviceMapper.updateIfUnchanged(currentRecord, targetRecord);
        if (updatedRows > 1) {
            throw new BaseDomainException("Unexpected updated rows for device update. deviceCode=" + target.deviceCode().value());
        }
        Device latest = findByDeviceCode(target.deviceCode())
                .orElseThrow(() -> new BaseDomainException("Failed to reload device. deviceCode=" + target.deviceCode().value()));
        return new DeviceUpdateResult(latest, updatedRows == 1);
    }

    @Override
    public Optional<Device> findByDeviceCode(DeviceCode deviceCode) {
        return Optional.ofNullable(deviceMapper.selectByDeviceCode(deviceCode.value()))
                .map(DeviceRecord::toDomain);
    }

    @Override
    public List<Device> findRecent(int limit) {
        return deviceMapper.selectRecent(limit).stream()
                .map(DeviceRecord::toDomain)
                .toList();
    }
}
