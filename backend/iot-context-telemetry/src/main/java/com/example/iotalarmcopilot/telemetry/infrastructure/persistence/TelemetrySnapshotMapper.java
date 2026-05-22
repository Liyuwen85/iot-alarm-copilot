package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TelemetrySnapshotMapper {

    @Insert("""
            INSERT INTO device_telemetry_snapshot (
                device_id,
                last_telemetry_event_id,
                temperature,
                humidity,
                metrics_json,
                last_reported_at,
                last_raw_json,
                created_at,
                updated_at
            ) VALUES (
                #{deviceId},
                #{lastTelemetryEventId},
                #{temperature},
                #{humidity},
                #{metricsJson},
                #{lastReportedAt},
                #{lastRawJson},
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (device_id) DO UPDATE
            SET last_telemetry_event_id = EXCLUDED.last_telemetry_event_id,
                temperature = EXCLUDED.temperature,
                humidity = EXCLUDED.humidity,
                metrics_json = EXCLUDED.metrics_json,
                last_reported_at = EXCLUDED.last_reported_at,
                last_raw_json = EXCLUDED.last_raw_json,
                updated_at = CURRENT_TIMESTAMP
            WHERE EXCLUDED.last_reported_at > device_telemetry_snapshot.last_reported_at
               OR (
                    EXCLUDED.last_reported_at = device_telemetry_snapshot.last_reported_at
                    AND EXCLUDED.last_telemetry_event_id >= device_telemetry_snapshot.last_telemetry_event_id
               )
            """)
    int upsert(TelemetrySnapshotRecord record);

    @Select("""
            SELECT
                device_id,
                last_telemetry_event_id,
                temperature,
                humidity,
                metrics_json,
                last_reported_at,
                last_raw_json
            FROM device_telemetry_snapshot
            WHERE device_id = #{deviceId}
            """)
    TelemetrySnapshotRecord selectByDeviceId(String deviceId);
}
