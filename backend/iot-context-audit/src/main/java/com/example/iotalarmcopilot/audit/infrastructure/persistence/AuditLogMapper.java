package com.example.iotalarmcopilot.audit.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

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
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(AuditLogRecord record);

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
