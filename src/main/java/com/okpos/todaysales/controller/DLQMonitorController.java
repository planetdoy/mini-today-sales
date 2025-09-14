package com.okpos.todaysales.controller;

import com.okpos.todaysales.service.DLQMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dlq")
@Tag(name = "DLQ Monitor", description = "Dead Letter Queue 모니터링 API")
public class DLQMonitorController {

    private final DLQMonitorService dlqMonitorService;

    public DLQMonitorController(DLQMonitorService dlqMonitorService) {
        this.dlqMonitorService = dlqMonitorService;
    }

    @GetMapping("/counts")
    @Operation(summary = "DLQ 메시지 수 조회", description = "모든 DLQ의 메시지 수를 조회합니다")
    public ResponseEntity<Map<String, Integer>> getDLQCounts() {
        return ResponseEntity.ok(dlqMonitorService.getDLQMessageCounts());
    }

    @DeleteMapping("/{queueName}")
    @Operation(summary = "DLQ 비우기", description = "특정 DLQ의 모든 메시지를 삭제합니다")
    public ResponseEntity<String> purgeDLQ(@PathVariable String queueName) {
        dlqMonitorService.purgeDLQ(queueName);
        return ResponseEntity.ok("DLQ " + queueName + " has been purged");
    }
}