package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse; // Usar DTO
import java.util.List;

public interface RetrieveInventoryUseCase {
    List<InventoryItemResponse> getAllInventory();
}