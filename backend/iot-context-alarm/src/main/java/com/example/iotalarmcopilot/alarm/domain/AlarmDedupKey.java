package com.example.iotalarmcopilot.alarm.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.Objects;

/**
 * 告警去重键值对象
 *
 * @param value
 */
public record AlarmDedupKey(String value) {

    public AlarmDedupKey {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new BaseDomainException("alarm dedup key must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
