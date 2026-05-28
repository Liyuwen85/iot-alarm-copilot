package com.example.iotalarmcopilot.command.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceCommandTest {

    @Test
    void shouldMarkCommandTimedOutFromSentStatus() {
        Instant createdAt = Instant.parse("2026-05-26T05:00:00Z");
        DeviceCommand created = DeviceCommand.createSetReportInterval(
                "cmd-test-001",
                "demo-timeout-001",
                "{\"commandType\":\"set_report_interval\"}",
                createdAt);

        DeviceCommand sent = created.markSent(createdAt.plusSeconds(1));
        DeviceCommand timedOut = sent.markTimedOut("command ack timeout", createdAt.plusSeconds(35));

        assertEquals(CommandStatus.TIMED_OUT, timedOut.status());
        assertEquals("command ack timeout", timedOut.ackMessage());
        assertTrue(timedOut.isTerminal());
        assertEquals(sent.sentAt(), timedOut.sentAt());
    }
}
