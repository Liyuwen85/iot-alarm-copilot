package com.example.iotalarmcopilot.alarm.domain;

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

    Alarm updateStatus(Alarm alarm);
}
