package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractMqttCommandConsumerTest {

    @Test
    void shouldMatchConcreteTopicAgainstSingleLevelWildcardFilter() {
        assertTrue(AbstractMqttCommandConsumer.matchesTopic(
                "iot/device/+/commands",
                "iot/device/demo-002/commands"));
    }

    @Test
    void shouldRejectTopicOutsideWildcardFilter() {
        assertFalse(AbstractMqttCommandConsumer.matchesTopic(
                "iot/device/+/commands",
                "iot/demo-002/telemetry"));
    }

    @Test
    void shouldMatchExactTopic() {
        assertTrue(AbstractMqttCommandConsumer.matchesTopic(
                "iot/device/demo-001/commands",
                "iot/device/demo-001/commands"));
    }
}
