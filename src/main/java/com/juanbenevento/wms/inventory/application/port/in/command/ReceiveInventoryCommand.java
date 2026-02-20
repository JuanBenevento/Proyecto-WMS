package com.juanbenevento.wms.inventory.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiveInventoryCommand(
        String productSku,
        BigDecimal quantity,
        String locationCode,
        String batchNumber,
        LocalDate expiryDate
) {
    public ReceiveInventoryCommand {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
    }
}