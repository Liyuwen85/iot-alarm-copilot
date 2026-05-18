package com.example.iotalarmcopilot.bridge;

/**
 * 统一为遥测消息
 */
public record KafkaTelemetryEnvelope(
        String topic,
        String payload) {
}
