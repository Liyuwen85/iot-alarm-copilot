package com.example.iotalarmcopilot.access.interfaces.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "iot.kafka")
public class KafkaAccessProperties {

    private boolean enabled = true;
    private String telemetryTopic = "iot.telemetry.raw";
    private String consumerGroup = "iot-platform-access";
    private final Retry retry = new Retry();
    private String deadLetterTopic;

    public String resolveDeadLetterTopic() {
        if (deadLetterTopic == null || deadLetterTopic.isBlank()) {
            return telemetryTopic + ".dlt";
        }
        return deadLetterTopic;
    }

    @Getter
    @Setter
    public static class Retry {

        private int maxRetries = 3;
        private long initialIntervalMs = 1_000L;
        private double multiplier = 2.0D;
        private long maxIntervalMs = 10_000L;
    }
}
