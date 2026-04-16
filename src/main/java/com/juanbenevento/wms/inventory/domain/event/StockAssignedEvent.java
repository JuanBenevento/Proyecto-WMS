package com.juanbenevento.wms.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado cuando Inventory asigna stock a las líneas de una orden.
 * Este evento es consumido por Orders para actualizar el estado de las líneas.
 */
public record StockAssignedEvent(
    String orderId,
    String orderNumber,
    List<LineAssignment> lines,
    String assignedBy,
    LocalDateTime occurredAt
) {
    /**
     * Información de asignación para cada línea.
     */
    public record LineAssignment(
        String lineId,
        String sku,
        BigDecimal requestedQuantity,
        BigDecimal allocatedQuantity,
        String inventoryItemId,  // LPN asignado
        String locationCode
    ) {}
}
