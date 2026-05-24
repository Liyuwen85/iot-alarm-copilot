package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringAiLlmGatewayTest {

    private final ChatClient chatClient = mock(ChatClient.class);
    private final ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    private final ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

    private SpringAiLlmGateway gateway;

    @BeforeEach
    void setUp() {
        AiProperties aiProperties = new AiProperties();
        aiProperties.setModel("gpt-5.4");
        gateway = new SpringAiLlmGateway(chatClient, new ObjectMapper(), aiProperties);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    void shouldGenerateStructuredSummaryFromPlainJson() {
        when(callResponseSpec.content()).thenReturn("""
                {
                  "summary": "temperature high",
                  "possibleCause": "ambient heat",
                  "inspectionSuggestion": "check fan",
                  "riskLevel": "medium",
                  "confidence": 0.90
                }
                """);

        LlmAlarmSummaryResult result = gateway.generateAlarmSummary(sampleRequest());

        assertThat(result.modelName()).isEqualTo("gpt-5.4");
        assertThat(result.promptVersion()).isEqualTo("alarm-summary-v1");
        assertThat(result.summary().summary()).isEqualTo("temperature high");
        assertThat(result.summary().possibleCause()).isEqualTo("ambient heat");
        assertThat(result.summary().inspectionSuggestion()).isEqualTo("check fan");
        assertThat(result.summary().riskLevel()).isEqualTo("MEDIUM");
        assertThat(result.summary().confidence()).isEqualByComparingTo(new BigDecimal("0.9"));
        verify(chatClient).prompt(any(Prompt.class));
    }

    @Test
    void shouldGenerateStructuredSummaryFromJsonFence() {
        when(callResponseSpec.content()).thenReturn("""
                ```json
                {
                  "summary": "temperature high",
                  "possibleCause": "ambient heat",
                  "inspectionSuggestion": "check fan",
                  "riskLevel": "high",
                  "confidence": 1.0
                }
                ```
                """);

        LlmAlarmSummaryResult result = gateway.generateAlarmSummary(sampleRequest());

        assertThat(result.summary().riskLevel()).isEqualTo("HIGH");
        assertThat(result.summary().confidence()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void shouldFailWhenJsonIsInvalid() {
        when(callResponseSpec.content()).thenReturn("not-json");

        assertThatThrownBy(() -> gateway.generateAlarmSummary(sampleRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to parse AI structured summary JSON");
    }

    @Test
    void shouldFailWhenRequiredFieldIsMissing() {
        when(callResponseSpec.content()).thenReturn("""
                {
                  "summary": "temperature high",
                  "possibleCause": "ambient heat",
                  "riskLevel": "medium",
                  "confidence": 0.8
                }
                """);

        assertThatThrownBy(() -> gateway.generateAlarmSummary(sampleRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Missing field in AI response: inspectionSuggestion");
    }

    private LlmAlarmSummaryRequest sampleRequest() {
        return new LlmAlarmSummaryRequest(
                9L,
                "temperature_high:demo-001:1755346170563974121",
                "temperature_high",
                "demo-001",
                "WARN",
                Instant.parse("2026-05-22T15:03:18.426Z"),
                "alarm-summary-v1",
                "summarize this alarm");
    }
}
