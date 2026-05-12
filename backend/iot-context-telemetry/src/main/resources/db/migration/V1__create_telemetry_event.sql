CREATE TABLE telemetry_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL,
    temperature DECIMAL(10, 2) NULL,
    humidity DECIMAL(10, 2) NULL,
    reported_at DATETIME(3) NOT NULL,
    raw_json TEXT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_telemetry_event_device_reported_at (device_id, reported_at)
);
