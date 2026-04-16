package com.juanbenevento.wms.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado cuando Inventory inicia el proceso de picking para una orden.
 * Consumido por Orders para transicionar a estado PICKING.
 */
public record PickingStartedEvent(
    String orderId,
    String orderNumber,
    List<String> lineIds,
    List<String> assignedLocations,
    String startedBy,
    LocalDateTime occurredAt
) {}
