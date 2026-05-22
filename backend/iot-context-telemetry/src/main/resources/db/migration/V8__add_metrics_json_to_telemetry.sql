ALTER TABLE telemetry_event
    ADD COLUMN IF NOT EXISTS metrics_json TEXT;

UPDATE telemetry_event
SET metrics_json = jsonb_strip_nulls(jsonb_build_object(
        'temperature', temperature,
        'humidity', humidity
    ))::text
WHERE metrics_json IS NULL;

ALTER TABLE telemetry_event
    ALTER COLUMN metrics_json SET NOT NULL;

ALTER TABLE device_telemetry_snapshot
    ADD COLUMN IF NOT EXISTS metrics_json TEXT;

UPDATE device_telemetry_snapshot
SET metrics_json = jsonb_strip_nulls(jsonb_build_object(
        'temperature', temperature,
        'humidity', humidity
    ))::text
WHERE metrics_json IS NULL;

ALTER TABLE device_telemetry_snapshot
    ALTER COLUMN metrics_json SET NOT NULL;
