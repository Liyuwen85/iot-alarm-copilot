CREATE TABLE access_dead_letter_log (
    id BIGSERIAL PRIMARY KEY,
    dead_letter_topic VARCHAR(120) NOT NULL,
    original_topic VARCHAR(120) NOT NULL,
    original_partition INTEGER NOT NULL,
    original_offset BIGINT NOT NULL,
    consumer_group VARCHAR(120),
    mqtt_topic VARCHAR(255),
    device_id VARCHAR(120),
    payload TEXT,
    exception_type VARCHAR(255) NOT NULL,
    exception_message TEXT,
    failed_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_access_dead_letter_original_position UNIQUE (original_topic, original_partition, original_offset)
);

CREATE INDEX idx_access_dead_letter_created_at ON access_dead_letter_log (created_at DESC);
CREATE INDEX idx_access_dead_letter_device_id ON access_dead_letter_log (device_id);
