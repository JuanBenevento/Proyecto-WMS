package com.juanbenevento.wms.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado cuando inicia el proceso de picking de una orden.
 */
public record PickingStartedEvent(
    String orderId,
    String orderNumber,
    List<String> lineIds,
    List<String> locationCodes,
    String initiatedBy,
    LocalDateTime startedAt
) {
    public static final String EVENT_TYPE = "INVENTORY.PICKING_STARTED";

    public String getEventType() {
        return EVENT_TYPE;
    }
}
