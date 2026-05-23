package com.example.iotalarmcopilot.inspection.domain.repository;

import com.example.iotalarmcopilot.inspection.domain.model.InspectionTicket;

import java.util.Objects;

/**
 * 工单状态更新结果
 */
public record InspectionTicketStatusUpdateResult(
        InspectionTicket ticket,
        boolean changed) {

    public InspectionTicketStatusUpdateResult {
        Objects.requireNonNull(ticket, "ticket must not be null");
    }
}
