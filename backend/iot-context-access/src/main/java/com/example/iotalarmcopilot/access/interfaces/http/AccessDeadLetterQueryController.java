package com.example.iotalarmcopilot.access.interfaces.http;

import com.example.iotalarmcopilot.access.application.AccessDeadLetterQueryApplicationService;
import com.example.iotalarmcopilot.access.application.AccessDeadLetterVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 死信查询
 */
@RestController
@RequestMapping("/api/access-dead-letters")
public class AccessDeadLetterQueryController {

    private final AccessDeadLetterQueryApplicationService accessDeadLetterQueryApplicationService;

    public AccessDeadLetterQueryController(AccessDeadLetterQueryApplicationService accessDeadLetterQueryApplicationService) {
        this.accessDeadLetterQueryApplicationService = accessDeadLetterQueryApplicationService;
    }

    @GetMapping("/recent")
    public List<AccessDeadLetterVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return accessDeadLetterQueryApplicationService.recent(limit);
    }
}
