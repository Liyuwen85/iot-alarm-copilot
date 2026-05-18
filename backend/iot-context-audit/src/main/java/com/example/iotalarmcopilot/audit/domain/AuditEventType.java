package com.example.iotalarmcopilot.audit.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 审计事件类型值对象
 *
 * @param value
 */
public record AuditEventType(String value) {

    public AuditEventType {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("audit eventType must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
