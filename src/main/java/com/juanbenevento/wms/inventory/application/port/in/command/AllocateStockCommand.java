package com.juanbenevento.wms.inventory.application.port.in.command;

import java.math.BigDecimal;

public record AllocateStockCommand(String sku, BigDecimal quantity) {
}
