package com.juanbenevento.wms.inventory.domain.event;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockReservedEvent(
        String sku,
        BigDecimal quantity,
        String username,
        LocalDateTime occurredAt
) {}