package com.example.iotalarmcopilot.access.domain;

/**
 * 遥测数据解析器
 */
public interface TelemetryMessageParser {

    TelemetryMessage parse(String rawJson);
}
