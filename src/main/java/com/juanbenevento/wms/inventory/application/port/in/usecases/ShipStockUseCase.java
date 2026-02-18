package com.juanbenevento.wms.inventory.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.command.ShipStockCommand;

public interface ShipStockUseCase {
    void shipStock(ShipStockCommand command);
}
