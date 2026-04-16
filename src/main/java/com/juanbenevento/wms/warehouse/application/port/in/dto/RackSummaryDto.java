package com.juanbenevento.wms.warehouse.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record RackSummaryDto(
        String rackCode,
        int totalPositions,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal occupancyPercentage,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal currentWeight,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal maxWeight,
        String status
) {}