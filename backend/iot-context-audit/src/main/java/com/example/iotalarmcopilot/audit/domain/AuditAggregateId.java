package com.example.iotalarmcopilot.audit.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 审计聚合标识值对象
 *
 * @param value
 */
public record AuditAggregateId(String value) {

    public AuditAggregateId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("audit aggregateId must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
