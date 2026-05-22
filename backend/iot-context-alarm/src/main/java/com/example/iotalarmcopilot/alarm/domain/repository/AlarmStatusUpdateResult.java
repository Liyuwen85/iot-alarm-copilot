package com.example.iotalarmcopilot.alarm.domain.repository;

import com.example.iotalarmcopilot.alarm.domain.model.Alarm;
import java.util.Objects;

/**
 * 告警状态更新结果
 */
public record AlarmStatusUpdateResult(
        Alarm alarm,
        boolean changed) {

    public AlarmStatusUpdateResult {
        Objects.requireNonNull(alarm, "alarm must not be null");
    }
}
