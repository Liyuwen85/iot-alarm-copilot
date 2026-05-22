package com.example.iotalarmcopilot.access.interfaces.kafka;

import com.example.iotalarmcopilot.access.application.TelemetryAccessApplicationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaTelemetryConsumerTest {

    @Test
    void should_delegate_to_access_application_service() {
        KafkaAccessProperties properties = new KafkaAccessProperties();
        TelemetryAccessApplicationService applicationService = mock(TelemetryAccessApplicationService.class);
        KafkaTelemetryConsumer consumer = new KafkaTelemetryConsumer(properties, applicationService);
        KafkaTelemetryEnvelope envelope = new KafkaTelemetryEnvelope("iot/dev-01/telemetry", "{\"deviceId\":\"dev-01\"}");

        doNothing().when(applicationService).ingestKafkaTelemetry(envelope.topic(), envelope.payload());

        assertDoesNotThrow(() -> consumer.onMessage(envelope));
        verify(applicationService).ingestKafkaTelemetry(envelope.topic(), envelope.payload());
    }

    @Test
    void should_rethrow_exception_for_kafka_retry_and_dead_letter_flow() {
        KafkaAccessProperties properties = new KafkaAccessProperties();
        TelemetryAccessApplicationService applicationService = mock(TelemetryAccessApplicationService.class);
        KafkaTelemetryConsumer consumer = new KafkaTelemetryConsumer(properties, applicationService);
        KafkaTelemetryEnvelope envelope = new KafkaTelemetryEnvelope("iot/dev-01/telemetry", "{\"deviceId\":\"dev-01\"}");
        RuntimeException expected = new RuntimeException("db unavailable");

        doThrow(expected).when(applicationService).ingestKafkaTelemetry(envelope.topic(), envelope.payload());

        RuntimeException actual = assertThrows(RuntimeException.class, () -> consumer.onMessage(envelope));

        assertSame(expected, actual);
        verify(applicationService).ingestKafkaTelemetry(envelope.topic(), envelope.payload());
    }
}
