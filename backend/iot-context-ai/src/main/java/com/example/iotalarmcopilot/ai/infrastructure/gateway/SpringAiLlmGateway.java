package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import com.example.iotalarmcopilot.ai.domain.model.AiStructuredSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.IOException;

/**
 * Spring AI LLM 适配器
 * openai兼容接口
 */
public class SpringAiLlmGateway implements LlmGateway {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;

    public SpringAiLlmGateway(
            ChatClient chatClient,
            ObjectMapper objectMapper,
            AiProperties aiProperties) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.aiProperties = aiProperties;
    }

    @Override
    public LlmAlarmSummaryResult generateAlarmSummary(LlmAlarmSummaryRequest request) {
        String rawResponse = chatClient.prompt(new Prompt(request.prompt()))
                .call()
                .content();
        AiStructuredSummary structuredSummary = parseStructuredSummary(rawResponse);
        return new LlmAlarmSummaryResult(
                structuredSummary,
                aiProperties.getModel(),
                request.promptVersion(),
                rawResponse);
    }

    /**
     * 解析结构化摘要
     *
     * @param rawResponse
     * @return
     */
    private AiStructuredSummary parseStructuredSummary(String rawResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(stripJsonFence(rawResponse));
            return new AiStructuredSummary(
                    readText(rootNode, "summary"),
                    readText(rootNode, "possibleCause"),
                    readText(rootNode, "inspectionSuggestion"),
                    readText(rootNode, "riskLevel"),
                    rootNode.path("confidence").decimalValue());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse AI structured summary JSON", exception);
        }
    }

    private String readText(JsonNode rootNode, String fieldName) {
        JsonNode fieldNode = rootNode.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            throw new IllegalStateException("Missing field in AI response: " + fieldName);
        }
        return fieldNode.asText();
    }

    /**
     * 去掉json fence
     *
     * @param rawResponse
     * @return
     */
    private String stripJsonFence(String rawResponse) {
        String trimmed = rawResponse == null ? "" : rawResponse.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7).trim();
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3).trim();
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed;
    }
}
