package com.juanbenevento.wms.application.ports.in.dto;

public record RackSummaryDto(
        String rackCode,
        int totalPositions,
        double occupancyPercentage,
        double currentWeight,
        double maxWeight,
        String status
) {}