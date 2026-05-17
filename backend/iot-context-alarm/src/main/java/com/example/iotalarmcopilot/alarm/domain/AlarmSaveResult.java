package com.example.iotalarmcopilot.alarm.domain;

import java.util.Objects;

/**
 * 告警保存结果。如果有重复的，created为false
 *
 * @param alarm
 * @param created
 */
public record AlarmSaveResult(
        Alarm alarm,
        boolean created) {

    public AlarmSaveResult {
        Objects.requireNonNull(alarm, "alarm must not be null");
    }
}
