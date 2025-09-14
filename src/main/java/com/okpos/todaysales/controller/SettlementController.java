package com.okpos.todaysales.controller;

import com.okpos.todaysales.entity.Sale;
import com.okpos.todaysales.entity.Settlement;
import com.okpos.todaysales.repository.SettlementRepository;
import com.okpos.todaysales.service.SettlementBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlement", description = "정산 관리 API")
public class SettlementController {

    private final SettlementBatchService settlementBatchService;
    private final SettlementRepository settlementRepository;

    @PostMapping("/manual")
    @Operation(summary = "수동 정산 실행", description = "특정 날짜의 매출을 수동으로 정산합니다")
    public ResponseEntity<Settlement> runManualSettlement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "정산 날짜 (YYYY-MM-DD)") LocalDate settlementDate) {

        try {
            Settlement settlement = settlementBatchService.processManualSettlement(settlementDate);
            return ResponseEntity.ok(settlement);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "정산 조회", description = "정산 ID로 정산 정보를 조회합니다")
    public ResponseEntity<Settlement> getSettlement(@PathVariable Long id) {
        return settlementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "날짜별 정산 조회", description = "특정 날짜의 정산 정보를 조회합니다")
    public ResponseEntity<Settlement> getSettlementByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return settlementRepository.findBySettlementDate(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/range")
    @Operation(summary = "기간별 정산 목록 조회", description = "특정 기간의 정산 목록을 조회합니다")
    public ResponseEntity<List<Settlement>> getSettlementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "시작 날짜") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "종료 날짜") LocalDate endDate) {

        List<Settlement> settlements = settlementRepository.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(settlements);
    }

    @GetMapping("/unsettled/{date}")
    @Operation(summary = "미정산 매출 조회", description = "특정 날짜의 미정산 매출 목록을 조회합니다")
    public ResponseEntity<List<Sale>> getUnsettledSales(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Sale> unsettledSales = settlementBatchService.getUnsettledSales(date);
        return ResponseEntity.ok(unsettledSales);
    }

    @PostMapping("/{id}/reprocess")
    @Operation(summary = "정산 재처리", description = "실패한 정산을 재처리합니다")
    public ResponseEntity<Settlement> reprocessSettlement(@PathVariable Long id) {
        try {
            Settlement settlement = settlementBatchService.reprocessSettlement(id);
            return ResponseEntity.ok(settlement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check/{date}")
    @Operation(summary = "정산 여부 확인", description = "특정 날짜가 이미 정산되었는지 확인합니다")
    public ResponseEntity<Boolean> checkSettlementExists(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean exists = settlementRepository.existsBySettlementDate(date);
        return ResponseEntity.ok(exists);
    }
}