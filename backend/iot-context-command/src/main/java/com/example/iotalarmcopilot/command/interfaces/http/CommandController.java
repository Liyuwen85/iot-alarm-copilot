package com.example.iotalarmcopilot.command.interfaces.http;

import com.example.iotalarmcopilot.command.application.CommandApplicationService;
import com.example.iotalarmcopilot.command.application.DeviceCommandVO;
import com.example.iotalarmcopilot.command.application.SendSetReportIntervalCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * 命令发送控制器
 */
@RestController
@RequestMapping("/api/commands")
public class CommandController {

    private final CommandApplicationService commandApplicationService;

    public CommandController(CommandApplicationService commandApplicationService) {
        this.commandApplicationService = commandApplicationService;
    }

    @PostMapping("/report-interval")
    public DeviceCommandVO sendReportInterval(@RequestBody SetReportIntervalRequest request) {
        return commandApplicationService.sendSetReportInterval(new SendSetReportIntervalCommand(
                request.deviceId(),
                request.intervalMs(),
                Instant.now()));
    }

    @GetMapping("/recent")
    public List<DeviceCommandVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return commandApplicationService.recent(limit);
    }

    public record SetReportIntervalRequest(
            @NotBlank String deviceId,
            @Min(500) int intervalMs) {
    }
}
