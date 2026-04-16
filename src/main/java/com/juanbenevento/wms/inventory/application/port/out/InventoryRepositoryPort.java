package com.juanbenevento.wms.inventory.application.port.out;

import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import java.util.Optional;
import java.util.List;

public interface InventoryRepositoryPort {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findByLpn(String lpn);
    InventoryItem findByInventoryItemId(String inventoryItemId);
    List<InventoryItem> findByProduct(String sku);
    List<InventoryItem> findAvailableStock(String sku);
    List<InventoryItem> findReservedStock(String sku);
    List<InventoryItem> findAll();
    List<InventoryItem> findAvailableStockForAllocation(String sku);
}