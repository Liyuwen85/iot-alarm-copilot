package com.example.iotalarmcopilot.access.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccessDeadLetterMapper {

    @Insert("""
            INSERT INTO access_dead_letter_log (
                dead_letter_topic,
                original_topic,
                original_partition,
                original_offset,
                consumer_group,
                mqtt_topic,
                device_id,
                payload,
                exception_type,
                exception_message,
                failed_at,
                created_at
            ) VALUES (
                #{deadLetterTopic},
                #{originalTopic},
                #{originalPartition},
                #{originalOffset},
                #{consumerGroup},
                #{mqttTopic},
                #{deviceId},
                #{payload},
                #{exceptionType},
                #{exceptionMessage},
                #{failedAt},
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (original_topic, original_partition, original_offset) DO NOTHING
            """)
    int insertIgnore(AccessDeadLetterRecord record);

    @Select("""
            SELECT
                id,
                dead_letter_topic,
                original_topic,
                original_partition,
                original_offset,
                consumer_group,
                mqtt_topic,
                device_id,
                payload,
                exception_type,
                exception_message,
                failed_at,
                created_at
            FROM access_dead_letter_log
            WHERE original_topic = #{originalTopic}
              AND original_partition = #{originalPartition}
              AND original_offset = #{originalOffset}
            """)
    AccessDeadLetterRecord selectOne(String originalTopic, Integer originalPartition, Long originalOffset);

    @Select("""
            SELECT
                id,
                dead_letter_topic,
                original_topic,
                original_partition,
                original_offset,
                consumer_group,
                mqtt_topic,
                device_id,
                payload,
                exception_type,
                exception_message,
                failed_at,
                created_at
            FROM access_dead_letter_log
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<AccessDeadLetterRecord> selectRecent(int limit);
}
