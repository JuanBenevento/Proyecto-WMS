package com.juanbenevento.wms.warehouse.application.port.in.dto;

import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;

import java.util.List;

public record LocationResponse(
        String locationCode,
        String aisle,
        String column,
        String level,
        String zoneType,
        Double maxWeight,
        Double currentWeight,
        Double availableWeight,
        Double maxVolume,
        Double currentVolume,
        Double occupancyPercentage,
        List<InventoryItemResponse> items
) {}