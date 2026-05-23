package com.example.iotalarmcopilot.inspection.interfaces.event;

import com.example.iotalarmcopilot.contract.event.AlarmCreatedEvent;
import com.example.iotalarmcopilot.inspection.application.CreateInspectionTicketFromAlarmCommand;
import com.example.iotalarmcopilot.inspection.application.InspectionApplicationService;
import com.example.iotalarmcopilot.inspection.domain.repository.InspectionTicketSaveResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 告警创建事件处理器
 */
@Slf4j
@Component
public class AlarmCreatedInspectionTicketHandler {

    private final InspectionApplicationService inspectionApplicationService;

    public AlarmCreatedInspectionTicketHandler(InspectionApplicationService inspectionApplicationService) {
        this.inspectionApplicationService = inspectionApplicationService;
    }

    /**
     * 告警创建事件处理
     *
     * @param event 告警创建事件
     */
    @EventListener
    public void onAlarmCreated(AlarmCreatedEvent event) {
        InspectionTicketSaveResult saveResult = inspectionApplicationService.createIfAbsent(
                new CreateInspectionTicketFromAlarmCommand(
                        event.alarmId(),
                        event.dedupKey(),
                        event.ruleCode(),
                        event.deviceId(),
                        event.severity(),
                        event.triggeredAt(),
                        Instant.now()));
        if (saveResult.created()) {
            log.warn(
                    "Inspection ticket created. ticketId={}, alarmId={}, ruleCode={}, deviceId={}",
                    saveResult.ticket().id(),
                    saveResult.ticket().alarmId(),
                    saveResult.ticket().ruleCode(),
                    saveResult.ticket().deviceId());
            return;
        }
        log.info(
                "Inspection ticket deduplicated. existingTicketId={}, alarmId={}, ruleCode={}, deviceId={}",
                saveResult.ticket().id(),
                saveResult.ticket().alarmId(),
                saveResult.ticket().ruleCode(),
                saveResult.ticket().deviceId());
    }
}
