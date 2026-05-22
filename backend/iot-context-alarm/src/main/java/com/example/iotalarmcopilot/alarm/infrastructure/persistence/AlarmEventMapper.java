package com.example.iotalarmcopilot.alarm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 告警记录持久化
 */
@Mapper
public interface AlarmEventMapper {

    @Insert("""
            INSERT INTO alarm_event (
                dedup_key,
                rule_code,
                telemetry_event_id,
                device_id,
                metric_name,
                metric_value,
                threshold_value,
                severity,
                status,
                triggered_at,
                acknowledged_at,
                closed_at,
                created_at
            ) VALUES (
                #{dedupKey},
                #{ruleCode},
                #{telemetryEventId},
                #{deviceId},
                #{metricName},
                #{metricValue},
                #{thresholdValue},
                #{severity},
                #{status},
                #{triggeredAt},
                #{acknowledgedAt},
                #{closedAt},
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (dedup_key) DO NOTHING
            """)
    int insertIgnore(AlarmRecord record);

    @Select("""
            SELECT
                id,
                dedup_key,
                rule_code,
                telemetry_event_id,
                device_id,
                metric_name,
                metric_value,
                threshold_value,
                severity,
                status,
                triggered_at,
                acknowledged_at,
                closed_at
            FROM alarm_event
            WHERE id = #{id}
            LIMIT 1
            """)
    AlarmRecord selectById(Long id);

    @Select("""
            SELECT
                id,
                dedup_key,
                rule_code,
                telemetry_event_id,
                device_id,
                metric_name,
                metric_value,
                threshold_value,
                severity,
                status,
                triggered_at,
                acknowledged_at,
                closed_at
            FROM alarm_event
            WHERE dedup_key = #{dedupKey}
            LIMIT 1
            """)
    AlarmRecord selectByDedupKey(String dedupKey);

    @Update("""
            UPDATE alarm_event
            SET status = #{record.status},
                acknowledged_at = #{record.acknowledgedAt},
                closed_at = #{record.closedAt}
            WHERE id = #{record.id}
              AND status = #{expectedStatus}
            """)
    int updateStatusIfCurrentStatusMatches(
            @Param("record") AlarmRecord record,
            @Param("expectedStatus") String expectedStatus);

    @Select("""
            SELECT
                id,
                dedup_key,
                rule_code,
                telemetry_event_id,
                device_id,
                metric_name,
                metric_value,
                threshold_value,
                severity,
                status,
                triggered_at,
                acknowledged_at,
                closed_at
            FROM alarm_event
            ORDER BY triggered_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<AlarmRecord> selectRecent(int limit);
}
