package com.example.iotalarmcopilot.mockdevice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 下行的命令-设置上报间隔
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SetReportIntervalCommandPayload(
        String commandId,
        String deviceId,
        String commandType,
        CommandParams params) {

    /**
     * 上报间隔-毫秒
     */
    public record CommandParams(int intervalMs) {
    }
}
