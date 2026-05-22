package com.example.iotalarmcopilot.access.application;

import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLog;
import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLogRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessDeadLetterCaptureApplicationServiceTest {

    @Test
    void should_record_access_dead_letter_log() {
        AccessDeadLetterLogRepository repository = mock(AccessDeadLetterLogRepository.class);
        AccessDeadLetterCaptureApplicationService service = new AccessDeadLetterCaptureApplicationService(repository);
        Instant failedAt = Instant.parse("2026-05-18T15:20:30Z");
        RecordAccessDeadLetterCommand command = new RecordAccessDeadLetterCommand(
                "iot.telemetry.raw.dlt",
                "iot.telemetry.raw",
                2,
                18L,
                "iot-platform-access",
                "iot/dev-01/telemetry",
                "dev-01",
                "{\"deviceId\":\"dev-01\"}",
                IllegalStateException.class.getName(),
                "db unavailable",
                failedAt);
        AccessDeadLetterLog saved = new AccessDeadLetterLog(
                1L,
                command.deadLetterTopic(),
                command.originalTopic(),
                command.originalPartition(),
                command.originalOffset(),
                command.consumerGroup(),
                command.mqttTopic(),
                command.deviceId(),
                command.payload(),
                command.exceptionType(),
                command.exceptionMessage(),
                command.failedAt(),
                failedAt);

        when(repository.saveIfAbsent(any(AccessDeadLetterLog.class))).thenReturn(saved);

        AccessDeadLetterLog actual = service.record(command);

        assertEquals(1L, actual.id());
        assertEquals("dev-01", actual.deviceId());
        assertEquals("db unavailable", actual.exceptionMessage());
    }
}
