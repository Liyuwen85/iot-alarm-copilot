package com.example.iotalarmcopilot.alarm.interfaces.event;

import com.example.iotalarmcopilot.alarm.application.AlarmApplicationService;
import com.example.iotalarmcopilot.alarm.application.CreateAlarmFromRuleCommand;
import com.example.iotalarmcopilot.alarm.domain.repository.AlarmSaveResult;
import com.example.iotalarmcopilot.contract.event.RuleTriggeredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 告警处理
 */
@Slf4j
@Component
public class RuleTriggeredAlarmHandler {

    private final AlarmApplicationService alarmApplicationService;

    public RuleTriggeredAlarmHandler(AlarmApplicationService alarmApplicationService) {
        this.alarmApplicationService = alarmApplicationService;
    }

    /**
     * 监听规则触发事件
     *
     * @param event
     */
    @EventListener
    public void onRuleTriggered(RuleTriggeredEvent event) {
        // 记录到告警
        AlarmSaveResult saveResult = alarmApplicationService.createIfAbsent(
                new CreateAlarmFromRuleCommand(
                        event.ruleCode(),
                        event.telemetryEventId(),
                        event.deviceId(),
                        event.metricName().value(),
                        event.metricValue(),
                        event.threshold(),
                        event.triggeredAt()));
        if (saveResult.created()) {
            log.warn(
                    "Alarm created. alarmId={}, ruleCode={}, deviceId={}, severity={}, dedupKey={}",
                    saveResult.alarm().id(),
                    saveResult.alarm().ruleCode(),
                    saveResult.alarm().deviceId(),
                    saveResult.alarm().severity().name(),
                    saveResult.alarm().dedupKey().value());
            return;
        }
        log.info(
                "Alarm deduplicated. existingAlarmId={}, ruleCode={}, deviceId={}, dedupKey={}",
                saveResult.alarm().id(),
                saveResult.alarm().ruleCode(),
                saveResult.alarm().deviceId(),
                saveResult.alarm().dedupKey().value());
    }
}
