package com.example.iotalarmcopilot.ai.infrastructure.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "iot.ai")
public class AiProperties {

    private boolean enabled;
    private String model = "gpt-4o-mini";
    private String promptVersion = "alarm-summary-v1";
}
