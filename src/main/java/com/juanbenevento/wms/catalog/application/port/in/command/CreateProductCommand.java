package com.juanbenevento.wms.catalog.application.port.in.command;

import java.math.BigDecimal;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal width,
        BigDecimal height,
        BigDecimal depth,
        BigDecimal weight
) {
    public CreateProductCommand {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU requerido");
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Peso inválido");
    }
}