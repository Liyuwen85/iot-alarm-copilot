package com.example.iotalarmcopilot.alarm.application;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 关闭告警命令
 *
 * @param alarmId
 * @param closedAt
 */
public record CloseAlarmCommand(
        Long alarmId,
        Instant closedAt) {

    public CloseAlarmCommand {
        Objects.requireNonNull(alarmId, "alarmId must not be null");
        Objects.requireNonNull(closedAt, "closedAt must not be null");
        if (alarmId <= 0) {
            throw new BaseDomainException("alarmId must be positive");
        }
    }
}
