package com.example.iotalarmcopilot.ai.interfaces.http;

import com.example.iotalarmcopilot.ai.application.AiAlarmSummaryVO;
import com.example.iotalarmcopilot.ai.application.AiSummaryQueryApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/alarm-summaries")
public class AiSummaryQueryController {

    private final AiSummaryQueryApplicationService aiSummaryQueryApplicationService;

    public AiSummaryQueryController(AiSummaryQueryApplicationService aiSummaryQueryApplicationService) {
        this.aiSummaryQueryApplicationService = aiSummaryQueryApplicationService;
    }

    @GetMapping("/{alarmId}")
    public AiAlarmSummaryVO getByAlarmId(@PathVariable("alarmId") Long alarmId) {
        return aiSummaryQueryApplicationService.getByAlarmId(alarmId);
    }

    @GetMapping("/recent")
    public List<AiAlarmSummaryVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return aiSummaryQueryApplicationService.recent(limit);
    }
}
