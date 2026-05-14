package com.example.iotalarmcopilot.access.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 遥测主题值对象
 *
 * @param value
 */
public record TelemetryTopic(String value) {

    private static final String TOPIC_PREFIX = "iot";
    private static final String TOPIC_SUFFIX = "telemetry";

    public TelemetryTopic {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("Telemetry topic must not be blank");
        }
    }

    public String deviceId() {
        String[] segments = value.split("/");
        if (segments.length != 3 || !TOPIC_PREFIX.equals(segments[0]) || !TOPIC_SUFFIX.equals(segments[2])) {
            throw new BaseDomainException("Unsupported telemetry topic: " + value);
        }
        if (segments[1].isBlank()) {
            throw new BaseDomainException("Device id in topic must not be blank");
        }
        return segments[1];
    }
}
