package com.example.iotalarmcopilot.mockdevice.interfaces.mqtt;

import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 抽象command处理类
 */
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

        // 设置回调
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                try {
                    if (client != null && client.isConnected()) {
                        // 订阅
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
                // 下行command处理
                String payloadText = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.printf("command-received topic=%s payload=%s%n", topic, payloadText);
                if (!matchesTopic(clientProvider.commandTopic(), topic)) {
                    return;
                }
                try {
                    SetReportIntervalCommandPayload command = objectMapper.readValue(
                            payloadText,
                            SetReportIntervalCommandPayload.class);

                    // 处理command
                    process(command);

                    System.out.printf("command-applied topic=%s commandId=%s intervalMs=%d%n",
                            topic,
                            command.commandId(),
                            command.params().intervalMs());
                } catch (Exception exception) {
                    System.out.printf("command-handling-failed topic=%s reason=%s%n",
                            topic,
                            exception.getMessage());
                    exception.printStackTrace(System.out);
                    throw exception;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        // 连接设置
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            client.connect(options);
        } catch (MqttException e) {
            throw new IllegalStateException("failed to connect to broker", e);
        }
    }

    /**
     * 处理command
     *
     * @param payload
     */
    protected abstract void process(SetReportIntervalCommandPayload payload);

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
