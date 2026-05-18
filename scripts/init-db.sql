-- Run this script with psql against the shared PostgreSQL / TimescaleDB instance.
-- Business tables are managed by Flyway in iot_alarm_copilot.
-- Timeseries schema is managed by the backend initializer in iot_telemetry_hot.

SELECT 'CREATE DATABASE iot_alarm_copilot'
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = 'iot_alarm_copilot'
)\gexec

SELECT 'CREATE DATABASE iot_telemetry_hot'
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = 'iot_telemetry_hot'
)\gexec
