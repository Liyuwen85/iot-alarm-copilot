package com.example.iotalarmcopilot.inspection.application;

import java.time.Instant;

public record CloseInspectionTicketCommand(
        Long ticketId,
        Instant closedAt) {
}
