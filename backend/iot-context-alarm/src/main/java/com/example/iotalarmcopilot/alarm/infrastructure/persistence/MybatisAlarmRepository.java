package com.example.iotalarmcopilot.alarm.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.alarm.domain.model.Alarm;
import com.example.iotalarmcopilot.alarm.domain.model.AlarmStatus;
import com.example.iotalarmcopilot.alarm.domain.repository.AlarmRepository;
import com.example.iotalarmcopilot.alarm.domain.repository.AlarmSaveResult;
import com.example.iotalarmcopilot.alarm.domain.repository.AlarmStatusUpdateResult;
import org.springframework.stereotype.Repository;

/**
 * 告警存储
 */
@Repository
public class MybatisAlarmRepository implements AlarmRepository {

    private final AlarmEventMapper alarmEventMapper;

    public MybatisAlarmRepository(AlarmEventMapper alarmEventMapper) {
        this.alarmEventMapper = alarmEventMapper;
    }

    @Override
    public AlarmSaveResult saveIfAbsent(Alarm alarm) {
        AlarmRecord record = AlarmRecord.fromDomain(alarm);
        // 如果已存在dedupKey的告警，则不处理
        int insertedRows = alarmEventMapper.insertIgnore(record);
        String dedupKey = alarm.dedupKey().value();
        // 获取已有的告警
        AlarmRecord savedRecord = alarmEventMapper.selectByDedupKey(dedupKey);
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load alarm. dedupKey=" + dedupKey);
        }
        return new AlarmSaveResult(savedRecord.toDomain(), insertedRows == 1);
    }

    @Override
    public Alarm load(Long alarmId) {
        AlarmRecord record = alarmEventMapper.selectById(alarmId);
        if (record == null) {
            throw new BaseDomainException("Alarm not found. id=" + alarmId);
        }
        return record.toDomain();
    }

    @Override
    public AlarmStatusUpdateResult updateStatusIfCurrentStatusMatches(Alarm alarm, AlarmStatus expectedCurrentStatus) {
        AlarmRecord record = AlarmRecord.fromDomain(alarm);
        int updatedRows = alarmEventMapper.updateStatusIfCurrentStatusMatches(record, expectedCurrentStatus.name());
        Alarm latestAlarm = load(alarm.id());
        if (updatedRows > 1) {
            throw new BaseDomainException("Unexpected updated rows for alarm status transition. id=" + alarm.id());
        }
        return new AlarmStatusUpdateResult(latestAlarm, updatedRows == 1);
    }
}
