package com.juanbenevento.wms.inventory.domain.strategy;

import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import org.springframework.stereotype.Component;

@Component
public class SmartPutAwayStrategy implements PutAwayStrategy {

    @Override
    public ZoneType determineZone(Product product) {
        String desc = product.getDescription() != null ? product.getDescription().toUpperCase() : "";
        String name = product.getName().toUpperCase();

        if (desc.contains("CONGELADO") || desc.contains("ICE") || name.contains("HELADO")) {
            return ZoneType.FROZEN_STORAGE;
        }
        if (desc.contains("REFRIGERADO") || desc.contains("FRESH") || name.contains("YOGURT")) {
            return ZoneType.COLD_STORAGE;
        }
        if (desc.contains("PELIGRO") || desc.contains("ACID") || desc.contains("QUIMICO")) {
            return ZoneType.HAZMAT;
        }

        return ZoneType.DRY_STORAGE;
    }

}