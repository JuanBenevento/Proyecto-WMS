package com.juanbenevento.wms.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryAdjustedEvent(
        String lpn,
        String productSku,
        BigDecimal oldQuantity,
        BigDecimal newQuantity,
        String reason,
        String locationCode,
        String username,
        LocalDateTime occurredAt
) {}