CREATE TABLE telemetry_event (
    id BIGINT NOT NULL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL,
    temperature NUMERIC(10, 2) NULL,
    humidity NUMERIC(10, 2) NULL,
    metrics_json TEXT NOT NULL,
    reported_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    raw_json TEXT NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_telemetry_event_device_reported_at
    ON telemetry_event (device_id, reported_at);
