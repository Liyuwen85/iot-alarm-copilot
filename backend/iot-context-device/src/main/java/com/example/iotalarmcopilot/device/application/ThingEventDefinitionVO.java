package com.example.iotalarmcopilot.device.application;

import java.util.List;

public record ThingEventDefinitionVO(
        String eventCode,
        String eventName,
        List<String> outputCapabilities) {
}
