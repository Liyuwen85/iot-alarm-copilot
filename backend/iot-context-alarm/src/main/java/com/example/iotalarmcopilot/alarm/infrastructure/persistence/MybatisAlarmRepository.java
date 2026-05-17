package com.example.iotalarmcopilot.alarm.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.alarm.domain.Alarm;
import com.example.iotalarmcopilot.alarm.domain.AlarmRepository;
import com.example.iotalarmcopilot.alarm.domain.AlarmSaveResult;
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
    public Alarm updateStatus(Alarm alarm) {
        AlarmRecord record = AlarmRecord.fromDomain(alarm);
        int updatedRows = alarmEventMapper.updateStatus(record);
        if (updatedRows != 1) {
            throw new BaseDomainException("Failed to update alarm status. id=" + alarm.id());
        }
        return load(alarm.id());
    }
}
