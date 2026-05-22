package com.example.iotalarmcopilot.alarm.domain.policy;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.alarm.domain.model.AlarmStatus;

import java.time.Instant;

/**
 * 告警状态策略
 */
public final class AlarmStatusPolicy {

    private AlarmStatusPolicy() {
    }

    /**
     * 验证告警生命周期是否正确
     *
     * @param status
     * @param triggeredAt
     * @param acknowledgedAt
     * @param closedAt
     */
    public static void validateLifecycle(
            AlarmStatus status,
            Instant triggeredAt,
            Instant acknowledgedAt,
            Instant closedAt) {
        if (acknowledgedAt != null && acknowledgedAt.isBefore(triggeredAt)) {
            throw new BaseDomainException("acknowledgedAt must not be before triggeredAt");
        }
        if (closedAt != null && closedAt.isBefore(triggeredAt)) {
            throw new BaseDomainException("closedAt must not be before triggeredAt");
        }
        if (status == AlarmStatus.OPEN && (acknowledgedAt != null || closedAt != null)) {
            throw new BaseDomainException("Open alarm must not have acknowledgedAt or closedAt");
        }
        if (status == AlarmStatus.ACKED) {
            if (acknowledgedAt == null) {
                throw new BaseDomainException("Acked alarm must have acknowledgedAt");
            }
            if (closedAt != null) {
                throw new BaseDomainException("Acked alarm must not have closedAt");
            }
        }
        if (status == AlarmStatus.CLOSED && closedAt == null) {
            throw new BaseDomainException("Closed alarm must have closedAt");
        }
    }

    /**
     * 是否可以确认
     *
     * @param status
     */
    public static void ensureAcknowledgeAllowed(AlarmStatus status) {
        if (status != AlarmStatus.OPEN) {
            throw new BaseDomainException("Only open alarm can be acknowledged");
        }
    }

    /**
     * 是否可以关闭
     *
     * @param status
     */
    public static void ensureCloseAllowed(AlarmStatus status) {
        if (status == AlarmStatus.CLOSED) {
            throw new BaseDomainException("Only open or acknowledged alarm can be closed");
        }
    }
}
