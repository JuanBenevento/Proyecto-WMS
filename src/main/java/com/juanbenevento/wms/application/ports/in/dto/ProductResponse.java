package com.juanbenevento.wms.application.ports.in.dto;

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