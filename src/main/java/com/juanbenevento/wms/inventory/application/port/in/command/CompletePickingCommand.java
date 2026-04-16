package com.juanbenevento.wms.inventory.application.port.in.command;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Comando para completar el proceso de picking de una orden.
 */
public record CompletePickingCommand(
    @NotBlank(message = "El ID de la orden es obligatorio")
    String orderId,

    List<PickResult> lines,

    String completedBy,
    String notes
) {
    /**
     * Resultado del pick de cada línea.
     */
    public record PickResult(
        String lineId,
        java.math.BigDecimal pickedQuantity,
        boolean wasShort,
        String notes
    ) {}
}
