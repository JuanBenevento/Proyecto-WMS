package com.juanbenevento.wms.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado cuando NO hay suficiente stock para una orden.
 * Orders recibe este evento para poner la orden en HOLD con razón INVENTORY_SHORTAGE.
 */
public record StockShortageEvent(
    String orderId,
    String orderNumber,
    List<LineShortage> shortages,
    String reason,
    String reportedBy,
    LocalDateTime occurredAt
) {
    /**
     * Información del faltante para cada línea.
     */
    public record LineShortage(
        String lineId,
        String sku,
        BigDecimal requestedQuantity,
        BigDecimal allocatedQuantity  // Puede ser cero o parcial
    ) {}
}
