package com.example.iotalarmcopilot.audit.application;

import com.example.iotalarmcopilot.audit.infrastructure.persistence.AuditLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计查询应用服务
 */
@Service
public class AuditQueryApplicationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AuditLogMapper auditLogMapper;

    public AuditQueryApplicationService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public List<AuditLogVO> recent(int limit) {
        int safeLimit = normalizeLimit(limit);
        return auditLogMapper.selectRecent(safeLimit).stream()
                .map(record -> new AuditLogVO(
                        record.getId(),
                        record.getEventType(),
                        record.getAggregateType(),
                        record.getAggregateId(),
                        record.getDeviceId(),
                        record.getPayloadJson(),
                        record.getOccurredAt(),
                        record.getCreatedAt()))
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
