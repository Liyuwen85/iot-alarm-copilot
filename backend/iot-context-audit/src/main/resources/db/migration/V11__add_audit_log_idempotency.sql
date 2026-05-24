DELETE FROM audit_log first_row
USING audit_log duplicate_row
WHERE first_row.id > duplicate_row.id
  AND first_row.event_type = duplicate_row.event_type
  AND first_row.aggregate_type = duplicate_row.aggregate_type
  AND first_row.aggregate_id = duplicate_row.aggregate_id
  AND first_row.occurred_at = duplicate_row.occurred_at;

ALTER TABLE audit_log
    ADD CONSTRAINT uk_audit_log_event_aggregate_occurred_at
        UNIQUE (event_type, aggregate_type, aggregate_id, occurred_at);
