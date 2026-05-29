package com.example.iotalarmcopilot.mockdevice.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 网关设备数据快照
 */
public record Lwm2mDeviceSnapshot(
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        OffsetDateTime updatedAt) {

    public boolean isComplete() {
        return temperature != null && humidity != null;
    }

    public Lwm2mDeviceSnapshot withTemperature(BigDecimal value, OffsetDateTime ts) {
        return new Lwm2mDeviceSnapshot(deviceId, value, humidity, ts);
    }

    public Lwm2mDeviceSnapshot withHumidity(BigDecimal value, OffsetDateTime ts) {
        return new Lwm2mDeviceSnapshot(deviceId, temperature, value, ts);
    }

    public GatewayUplinkMessage toGatewayMessage(String gatewayId) {
        return new GatewayUplinkMessage(
                deviceId,
                gatewayId,
                "lwm2m-gateway",
                temperature,
                humidity,
                updatedAt.toString());
    }

    public static Lwm2mDeviceSnapshot empty(String deviceId) {
        return new Lwm2mDeviceSnapshot(deviceId, null, null, OffsetDateTime.now());
    }
}
