CREATE TABLE device (
    id BIGSERIAL PRIMARY KEY,
    device_code VARCHAR(64) NOT NULL,
    product_code VARCHAR(64) NOT NULL,
    device_name VARCHAR(128) NOT NULL,
    group_code VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    shadow_version BIGINT,
    shadow_document TEXT,
    shadow_updated_at TIMESTAMP(3) WITH TIME ZONE,
    registered_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    status_changed_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_device_device_code UNIQUE (device_code)
);

CREATE INDEX idx_device_product_code
    ON device (product_code);

CREATE INDEX idx_device_group_code
    ON device (group_code);

CREATE INDEX idx_device_status
    ON device (status);
