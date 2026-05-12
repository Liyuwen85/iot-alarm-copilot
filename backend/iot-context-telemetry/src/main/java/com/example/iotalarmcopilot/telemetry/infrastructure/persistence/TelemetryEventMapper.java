package com.example.iotalarmcopilot.telemetry.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface TelemetryEventMapper {

    @Insert("""
            INSERT INTO telemetry_event (
                device_id,
                temperature,
                humidity,
                reported_at,
                raw_json,
                created_at
            ) VALUES (
                #{deviceId},
                #{temperature},
                #{humidity},
                #{reportedAt},
                #{rawJson},
                CURRENT_TIMESTAMP
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TelemetryEventRecord record);
}
