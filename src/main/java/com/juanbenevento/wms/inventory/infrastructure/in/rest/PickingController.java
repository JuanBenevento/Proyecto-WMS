package com.juanbenevento.wms.inventory.infrastructure.in.rest;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.CompletePickingCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.PickLineCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.ShipStockCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand.ShortPickDecision;
import com.juanbenevento.wms.inventory.application.port.in.dto.PickingSessionResponse;
import com.juanbenevento.wms.inventory.application.port.in.usecases.AllocateStockUseCase;
import com.juanbenevento.wms.inventory.application.port.in.usecases.ShipStockUseCase;
import com.juanbenevento.wms.inventory.application.service.PickingService;
import com.juanbenevento.wms.inventory.domain.model.PickingSession;
import com.juanbenevento.wms.shared.infrastructure.idempotency.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/picking")
@RequiredArgsConstructor
@Tag(name = "4. Salidas y Picking (Outbound)", description = "Reserva de stock, picking de órdenes y despacho.")
public class PickingController {

    private final AllocateStockUseCase allocateStockUseCase;
    private final ShipStockUseCase shipStockUseCase;
    private final PickingService pickingService;

    // ==================== PICKING WORKFLOW ====================

    @Operation(
        summary = "1. Iniciar Sesión de Picking",
        description = "Inicia el proceso de picking para una orden. Crea una sesión de picking y marca el stock como PICKING.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Sesión iniciada exitosamente",
                content = @Content(schema = @Schema(implementation = PickingSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
        }
    )
    @PostMapping("/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Transactional
    public ResponseEntity<PickingSessionResponse> startPicking(
            @RequestBody @Valid StartPickingRequest request) {

        StartPickingCommand command = new StartPickingCommand(
                request.orderId(),
                request.shortPickDecision(),
                request.preferredLocations(),
                request.assignedOperator()
        );

        PickingSession session = pickingService.startPicking(command);
        return ResponseEntity.ok(PickingSessionResponse.from(session));
    }

    @Operation(
        summary = "2. Registrar Pick de Línea",
        description = "Registra el pick de una línea individual. Maneja short picks según la configuración de la sesión.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Pick registrado exitosamente",
                content = @Content(schema = @Schema(implementation = PickingSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sesión no encontrada o datos inválidos")
        }
    )
    @PostMapping("/pick-line")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Transactional
    public ResponseEntity<PickingSessionResponse> pickLine(
            @RequestBody @Valid PickLineRequest request) {

        PickLineCommand command = new PickLineCommand(
                request.orderId(),
                request.lineId(),
                request.pickedQuantity(),
                request.notes(),
                request.pickedBy()
        );

        PickingSession session = pickingService.pickLine(command);
        return ResponseEntity.ok(PickingSessionResponse.from(session));
    }

    @Operation(
        summary = "3. Completar Sesión de Picking",
        description = "Finaliza el proceso de picking. Procesa short picks según la decisión configurada.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Sesión completada exitosamente",
                content = @Content(schema = @Schema(implementation = PickingSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sesión no encontrada o no puede completarse")
        }
    )
    @PostMapping("/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Transactional
    public ResponseEntity<PickingSessionResponse> completePicking(
            @RequestBody @Valid CompletePickingRequest request) {

        List<CompletePickingCommand.PickResult> results = request.lines().stream()
                .map(line -> new CompletePickingCommand.PickResult(
                        line.lineId(),
                        line.pickedQuantity(),
                        line.wasShort(),
                        line.notes()
                ))
                .toList();

        CompletePickingCommand command = new CompletePickingCommand(
                request.orderId(),
                results,
                request.completedBy(),
                request.notes()
        );

        PickingSession session = pickingService.completePicking(command);
        return ResponseEntity.ok(PickingSessionResponse.from(session));
    }

    @Operation(
        summary = "4. Obtener Sesión Activa",
        description = "Retorna la sesión de picking activa para una orden.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Sesión encontrada",
                content = @Content(schema = @Schema(implementation = PickingSessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "No hay sesión activa para esta orden")
        }
    )
    @GetMapping("/{orderId}/session")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<PickingSessionResponse> getActiveSession(
            @PathVariable @NotBlank String orderId) {

        PickingSession session = pickingService.getActiveSession(orderId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PickingSessionResponse.from(session));
    }

    @Operation(
        summary = "5. Cancelar Sesión de Picking",
        description = "Cancela una sesión de picking activa y restaura el estado del inventory.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Sesión cancelada exitosamente"),
            @ApiResponse(responseCode = "404", description = "No hay sesión activa para esta orden")
        }
    )
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Transactional
    public ResponseEntity<Void> cancelPicking(@PathVariable @NotBlank String orderId) {
        pickingService.cancelPicking(orderId);
        return ResponseEntity.noContent().build();
    }

    // ==================== LEGACY ALLOCATE/SHIP ====================

    @Operation(summary = "6. Reservar Stock (Allocate) [Legacy]",
        description = "Busca stock (FEFO) y lo marca como RESERVED. Usar /start para flujo completo de picking.")
    @PostMapping("/allocate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Idempotent
    @Transactional
    public ResponseEntity<String> allocateOrder(@RequestBody @Valid PickingRequest request) {
        allocateStockUseCase.allocateStock(new AllocateStockCommand(
                request.sku(), request.quantity()
        ));
        return ResponseEntity.ok("Stock reservado exitosamente. Listo para picking.");
    }

    @Operation(summary = "7. Confirmar Despacho (Ship) [Legacy]",
        description = "Da de baja el stock y libera el peso. Usar /complete para flujo completo de picking.")
    @PostMapping("/ship")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    @Idempotent
    @Transactional
    public ResponseEntity<String> shipOrder(@RequestBody @Valid PickingRequest request) {
        shipStockUseCase.shipStock(new ShipStockCommand(
                request.sku(), request.quantity()
        ));
        return ResponseEntity.ok("Pedido despachado correctamente.");
    }

    // ==================== REQUEST RECORDS ====================

    public record PickingRequest(
            @Schema(example = "TV-LG-65") @NotBlank String sku,
            @Schema(example = "5.0") @NotNull @Positive BigDecimal quantity
    ) {}

    public record StartPickingRequest(
            @Schema(example = "ord-12345", description = "ID de la orden en el sistema de órdenes")
            @NotBlank String orderId,

            @Schema(description = "Decisión para manejar short picks")
            @NotNull ShortPickDecision shortPickDecision,

            @Schema(example = "[\"A-01-01\", \"A-01-02\"]", description = "Ubicaciones preferidas para optimizar ruta")
            List<String> preferredLocations,

            @Schema(example = "operador-001", description = "Operador asignado al picking")
            String assignedOperator
    ) {}

    public record PickLineRequest(
            @Schema(example = "ord-12345")
            @NotBlank String orderId,

            @Schema(example = "line-001")
            @NotBlank String lineId,

            @Schema(example = "5.0")
            @NotNull @Positive BigDecimal pickedQuantity,

            @Schema(example = "Pick realizado sin incidencias", description = "Notas opcionales del pick")
            String notes,

            @Schema(example = "operador-001")
            String pickedBy
    ) {}

    public record CompletePickingRequest(
            @Schema(example = "ord-12345")
            @NotBlank String orderId,

            @Schema(description = "Resultados de líneas no registradas individualmente")
            List<PickResultLine> lines,

            @Schema(example = "operador-001")
            String completedBy,

            @Schema(example = "Picking completado exitosamente", description = "Notas finales del picking")
            String notes
    ) {
        public record PickResultLine(
                @Schema(example = "line-001")
                @NotBlank String lineId,

                @Schema(example = "5.0")
                @NotNull BigDecimal pickedQuantity,

                boolean wasShort,

                String notes
        ) {}
    }
}