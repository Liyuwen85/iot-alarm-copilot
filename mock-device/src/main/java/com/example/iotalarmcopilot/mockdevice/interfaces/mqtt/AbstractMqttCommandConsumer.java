package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public abstract class AbstractMqttCommandConsumer implements AutoCloseable {

    protected final MqttSubscriberClientProvider clientProvider;
    private final ObjectMapper objectMapper;
    protected MqttClient client;

    public AbstractMqttCommandConsumer(MqttSubscriberClientProvider clientProvider) {
        this.clientProvider = clientProvider;
        this.objectMapper = new ObjectMapper();
    }

    public void subscribe() {
        if (client == null || !client.isConnected()) {
            this.client = clientProvider.create();
        }

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                try {
                    if (client != null && client.isConnected()) {
                        client.subscribe(clientProvider.commandTopic(), clientProvider.qos());
                    }
                } catch (MqttException exception) {
                    throw new IllegalStateException("failed to subscribe command topic", exception);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payloadText = new String(message.getPayload(), StandardCharsets.UTF_8);
                logger().info("command-received topic={}", topic);
                logger().debug("command-payload topic={} payload={}", topic, payloadText);
                if (!matchesTopic(clientProvider.commandTopic(), topic)) {
                    return;
                }
                try {
                    SetReportIntervalCommandPayload command = objectMapper.readValue(
                            payloadText,
                            SetReportIntervalCommandPayload.class);
                    process(command);
                    logger().info("command-applied topic={} commandId={} intervalMs={}",
                            topic,
                            command.commandId(),
                            command.params().intervalMs());
                } catch (Exception exception) {
                    logger().warn("command-handling-failed topic={} reason={}",
                            topic,
                            exception.getMessage());
                    throw exception;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            client.connect(options);
        } catch (MqttException e) {
            throw new IllegalStateException("failed to connect to broker", e);
        }
    }

    protected abstract void process(SetReportIntervalCommandPayload payload);

    protected abstract Logger logger();

    @Override
    public void close() {
        clientProvider.close(client);
    }

    static boolean matchesTopic(String topicFilter, String topic) {
        return Pattern.compile(toRegex(topicFilter)).matcher(topic).matches();
    }

    private static String toRegex(String topicFilter) {
        StringBuilder pattern = new StringBuilder("^");
        for (int i = 0; i < topicFilter.length(); i++) {
            char ch = topicFilter.charAt(i);
            if (ch == '+') {
                pattern.append("[^/]+");
                continue;
            }
            if (ch == '#') {
                pattern.append(".+");
                continue;
            }
            if ("\\.[]{}()*+-?^$|".indexOf(ch) >= 0) {
                pattern.append('\\');
            }
            pattern.append(ch);
        }
        pattern.append('$');
        return pattern.toString();
    }
}
