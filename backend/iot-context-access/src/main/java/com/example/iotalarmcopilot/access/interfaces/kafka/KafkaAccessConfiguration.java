package com.example.iotalarmcopilot.access.interfaces.kafka;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.application.AccessDeadLetterCaptureApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.time.Duration;

/**
 * Kafka消息处理配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaAccessProperties.class)
public class KafkaAccessConfiguration {

    /**
     * 死信恢复器
     *
     * @param kafkaOperations           Kafka操作
     * @param properties                Kafka属性
     * @param captureApplicationService 捕获应用服务
     * @return 消息处理失败的恢复者
     */
    @Bean
    public ConsumerRecordRecoverer kafkaAccessDeadLetterRecoverer(
            KafkaOperations<Object, Object> kafkaOperations,
            KafkaAccessProperties properties,
            AccessDeadLetterCaptureApplicationService captureApplicationService) {
        // 创建死信恢复者
        DeadLetterPublishingRecoverer delegate = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(properties.resolveDeadLetterTopic(), record.partition()));
        // 配置发送行为
        delegate.setFailIfSendResultIsError(true);  // 发送失败时抛出异常
        delegate.setWaitForSendResultTimeout(Duration.ofSeconds(5));    // 等待发送结果超时
        // 包装成自定义恢复器(存入数据库)
        return new KafkaAccessDeadLetterRecoverer(properties, captureApplicationService, delegate);
    }

    /**
     * 通用错误处理器
     *
     * @param recoverer  自定义的恢复器
     * @param properties 属性
     * @return 错误处理程序
     */
    @Bean
    public CommonErrorHandler kafkaAccessCommonErrorHandler(
            ConsumerRecordRecoverer recoverer,
            KafkaAccessProperties properties) {
        KafkaAccessProperties.Retry retry = properties.getRetry();

        // 指数退避策略
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retry.getMaxRetries());
        backOff.setInitialInterval(retry.getInitialIntervalMs());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxIntervalMs());

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        // 消息成功恢复后提交offset，避免重复消费
        errorHandler.setCommitRecovered(true);
        // 不可重试的异常
        errorHandler.addNotRetryableExceptions(
                BaseDomainException.class,
                IllegalArgumentException.class,
                ClassCastException.class,
                DeserializationException.class);

        // 重试监听器（记录每次重试日志）
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) -> log.warn(
                "Retrying kafka telemetry message. topic={}, partition={}, offset={}, attempt={}, reason={}",
                record.topic(),
                record.partition(),
                record.offset(),
                deliveryAttempt,
                exception.getMessage()));
        return errorHandler;
    }
}
