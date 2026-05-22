package com.example.iotalarmcopilot.access.infrastructure.persistence;

import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLog;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 死信记录数据库实体
 */
@Getter
@Setter
public class AccessDeadLetterRecord {

    private Long id;
    private String deadLetterTopic;
    private String originalTopic;
    private Integer originalPartition;
    private Long originalOffset;
    private String consumerGroup;
    private String mqttTopic;
    private String deviceId;
    private String payload;
    private String exceptionType;
    private String exceptionMessage;
    private Instant failedAt;
    private Instant createdAt;

    public static AccessDeadLetterRecord fromDomain(AccessDeadLetterLog log) {
        AccessDeadLetterRecord record = new AccessDeadLetterRecord();
        record.setId(log.id());
        record.setDeadLetterTopic(log.deadLetterTopic());
        record.setOriginalTopic(log.originalTopic());
        record.setOriginalPartition(log.originalPartition());
        record.setOriginalOffset(log.originalOffset());
        record.setConsumerGroup(log.consumerGroup());
        record.setMqttTopic(log.mqttTopic());
        record.setDeviceId(log.deviceId());
        record.setPayload(log.payload());
        record.setExceptionType(log.exceptionType());
        record.setExceptionMessage(log.exceptionMessage());
        record.setFailedAt(log.failedAt());
        record.setCreatedAt(log.createdAt());
        return record;
    }

    public AccessDeadLetterLog toDomain() {
        return new AccessDeadLetterLog(
                id,
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
                createdAt);
    }

}
