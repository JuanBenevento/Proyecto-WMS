package com.juanbenevento.wms.inventory.application.port.in.command;

public record ShipStockCommand(String sku, Double quantity) {
    public ShipStockCommand {
        if (quantity <= 0) throw new IllegalArgumentException("Cantidad inválida");
    }
}