package com.example.iotalarmcopilot.command.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;
import java.util.List;

@Mapper
public interface DeviceCommandMapper {

    @Insert("""
            INSERT INTO device_command (
                command_id,
                device_id,
                command_type,
                payload_json,
                status,
                ack_message,
                sent_at,
                acked_at,
                created_at,
                updated_at
            ) VALUES (
                #{commandId},
                #{deviceId},
                #{commandType},
                #{payloadJson},
                #{status},
                #{ackMessage},
                #{sentAt},
                #{ackedAt},
                #{createdAt},
                #{updatedAt}
            )
            ON CONFLICT (command_id) DO NOTHING
            """)
    int insertIgnore(DeviceCommandRecord record);

    @Select("""
            SELECT
                id,
                command_id,
                device_id,
                command_type,
                payload_json,
                status,
                ack_message,
                sent_at,
                acked_at,
                created_at,
                updated_at
            FROM device_command
            WHERE command_id = #{commandId}
            LIMIT 1
            """)
    DeviceCommandRecord selectByCommandId(String commandId);

    @Update("""
            UPDATE device_command
            SET status = #{status},
                ack_message = #{ackMessage},
                acked_at = #{ackedAt},
                updated_at = #{updatedAt}
            WHERE command_id = #{commandId}
            """)
    int updateStatus(DeviceCommandRecord record);

    @Update("""
            UPDATE device_command
            SET status = #{record.status},
                ack_message = #{record.ackMessage},
                acked_at = #{record.ackedAt},
                updated_at = #{record.updatedAt}
            WHERE command_id = #{record.commandId}
              AND status = #{currentStatus}
            """)
    int updateStatusIfCurrentStatus(
            @Param("record") DeviceCommandRecord record,
            @Param("currentStatus") String currentStatus);

    @Select("""
            SELECT
                id,
                command_id,
                device_id,
                command_type,
                payload_json,
                status,
                ack_message,
                sent_at,
                acked_at,
                created_at,
                updated_at
            FROM device_command
            WHERE status = 'SENT'
              AND sent_at <= #{sentBefore}
            ORDER BY sent_at ASC, id ASC
            LIMIT #{limit}
            """)
    List<DeviceCommandRecord> selectTimedOutCandidates(
            @Param("sentBefore") Instant sentBefore,
            @Param("limit") int limit);

    @Select("""
            SELECT
                id,
                command_id,
                device_id,
                command_type,
                payload_json,
                status,
                ack_message,
                sent_at,
                acked_at,
                created_at,
                updated_at
            FROM device_command
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<DeviceCommandRecord> selectRecent(@Param("limit") int limit);
}
