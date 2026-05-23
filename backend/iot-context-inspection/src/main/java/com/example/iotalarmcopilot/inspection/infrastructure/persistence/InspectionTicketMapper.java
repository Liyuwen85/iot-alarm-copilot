package com.example.iotalarmcopilot.inspection.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 工单持久化接口
 */
@Mapper
public interface InspectionTicketMapper {

    @Insert("""
            INSERT INTO inspection_ticket (
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                summary,
                suggestion,
                status,
                alarm_triggered_at,
                created_at,
                updated_at,
                confirmed_at,
                closed_at
            ) VALUES (
                #{alarmId},
                #{alarmDedupKey},
                #{ruleCode},
                #{deviceId},
                #{severity},
                #{summary},
                #{suggestion},
                #{status},
                #{alarmTriggeredAt},
                #{createdAt},
                #{updatedAt},
                #{confirmedAt},
                #{closedAt}
            )
            ON CONFLICT (alarm_id) DO NOTHING
            """)
    int insertIgnore(InspectionTicketRecord record);

    @Select("""
            SELECT
                id,
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                summary,
                suggestion,
                status,
                alarm_triggered_at,
                created_at,
                updated_at,
                confirmed_at,
                closed_at
            FROM inspection_ticket
            WHERE id = #{id}
            LIMIT 1
            """)
    InspectionTicketRecord selectById(Long id);

    @Select("""
            SELECT
                id,
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                summary,
                suggestion,
                status,
                alarm_triggered_at,
                created_at,
                updated_at,
                confirmed_at,
                closed_at
            FROM inspection_ticket
            WHERE alarm_id = #{alarmId}
            LIMIT 1
            """)
    InspectionTicketRecord selectByAlarmId(Long alarmId);

    @Update("""
            UPDATE inspection_ticket
            SET status = #{record.status},
                updated_at = #{record.updatedAt},
                confirmed_at = #{record.confirmedAt},
                closed_at = #{record.closedAt}
            WHERE id = #{record.id}
              AND status = #{expectedStatus}
            """)
    int updateStatusIfCurrentStatusMatches(
            @Param("record") InspectionTicketRecord record,
            @Param("expectedStatus") String expectedStatus);

    @Select("""
            SELECT
                id,
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                summary,
                suggestion,
                status,
                alarm_triggered_at,
                created_at,
                updated_at,
                confirmed_at,
                closed_at
            FROM inspection_ticket
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<InspectionTicketRecord> selectRecent(int limit);
}
