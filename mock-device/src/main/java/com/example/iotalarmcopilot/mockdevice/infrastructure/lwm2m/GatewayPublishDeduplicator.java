package com.example.iotalarmcopilot.mockdevice.infrastructure.lwm2m;

import com.example.iotalarmcopilot.mockdevice.domain.GatewayUplinkMessage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * 网关发送器（含去重）
 */
public final class GatewayPublishDeduplicator {

    private final long duplicateWindowMs;
    private final LongSupplier currentTimeMillis;
    private final Map<String, PublishedFingerprint> lastPublishedByDevice;

    public GatewayPublishDeduplicator(long duplicateWindowMs) {
        this(duplicateWindowMs, System::currentTimeMillis);
    }

    GatewayPublishDeduplicator(long duplicateWindowMs, LongSupplier currentTimeMillis) {
        if (duplicateWindowMs <= 0) {
            throw new IllegalArgumentException("duplicateWindowMs must be positive");
        }
        this.duplicateWindowMs = duplicateWindowMs;
        this.currentTimeMillis = Objects.requireNonNull(currentTimeMillis, "currentTimeMillis cannot be null");
        this.lastPublishedByDevice = new ConcurrentHashMap<>();
    }

    public boolean isDuplicate(GatewayUplinkMessage message) {
        PublishedFingerprint previous = lastPublishedByDevice.get(message.deviceId());
        if (previous == null) {
            return false;
        }
        if (!previous.fingerprint().equals(fingerprintOf(message))) {
            return false;
        }
        long ageMs = currentTimeMillis.getAsLong() - previous.publishedAtMs();
        return ageMs >= 0 && ageMs <= duplicateWindowMs;
    }

    public void markPublished(GatewayUplinkMessage message) {
        lastPublishedByDevice.put(
                message.deviceId(),
                new PublishedFingerprint(fingerprintOf(message), currentTimeMillis.getAsLong()));
    }

    public void forget(String deviceId) {
        lastPublishedByDevice.remove(deviceId);
    }

    public void clear() {
        lastPublishedByDevice.clear();
    }

    private String fingerprintOf(GatewayUplinkMessage message) {
        return String.join("|",
                message.deviceId(),
                normalizeDecimal(message.temperature()),
                normalizeDecimal(message.humidity()),
                String.valueOf(message.ts()));
    }

    private String normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return "null";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private record PublishedFingerprint(String fingerprint, long publishedAtMs) {
    }
}
