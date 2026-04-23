package com.juanbenevento.wms.monitoring.api;

import com.juanbenevento.wms.monitoring.application.port.in.ColdChainMonitorUseCase;
import com.juanbenevento.wms.monitoring.application.port.in.dto.TemperatureAlertDto;
import com.juanbenevento.wms.monitoring.application.port.in.dto.TemperatureReadingDto;
import com.juanbenevento.wms.shared.infrastructure.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cold Chain Monitoring REST API.
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
@Tag(name = "Cold Chain", description = "Temperature monitoring and alerts")
public class ColdChainController {

    private final ColdChainMonitorUseCase monitorUseCase;

    @Operation(summary = "Record temperature reading", description = "Record a temperature reading for a location")
    @PostMapping("/temperature")
    public ResponseEntity<ApiResponse<Void>> recordTemperature(
            @RequestParam String locationCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam Double temperature) {
        monitorUseCase.recordTemperature(locationCode, timestamp, temperature);
        return ResponseEntity.ok(ApiResponse.success(null, "Temperature recorded"));
    }

    @Operation(summary = "Get active temperature alerts")
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<TemperatureAlertDto>>> getActiveAlerts() {
        return ResponseEntity.ok(ApiResponse.success(monitorUseCase.getActiveAlerts()));
    }

    @Operation(summary = "Get alerts for a location")
    @GetMapping("/alerts/{locationCode}")
    public ResponseEntity<ApiResponse<List<TemperatureAlertDto>>> getAlertsByLocation(
            @PathVariable String locationCode) {
        return ResponseEntity.ok(ApiResponse.success(monitorUseCase.getAlertsByLocation(locationCode)));
    }

    @Operation(summary = "Get temperature history for a location")
    @GetMapping("/temperature/history/{locationCode}")
    public ResponseEntity<ApiResponse<List<TemperatureReadingDto>>> getTemperatureHistory(
            @PathVariable String locationCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(
            monitorUseCase.getTemperatureHistory(locationCode, from, to)));
    }

    @Operation(summary = "Acknowledge an alert")
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<Void>> acknowledgeAlert(
            @PathVariable String alertId,
            @RequestParam String acknowledgedBy) {
        monitorUseCase.acknowledgeAlert(alertId, acknowledgedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Alert acknowledged"));
    }
}