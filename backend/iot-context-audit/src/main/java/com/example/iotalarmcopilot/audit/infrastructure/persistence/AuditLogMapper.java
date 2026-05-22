package com.example.iotalarmcopilot.audit.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("""
            INSERT INTO audit_log (
                event_type,
                aggregate_type,
                aggregate_id,
                device_id,
                payload_json,
                occurred_at,
                created_at
            ) VALUES (
                #{eventType},
                #{aggregateType},
                #{aggregateId},
                #{deviceId},
                #{payloadJson},
                #{occurredAt},
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (event_type, aggregate_type, aggregate_id, occurred_at) DO NOTHING
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertIgnore(AuditLogRecord record);

    @Select("""
            SELECT
                id,
                event_type,
                aggregate_type,
                aggregate_id,
                device_id,
                payload_json,
                occurred_at,
                created_at
            FROM audit_log
            WHERE event_type = #{eventType}
              AND aggregate_type = #{aggregateType}
              AND aggregate_id = #{aggregateId}
              AND occurred_at = #{occurredAt}
            LIMIT 1
            """)
    AuditLogRecord selectOne(
            @Param("eventType") String eventType,
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId,
            @Param("occurredAt") Instant occurredAt);

    @Select("""
            SELECT
                id,
                event_type,
                aggregate_type,
                aggregate_id,
                device_id,
                payload_json,
                occurred_at,
                created_at
            FROM audit_log
            ORDER BY occurred_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<AuditLogRecord> selectRecent(int limit);
}
