CREATE TABLE alarm_event (
    id BIGSERIAL PRIMARY KEY,
    dedup_key VARCHAR(160) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    telemetry_event_id BIGINT NOT NULL,
    device_id VARCHAR(64) NOT NULL,
    metric_name VARCHAR(32) NOT NULL,
    metric_value NUMERIC(10, 2) NOT NULL,
    threshold_value NUMERIC(10, 2) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    triggered_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_alarm_event_dedup_key UNIQUE (dedup_key)
);

CREATE INDEX idx_alarm_event_device_created_at
    ON alarm_event (device_id, created_at);

CREATE INDEX idx_alarm_event_rule_device
    ON alarm_event (rule_code, device_id);
