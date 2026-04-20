package com.juanbenevento.wms.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado cuando se completa el proceso de picking de una orden.
 */
public record PickingCompletedEvent(
    String orderId,
    String orderNumber,
    List<PickedLine> pickedLines,
    String completedBy,
    LocalDateTime completedAt
) {
    public static final String EVENT_TYPE = "INVENTORY.PICKING_COMPLETED";

    public record PickedLine(
        String lineId,
        java.math.BigDecimal pickedQuantity,
        boolean wasShort
    ) {}

    public String getEventType() {
        return EVENT_TYPE;
    }
}
