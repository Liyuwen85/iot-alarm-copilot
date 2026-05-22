package com.example.iotalarmcopilot.alarm.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

class AlarmTest {

    @Test
    void should_acknowledge_open_alarm() {
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant acknowledgedAt = Instant.parse("2026-05-13T10:05:00Z");
        Alarm alarm = Alarm.openFromRule(
                "temperature_high",
                1001L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt);

        Alarm acknowledgedAlarm = alarm.acknowledge(acknowledgedAt);

        assertEquals(AlarmStatus.ACKED, acknowledgedAlarm.status());
        assertEquals(acknowledgedAt, acknowledgedAlarm.acknowledgedAt());
        assertEquals(null, acknowledgedAlarm.closedAt());
    }

    @Test
    void should_close_open_alarm_without_implied_acknowledge() {
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant closedAt = Instant.parse("2026-05-13T10:06:00Z");
        Alarm alarm = Alarm.openFromRule(
                "temperature_high",
                1001L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt);

        Alarm closedAlarm = alarm.close(closedAt);

        assertEquals(AlarmStatus.CLOSED, closedAlarm.status());
        assertEquals(closedAt, closedAlarm.closedAt());
        assertNull(closedAlarm.acknowledgedAt());
    }

    @Test
    void should_close_acknowledged_alarm_and_keep_ack_time() {
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant acknowledgedAt = Instant.parse("2026-05-13T10:05:00Z");
        Instant closedAt = Instant.parse("2026-05-13T10:06:00Z");
        Alarm acknowledgedAlarm = Alarm.openFromRule(
                "temperature_high",
                1001L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt).acknowledge(acknowledgedAt);

        Alarm closedAlarm = acknowledgedAlarm.close(closedAt);

        assertEquals(AlarmStatus.CLOSED, closedAlarm.status());
        assertEquals(acknowledgedAt, closedAlarm.acknowledgedAt());
        assertEquals(closedAt, closedAlarm.closedAt());
    }

    @Test
    void should_reject_acknowledging_closed_alarm() {
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant closedAt = Instant.parse("2026-05-13T10:06:00Z");
        Alarm closedAlarm = Alarm.openFromRule(
                "temperature_high",
                1001L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt).close(closedAt);

        assertThrows(BaseDomainException.class, () ->
                closedAlarm.acknowledge(Instant.parse("2026-05-13T10:07:00Z")));
    }

    @Test
    void should_reject_acknowledging_acked_alarm_twice() {
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant acknowledgedAt = Instant.parse("2026-05-13T10:05:00Z");
        Alarm acknowledgedAlarm = Alarm.openFromRule(
                "temperature_high",
                1001L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt).acknowledge(acknowledgedAt);

        assertThrows(BaseDomainException.class, () ->
                acknowledgedAlarm.acknowledge(Instant.parse("2026-05-13T10:06:00Z")));
    }

    @Test
    void should_reject_closing_closed_alarm_twice() {
        Instant triggeredAt = Instant.parse("2026-05-13T10:00:00Z");
        Instant closedAt = Instant.parse("2026-05-13T10:06:00Z");
        Alarm closedAlarm = Alarm.openFromRule(
                "temperature_high",
                1001L,
                "dev-01",
                "temperature",
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(80),
                triggeredAt).close(closedAt);

        assertThrows(BaseDomainException.class, () ->
                closedAlarm.close(Instant.parse("2026-05-13T10:07:00Z")));
    }
}
