package com.juanbenevento.wms.inventory.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;

public interface AllocateStockUseCase {
    void allocateStock(AllocateStockCommand command);
}
