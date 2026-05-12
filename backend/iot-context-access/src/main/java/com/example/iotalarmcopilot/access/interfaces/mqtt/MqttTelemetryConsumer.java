package com.example.iotalarmcopilot.access.interfaces.mqtt;

import com.example.iotalarmcopilot.access.application.TelemetryAccessApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 启动（或停止）注册MQTT消费者
 * 注：目前仅支持Version 3.1.1
 */
@Slf4j
@Component
public class MqttTelemetryConsumer implements SmartLifecycle {

    private final MqttAccessProperties properties;
    private final TelemetryAccessApplicationService telemetryAccessApplicationService;

    private volatile boolean running;
    private MqttClient client;

    public MqttTelemetryConsumer(
            MqttAccessProperties properties,
            TelemetryAccessApplicationService telemetryAccessApplicationService) {
        this.properties = properties;
        this.telemetryAccessApplicationService = telemetryAccessApplicationService;
    }

    @Override
    public synchronized void start() {
        if (running || !properties.isEnabled()) {
            if (!properties.isEnabled()) {
                log.info("MQTT access consumer is disabled by configuration");
            }
            return;
        }

        try {
            MqttClient mqttClient = new MqttClient(
                    properties.getBrokerUrl(),
                    properties.getClientId(),
                    new MemoryPersistence());

            // 设置回调
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    try {
                        // 订阅主题
                        mqttClient.subscribe(properties.getInboundTopic(), properties.getQos());
                        log.info("MQTT access subscription ready. topic={}, reconnect={}",
                                properties.getInboundTopic(),
                                reconnect);
                    } catch (MqttException exception) {
                        log.error("Failed to subscribe telemetry topic {}", properties.getInboundTopic(), exception);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT access connection lost", cause);
                }

                /**
                 * 接受消息
                 * @param topic
                 * @param message
                 */
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    try {
                        telemetryAccessApplicationService.ingestMqttTelemetry(topic, payload);
                        log.info("Telemetry ingested from topic={}", topic);
                    } catch (Exception exception) {
                        log.error("Failed to process telemetry message. topic={}, payload={}", topic, payload, exception);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // no-op for subscriber
                }
            });

            // 连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            mqttClient.connect(options);

            client = mqttClient;
            running = true;
            log.info("MQTT access client connected. broker={}, clientId={}",
                    properties.getBrokerUrl(),
                    properties.getClientId());
        } catch (MqttException exception) {
            log.warn("MQTT access client failed to connect. broker={}, clientId={}",
                    properties.getBrokerUrl(),
                    properties.getClientId(),
                    exception);
        }
    }

    @Override
    public synchronized void stop() {
        if (client == null) {
            running = false;
            return;
        }

        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException exception) {
            log.warn("Error while stopping MQTT access client", exception);
        } finally {
            client = null;
            running = false;
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
