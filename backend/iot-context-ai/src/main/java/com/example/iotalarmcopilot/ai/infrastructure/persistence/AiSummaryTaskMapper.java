package com.example.iotalarmcopilot.ai.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MyBatis 映射器接口
 */
@Mapper
public interface AiSummaryTaskMapper {

    // 幂等
    @Insert("""
            INSERT INTO ai_alarm_summary_task (
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                status,
                attempt_count,
                summary,
                possible_cause,
                inspection_suggestion,
                risk_level,
                confidence,
                model_name,
                prompt_version,
                request_payload,
                response_payload,
                error_code,
                error_message,
                alarm_triggered_at,
                created_at,
                updated_at,
                started_at,
                finished_at
            ) VALUES (
                #{alarmId},
                #{alarmDedupKey},
                #{ruleCode},
                #{deviceId},
                #{severity},
                #{status},
                #{attemptCount},
                #{summary},
                #{possibleCause},
                #{inspectionSuggestion},
                #{riskLevel},
                #{confidence},
                #{modelName},
                #{promptVersion},
                #{requestPayload},
                #{responsePayload},
                #{errorCode},
                #{errorMessage},
                #{alarmTriggeredAt},
                #{createdAt},
                #{updatedAt},
                #{startedAt},
                #{finishedAt}
            )
            ON CONFLICT (alarm_id) DO NOTHING
            """)
    int insertIgnore(AiSummaryTaskRecord record);

    @Select("""
            SELECT
                id,
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                status,
                attempt_count,
                summary,
                possible_cause,
                inspection_suggestion,
                risk_level,
                confidence,
                model_name,
                prompt_version,
                request_payload,
                response_payload,
                error_code,
                error_message,
                alarm_triggered_at,
                created_at,
                updated_at,
                started_at,
                finished_at
            FROM ai_alarm_summary_task
            WHERE id = #{id}
            LIMIT 1
            """)
    AiSummaryTaskRecord selectById(Long id);

    @Select("""
            SELECT
                id,
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                status,
                attempt_count,
                summary,
                possible_cause,
                inspection_suggestion,
                risk_level,
                confidence,
                model_name,
                prompt_version,
                request_payload,
                response_payload,
                error_code,
                error_message,
                alarm_triggered_at,
                created_at,
                updated_at,
                started_at,
                finished_at
            FROM ai_alarm_summary_task
            WHERE alarm_id = #{alarmId}
            LIMIT 1
            """)
    AiSummaryTaskRecord selectByAlarmId(Long alarmId);

    @Update("""
            UPDATE ai_alarm_summary_task
            SET status = #{record.status},
                attempt_count = #{record.attemptCount},
                summary = #{record.summary},
                possible_cause = #{record.possibleCause},
                inspection_suggestion = #{record.inspectionSuggestion},
                risk_level = #{record.riskLevel},
                confidence = #{record.confidence},
                model_name = #{record.modelName},
                prompt_version = #{record.promptVersion},
                request_payload = #{record.requestPayload},
                response_payload = #{record.responsePayload},
                error_code = #{record.errorCode},
                error_message = #{record.errorMessage},
                updated_at = #{record.updatedAt},
                started_at = #{record.startedAt},
                finished_at = #{record.finishedAt}
            WHERE id = #{record.id}
              AND status = #{expectedStatus}
            """)
    int updateStatusIfCurrentStatusMatches(
            @Param("record") AiSummaryTaskRecord record,
            @Param("expectedStatus") String expectedStatus);

    @Select("""
            SELECT
                id,
                alarm_id,
                alarm_dedup_key,
                rule_code,
                device_id,
                severity,
                status,
                attempt_count,
                summary,
                possible_cause,
                inspection_suggestion,
                risk_level,
                confidence,
                model_name,
                prompt_version,
                request_payload,
                response_payload,
                error_code,
                error_message,
                alarm_triggered_at,
                created_at,
                updated_at,
                started_at,
                finished_at
            FROM ai_alarm_summary_task
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<AiSummaryTaskRecord> selectRecent(int limit);
}
