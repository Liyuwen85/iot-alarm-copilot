package com.example.iotalarmcopilot.inspection.interfaces.http;

import com.example.iotalarmcopilot.inspection.application.CloseInspectionTicketCommand;
import com.example.iotalarmcopilot.inspection.application.ConfirmInspectionTicketCommand;
import com.example.iotalarmcopilot.inspection.application.InspectionApplicationService;
import com.example.iotalarmcopilot.inspection.application.InspectionQueryApplicationService;
import com.example.iotalarmcopilot.inspection.application.InspectionTicketVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * 巡检查询控制器
 */
@RestController
@RequestMapping("/api/inspection-tickets")
public class InspectionQueryController {

    private final InspectionQueryApplicationService inspectionQueryApplicationService;
    private final InspectionApplicationService inspectionApplicationService;

    public InspectionQueryController(
            InspectionQueryApplicationService inspectionQueryApplicationService,
            InspectionApplicationService inspectionApplicationService) {
        this.inspectionQueryApplicationService = inspectionQueryApplicationService;
        this.inspectionApplicationService = inspectionApplicationService;
    }

    @GetMapping("/{ticketId}")
    public InspectionTicketVO get(@PathVariable("ticketId") Long ticketId) {
        return inspectionQueryApplicationService.get(ticketId);
    }

    @GetMapping("/recent")
    public List<InspectionTicketVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return inspectionQueryApplicationService.recent(limit);
    }

    @PostMapping("/{ticketId}/confirm")
    public InspectionTicketVO confirm(@PathVariable("ticketId") Long ticketId) {
        return inspectionQueryApplicationService.toVO(
                inspectionApplicationService.confirm(new ConfirmInspectionTicketCommand(ticketId, Instant.now())));
    }

    @PostMapping("/{ticketId}/close")
    public InspectionTicketVO close(@PathVariable("ticketId") Long ticketId) {
        return inspectionQueryApplicationService.toVO(
                inspectionApplicationService.close(new CloseInspectionTicketCommand(ticketId, Instant.now())));
    }
}
