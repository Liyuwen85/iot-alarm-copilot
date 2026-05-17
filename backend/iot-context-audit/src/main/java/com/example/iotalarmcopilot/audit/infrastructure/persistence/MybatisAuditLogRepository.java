package com.example.iotalarmcopilot.audit.infrastructure.persistence;

import com.example.iotalarmcopilot.audit.domain.AuditLogEntry;
import com.example.iotalarmcopilot.audit.domain.AuditLogRepository;
import com.example.iotalarmcopilot.BaseDomainException;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisAuditLogRepository implements AuditLogRepository {

    private final AuditLogMapper auditLogMapper;

    public MybatisAuditLogRepository(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLogEntry save(AuditLogEntry entry) {
        AuditLogRecord record = AuditLogRecord.fromDomain(entry);
        int insertedRows = auditLogMapper.insert(record);
        if (insertedRows != 1) {
            throw new BaseDomainException("Failed to persist audit log");
        }
        return record.toDomain();
    }
}
