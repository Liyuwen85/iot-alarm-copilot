package com.example.iotalarmcopilot.alarm.interfaces.http;

import com.example.iotalarmcopilot.alarm.application.*;
import com.example.iotalarmcopilot.alarm.domain.model.Alarm;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * 告警查询控制器
 */
@RestController
@RequestMapping("/api/alarms")
public class AlarmQueryController {

    private final AlarmQueryApplicationService alarmQueryApplicationService;
    private final AlarmApplicationService alarmApplicationService;

    public AlarmQueryController(
            AlarmQueryApplicationService alarmQueryApplicationService,
            AlarmApplicationService alarmApplicationService) {
        this.alarmQueryApplicationService = alarmQueryApplicationService;
        this.alarmApplicationService = alarmApplicationService;
    }

    @GetMapping("/recent")
    public List<AlarmVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return alarmQueryApplicationService.recent(limit);
    }

    @PostMapping("/{alarmId}/ack")
    public AlarmVO acknowledge(@PathVariable Long alarmId) {
        Alarm alarm = alarmApplicationService.acknowledge(
                new AcknowledgeAlarmCommand(alarmId, Instant.now()));
        return toVO(alarm);
    }

    @PostMapping("/{alarmId}/close")
    public AlarmVO close(@PathVariable Long alarmId) {
        Alarm alarm = alarmApplicationService.close(
                new CloseAlarmCommand(alarmId, Instant.now()));
        return toVO(alarm);
    }

    private AlarmVO toVO(Alarm alarm) {
        return new AlarmVO(
                alarm.id(),
                alarm.dedupKey().value(),
                alarm.ruleCode(),
                alarm.telemetryEventId(),
                alarm.deviceId(),
                alarm.metricName(),
                alarm.metricValue(),
                alarm.threshold(),
                alarm.severity().name(),
                alarm.status().name(),
                alarm.triggeredAt(),
                alarm.acknowledgedAt(),
                alarm.closedAt());
    }
}
