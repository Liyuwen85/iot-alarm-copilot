package com.example.iotalarmcopilot.shared;

import java.time.Instant;

public interface DomainEvent {

    String eventType();

    Instant occurredAt();
}

