package com.example.iotalarmcopilot.mockdevice.application;

import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerHandler;
import com.example.iotalarmcopilot.mockdevice.application.port.MqttMessagePublisher;
import com.example.iotalarmcopilot.mockdevice.domain.Lwm2mDeviceSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GatewayTelemetryForwarder implements Lwm2mServerHandler {

    private final MqttMessagePublisher mqttMessagePublisher;
    private final Map<String, Lwm2mDeviceSnapshot> snapshots;

    public GatewayTelemetryForwarder(MqttMessagePublisher mqttMessagePublisher) {
        this.mqttMessagePublisher = mqttMessagePublisher;
        this.snapshots = new ConcurrentHashMap<>();
    }

    @Override
    public void onClientRegistered(String endpoint) {
        snapshots.put(endpoint, Lwm2mDeviceSnapshot.empty(endpoint));
    }

    @Override
    public void onTelemetryReported(String endpoint, Lwm2mDeviceSnapshot snapshot) {
        snapshots.put(endpoint, snapshot);
    }
}
