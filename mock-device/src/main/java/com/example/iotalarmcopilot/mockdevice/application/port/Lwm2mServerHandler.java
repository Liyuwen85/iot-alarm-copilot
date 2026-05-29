package com.example.iotalarmcopilot.mockdevice.application.port;

import com.example.iotalarmcopilot.mockdevice.domain.Lwm2mDeviceSnapshot;

public interface Lwm2mServerHandler {

    void onClientRegistered(String endpoint);

    void onTelemetryReported(String endpoint, Lwm2mDeviceSnapshot snapshot);
}
