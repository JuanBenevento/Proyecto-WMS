package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.usecases.RetrieveAuditLogsUseCase;
import com.juanbenevento.wms.domain.model.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría de Movimientos", description = "Consulta de historial de movimientos de stock")
public class AuditController {

    private final RetrieveAuditLogsUseCase retrieveAuditLogsUseCase;

    @Operation(
            summary = "Consultar historial de movimientos",
            description = "Permite filtrar movimientos por SKU, LPN y/o rango de fechas. Todos los parámetros son opcionales."
    )
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @Parameter(description = "SKU del producto a filtrar")
            @RequestParam(required = false) String sku,

            @Parameter(description = "LPN (License Plate Number) a filtrar")
            @RequestParam(required = false) String lpn,

            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Fecha de fin (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable
    ) {
        Page<AuditLog> result = retrieveAuditLogsUseCase.getAuditLogs(sku, lpn, startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }
}

