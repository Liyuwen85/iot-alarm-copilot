package com.example.iotalarmcopilot.mockdevice.application;

import com.example.iotalarmcopilot.mockdevice.domain.GatewayUplinkMessage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 去重策略
 */
public class GatewayTelemetryDeduplicator {

    private final long supppressWindowMs;
    private final Map<String, PublishedFingerprint> lastPublishedByDevice;

    public GatewayTelemetryDeduplicator(long supppressWindowMs) {
        this.supppressWindowMs = supppressWindowMs;
        this.lastPublishedByDevice = new ConcurrentHashMap<>();
    }

    public boolean isDuplicate(GatewayUplinkMessage message) {
        PublishedFingerprint previous = lastPublishedByDevice.get(message.deviceId());
        if (previous == null) {
            return false;
        }

        long ageMs = System.currentTimeMillis() - previous.publishedAtMs();
        if (ageMs > supppressWindowMs) {
            return false;
        }

        return previous.fingerprint.equals(fingerprintOf(message));
    }

    public void markPublished(GatewayUplinkMessage message) {
        lastPublishedByDevice.put(message.deviceId(), new PublishedFingerprint(
                fingerprintOf(message),
                System.currentTimeMillis()
        ));
    }

    public void forget(String deviceId) {
        lastPublishedByDevice.remove(deviceId);
    }

    private String fingerprintOf(GatewayUplinkMessage message) {
        return String.join("|",
                message.deviceId(),
                normalizeDecimal(message.temperature()),
                normalizeDecimal(message.humidity()),
                message.ts());
    }

    private String normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return "null";
        }
        // eg: 26.00 same 26.0
        return value.stripTrailingZeros().toPlainString();
    }

    private record PublishedFingerprint(String fingerprint, long publishedAtMs) {
    }
}
