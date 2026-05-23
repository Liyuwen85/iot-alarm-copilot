package com.example.iotalarmcopilot.inspection.application;

import java.time.Instant;

public record ConfirmInspectionTicketCommand(
        Long ticketId,
        Instant confirmedAt) {
}
