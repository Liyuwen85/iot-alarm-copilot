package com.example.iotalarmcopilot.rule.interfaces.event;

import com.example.iotalarmcopilot.contract.event.TelemetryRecordedEvent;
import com.example.iotalarmcopilot.rule.application.EvaluateTelemetryRuleCommand;
import com.example.iotalarmcopilot.rule.application.TelemetryRuleEvaluationApplicationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 遥测数据记录事件监听器
 */
@Component
public class TelemetryRecordedRuleHandler {

    private final TelemetryRuleEvaluationApplicationService telemetryRuleEvaluationApplicationService;

    public TelemetryRecordedRuleHandler(
            TelemetryRuleEvaluationApplicationService telemetryRuleEvaluationApplicationService) {
        this.telemetryRuleEvaluationApplicationService = telemetryRuleEvaluationApplicationService;
    }

    /**
     * 监听遥测数据记录事件
     *
     * @param event
     */
    @EventListener
    public void onTelemetryRecorded(TelemetryRecordedEvent event) {
        // 触发规则评估
        telemetryRuleEvaluationApplicationService.evaluate(new EvaluateTelemetryRuleCommand(
                event.telemetryEventId(),
                event.deviceId(),
                event.metrics(),
                event.reportedAt()));
    }
}
