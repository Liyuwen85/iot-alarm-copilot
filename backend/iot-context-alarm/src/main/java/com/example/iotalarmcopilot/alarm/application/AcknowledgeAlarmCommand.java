package com.example.iotalarmcopilot.alarm.application;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 告警确认命令
 */
public record AcknowledgeAlarmCommand(
        Long alarmId,
        Instant acknowledgedAt) {

    public AcknowledgeAlarmCommand {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(acknowledgedAt, "acknowledgedAt must not be null");
        if (alarmId <= 0) {
            throw new BaseDomainException("alarmId must be positive");
        }
    }
}
