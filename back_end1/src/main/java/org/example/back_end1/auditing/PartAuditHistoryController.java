package org.example.back_end1.auditing;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class PartAuditHistoryController {

    private final PartAuditHistoryService historyService;

    public PartAuditHistoryController(PartAuditHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/parts/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PartHistoryDto> getPartHistory(@PathVariable Long id) {
        return historyService.getPartHistory(id);
    }
}