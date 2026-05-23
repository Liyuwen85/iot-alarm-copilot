package com.example.iotalarmcopilot.inspection.domain.policy;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.inspection.domain.model.InspectionStatus;

import java.time.Instant;

/**
 * 工单状态策略
 */
public final class InspectionStatusPolicy {

    private InspectionStatusPolicy() {
    }

    /**
     * 工单生命周期验证
     *
     * @param status 工单状态
     * @param createdAt 工单创建时间
     * @param confirmedAt 工单确认时间
     * @param closedAt 工单关闭时间
     */
    public static void validateLifecycle(
            InspectionStatus status,
            Instant createdAt,
            Instant confirmedAt,
            Instant closedAt) {
        if (confirmedAt != null && confirmedAt.isBefore(createdAt)) {
            throw new BaseDomainException("confirmedAt must not be before createdAt");
        }
        if (closedAt != null && closedAt.isBefore(createdAt)) {
            throw new BaseDomainException("closedAt must not be before createdAt");
        }
        if (confirmedAt != null && closedAt != null && closedAt.isBefore(confirmedAt)) {
            throw new BaseDomainException("closedAt must not be before confirmedAt");
        }
        if (status == InspectionStatus.PENDING && (confirmedAt != null || closedAt != null)) {
            throw new BaseDomainException("Pending ticket must not have confirmedAt or closedAt");
        }
        if (status == InspectionStatus.CONFIRMED) {
            if (confirmedAt == null) {
                throw new BaseDomainException("Confirmed ticket must have confirmedAt");
            }
            if (closedAt != null) {
                throw new BaseDomainException("Confirmed ticket must not have closedAt");
            }
        }
        if (status == InspectionStatus.CLOSED) {
            if (confirmedAt == null) {
                throw new BaseDomainException("Closed ticket must have confirmedAt");
            }
            if (closedAt == null) {
                throw new BaseDomainException("Closed ticket must have closedAt");
            }
        }
    }

    /**
     * 允许工单确认
     *
     * @param status 工单状态
     */
    public static void ensureConfirmAllowed(InspectionStatus status) {
        if (status != InspectionStatus.PENDING) {
            throw new BaseDomainException("Only pending ticket can be confirmed");
        }
    }

    /**
     * 允许工单关闭
     *
     * @param status 工单状态
     */
    public static void ensureCloseAllowed(InspectionStatus status) {
        if (status != InspectionStatus.CONFIRMED) {
            throw new BaseDomainException("Only confirmed ticket can be closed");
        }
    }
}
