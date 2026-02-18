package com.juanbenevento.wms.inventory.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.command.InternalMoveCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.InventoryAdjustmentCommand;

public interface ManageInventoryOperationsUseCase {
    void processInternalMove(InternalMoveCommand command);
    void processInventoryAdjustment(InventoryAdjustmentCommand command);
}