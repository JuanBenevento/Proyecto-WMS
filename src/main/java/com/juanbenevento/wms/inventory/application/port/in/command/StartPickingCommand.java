package com.juanbenevento.wms.inventory.application.port.in.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Comando para iniciar el proceso de picking de una orden.
 */
public record StartPickingCommand(
    @NotBlank(message = "El ID de la orden es obligatorio")
    String orderId,

    @NotNull(message = "La decisión para short picks es obligatoria")
    ShortPickDecision shortPickDecision,

    List<String> preferredLocations,  // Ubicaciones preferidas para optimizar ruta
    String assignedOperator
) {
    /**
     * Decisiones disponibles para manejar short picks.
     */
    public enum ShortPickDecision {
        /** Envía lo que hay, crea backorder por lo faltante */
        ALLOW_PARTIAL_SHIPMENT,

        /** Orden a HOLD hasta que llegue stock o se cancele */
        BLOCK_UNTIL_COMPLETE,

        /** Intenta buscar stock en otra ubicación automáticamente */
        AUTO_REPLENISH,

        /** Requiere decisión manual de un supervisor */
        MANUAL_DECISION
    }
}
