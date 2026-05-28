package com.example.iotalarmcopilot.command.interfaces.kafka;

import java.util.Objects;

public record KafkaCommandEnvelope(
        String topic,
        String payload) {

    public KafkaCommandEnvelope {
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        if (topic.isBlank()) {
            throw new IllegalArgumentException("Kafka command topic must not be blank");
        }
        if (payload.isBlank()) {
            throw new IllegalArgumentException("Kafka command payload must not be blank");
        }
    }
}
