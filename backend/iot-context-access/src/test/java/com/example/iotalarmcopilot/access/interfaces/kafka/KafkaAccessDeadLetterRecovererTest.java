package com.example.iotalarmcopilot.access.interfaces.kafka;

import com.example.iotalarmcopilot.access.application.AccessDeadLetterCaptureApplicationService;
import com.example.iotalarmcopilot.access.application.RecordAccessDeadLetterCommand;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaAccessDeadLetterRecovererTest {

    @Test
    void should_capture_dead_letter_and_delegate_to_kafka_recoverer() {
        KafkaAccessProperties properties = new KafkaAccessProperties();
        AccessDeadLetterCaptureApplicationService captureApplicationService = mock(AccessDeadLetterCaptureApplicationService.class);
        ConsumerRecordRecoverer delegate = mock(ConsumerRecordRecoverer.class);
        KafkaAccessDeadLetterRecoverer recoverer =
                new KafkaAccessDeadLetterRecoverer(properties, captureApplicationService, delegate);
        ConsumerRecord<String, KafkaTelemetryEnvelope> record = new ConsumerRecord<>(
                "iot.telemetry.raw",
                0,
                12L,
                "dev-01",
                new KafkaTelemetryEnvelope("iot/dev-01/telemetry", "{\"deviceId\":\"dev-01\"}"));
        RuntimeException exception = new RuntimeException("processing failed");

        when(captureApplicationService.record(any(RecordAccessDeadLetterCommand.class))).thenReturn(null);

        recoverer.accept(record, exception);

        verify(captureApplicationService).record(any(RecordAccessDeadLetterCommand.class));
        verify(delegate).accept(record, exception);
    }

    @Test
    void should_still_delegate_when_capture_storage_fails() {
        KafkaAccessProperties properties = new KafkaAccessProperties();
        AccessDeadLetterCaptureApplicationService captureApplicationService = mock(AccessDeadLetterCaptureApplicationService.class);
        ConsumerRecordRecoverer delegate = mock(ConsumerRecordRecoverer.class);
        KafkaAccessDeadLetterRecoverer recoverer =
                new KafkaAccessDeadLetterRecoverer(properties, captureApplicationService, delegate);
        ConsumerRecord<String, KafkaTelemetryEnvelope> record = new ConsumerRecord<>(
                "iot.telemetry.raw",
                1,
                22L,
                "dev-02",
                new KafkaTelemetryEnvelope("iot/dev-02/telemetry", "{\"deviceId\":\"dev-02\"}"));
        RuntimeException exception = new RuntimeException("processing failed");

        doThrow(new RuntimeException("db down")).when(captureApplicationService).record(any(RecordAccessDeadLetterCommand.class));

        assertDoesNotThrow(() -> recoverer.accept(record, exception));

        verify(captureApplicationService).record(any(RecordAccessDeadLetterCommand.class));
        verify(delegate).accept(record, exception);
    }
}
