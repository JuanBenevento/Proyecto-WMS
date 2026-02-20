package com.juanbenevento.wms.inventory.application.port.in.command;

import java.math.BigDecimal;

public record ShipStockCommand(String sku, BigDecimal quantity) {
    public ShipStockCommand {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Cantidad inválida");
    }
}