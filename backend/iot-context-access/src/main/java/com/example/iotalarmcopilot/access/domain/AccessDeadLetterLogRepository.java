package com.example.iotalarmcopilot.access.domain;

public interface AccessDeadLetterLogRepository {

    AccessDeadLetterLog saveIfAbsent(AccessDeadLetterLog log);
}
