package com.example.iotalarmcopilot.access.interfaces.kafka;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * Kafka 遥测入站消息，保留 topic + payload 语义，避免引入额外复杂模型。
 *
 * @param topic 原始 MQTT topic
 * @param payload 原始消息体
 */
public record KafkaTelemetryEnvelope(
        String topic,
        String payload) {

    public KafkaTelemetryEnvelope {
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        if (topic.isBlank()) {
            throw new BaseDomainException("Kafka telemetry topic must not be blank");
        }
        if (payload.isBlank()) {
            throw new BaseDomainException("Kafka telemetry payload must not be blank");
        }
    }
}
