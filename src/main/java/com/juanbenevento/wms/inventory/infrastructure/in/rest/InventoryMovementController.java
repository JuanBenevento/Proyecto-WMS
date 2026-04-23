package com.juanbenevento.wms.inventory.infrastructure.in.rest;

import com.juanbenevento.wms.inventory.application.*;
import com.juanbenevento.wms.inventory.application.RegisterReceiptService;
import com.juanbenevento.wms.inventory.application.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for inventory movement operations.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory Movement", description = "Inventory movement with lot traceability")
public class InventoryMovementController {

    private final RegisterReceiptService registerReceiptService;
    private final RegisterIssueService registerIssueService;
    private final TransferInventoryService transferInventoryService;
    private final QueryLotHistoryService queryLotHistoryService;
    private final QueryExpiringLotsService queryExpiringLotsService;
    private final RecordTemperatureService recordTemperatureService;

    public InventoryMovementController(
            RegisterReceiptService registerReceiptService,
            RegisterIssueService registerIssueService,
            TransferInventoryService transferInventoryService,
            QueryLotHistoryService queryLotHistoryService,
            QueryExpiringLotsService queryExpiringLotsService,
            RecordTemperatureService recordTemperatureService
    ) {
        this.registerReceiptService = registerReceiptService;
        this.registerIssueService = registerIssueService;
        this.transferInventoryService = transferInventoryService;
        this.queryLotHistoryService = queryLotHistoryService;
        this.queryExpiringLotsService = queryExpiringLotsService;
        this.recordTemperatureService = recordTemperatureService;
    }

    // ============== RECEIPTS ==============

    @PostMapping("/receipts")
    @Operation(summary = "Register a receipt of goods")
    public ResponseEntity<ReceiptResponse> registerReceipt(@RequestBody ReceiptRequest request) {
        log.info("POST /api/v1/inventory/receipts - SKU={}", request.productSku());

        RegisterReceiptCommand command = new RegisterReceiptCommand(
                request.lotNumber(),
                request.productSku(),
                request.quantity(),
                request.locationCode(),
                request.batchNumber(),
                request.productionDate(),
                request.origin(),
                request.expiryDate(),
                request.minTemperature(),
                request.maxTemperature(),
                request.netWeight(),
                request.grossWeight(),
                request.metadata(),
                request.temperatureAtReceipt(),
                request.certificateUrl(),
                request.performedBy()
        );

        ReceiptResponse response = registerReceiptService.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============== ISSUES ==============

    @PostMapping("/issues")
    @Operation(summary = "Register an issue (picking/shipment)")
    public ResponseEntity<IssueResponse> registerIssue(@RequestBody IssueRequest request) {
        log.info("POST /api/v1/inventory/issues - SKU={}", request.productSku());

        RegisterIssueCommand command = new RegisterIssueCommand(
                request.productSku(),
                request.quantity(),
                request.toLocation(),
                request.reason(),
                request.allocationStrategy(),
                request.preferredLots(),
                request.performedBy()
        );

        RegisterIssueService.IssueResponse response = registerIssueService.execute(command);
        return ResponseEntity.ok(new IssueResponse(
                response.allocations().stream()
                        .map(a -> new LotAllocationDto(a.lotNumber(), a.quantity(), a.reason()))
                        .toList(),
                response.movementId(),
                response.message()
        ));
    }

    // ============== TRANSFERS ==============

    @PostMapping("/transfers")
    @Operation(summary = "Transfer inventory between locations")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request) {
        log.info("POST /api/v1/inventory/transfers - lot={}", request.lotNumber());

        TransferInventoryCommand command = new TransferInventoryCommand(
                request.lotNumber(),
                request.quantity(),
                request.fromLocation(),
                request.toLocation(),
                request.reason(),
                request.performedBy()
        );

        TransferInventoryService.TransferResponse response = transferInventoryService.execute(command);
        return ResponseEntity.ok(new TransferResponse(
                response.movementId() != null ? response.movementId().toString() : null,
                response.message()
        ));
    }

    // ============== TEMPERATURE ==============

    @PostMapping("/temperature")
    @Operation(summary = "Record a temperature reading")
    public ResponseEntity<TemperatureResponse> recordTemperature(@RequestBody TemperatureRequest request) {
        log.info("POST /api/v1/inventory/temperature - lot={}", request.lotNumber());

        RecordTemperatureCommand command = new RecordTemperatureCommand(
                request.lotNumber(),
                request.temperature(),
                java.time.LocalDateTime.now(),
                request.location(),
                request.recordedBy()
        );

        RecordTemperatureService.TemperatureResponse response = recordTemperatureService.execute(command);
        return ResponseEntity.ok(new TemperatureResponse(
                response.recorded(),
                response.message(),
                response.withinRange()
        ));
    }

    // ============== QUERIES ==============

    @GetMapping("/lots/{lotNumber}/history")
    @Operation(summary = "Get lot history (traceability)")
    public ResponseEntity<LotHistoryResponse> getLotHistory(@PathVariable String lotNumber) {
        log.info("GET /api/v1/inventory/lots/{}/history", lotNumber);

        QueryLotHistoryService.LotHistoryResponse response = queryLotHistoryService.execute(lotNumber);

        if (response.lot() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new LotHistoryResponse(
                response.lot().getLotNumber(),
                response.lot().getProductSku(),
                response.lot().getStatus().toString(),
                response.movements().stream()
                        .map(m -> new MovementDto(
                                m.getMovementId().toString(),
                                m.getType().toString(),
                                m.getQuantity(),
                                m.getFromLocation(),
                                m.getToLocation(),
                                m.getReason(),
                                m.getTimestamp().toString()
                        ))
                        .toList()
        ));
    }

    @GetMapping("/lots/expiring")
    @Operation(summary = "Get lots expiring within N days")
    public ResponseEntity<ExpiringLotsResponse> getExpiringLots(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) String productSku
    ) {
        log.info("GET /api/v1/inventory/lots/expiring?days={}", days);

        QueryExpiringLotsService.ExpiringLotsResponse response = queryExpiringLotsService.execute(days, productSku);

        return ResponseEntity.ok(new ExpiringLotsResponse(
                response.lots().stream()
                        .map(l -> new LotDto(
                                l.getLotNumber(),
                                l.getProductSku(),
                                l.getExpiryDate() != null ? l.getExpiryDate().toString() : null,
                                l.getDaysUntilExpiry()
                        ))
                        .toList(),
                response.totalCount()
        ));
    }

    // ============== DTOs ==============

    public record ReceiptRequest(
            String lotNumber,
            String productSku,
            BigDecimal quantity,
            String locationCode,
            String batchNumber,
            LocalDate productionDate,
            String origin,
            LocalDate expiryDate,
            BigDecimal minTemperature,
            BigDecimal maxTemperature,
            BigDecimal netWeight,
            BigDecimal grossWeight,
            Map<String, String> metadata,
            BigDecimal temperatureAtReceipt,
            String certificateUrl,
            String performedBy
    ) {}

    public record IssueRequest(
            String productSku,
            BigDecimal quantity,
            String toLocation,
            String reason,
            String allocationStrategy,
            List<String> preferredLots,
            String performedBy
    ) {}

    public record TransferRequest(
            String lotNumber,
            BigDecimal quantity,
            String fromLocation,
            String toLocation,
            String reason,
            String performedBy
    ) {}

    public record TemperatureRequest(
            String lotNumber,
            BigDecimal temperature,
            String location,
            String recordedBy
    ) {}

    public record LotAllocationDto(String lotNumber, BigDecimal quantity, String reason) {}

    public record MovementDto(String movementId, String type, BigDecimal quantity, String fromLocation, String toLocation, String reason, String timestamp) {}

    public record LotDto(String lotNumber, String productSku, String expiryDate, long daysUntilExpiry) {}

    public record IssueResponse(List<LotAllocationDto> allocations, java.util.UUID movementId, String message) {}

    public record TransferResponse(String movementId, String message) {}

    public record TemperatureResponse(boolean recorded, String message, boolean withinRange) {}

    public record LotHistoryResponse(String lotNumber, String productSku, String status, List<MovementDto> movements) {}

    public record ExpiringLotsResponse(List<LotDto> lots, int totalCount) {}
}