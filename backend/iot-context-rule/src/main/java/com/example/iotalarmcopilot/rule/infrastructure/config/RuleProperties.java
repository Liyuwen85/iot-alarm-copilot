package com.example.iotalarmcopilot.rule.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 规则属性配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "iot.rule")
public class RuleProperties {

    private boolean enabled = true;
    // 规则定义,提供默认规则
    private List<DefinitionItem> definitions = new ArrayList<>(List.of(
            defaultTemperatureHighRule(),
            defaultTemperatureCriticalRule(),
            defaultHumidityHighRule()));

    private static DefinitionItem defaultTemperatureHighRule() {
        DefinitionItem item = new DefinitionItem();
        item.setCode("temperature_high");
        item.setName("高温告警规则");
        item.setEnabled(true);
        item.setMetricName("temperature");
        item.setThreshold(BigDecimal.valueOf(80));
        item.setExpression("temperature != null and temperature >= 80");
        return item;
    }

    private static DefinitionItem defaultTemperatureCriticalRule() {
        DefinitionItem item = new DefinitionItem();
        item.setCode("temperature_critical");
        item.setName("超高温告警规则");
        item.setEnabled(true);
        item.setMetricName("temperature");
        item.setThreshold(BigDecimal.valueOf(95));
        item.setExpression("temperature != null and temperature >= 95");
        return item;
    }

    private static DefinitionItem defaultHumidityHighRule() {
        DefinitionItem item = new DefinitionItem();
        item.setCode("humidity_high");
        item.setName("高湿告警规则");
        item.setEnabled(true);
        item.setMetricName("humidity");
        item.setThreshold(BigDecimal.valueOf(90));
        item.setExpression("humidity != null and humidity >= 90");
        return item;
    }

    @Getter
    @Setter
    public static class DefinitionItem {

        private String code;
        private String name;
        private boolean enabled = true;
        private String metricName;
        private BigDecimal threshold;
        private String expression;
    }
}
