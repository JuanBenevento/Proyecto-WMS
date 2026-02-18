package com.juanbenevento.wms.inventory.domain.event;
import java.time.LocalDateTime;

public record StockShippedEvent(
        String sku,
        Double quantity,
        String locationCode,
        String username,
        LocalDateTime occurredAt
) {}