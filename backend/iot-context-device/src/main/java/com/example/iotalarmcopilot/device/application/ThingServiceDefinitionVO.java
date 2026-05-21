package com.example.iotalarmcopilot.device.application;

import java.util.List;

public record ThingServiceDefinitionVO(
        String serviceCode,
        String serviceName,
        List<String> inputCapabilities) {
}
