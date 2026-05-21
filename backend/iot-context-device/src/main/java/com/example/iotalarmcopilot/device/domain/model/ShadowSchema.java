package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

import java.util.List;
import java.util.Objects;

/**
 * 设备影子模式
 *
 * @param reportedFields
 * @param desiredFields
 */
public record ShadowSchema(
        List<String> reportedFields,
        List<String> desiredFields) {

    public ShadowSchema {
        Objects.requireNonNull(reportedFields, "reportedFields must not be null");
        Objects.requireNonNull(desiredFields, "desiredFields must not be null");
        reportedFields = normalize(reportedFields, "reportedField");
        desiredFields = normalize(desiredFields, "desiredField");
    }

    public boolean supportsReportedField(String fieldName) {
        return reportedFields.contains(normalizeFieldName(fieldName, "reportedField"));
    }

    public boolean supportsDesiredField(String fieldName) {
        return desiredFields.contains(normalizeFieldName(fieldName, "desiredField"));
    }

    /**
     * 验证影子文档（仅验证是否了对应的字段）
     *
     * @param shadowDocument
     */
    public void validateDocument(String shadowDocument) {
        Objects.requireNonNull(shadowDocument, "shadowDocument must not be null");
        if (shadowDocument.isBlank()) {
            throw new BaseDomainException("shadowDocument must not be blank");
        }
        boolean containsReported = containsSection(shadowDocument, "\"reported\"", reportedFields);
        boolean containsDesired = containsSection(shadowDocument, "\"desired\"", desiredFields);
        if (!containsReported || !containsDesired) {
            throw new BaseDomainException("shadowDocument does not match product shadow schema");
        }
    }

    private static List<String> normalize(List<String> fields, String fieldName) {
        return List.copyOf(fields).stream()
                .map(field -> normalizeFieldName(field, fieldName))
                .toList();
    }

    /**
     * 归一化字段名称。如支持 "temperature"、temperature这两种写法
     *
     * @param field
     * @param fieldName
     * @return
     */
    private static String normalizeFieldName(String field, String fieldName) {
        if (field == null) {
            throw new BaseDomainException(fieldName + " must not be blank");
        }
        String normalized = field.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        if (normalized.isBlank()) {
            throw new BaseDomainException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private boolean containsSection(String shadowDocument, String sectionName, List<String> requiredFields) {
        if (requiredFields.isEmpty()) {
            return true;
        }
        if (!shadowDocument.contains(sectionName)) {
            return false;
        }
        return requiredFields.stream()
                .map(field -> "\"" + field + "\"")
                .allMatch(shadowDocument::contains);
    }
}
