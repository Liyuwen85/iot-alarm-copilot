package com.example.iotalarmcopilot.audit.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 审计聚合类型值对象
 *
 * @param value
 */
public record AuditAggregateType(String value) {

    public AuditAggregateType {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("audit aggregateType must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
