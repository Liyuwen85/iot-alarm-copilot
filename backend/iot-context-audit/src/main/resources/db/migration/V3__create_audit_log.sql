CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    device_id VARCHAR(64) NULL,
    payload_json TEXT NOT NULL,
    occurred_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_device_occurred_at
    ON audit_log (device_id, occurred_at);

CREATE INDEX idx_audit_log_event_occurred_at
    ON audit_log (event_type, occurred_at);

CREATE INDEX idx_audit_log_aggregate
    ON audit_log (aggregate_type, aggregate_id);
