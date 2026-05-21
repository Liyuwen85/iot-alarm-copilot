package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeviceTest {

    @Test
    void should_transition_from_registered_to_activated() {
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");
        Device device = Device.register(
                new DeviceCode("dev-01"),
                new ProductCode("prod-sensor"),
                "Boiler Sensor",
                new DeviceGroupCode("factory-a"),
                registeredAt);

        Device activated = device.activate(Instant.parse("2026-05-18T10:05:00Z"));

        assertEquals(DeviceStatus.ACTIVATED, activated.status());
    }

    @Test
    void should_reject_maintenance_when_device_not_activated() {
        Instant registeredAt = Instant.parse("2026-05-18T10:00:00Z");
        Device device = Device.register(
                new DeviceCode("dev-01"),
                new ProductCode("prod-sensor"),
                "Boiler Sensor",
                null,
                registeredAt);

        assertThrows(BaseDomainException.class, () ->
                device.startMaintenance(Instant.parse("2026-05-18T10:05:00Z")));
    }
}
