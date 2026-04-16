package com.juanbenevento.wms.inventory.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;

public interface ReceiveInventoryUseCase {
    InventoryItemResponse receiveInventory(ReceiveInventoryCommand command);
}