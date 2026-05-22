ALTER TABLE audit_log
    ADD CONSTRAINT uk_audit_log_event_aggregate_occurred_at
        UNIQUE (event_type, aggregate_type, aggregate_id, occurred_at);
