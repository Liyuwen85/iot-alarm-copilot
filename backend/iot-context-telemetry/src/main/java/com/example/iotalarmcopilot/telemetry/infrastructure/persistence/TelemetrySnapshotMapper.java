package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TelemetrySnapshotMapper {

    /**
     * 插入或更新设备遥测快照。另外保证数据的一致性和正确性
     *
     * @param record 设备遥测快照记录
     * @return 影响行数
     */
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
            SET last_telemetry_event_id = CASE
                    WHEN EXCLUDED.last_reported_at > device_telemetry_snapshot.last_reported_at
                        THEN EXCLUDED.last_telemetry_event_id
                    WHEN EXCLUDED.last_reported_at = device_telemetry_snapshot.last_reported_at
                        THEN GREATEST(EXCLUDED.last_telemetry_event_id, device_telemetry_snapshot.last_telemetry_event_id)
                    ELSE device_telemetry_snapshot.last_telemetry_event_id
                END,
                temperature = CASE
                    WHEN EXCLUDED.last_reported_at > device_telemetry_snapshot.last_reported_at
                        THEN EXCLUDED.temperature
                    WHEN EXCLUDED.last_reported_at = device_telemetry_snapshot.last_reported_at
                        THEN COALESCE(EXCLUDED.temperature, device_telemetry_snapshot.temperature)
                    ELSE device_telemetry_snapshot.temperature
                END,
                humidity = CASE
                    WHEN EXCLUDED.last_reported_at > device_telemetry_snapshot.last_reported_at
                        THEN EXCLUDED.humidity
                    WHEN EXCLUDED.last_reported_at = device_telemetry_snapshot.last_reported_at
                        THEN COALESCE(EXCLUDED.humidity, device_telemetry_snapshot.humidity)
                    ELSE device_telemetry_snapshot.humidity
                END,
                metrics_json = CASE
                    WHEN EXCLUDED.last_reported_at > device_telemetry_snapshot.last_reported_at
                        THEN EXCLUDED.metrics_json
                    WHEN EXCLUDED.last_reported_at = device_telemetry_snapshot.last_reported_at
                        THEN ((device_telemetry_snapshot.metrics_json)::jsonb || (EXCLUDED.metrics_json)::jsonb)::text
                    ELSE device_telemetry_snapshot.metrics_json
                END,
                last_reported_at = CASE
                    WHEN EXCLUDED.last_reported_at > device_telemetry_snapshot.last_reported_at
                        THEN EXCLUDED.last_reported_at
                    ELSE device_telemetry_snapshot.last_reported_at
                END,
                last_raw_json = CASE
                    WHEN EXCLUDED.last_reported_at >= device_telemetry_snapshot.last_reported_at
                        THEN EXCLUDED.last_raw_json
                    ELSE device_telemetry_snapshot.last_raw_json
                END,
                updated_at = CURRENT_TIMESTAMP
            WHERE EXCLUDED.last_reported_at >= device_telemetry_snapshot.last_reported_at
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
