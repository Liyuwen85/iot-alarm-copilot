CREATE TABLE inspection_ticket (
    id BIGSERIAL PRIMARY KEY,
    alarm_id BIGINT NOT NULL,
    alarm_dedup_key VARCHAR(160) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    device_id VARCHAR(64) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    summary VARCHAR(256) NOT NULL,
    suggestion TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    alarm_triggered_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP(3) WITH TIME ZONE NULL,
    closed_at TIMESTAMP(3) WITH TIME ZONE NULL,
    CONSTRAINT uk_inspection_ticket_alarm_id UNIQUE (alarm_id)
);

CREATE INDEX idx_inspection_ticket_device_created_at
    ON inspection_ticket (device_id, created_at);

CREATE INDEX idx_inspection_ticket_status_created_at
    ON inspection_ticket (status, created_at);
