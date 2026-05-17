CREATE TABLE device_telemetry_snapshot (
    device_id VARCHAR(64) NOT NULL PRIMARY KEY,
    last_telemetry_event_id BIGINT NOT NULL,
    temperature NUMERIC(10, 2) NULL,
    humidity NUMERIC(10, 2) NULL,
    last_reported_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    last_raw_json TEXT NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_telemetry_snapshot_reported_at
    ON device_telemetry_snapshot (last_reported_at);
