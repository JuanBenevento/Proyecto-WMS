package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;

public interface ReceiveInventoryUseCase {
    InventoryItemResponse receiveInventory(ReceiveInventoryCommand command);
}