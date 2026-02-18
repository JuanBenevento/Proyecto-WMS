package com.juanbenevento.wms.catalog.application.port.in.command;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        Double width,
        Double height,
        Double depth,
        Double weight
) {
    public CreateProductCommand {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU requerido");
        if (weight == null || weight <= 0) throw new IllegalArgumentException("Peso inválido");
    }
}