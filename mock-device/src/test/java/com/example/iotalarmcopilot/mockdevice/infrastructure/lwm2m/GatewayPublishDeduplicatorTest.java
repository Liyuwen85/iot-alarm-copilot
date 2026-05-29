package com.example.iotalarmcopilot.mockdevice.infrastructure.lwm2m;

import com.example.iotalarmcopilot.mockdevice.domain.GatewayUplinkMessage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayPublishDeduplicatorTest {

    @Test
    void shouldSuppressSameFingerprintWithinWindow() {
        AtomicLong now = new AtomicLong(1_000L);
        GatewayPublishDeduplicator deduplicator = new GatewayPublishDeduplicator(5_000L, now::get);
        GatewayUplinkMessage message = message("2026-05-26T08:53:20Z");

        assertFalse(deduplicator.isDuplicate(message));

        deduplicator.markPublished(message);

        now.set(2_000L);
        assertTrue(deduplicator.isDuplicate(message));
    }

    @Test
    void shouldAllowSamePayloadAfterWindowExpires() {
        AtomicLong now = new AtomicLong(1_000L);
        GatewayPublishDeduplicator deduplicator = new GatewayPublishDeduplicator(5_000L, now::get);
        GatewayUplinkMessage message = message("2026-05-26T08:53:20Z");
        deduplicator.markPublished(message);

        now.set(6_100L);

        assertFalse(deduplicator.isDuplicate(message));
    }

    @Test
    void shouldAllowSameMetricsWhenTimestampChanges() {
        AtomicLong now = new AtomicLong(1_000L);
        GatewayPublishDeduplicator deduplicator = new GatewayPublishDeduplicator(5_000L, now::get);
        deduplicator.markPublished(message("2026-05-26T08:53:20Z"));

        now.set(2_000L);

        assertFalse(deduplicator.isDuplicate(message("2026-05-26T08:53:21Z")));
    }

    @Test
    void shouldForgetDeviceState() {
        AtomicLong now = new AtomicLong(1_000L);
        GatewayPublishDeduplicator deduplicator = new GatewayPublishDeduplicator(5_000L, now::get);
        GatewayUplinkMessage message = message("2026-05-26T08:53:20Z");
        deduplicator.markPublished(message);

        deduplicator.forget(message.deviceId());

        assertFalse(deduplicator.isDuplicate(message));
    }

    private GatewayUplinkMessage message(String ts) {
        return new GatewayUplinkMessage(
                "demo-002",
                "mock-gateway-01",
                "lwm2m-gateway",
                new BigDecimal("26.0"),
                new BigDecimal("58.0"),
                ts);
    }
}
