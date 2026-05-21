package com.example.iotalarmcopilot.device.application;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.contract.event.DeviceRegisteredEvent;
import com.example.iotalarmcopilot.contract.event.DeviceShadowUpdatedEvent;
import com.example.iotalarmcopilot.device.domain.model.CapabilityCode;
import com.example.iotalarmcopilot.device.domain.model.Device;
import com.example.iotalarmcopilot.device.domain.model.DeviceCode;
import com.example.iotalarmcopilot.device.domain.model.DeviceStatus;
import com.example.iotalarmcopilot.device.domain.model.ProductCode;
import com.example.iotalarmcopilot.device.domain.model.ProductModel;
import com.example.iotalarmcopilot.device.domain.model.ShadowSchema;
import com.example.iotalarmcopilot.device.domain.model.TelemetryMetricDefinition;
import com.example.iotalarmcopilot.device.domain.model.TelemetrySchema;
import com.example.iotalarmcopilot.device.domain.model.TelemetryTransformType;
import com.example.iotalarmcopilot.device.domain.model.ThingModel;
import com.example.iotalarmcopilot.device.domain.repository.DeviceRepository;
import com.example.iotalarmcopilot.device.domain.repository.DeviceSaveResult;
import com.example.iotalarmcopilot.device.domain.repository.DeviceUpdateResult;
import com.example.iotalarmcopilot.device.domain.repository.ProductModelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceApplicationServiceTest {

    @Test
    void should_publish_registered_event_when_device_is_new() {
        DeviceRepository repository = mock(DeviceRepository.class);
        ProductModelRepository productModelRepository = mock(ProductModelRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DeviceApplicationService service = new DeviceApplicationService(repository, productModelRepository, publisher);
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");
        ProductModel productModel = productModel(registeredAt);
        Device saved = new Device(
                1L,
                new DeviceCode("dev-01"),
                new ProductCode("prod-sensor"),
                "Boiler Sensor",
                null,
                DeviceStatus.REGISTERED,
                null,
                registeredAt,
                registeredAt,
                registeredAt,
                registeredAt);

        when(productModelRepository.findByProductCode(new ProductCode("prod-sensor"))).thenReturn(Optional.of(productModel));
        when(repository.saveIfAbsent(any(Device.class))).thenReturn(new DeviceSaveResult(saved, true));

        service.register(new RegisterDeviceCommand("dev-01", "prod-sensor", "Boiler Sensor", null, registeredAt));

        verify(publisher).publishEvent(new DeviceRegisteredEvent(
                "dev-01",
                "prod-sensor",
                "Boiler Sensor",
                null,
                registeredAt));
    }

    @Test
    void should_publish_shadow_updated_event_when_shadow_changes() {
        DeviceRepository repository = mock(DeviceRepository.class);
        ProductModelRepository productModelRepository = mock(ProductModelRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DeviceApplicationService service = new DeviceApplicationService(repository, productModelRepository, publisher);
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-05-18T10:10:00Z");
        ProductModel productModel = productModel(registeredAt);
        Device device = new Device(
                1L,
                new DeviceCode("dev-01"),
                new ProductCode("prod-sensor"),
                "Boiler Sensor",
                null,
                DeviceStatus.ACTIVATED,
                null,
                registeredAt,
                registeredAt,
                registeredAt,
                registeredAt);
        Device updated = device.updateShadow(
                "{\"reported\":{\"temperature\":36.5},\"desired\":{\"sampleInterval\":10}}",
                updatedAt);

        when(repository.findByDeviceCode(new DeviceCode("dev-01"))).thenReturn(Optional.of(device));
        when(productModelRepository.findByProductCode(new ProductCode("prod-sensor"))).thenReturn(Optional.of(productModel));
        when(repository.updateIfUnchanged(any(Device.class), any(Device.class)))
                .thenReturn(new DeviceUpdateResult(updated, true));

        service.updateShadow(new UpdateDeviceShadowCommand(
                "dev-01",
                "{\"reported\":{\"temperature\":36.5},\"desired\":{\"sampleInterval\":10}}",
                updatedAt));

        verify(publisher).publishEvent(new DeviceShadowUpdatedEvent(
                "dev-01",
                1L,
                "{\"reported\":{\"temperature\":36.5},\"desired\":{\"sampleInterval\":10}}",
                updatedAt));
    }

    @Test
    void should_not_publish_shadow_event_when_other_node_already_updated_device() {
        DeviceRepository repository = mock(DeviceRepository.class);
        ProductModelRepository productModelRepository = mock(ProductModelRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DeviceApplicationService service = new DeviceApplicationService(repository, productModelRepository, publisher);
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-05-18T10:10:00Z");
        ProductModel productModel = productModel(registeredAt);
        Device current = new Device(
                1L,
                new DeviceCode("dev-01"),
                new ProductCode("prod-sensor"),
                "Boiler Sensor",
                null,
                DeviceStatus.ACTIVATED,
                null,
                registeredAt,
                registeredAt,
                registeredAt,
                registeredAt);
        Device latest = current.updateShadow(
                "{\"reported\":{\"temperature\":36.6},\"desired\":{\"sampleInterval\":5}}",
                Instant.parse("2026-05-18T10:11:00Z"));

        when(repository.findByDeviceCode(new DeviceCode("dev-01"))).thenReturn(Optional.of(current));
        when(productModelRepository.findByProductCode(new ProductCode("prod-sensor"))).thenReturn(Optional.of(productModel));
        when(repository.updateIfUnchanged(any(Device.class), any(Device.class)))
                .thenReturn(new DeviceUpdateResult(latest, false));

        service.updateShadow(new UpdateDeviceShadowCommand(
                "dev-01",
                "{\"reported\":{\"temperature\":36.5},\"desired\":{\"sampleInterval\":10}}",
                updatedAt));

        verify(publisher, never()).publishEvent(any(DeviceShadowUpdatedEvent.class));
    }

    @Test
    void should_reject_register_when_product_model_not_found() {
        DeviceRepository repository = mock(DeviceRepository.class);
        ProductModelRepository productModelRepository = mock(ProductModelRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DeviceApplicationService service = new DeviceApplicationService(repository, productModelRepository, publisher);
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");

        when(productModelRepository.findByProductCode(new ProductCode("prod-missing"))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.register(new RegisterDeviceCommand("dev-01", "prod-missing", "Boiler Sensor", null, registeredAt)));
    }

    @Test
    void should_reject_shadow_update_when_document_not_match_product_model() {
        DeviceRepository repository = mock(DeviceRepository.class);
        ProductModelRepository productModelRepository = mock(ProductModelRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DeviceApplicationService service = new DeviceApplicationService(repository, productModelRepository, publisher);
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-05-18T10:10:00Z");
        ProductModel productModel = productModel(registeredAt);
        Device device = new Device(
                1L,
                new DeviceCode("dev-01"),
                new ProductCode("prod-sensor"),
                "Boiler Sensor",
                null,
                DeviceStatus.ACTIVATED,
                null,
                registeredAt,
                registeredAt,
                registeredAt,
                registeredAt);

        when(repository.findByDeviceCode(new DeviceCode("dev-01"))).thenReturn(Optional.of(device));
        when(productModelRepository.findByProductCode(new ProductCode("prod-sensor"))).thenReturn(Optional.of(productModel));

        assertThrows(BaseDomainException.class, () ->
                service.updateShadow(new UpdateDeviceShadowCommand(
                        "dev-01",
                        "{\"reported\":{\"temperature\":36.5}}",
                        updatedAt)));
    }

    private static ProductModel productModel(Instant registeredAt) {
        return new ProductModel(
                1L,
                new ProductCode("prod-sensor"),
                "Environment Sensor",
                List.of(new CapabilityCode("temperature")),
                new TelemetrySchema(
                        List.of(new TelemetryMetricDefinition(
                                new CapabilityCode("temperature"),
                                "/temperature",
                                null,
                                TelemetryTransformType.IDENTITY,
                                null,
                                null,
                                true,
                                "C",
                                null,
                                null)),
                        List.of()),
                new ShadowSchema(List.of("\"temperature\""), List.of("\"sampleInterval\"")),
                ThingModel.empty(),
                registeredAt,
                registeredAt);
    }
}
