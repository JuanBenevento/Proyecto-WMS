package com.juanbenevento.wms.inventory.domain.strategy;

import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;

public interface PutAwayStrategy {
    ZoneType determineZone(Product product);
}