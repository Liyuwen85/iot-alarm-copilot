package com.example.iotalarmcopilot.device.application;

import com.example.iotalarmcopilot.contract.event.DeviceGroupChangedEvent;
import com.example.iotalarmcopilot.contract.event.DeviceRegisteredEvent;
import com.example.iotalarmcopilot.contract.event.DeviceShadowUpdatedEvent;
import com.example.iotalarmcopilot.contract.event.DeviceStatusChangedEvent;
import com.example.iotalarmcopilot.device.domain.model.*;
import com.example.iotalarmcopilot.device.domain.repository.DeviceRepository;
import com.example.iotalarmcopilot.device.domain.repository.DeviceSaveResult;
import com.example.iotalarmcopilot.device.domain.repository.DeviceUpdateResult;
import com.example.iotalarmcopilot.device.domain.repository.ProductModelRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 设备应用服务。主要生命周期管理
 */
@Service
public class DeviceApplicationService {

    private final DeviceRepository deviceRepository;
    private final ProductModelRepository productModelRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DeviceApplicationService(
            DeviceRepository deviceRepository,
            ProductModelRepository productModelRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.deviceRepository = deviceRepository;
        this.productModelRepository = productModelRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public Device register(RegisterDeviceCommand command) {
        ProductModel productModel = loadProductModel(command.productCode());
        DeviceSaveResult saveResult = deviceRepository.saveIfAbsent(Device.register(
                new DeviceCode(command.deviceCode()),
                productModel.productCode(),
                command.deviceName(),
                toGroupCode(command.groupCode()),
                command.registeredAt()));
        if (saveResult.created()) {
            publishRegistered(saveResult.device());
        }
        return saveResult.device();
    }

    @Transactional
    public Device activate(ActivateDeviceCommand command) {
        Device current = load(command.deviceCode());
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.activate(command.activatedAt()));
        if (updateResult.changed() && updateResult.device().status() != current.status()) {
            publishStatusChanged(updateResult.device());
        }
        return updateResult.device();
    }

    @Transactional
    public Device startMaintenance(StartMaintenanceCommand command) {
        Device current = load(command.deviceCode());
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.startMaintenance(command.startedAt()));
        if (updateResult.changed() && updateResult.device().status() != current.status()) {
            publishStatusChanged(updateResult.device());
        }
        return updateResult.device();
    }

    @Transactional
    public Device finishMaintenance(FinishMaintenanceCommand command) {
        Device current = load(command.deviceCode());
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.finishMaintenance(command.finishedAt()));
        if (updateResult.changed() && updateResult.device().status() != current.status()) {
            publishStatusChanged(updateResult.device());
        }
        return updateResult.device();
    }

    @Transactional
    public Device disable(DisableDeviceCommand command) {
        Device current = load(command.deviceCode());
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.disable(command.disabledAt()));
        if (updateResult.changed() && updateResult.device().status() != current.status()) {
            publishStatusChanged(updateResult.device());
        }
        return updateResult.device();
    }

    @Transactional
    public Device retire(RetireDeviceCommand command) {
        Device current = load(command.deviceCode());
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.retire(command.retiredAt()));
        if (updateResult.changed() && updateResult.device().status() != current.status()) {
            publishStatusChanged(updateResult.device());
        }
        return updateResult.device();
    }

    @Transactional
    public Device changeGroup(ChangeDeviceGroupCommand command) {
        Device current = load(command.deviceCode());
        String oldGroupCode = current.groupCodeValue();
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.changeGroup(
                toGroupCode(command.groupCode()),
                command.changedAt()));
        Device saved = updateResult.device();
        if (updateResult.changed() && !Objects.equals(oldGroupCode, saved.groupCodeValue())) {
            applicationEventPublisher.publishEvent(new DeviceGroupChangedEvent(
                    saved.deviceCode().value(),
                    oldGroupCode,
                    saved.groupCodeValue(),
                    command.changedAt()));
        }
        return saved;
    }

    @Transactional
    public Device updateShadow(UpdateDeviceShadowCommand command) {
        Device current = load(command.deviceCode());
        ProductModel productModel = loadProductModel(current.productCode().value());
        productModel.validateShadowDocument(command.shadowDocument());
        DeviceUpdateResult updateResult = deviceRepository.updateIfUnchanged(current, current.updateShadow(
                command.shadowDocument(),
                command.updatedAt()));
        Device saved = updateResult.device();
        if (updateResult.changed()) {
            applicationEventPublisher.publishEvent(new DeviceShadowUpdatedEvent(
                    saved.deviceCode().value(),
                    saved.shadowVersion(),
                    saved.shadowDocument(),
                    saved.shadowUpdatedAt()));
        }
        return saved;
    }

    private Device load(String deviceCode) {
        return deviceRepository.findByDeviceCode(new DeviceCode(deviceCode))
                .orElseThrow(() -> new IllegalArgumentException("Device not found. deviceCode=" + deviceCode));
    }

    private ProductModel loadProductModel(String productCode) {
        return productModelRepository.findByProductCode(new ProductCode(productCode))
                .orElseThrow(() -> new IllegalArgumentException("ProductModel not found. productCode=" + productCode));
    }

    private DeviceGroupCode toGroupCode(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return null;
        }
        return new DeviceGroupCode(groupCode);
    }

    private void publishRegistered(Device device) {
        applicationEventPublisher.publishEvent(new DeviceRegisteredEvent(
                device.deviceCode().value(),
                device.productCode().value(),
                device.deviceName(),
                device.groupCodeValue(),
                device.registeredAt()));
    }

    private void publishStatusChanged(Device device) {
        applicationEventPublisher.publishEvent(new DeviceStatusChangedEvent(
                device.deviceCode().value(),
                device.status().name(),
                device.statusChangedAt()));
    }
}
