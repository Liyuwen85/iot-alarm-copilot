package com.example.iotalarmcopilot.access.infrastructure.parser;

import com.example.iotalarmcopilot.access.domain.TelemetryMessage;
import com.example.iotalarmcopilot.BaseDomainException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonTelemetryMessageParserTest {

    private final JacksonTelemetryMessageParser parser =
            new JacksonTelemetryMessageParser(new ObjectMapper());

    @Test
    void should_parse_valid_payload() {
        String rawJson = """
                {
                  "deviceId":"dev-01",
                  "temperature":"81.5",
                  "reportedAt":"2026-05-13T18:00:00+08:00"
                }
                """;

        TelemetryMessage message = parser.parse(rawJson);

        assertEquals("dev-01", message.deviceId());
        assertEquals(BigDecimal.valueOf(81.5), message.metrics().temperature());
        assertEquals(Instant.parse("2026-05-13T10:00:00Z"), message.reportedAt());
    }

    @Test
    void should_reject_invalid_number() {
        String rawJson = """
                {
                  "deviceId":"dev-01",
                  "temperature":"bad-number"
                }
                """;

        assertThrows(BaseDomainException.class, () -> parser.parse(rawJson));
    }
}
