package com.example.iotalarmcopilot;

import java.time.Instant;

/**
 * 领域基础事件
 */
public interface DomainEvent {

    String eventType();

    Instant occurredAt();
}

