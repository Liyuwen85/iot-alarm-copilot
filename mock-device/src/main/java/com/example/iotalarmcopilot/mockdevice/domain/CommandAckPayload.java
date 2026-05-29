package com.example.iotalarmcopilot.mockdevice.domain;

/**
 * 下行命令ACK回复消息
 */
public record CommandAckPayload(
        String commandId,
        String deviceId,
        String status,
        String ackedAt,
        String message) {
}
