package com.example.iotalarmcopilot.alarm.application;

import com.example.iotalarmcopilot.alarm.domain.Alarm;
import com.example.iotalarmcopilot.alarm.domain.AlarmRepository;
import com.example.iotalarmcopilot.alarm.domain.AlarmSaveResult;
import com.example.iotalarmcopilot.alarm.domain.AlarmStatus;
import com.example.iotalarmcopilot.contract.event.AlarmAcknowledgedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmClosedEvent;
import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 告警应用服务
 */
@Service
public class AlarmApplicationService {

    private final AlarmRepository alarmRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AlarmApplicationService(
            AlarmRepository alarmRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.alarmRepository = alarmRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 不存在就创建新的告警
     *
     * @param command
     * @return
     */
    public AlarmSaveResult createIfAbsent(CreateAlarmFromRuleCommand command) {
        Alarm alarm = Alarm.openFromRule(
                command.ruleCode(),
                command.telemetryEventId(),
                command.deviceId(),
                command.metricName(),
                command.metricValue(),
                command.threshold(),
                command.triggeredAt());
        AlarmSaveResult saveResult = alarmRepository.saveIfAbsent(alarm);
        if (saveResult.created()) {
            publishCreated(saveResult.alarm());
        }
        return saveResult;
    }

    /**
     * 确认告警
     *
     * @param command
     * @return
     */
    public Alarm acknowledge(AcknowledgeAlarmCommand command) {
        Alarm currentAlarm = alarmRepository.load(command.alarmId());
        Alarm acknowledgedAlarm = currentAlarm.acknowledge(command.acknowledgedAt());
        Alarm savedAlarm = alarmRepository.updateStatus(acknowledgedAlarm);
        if (currentAlarm.status() != AlarmStatus.ACKED && savedAlarm.status() == AlarmStatus.ACKED) {
            publishAcknowledged(savedAlarm);
        }
        return savedAlarm;
    }

    /**
     * 关闭告警
     *
     * @param command
     * @return
     */
    public Alarm close(CloseAlarmCommand command) {
        Alarm currentAlarm = alarmRepository.load(command.alarmId());
        Alarm closedAlarm = currentAlarm.close(command.closedAt());
        Alarm savedAlarm = alarmRepository.updateStatus(closedAlarm);
        if (currentAlarm.status() != AlarmStatus.CLOSED && savedAlarm.status() == AlarmStatus.CLOSED) {
            publishClosed(savedAlarm);
        }
        return savedAlarm;
    }

    /**
     * 发布创建告警事件
     *
     * @param alarm
     */
    private void publishCreated(Alarm alarm) {
        applicationEventPublisher.publishEvent(new AlarmCreatedEvent(
                alarm.id(),
                alarm.dedupKey().value(),
                alarm.ruleCode(),
                alarm.deviceId(),
                alarm.severity().name(),
                alarm.triggeredAt()));
    }

    /**
     * 发布确认告警事件
     *
     * @param alarm
     */
    private void publishAcknowledged(Alarm alarm) {
        applicationEventPublisher.publishEvent(new AlarmAcknowledgedEvent(
                alarm.id(),
                alarm.dedupKey().value(),
                alarm.ruleCode(),
                alarm.deviceId(),
                alarm.severity().name(),
                alarm.acknowledgedAt()));
    }

    /**
     * 发布关闭告警事件
     *
     * @param alarm
     */
    private void publishClosed(Alarm alarm) {
        applicationEventPublisher.publishEvent(new AlarmClosedEvent(
                alarm.id(),
                alarm.dedupKey().value(),
                alarm.ruleCode(),
                alarm.deviceId(),
                alarm.severity().name(),
                alarm.closedAt()));
    }
}
