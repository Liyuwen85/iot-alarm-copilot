package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.retry.support.RetryTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试远程AI调用
 */
@EnabledIfSystemProperty(named = "runRemoteAiTest", matches = "true")
class SpringAiLlmGatewayRemoteCallTest {

    @Test
    void should_call_remote_model_and_return_structured_summary() {
        String baseUrl = System.getenv("MY_OPENAI_BASE_URL");
        String model = System.getenv("OPENAI_MODEL");
        String apiKey = System.getenv("MY_OPENAI_API_KEY");

        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "MY_OPENAI_API_KEY is required");
        Assumptions.assumeTrue(baseUrl != null && !baseUrl.isBlank(), "MY_OPENAI_BASE_URL is required");

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model == null || model.isBlank() ? "gpt-5.4" : model)
                        .build())
                .retryTemplate(RetryTemplate.defaultInstance())
                .build();

        ChatClient chatClient = ChatClient.create(chatModel);
        AiProperties aiProperties = new AiProperties();
        aiProperties.setModel(model == null || model.isBlank() ? "gpt-5.4" : model);
        aiProperties.setEnabled(true);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient, new ObjectMapper(), aiProperties);
        LlmAlarmSummaryResult result = gateway.generateAlarmSummary(new LlmAlarmSummaryRequest(
                9L,
                "temperature_high:demo-001:1755346170563974121",
                "temperature_high",
                "demo-001",
                "WARN",
                Instant.parse("2026-05-22T15:03:18.426Z"),
                "alarm-summary-v1",
                """
                        请仅返回包含以下字段的 JSON：summary, possibleCause, inspectionSuggestion, riskLevel, confidence。
                        告警信息：设备 demo-001，规则 temperature_high，严重程度 WARN。
                        """));

        assertNotNull(result.rawResponse());
        assertEquals(model == null || model.isBlank() ? "gpt-5.4" : model, result.modelName());
        assertNotNull(result.summary().summary());
        assertNotNull(result.summary().possibleCause());
        assertNotNull(result.summary().inspectionSuggestion());
        assertNotNull(result.summary().riskLevel());
        assertNotNull(result.summary().confidence());
    }
}
