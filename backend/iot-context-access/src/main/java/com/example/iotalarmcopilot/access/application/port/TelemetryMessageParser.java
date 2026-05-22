package com.example.iotalarmcopilot.access.application.port;

import com.example.iotalarmcopilot.access.application.model.TelemetryMessage;
import com.example.iotalarmcopilot.contract.device.DeviceTelemetryModel;

/**
 * 遥测数据解析器
 */
public interface TelemetryMessageParser {

    TelemetryMessage parse(String rawJson, DeviceTelemetryModel deviceTelemetryModel);
}
