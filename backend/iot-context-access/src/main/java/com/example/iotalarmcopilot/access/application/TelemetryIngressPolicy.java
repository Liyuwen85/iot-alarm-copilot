package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.application.model.TelemetryMessage;
import com.example.iotalarmcopilot.access.application.model.TelemetryPayload;
import com.example.iotalarmcopilot.access.application.model.TelemetryTopic;

/**
 * 接入层归一化策略
 */
public class TelemetryIngressPolicy {

    public TelemetryPayload normalize(String topic, TelemetryMessage message) {
        return normalize(new TelemetryTopic(topic), message);
    }

    public TelemetryPayload normalize(TelemetryTopic telemetryTopic, TelemetryMessage message) {
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
