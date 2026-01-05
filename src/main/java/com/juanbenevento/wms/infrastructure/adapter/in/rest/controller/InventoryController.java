package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.command.InternalMoveCommand;
import com.juanbenevento.wms.application.ports.in.command.InventoryAdjustmentCommand;
import com.juanbenevento.wms.application.ports.in.command.PutAwayInventoryCommand;
import com.juanbenevento.wms.application.ports.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.application.ports.in.usecases.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "3. Operaciones de Inventario (Inbound)", description = "Recepción, control de calidad y movimientos.")
public class InventoryController {

    private final ReceiveInventoryUseCase receiveInventoryUseCase;
    private final PutAwayUseCase putAwayUseCase;
    private final ManageInventoryOperationsUseCase operationsUseCase;
    private final RetrieveInventoryUseCase retrieveInventoryUseCase;
    private final SuggestLocationUseCase suggestLocationUseCase;

    @Operation(summary = "Ver stock real", description = "Lista todos los items con su LPN y estado.")
    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getAllInventory() {
        return ResponseEntity.ok(retrieveInventoryUseCase.getAllInventory());
    }

    @Operation(summary = "Recepción de Mercadería", description = "Genera LPN y valida capacidad física de la ubicación.")
    @PostMapping("/receive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<InventoryItemResponse> receiveInventory(@RequestBody @Valid ReceiveInventoryRequest request) {
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(
                request.productSku(), request.quantity(), request.locationCode(),
                request.batchNumber(), request.expiryDate()
        );
        return new ResponseEntity<>(receiveInventoryUseCase.receiveInventory(command), HttpStatus.CREATED);
    }

    @Operation(summary = "Confirmar Ubicación (Put-Away)", description = "Mueve stock de recepción a su ubicación final.")
    @PutMapping("/put-away")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> putAway(@RequestBody @Valid PutAwayRequest request) {
        PutAwayInventoryCommand command = new PutAwayInventoryCommand(
                request.lpn(), request.targetLocationCode()
        );
        putAwayUseCase.putAwayInventory(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Consultar Estrategia", description = "El sistema sugiere dónde guardar según el perfil del producto.")
    @GetMapping("/suggest-location")
    public ResponseEntity<String> suggestLocation(@RequestParam String sku, @RequestParam Double quantity) {
        // Nota: Si el caso de uso lanza excepción, el GlobalExceptionHandler se encarga.
        String locationCode = suggestLocationUseCase.suggestBestLocation(sku, quantity);
        return ResponseEntity.ok(locationCode);
    }

    @Operation(summary = "Movimiento Interno", description = "Mueve un LPN de una ubicación a otra validando capacidades.")
    @PostMapping("/move")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> internalMove(@RequestBody @Valid InternalMoveRequest request) {
        InternalMoveCommand command = new InternalMoveCommand(
                request.lpn(), request.targetLocationCode(), request.reason()
        );
        operationsUseCase.processInternalMove(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Ajuste de Inventario", description = "Modifica la cantidad de un LPN (Pérdida/Ganancia). Solo ADMIN.")
    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> adjustInventory(@RequestBody @Valid InventoryAdjustmentRequest request) {
        InventoryAdjustmentCommand command = new InventoryAdjustmentCommand(
                request.lpn(), request.newQuantity(), request.reason()
        );
        operationsUseCase.processInventoryAdjustment(command);
        return ResponseEntity.ok().build();
    }

    // --- DTOs WEB (Request Body) ---
    // Estos DTOs son específicos de la capa Web (Input).
    // Podrían ir en un paquete 'request' separado, pero como records internos quedan limpios aquí.

    public record ReceiveInventoryRequest(
            @Schema(example = "TV-LG-65") @NotBlank String productSku,
            @Schema(example = "10.0") @NotNull @Min(1) Double quantity,
            @Schema(example = "A-01-01-1") @NotBlank String locationCode,
            @Schema(example = "LOTE-2025") @NotBlank String batchNumber,
            @Schema(example = "2030-12-31") @NotNull LocalDate expiryDate
    ) {}

    public record PutAwayRequest(
            @Schema(example = "LPN-173...") @NotBlank String lpn,
            @Schema(example = "A-01-01-1") @NotBlank String targetLocationCode
    ) {}

    public record InternalMoveRequest(
            @Schema(example = "LPN-173...") @NotBlank String lpn,
            @Schema(example = "B-02-02") @NotBlank String targetLocationCode,
            @Schema(example = "Reorganización") String reason
    ) {}

    public record InventoryAdjustmentRequest(
            @Schema(example = "LPN-173...") @NotBlank String lpn,
            @Schema(example = "5.0") @NotNull @Min(0) Double newQuantity,
            @Schema(example = "Dañado / Pérdida") @NotBlank String reason
    ) {}
}