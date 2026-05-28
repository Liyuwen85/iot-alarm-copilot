package com.example.iotalarmcopilot.command.domain.model;

/**
 * 命令状态
 */
public enum CommandStatus {
    // 已创建
    CREATED,
    // 已发送
    SENT,
    // 发送超时
    TIMED_OUT,
    // ACK确认成功
    ACKED_SUCCESS,
    // ACK确认失败
    ACKED_FAILED
}
