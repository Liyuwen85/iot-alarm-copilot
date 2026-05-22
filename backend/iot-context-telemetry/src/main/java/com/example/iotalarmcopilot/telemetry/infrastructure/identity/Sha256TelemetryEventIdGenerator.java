package com.example.iotalarmcopilot.telemetry.infrastructure.identity;

import com.example.iotalarmcopilot.telemetry.application.port.TelemetryEventIdGenerator;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * 模拟生成唯一ID
 */
@Component
public class Sha256TelemetryEventIdGenerator implements TelemetryEventIdGenerator {

    // 分布式中最好用snowflake算法或uid生成
    @Override
    public Long nextId(String deviceId, Instant reportedAt, String rawJson) {
        byte[] source = (deviceId + "\n" + reportedAt + "\n" + rawJson)
                .getBytes(StandardCharsets.UTF_8);
        byte[] hash = digest(source);
        long candidate = ByteBuffer.wrap(hash, 0, Long.BYTES).getLong() & Long.MAX_VALUE;
        return candidate == 0L ? 1L : candidate;
    }

    private byte[] digest(byte[] source) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(source);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
