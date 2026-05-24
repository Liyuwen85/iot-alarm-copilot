package com.example.iotalarmcopilot.ai.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * AI结构化摘要输出值对象
 *
 * @param summary              摘要
 * @param possibleCause        AI对可能原因的分析
 * @param inspectionSuggestion 建议动作
 * @param riskLevel            AI给出的风险等级
 * @param confidence           置信度
 */
public record AiStructuredSummary(
        String summary,
        String possibleCause,
        String inspectionSuggestion,
        String riskLevel,
        BigDecimal confidence) {

    public AiStructuredSummary {
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(possibleCause, "possibleCause must not be null");
        Objects.requireNonNull(inspectionSuggestion, "inspectionSuggestion must not be null");
        Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        Objects.requireNonNull(confidence, "confidence must not be null");
        if (summary.isBlank()) {
            throw new BaseDomainException("summary must not be blank");
        }
        if (possibleCause.isBlank()) {
            throw new BaseDomainException("possibleCause must not be blank");
        }
        if (inspectionSuggestion.isBlank()) {
            throw new BaseDomainException("inspectionSuggestion must not be blank");
        }
        if (riskLevel.isBlank()) {
            throw new BaseDomainException("riskLevel must not be blank");
        }
        if (confidence.compareTo(BigDecimal.ZERO) < 0 || confidence.compareTo(BigDecimal.ONE) > 0) {
            throw new BaseDomainException("confidence must be between 0 and 1");
        }
        riskLevel = riskLevel.toUpperCase(Locale.ROOT);
        confidence = confidence.stripTrailingZeros();
    }
}
