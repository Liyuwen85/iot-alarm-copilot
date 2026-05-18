package com.example.iotalarmcopilot.audit.application;

import com.example.iotalarmcopilot.audit.domain.AuditLogEntry;
import com.example.iotalarmcopilot.audit.domain.AuditLogRepository;
import org.springframework.stereotype.Service;

/**
 * 审计应用服务
 */
@Service
public class AuditApplicationService {

    private final AuditLogRepository auditLogRepository;

    public AuditApplicationService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 记录审计日志
     *
     * @param command
     * @return
     */
    public AuditLogEntry record(RecordAuditLogCommand command) {
        AuditLogEntry entry = AuditLogEntry.record(
                command.eventType(),
                command.aggregateType(),
                command.aggregateId(),
                command.deviceId(),
                command.payloadJson(),
                command.occurredAt());
        return auditLogRepository.save(entry);
    }
}
