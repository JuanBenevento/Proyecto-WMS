package com.juanbenevento.wms.inventory.domain.event;

import java.time.LocalDateTime;

public record StockMovedEvent(
        String lpn,
        String sku,
        Double quantity,
        String oldLocation,
        String newLocation,
        String username,
        String type,
        LocalDateTime occurredAt
) {}