package com.juanbenevento.wms.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado cuando Inventory completa el proceso de picking para una orden.
 * Consumido por Orders para transicionar a estado PACKED.
 */
public record PickingCompletedEvent(
    String orderId,
    String orderNumber,
    List<PickedLine> pickedLines,
    String completedBy,
    LocalDateTime occurredAt
) {
    /**
     * Información de cada línea pickeada.
     */
    public record PickedLine(
        String lineId,
        BigDecimal pickedQuantity,
        boolean wasShort  // true si se pickeó menos de lo asignado
    ) {}
}
