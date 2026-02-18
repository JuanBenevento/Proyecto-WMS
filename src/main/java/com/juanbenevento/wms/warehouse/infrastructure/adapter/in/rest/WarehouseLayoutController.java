package com.juanbenevento.wms.warehouse.infrastructure.adapter.in.rest;

import com.juanbenevento.wms.warehouse.application.port.in.command.SaveLayoutCommand;
import com.juanbenevento.wms.warehouse.application.port.in.dto.RackSummaryDto;
import com.juanbenevento.wms.warehouse.application.port.in.dto.WarehouseLayoutResponse;
import com.juanbenevento.wms.warehouse.application.port.in.usecases.ManageLayoutUseCase;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "2. Configuracion de Déposito (WMS)", description = "Diseño visual y gestión de mapa físico")
public class WarehouseLayoutController {

    private final ManageLayoutUseCase manageLayoutUseCase;

    @GetMapping("/warehouse/layout/getLayout")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Operation(summary = "Obtener Diseño Actual", description = "Devuelve el Json del mapa físico configurado.")
    public ResponseEntity<WarehouseLayoutResponse> getLayout(@RequestParam(required = false) String tenantId) {
        String actualTenant = (tenantId != null) ? tenantId : TenantContext.getTenantId();
        if (actualTenant == null) throw new IllegalStateException("No se pudo identificar la empresa.");

        return ResponseEntity.ok(manageLayoutUseCase.getLayout(actualTenant));
    }

    @PostMapping("/warehouse/layout/saveLayout")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Guardar Diseño", description = "Actualiza la configuración visual.")
    public ResponseEntity<WarehouseLayoutResponse> saveLayout(@RequestBody @Valid LayoutRequest request) {
        String targetTenantId = request.tenantId();

        if (targetTenantId == null || targetTenantId.isBlank()) {
            targetTenantId = TenantContext.getTenantId();
        }

        if (targetTenantId == null) {
            throw new IllegalStateException("No se pudo identificar la empresa (Tenant ID faltante).");
        }

        SaveLayoutCommand command = new SaveLayoutCommand(targetTenantId, request.layoutJson());
        return ResponseEntity.ok(manageLayoutUseCase.saveLayout(command));
    }

    @GetMapping("/locations/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Operation(summary = "Buscar Ubicaciones", description = "Autocomplete para el inspector de propiedades.")
    public ResponseEntity<List<String>> searchLocations(@RequestParam String query) {
        return ResponseEntity.ok(manageLayoutUseCase.searchLocations(query));
    }

    @GetMapping("/locations/racks/{code}/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Operation(summary = "Estado del Rack", description = "Calcula ocupación y estado para el Heatmap.")
    public ResponseEntity<RackSummaryDto> getRackSummary(@PathVariable String code) {
        return ResponseEntity.ok(manageLayoutUseCase.getRackSummary(code));
    }

    // DTO Interno para el Request
    public record LayoutRequest(
            @Schema(description = "ID del Tenant (Opcional, si no se envía se usa el del Token)")
            String tenantId,
            @Schema(description = "JSON de Fabric.js", example = "{\"objects\":[]}")
            @NotBlank(message = "El contenido no puede estar vacío")
            String layoutJson
    ) {}
}