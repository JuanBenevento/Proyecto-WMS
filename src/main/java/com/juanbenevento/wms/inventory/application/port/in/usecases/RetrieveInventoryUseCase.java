package com.juanbenevento.wms.inventory.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse; // Usar DTO
import java.util.List;

public interface RetrieveInventoryUseCase {
    List<InventoryItemResponse> getAllInventory();
}