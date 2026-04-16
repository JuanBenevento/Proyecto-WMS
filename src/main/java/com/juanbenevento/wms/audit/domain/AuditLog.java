package com.juanbenevento.wms.audit.domain;

import com.juanbenevento.wms.inventory.domain.model.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuditLog(
        Long id,
        LocalDateTime timestamp,
        StockMovementType type,
        String sku,
        String lpn,
        BigDecimal quantity,
        BigDecimal oldQuantity,
        BigDecimal newQuantity,
        String user,
        String reason
) {}

