package com.juanbenevento.wms.inventory.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.command.PutAwayInventoryCommand;

public interface PutAwayUseCase {
    void putAwayInventory(PutAwayInventoryCommand command);
}