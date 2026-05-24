package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 配置LlmGateway
 */
@Configuration
public class AiGatewayConfiguration {

    @Bean
    @ConditionalOnClass(ChatClient.class)
    @ConditionalOnProperty(prefix = "iot.ai", name = "enabled", havingValue = "true")
    public LlmGateway springAiLlmGateway(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectMapper objectMapper,
            AiProperties aiProperties) {
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder == null) {
            throw new IllegalStateException("Spring AI ChatClient.Builder is not available. Check Spring AI dependency and provider configuration.");
        }
        return new SpringAiLlmGateway(chatClientBuilder.build(), objectMapper, aiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(LlmGateway.class)
    public LlmGateway disabledLlmGateway(AiProperties aiProperties) {
        return new DisabledLlmGateway(aiProperties);
    }
}
