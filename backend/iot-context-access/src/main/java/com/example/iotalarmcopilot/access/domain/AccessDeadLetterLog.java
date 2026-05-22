package com.example.iotalarmcopilot.access.domain;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 原始死信日志（在access中承担失败事实实体）
 *
 * @param id
 * @param deadLetterTopic   死信落点Topic
 * @param originalTopic     原始消费topic
 * @param originalPartition 原始分区，配合offset做定位
 * @param originalOffset    原始偏移量，幂等和追踪核心
 * @param consumerGroup     失败发生在哪个消费组
 * @param mqttTopic         原始的mqtt topic，方便回溯设备来源
 * @param deviceId
 * @param payload           原始消息体
 * @param exceptionType     异常类型
 * @param exceptionMessage
 * @param failedAt          失败事件时间
 * @param createdAt         入库时间
 */
public record AccessDeadLetterLog(
        Long id,
        String deadLetterTopic,
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        String consumerGroup,
        String mqttTopic,
        String deviceId,
        String payload,
        String exceptionType,
        String exceptionMessage,
        Instant failedAt,
        Instant createdAt) {

    public AccessDeadLetterLog {
        if (deadLetterTopic == null || deadLetterTopic.isBlank()) {
            throw new BaseDomainException("deadLetterTopic must not be blank");
        }
        if (originalTopic == null || originalTopic.isBlank()) {
            throw new BaseDomainException("originalTopic must not be blank");
        }
        if (originalPartition == null || originalPartition < 0) {
            throw new BaseDomainException("originalPartition must not be negative");
        }
        if (originalOffset == null || originalOffset < 0) {
            throw new BaseDomainException("originalOffset must not be negative");
        }
        if (exceptionType == null || exceptionType.isBlank()) {
            throw new BaseDomainException("exceptionType must not be blank");
        }
        Objects.requireNonNull(failedAt, "failedAt must not be null");
    }

    public static AccessDeadLetterLog create(
            String deadLetterTopic,
            String originalTopic,
            Integer originalPartition,
            Long originalOffset,
            String consumerGroup,
            String mqttTopic,
            String deviceId,
            String payload,
            String exceptionType,
            String exceptionMessage,
            Instant failedAt) {
        return new AccessDeadLetterLog(
                null,
                deadLetterTopic,
                originalTopic,
                originalPartition,
                originalOffset,
                consumerGroup,
                mqttTopic,
                deviceId,
                payload,
                exceptionType,
                exceptionMessage,
                failedAt,
                null);
    }
}
