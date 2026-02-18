package com.juanbenevento.wms.audit.domain;

import com.juanbenevento.wms.inventory.domain.model.StockMovementType;

import java.time.LocalDateTime;

public record AuditLog(
        Long id,
        LocalDateTime timestamp,
        StockMovementType type,
        String sku,
        String lpn,
        Double quantity,
        Double oldQuantity,
        Double newQuantity,
        String user,
        String reason
) {}

