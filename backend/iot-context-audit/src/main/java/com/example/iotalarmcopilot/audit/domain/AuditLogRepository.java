package com.example.iotalarmcopilot.audit.domain;

/**
 * 审计日志存储接口
 */
public interface AuditLogRepository {

    AuditLogEntry save(AuditLogEntry entry);
}
