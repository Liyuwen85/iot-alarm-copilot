CREATE TABLE IF NOT EXISTS ai_alarm_summary_task (
    id BIGSERIAL PRIMARY KEY,
    alarm_id BIGINT NOT NULL,
    alarm_dedup_key VARCHAR(128) NOT NULL,
    rule_code VARCHAR(128) NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    summary TEXT NULL,
    possible_cause TEXT NULL,
    inspection_suggestion TEXT NULL,
    risk_level VARCHAR(32) NULL,
    confidence NUMERIC(5, 4) NULL,
    model_name VARCHAR(128) NULL,
    prompt_version VARCHAR(128) NULL,
    request_payload TEXT NULL,
    response_payload TEXT NULL,
    error_code VARCHAR(128) NULL,
    error_message TEXT NULL,
    alarm_triggered_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    CONSTRAINT uk_ai_alarm_summary_task_alarm UNIQUE (alarm_id)
);

CREATE INDEX IF NOT EXISTS idx_ai_alarm_summary_task_status_created_at
    ON ai_alarm_summary_task (status, created_at DESC, id DESC);
