package com.juanbenevento.wms.warehouse.application.port.in.dto;

public record RackSummaryDto(
        String rackCode,
        int totalPositions,
        double occupancyPercentage,
        double currentWeight,
        double maxWeight,
        String status
) {}