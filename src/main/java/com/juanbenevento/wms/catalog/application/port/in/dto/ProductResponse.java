package com.juanbenevento.wms.catalog.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal width,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal height,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal depth,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal weight
) {}