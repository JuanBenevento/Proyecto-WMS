package com.juanbenevento.wms.catalog.application.port.in.dto;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        Double width,
        Double height,
        Double depth,
        Double weight
) {}