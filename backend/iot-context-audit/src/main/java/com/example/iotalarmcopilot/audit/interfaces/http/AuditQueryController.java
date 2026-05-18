package com.example.iotalarmcopilot.audit.interfaces.http;

import com.example.iotalarmcopilot.audit.application.AuditLogVO;
import com.example.iotalarmcopilot.audit.application.AuditQueryApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审计查询控制器
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditQueryController {

    private final AuditQueryApplicationService auditQueryApplicationService;

    public AuditQueryController(AuditQueryApplicationService auditQueryApplicationService) {
        this.auditQueryApplicationService = auditQueryApplicationService;
    }

    @GetMapping("/recent")
    public List<AuditLogVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return auditQueryApplicationService.recent(limit);
    }
}
