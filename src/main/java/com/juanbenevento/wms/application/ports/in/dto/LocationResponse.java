package com.juanbenevento.wms.application.ports.in.dto;

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