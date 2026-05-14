package com.example.iotalarmcopilot.access.domain;

import com.example.iotalarmcopilot.BaseDomainException;

/**
 * 接入层归一化策略
 */
public class TelemetryIngressPolicy {

    public TelemetryPayload normalize(String topic, TelemetryMessage message) {
        TelemetryTopic telemetryTopic = new TelemetryTopic(topic);
        String topicDeviceId = telemetryTopic.deviceId();
        validatePayloadDeviceId(topicDeviceId, message.deviceId());
        return new TelemetryPayload(
                topicDeviceId,
                message.metrics(),
                message.reportedAt(),
                message.rawJson());
    }

    private void validatePayloadDeviceId(String topicDeviceId, String payloadDeviceId) {
        if (payloadDeviceId != null && !payloadDeviceId.isBlank() && !topicDeviceId.equals(payloadDeviceId)) {
            throw new BaseDomainException("Payload deviceId does not match topic deviceId");
        }
    }
}
