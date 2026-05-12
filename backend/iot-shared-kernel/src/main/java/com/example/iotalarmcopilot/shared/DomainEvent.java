package com.example.iotalarmcopilot.shared;

import java.time.Instant;

/**
 * 领域基础事件
 */
public interface DomainEvent {

    String eventType();

    Instant occurredAt();
}

