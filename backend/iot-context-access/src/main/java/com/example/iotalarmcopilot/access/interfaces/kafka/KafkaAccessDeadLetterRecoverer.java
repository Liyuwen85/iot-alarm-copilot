package com.example.iotalarmcopilot.access.interfaces.kafka;

import com.example.iotalarmcopilot.access.application.AccessDeadLetterCaptureApplicationService;
import com.example.iotalarmcopilot.access.application.RecordAccessDeadLetterCommand;
import com.example.iotalarmcopilot.access.application.model.TelemetryTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 恢复层，处理死信逻辑
 */
@Slf4j
public class KafkaAccessDeadLetterRecoverer implements ConsumerRecordRecoverer {

    private final KafkaAccessProperties properties;
    private final AccessDeadLetterCaptureApplicationService captureApplicationService;
    private final ConsumerRecordRecoverer delegate;

    public KafkaAccessDeadLetterRecoverer(
            KafkaAccessProperties properties,
            AccessDeadLetterCaptureApplicationService captureApplicationService,
            ConsumerRecordRecoverer delegate) {
        this.properties = properties;
        this.captureApplicationService = captureApplicationService;
        this.delegate = delegate;
    }

    @Override
    public void accept(ConsumerRecord<?, ?> record, Exception exception) {
        // 先入库
        captureBestEffort(record, exception);
        // 再委托将消息发送到DLT中
        delegate.accept(record, exception);
    }

    /**
     * 尝试将消息记录到数据库中
     */
    private void captureBestEffort(ConsumerRecord<?, ?> record, Exception exception) {
        try {
            Throwable rootCause = mostSpecificCause(exception);
            String mqttTopic = resolveMqttTopic(record.value());
            captureApplicationService.record(new RecordAccessDeadLetterCommand(
                    properties.resolveDeadLetterTopic(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    properties.getConsumerGroup(),
                    mqttTopic,
                    resolveDeviceId(mqttTopic),
                    resolvePayload(record.value()),
                    rootCause.getClass().getName(),
                    rootCause.getMessage(),
                    Instant.now()));
        } catch (Exception captureException) {
            log.error("Failed to persist access dead letter observation. originalTopic={}, partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    captureException);
        }
    }

    private Throwable mostSpecificCause(Throwable exception) {
        Throwable candidate = exception;
        while (candidate.getCause() != null && candidate.getCause() != candidate) {
            candidate = candidate.getCause();
        }
        return candidate;
    }

    private String resolveMqttTopic(Object value) {
        if (value instanceof KafkaTelemetryEnvelope envelope) {
            return envelope.topic();
        }
        return null;
    }

    private String resolveDeviceId(String mqttTopic) {
        if (mqttTopic == null || mqttTopic.isBlank()) {
            return null;
        }
        try {
            return new TelemetryTopic(mqttTopic).deviceId();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String resolvePayload(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof KafkaTelemetryEnvelope envelope) {
            return envelope.payload();
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return String.valueOf(value);
    }
}
