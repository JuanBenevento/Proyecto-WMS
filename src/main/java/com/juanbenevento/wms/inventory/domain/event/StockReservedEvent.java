package com.juanbenevento.wms.inventory.domain.event;
import java.time.LocalDateTime;

public record StockReservedEvent(
        String sku,
        Double quantity,
        String username,
        LocalDateTime occurredAt
) {}