package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TelemetryEventMapper {

    @Insert("""
            INSERT INTO telemetry_event (
                id,
                device_id,
                temperature,
                humidity,
                reported_at,
                raw_json,
                created_at
            ) VALUES (
                #{id},
                #{deviceId},
                #{temperature},
                #{humidity},
                #{reportedAt},
                #{rawJson},
                CURRENT_TIMESTAMP
            )
            """)
    int insert(TelemetryEventRecord record);

    @Select("""
            SELECT
                id,
                device_id,
                temperature,
                humidity,
                reported_at,
                raw_json
            FROM telemetry_event
            ORDER BY reported_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<TelemetryEventRecord> selectRecent(int limit);
}
