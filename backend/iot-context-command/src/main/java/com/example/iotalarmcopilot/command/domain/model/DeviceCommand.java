package com.example.iotalarmcopilot.command.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * 下行领域实体
 *
 * @param id
 * @param commandId
 * @param deviceId
 * @param commandType
 * @param payloadJson
 * @param status
 * @param ackMessage  ack消息
 * @param sentAt
 * @param ackedAt
 * @param createdAt
 * @param updatedAt
 */
public record DeviceCommand(
        Long id,
        String commandId,
        String deviceId,
        CommandType commandType,
        String payloadJson,
        CommandStatus status,
        String ackMessage,
        Instant sentAt,
        Instant ackedAt,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * 创建一个设置上报间隔的命令
     */
    public static DeviceCommand createSetReportInterval(
            String commandId,
            String deviceId,
            String payloadJson,
            Instant createdAt) {
        return new DeviceCommand(
                null,
                commandId,
                deviceId,
                CommandType.SET_REPORT_INTERVAL,
                payloadJson,
                CommandStatus.CREATED,
                null,
                createdAt,
                null,
                createdAt,
                createdAt);
    }

    public DeviceCommand markSent(Instant sentAt) {
        Objects.requireNonNull(sentAt, "sentAt must not be null");
        if (status != CommandStatus.CREATED) {
            throw new BaseDomainException("Only created command can be marked as sent");
        }
        return new DeviceCommand(
                id,
                commandId,
                deviceId,
                commandType,
                payloadJson,
                CommandStatus.SENT,
                null,
                sentAt,
                null,
                createdAt,
                sentAt);
    }

    public DeviceCommand markAckSuccess(String ackMessage, Instant ackedAt) {
        Objects.requireNonNull(ackedAt, "ackedAt must not be null");
        ensureAckAllowed();
        return new DeviceCommand(
                id,
                commandId,
                deviceId,
                commandType,
                payloadJson,
                CommandStatus.ACKED_SUCCESS,
                normalizeAckMessage(ackMessage),
                sentAt,
                ackedAt,
                createdAt,
                ackedAt);
    }

    public DeviceCommand markAckFailed(String ackMessage, Instant ackedAt) {
        Objects.requireNonNull(ackedAt, "ackedAt must not be null");
        ensureAckAllowed();
        return new DeviceCommand(
                id,
                commandId,
                deviceId,
                commandType,
                payloadJson,
                CommandStatus.ACKED_FAILED,
                normalizeAckMessage(ackMessage),
                sentAt,
                ackedAt,
                createdAt,
                ackedAt);
    }

    public boolean isAcked() {
        return status == CommandStatus.ACKED_SUCCESS || status == CommandStatus.ACKED_FAILED;
    }

    public DeviceCommand markTimedOut(String timeoutMessage, Instant timedOutAt) {
        Objects.requireNonNull(timedOutAt, "timedOutAt must not be null");
        if (status != CommandStatus.SENT) {
            throw new BaseDomainException("Only sent command can be timed out");
        }
        return new DeviceCommand(
                id,
                commandId,
                deviceId,
                commandType,
                payloadJson,
                CommandStatus.TIMED_OUT,
                normalizeAckMessage(timeoutMessage),
                sentAt,
                null,
                createdAt,
                timedOutAt);
    }

    public boolean isTerminal() {
        return status == CommandStatus.ACKED_SUCCESS
                || status == CommandStatus.ACKED_FAILED
                || status == CommandStatus.TIMED_OUT;
    }

    public DeviceCommand {
        Objects.requireNonNull(commandId, "commandId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(commandType, "commandType must not be null");
        Objects.requireNonNull(payloadJson, "payloadJson must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(sentAt, "sentAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (commandId.isBlank()) {
            throw new BaseDomainException("commandId must not be blank");
        }
        if (deviceId.isBlank()) {
            throw new BaseDomainException("deviceId must not be blank");
        }
        if (payloadJson.isBlank()) {
            throw new BaseDomainException("payloadJson must not be blank");
        }
        if (status == CommandStatus.ACKED_SUCCESS || status == CommandStatus.ACKED_FAILED) {
            if (ackedAt == null) {
                throw new BaseDomainException("ackedAt must not be null when command is acked");
            }
        }
    }

    private void ensureAckAllowed() {
        if (status != CommandStatus.SENT) {
            throw new BaseDomainException("Only sent command can be acknowledged");
        }
    }

    private static String normalizeAckMessage(String ackMessage) {
        return ackMessage == null ? "" : ackMessage.trim();
    }
}
