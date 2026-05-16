package com.example.iotalarmcopilot.telemetry.infrastructure.timeseries;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 遥测时序库配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "iot.timeseries")
public class TelemetryTimeseriesProperties {

    private boolean enabled = false;
    private String url = "jdbc:postgresql://localhost:5432/iot_telemetry_hot";
    private String username = "postgres";
    private String password = "postgres";
    private String driverClassName = "org.postgresql.Driver";
}
