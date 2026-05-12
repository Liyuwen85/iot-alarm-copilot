package com.example.iotalarmcopilot.telemetry.interfaces.http;

import com.example.iotalarmcopilot.telemetry.application.TelemetryEventVO;
import com.example.iotalarmcopilot.telemetry.application.TelemetryQueryApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 遥测controller
 */
@RestController
@RequestMapping("/api/telemetry-events")
public class TelemetryQueryController {

    private final TelemetryQueryApplicationService telemetryQueryApplicationService;

    public TelemetryQueryController(TelemetryQueryApplicationService telemetryQueryApplicationService) {
        this.telemetryQueryApplicationService = telemetryQueryApplicationService;
    }

    @GetMapping("/recent")
    public List<TelemetryEventVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return telemetryQueryApplicationService.recent(limit);
    }
}
