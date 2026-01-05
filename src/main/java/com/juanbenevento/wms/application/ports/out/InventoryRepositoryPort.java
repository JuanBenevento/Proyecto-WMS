package com.juanbenevento.wms.application.ports.out;

import com.juanbenevento.wms.domain.model.InventoryItem;
import java.util.Optional;
import java.util.List;

public interface InventoryRepositoryPort {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findByLpn(String lpn);
    List<InventoryItem> findByProduct(String sku);
    List<InventoryItem> findAvailableStock(String sku);
    List<InventoryItem> findReservedStock(String sku);
    List<InventoryItem> findAll();
    List<InventoryItem> findAvailableStockForAllocation(String sku);
}