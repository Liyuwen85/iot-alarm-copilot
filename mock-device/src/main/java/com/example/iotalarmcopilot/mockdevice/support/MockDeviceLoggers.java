package com.example.iotalarmcopilot.mockdevice.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MockDeviceLoggers {

    private static final Logger DEVICE_LOGGER = LoggerFactory.getLogger("mock-device.device");
    private static final Logger GATEWAY_LOGGER = LoggerFactory.getLogger("mock-device.gateway");

    private MockDeviceLoggers() {
    }

    public static Logger deviceLogger() {
        return DEVICE_LOGGER;
    }

    public static Logger gatewayLogger() {
        return GATEWAY_LOGGER;
    }
}
