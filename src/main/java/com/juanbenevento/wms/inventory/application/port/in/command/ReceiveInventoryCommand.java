package com.juanbenevento.wms.inventory.application.port.in.command;

import java.time.LocalDate;

public record ReceiveInventoryCommand(
        String productSku,
        Double quantity,
        String locationCode,
        String batchNumber,
        LocalDate expiryDate
) {
    public ReceiveInventoryCommand {
        if (quantity <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
    }
}