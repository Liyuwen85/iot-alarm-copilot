package com.example.iotalarmcopilot.inspection.infrastructure.persistence;

import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;
import lombok.Data;

import java.time.Instant;

/**
 * 工单记录数据库实体
 */
@Data
public class InspectionTicketRecord {

    private Long id;
    private Long alarmId;
    private String alarmDedupKey;
    private String ruleCode;
    private String deviceId;
    private String severity;
    private String summary;
    private String suggestion;
    private String status;
    private Instant alarmTriggeredAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant confirmedAt;
    private Instant closedAt;

    public static InspectionTicketRecord fromDomain(InspectionTicket ticket) {
        InspectionTicketRecord record = new InspectionTicketRecord();
        record.setId(ticket.id());
        record.setAlarmId(ticket.alarmId());
        record.setAlarmDedupKey(ticket.alarmDedupKey());
        record.setRuleCode(ticket.ruleCode());
        record.setDeviceId(ticket.deviceId());
        record.setSeverity(ticket.severity());
        record.setSummary(ticket.summary());
        record.setSuggestion(ticket.suggestion());
        record.setStatus(ticket.status().name());
        record.setAlarmTriggeredAt(ticket.alarmTriggeredAt());
        record.setCreatedAt(ticket.createdAt());
        record.setUpdatedAt(ticket.closedAt() != null ? ticket.closedAt() : ticket.confirmedAt() != null ? ticket.confirmedAt() : ticket.createdAt());
        record.setConfirmedAt(ticket.confirmedAt());
        record.setClosedAt(ticket.closedAt());
        return record;
    }

    public InspectionTicket toDomain() {
        return new InspectionTicket(
                id,
                alarmId,
                alarmDedupKey,
                ruleCode,
                deviceId,
                severity,
                summary,
                suggestion,
                InspectionStatus.valueOf(status),
                alarmTriggeredAt,
                createdAt,
                confirmedAt,
                closedAt);
    }
}
