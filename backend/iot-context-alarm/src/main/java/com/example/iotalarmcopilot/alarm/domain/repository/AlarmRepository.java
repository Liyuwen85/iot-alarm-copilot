package com.example.iotalarmcopilot.alarm.domain.repository;

import com.example.iotalarmcopilot.alarm.domain.model.Alarm;
import com.example.iotalarmcopilot.alarm.domain.model.AlarmStatus;

/**
 * 告警存储
 */
public interface AlarmRepository {

    /**
     * 不存在就保存告警
     *
     * @param alarm
     * @return
     */
    AlarmSaveResult saveIfAbsent(Alarm alarm);

    Alarm load(Long alarmId);

    AlarmStatusUpdateResult updateStatusIfCurrentStatusMatches(Alarm alarm, AlarmStatus expectedCurrentStatus);
}
