CREATE TABLE device_command (
    id BIGSERIAL PRIMARY KEY,
    command_id VARCHAR(64) NOT NULL,
    device_id VARCHAR(64) NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    ack_message VARCHAR(256) NULL,
    sent_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    acked_at TIMESTAMP(3) WITH TIME ZONE NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_device_command_command_id UNIQUE (command_id)
);

CREATE INDEX idx_device_command_device_created_at
    ON device_command (device_id, created_at);

CREATE INDEX idx_device_command_status_created_at
    ON device_command (status, created_at);
