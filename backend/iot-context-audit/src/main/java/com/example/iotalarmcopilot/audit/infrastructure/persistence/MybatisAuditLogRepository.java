package com.example.iotalarmcopilot.audit.infrastructure.persistence;

import com.example.iotalarmcopilot.audit.domain.AuditLogEntry;
import com.example.iotalarmcopilot.audit.domain.AuditLogRepository;
import com.example.iotalarmcopilot.BaseDomainException;
import org.springframework.stereotype.Repository;

import java.time.temporal.ChronoUnit;

/**
 * MyBatis 实现的审计日志存储仓库
 */
@Repository
public class MybatisAuditLogRepository implements AuditLogRepository {

    private final AuditLogMapper auditLogMapper;

    public MybatisAuditLogRepository(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLogEntry saveIfAbsent(AuditLogEntry entry) {
        AuditLogRecord record = AuditLogRecord.fromDomain(entry);
        record.setOccurredAt(record.getOccurredAt().truncatedTo(ChronoUnit.MILLIS));
        auditLogMapper.insertIgnore(record);
        AuditLogRecord savedRecord = auditLogMapper.selectOne(
                record.getEventType(),
                record.getAggregateType(),
                record.getAggregateId(),
                record.getOccurredAt());
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load audit log");
        }
        return savedRecord.toDomain();
    }
}
