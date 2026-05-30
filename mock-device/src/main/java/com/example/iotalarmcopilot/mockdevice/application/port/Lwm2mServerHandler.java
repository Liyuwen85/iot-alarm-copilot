package com.example.iotalarmcopilot.mockdevice.application.port;

import com.example.iotalarmcopilot.mockdevice.domain.Lwm2mDeviceSnapshot;

public interface Lwm2mServerHandler {

    void onClientRegistered(String endpoint);

    void onClientUnregistered(String endpoint);

    void onTelemetryReported(Lwm2mDeviceSnapshot snapshot);
}
