package com.juanbenevento.wms.warehouse.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;

import java.math.BigDecimal;
import java.util.List;

public record LocationResponse(
        String locationCode,
        String aisle,
        String column,
        String level,
        String zoneType,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal maxWeight,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal currentWeight,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal availableWeight,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal maxVolume,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal currentVolume,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal occupancyPercentage,
        List<InventoryItemResponse> items
) {}