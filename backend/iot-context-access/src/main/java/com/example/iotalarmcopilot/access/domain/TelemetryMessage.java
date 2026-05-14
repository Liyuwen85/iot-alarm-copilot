package com.example.iotalarmcopilot.access.domain;

import com.example.iotalarmcopilot.contract.telemetry.TelemetryMetrics;
import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 外部协议报文解析后的接入消息
 *
 * @param deviceId
 * @param metrics
 * @param reportedAt
 * @param rawJson
 */
public record TelemetryMessage(
        String deviceId,
        TelemetryMetrics metrics,
        Instant reportedAt,
        String rawJson) {

    public TelemetryMessage {
        Objects.requireNonNull(metrics, "metrics must not be null");
        Objects.requireNonNull(reportedAt, "reportedAt must not be null");
        Objects.requireNonNull(rawJson, "rawJson must not be null");
        if (deviceId != null) {
            deviceId = deviceId.trim();
            if (deviceId.isBlank()) {
                deviceId = null;
            }
        }
        if (rawJson.isBlank()) {
            throw new BaseDomainException("rawJson must not be blank");
        }
    }
}
