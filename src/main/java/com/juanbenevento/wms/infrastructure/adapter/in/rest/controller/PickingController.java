package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.command.AllocateStockCommand;
import com.juanbenevento.wms.application.ports.in.command.ShipStockCommand;
import com.juanbenevento.wms.application.ports.in.usecases.AllocateStockUseCase;
import com.juanbenevento.wms.application.ports.in.usecases.ShipStockUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/picking")
@RequiredArgsConstructor
@Tag(name = "4. Salidas y Despacho (Outbound)", description = "Reserva de stock y liberación de espacio.")
public class PickingController {

    private final AllocateStockUseCase allocateStockUseCase;
    private final ShipStockUseCase shipStockUseCase;

    @Operation(summary = "1. Reservar Stock (Allocate)", description = "Busca stock (FEFO) y lo marca como RESERVED.")
    @PostMapping("/allocate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<String> allocateOrder(@RequestBody @Valid PickingRequest request) {
        allocateStockUseCase.allocateStock(new AllocateStockCommand(
                request.sku(), request.quantity()
        ));
        return ResponseEntity.ok("Stock reservado exitosamente. Listo para picking.");
    }

    @Operation(summary = "2. Confirmar Despacho (Ship)", description = "Da de baja el stock y libera el peso.")
    @PostMapping("/ship")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OPERATOR')")
    public ResponseEntity<String> shipOrder(@RequestBody @Valid PickingRequest request) {
        shipStockUseCase.shipStock(new ShipStockCommand(
                request.sku(), request.quantity()
        ));
        return ResponseEntity.ok("Pedido despachado correctamente.");
    }

    public record PickingRequest(
            @Schema(example = "TV-LG-65") @NotBlank String sku,
            @Schema(example = "5.0") @NotNull @Positive Double quantity
    ) {}
}