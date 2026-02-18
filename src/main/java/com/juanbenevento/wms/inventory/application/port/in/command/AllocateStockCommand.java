package com.juanbenevento.wms.inventory.application.port.in.command;

public record AllocateStockCommand(String sku, Double quantity) {
}
