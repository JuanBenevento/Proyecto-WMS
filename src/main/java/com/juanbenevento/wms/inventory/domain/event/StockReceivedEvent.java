package com.juanbenevento.wms.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockReceivedEvent(
        String lpn,
        String sku,
        BigDecimal quantity,
        String locationCode,
        String username,
        LocalDateTime occurredAt
) {}