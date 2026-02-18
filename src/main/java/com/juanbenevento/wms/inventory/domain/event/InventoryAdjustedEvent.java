package com.juanbenevento.wms.inventory.domain.event;

import java.time.LocalDateTime;

public record InventoryAdjustedEvent(
        String lpn,
        String productSku,
        Double oldQuantity,
        Double newQuantity,
        String reason,
        String locationCode,
        String username,
        LocalDateTime occurredAt
) {}