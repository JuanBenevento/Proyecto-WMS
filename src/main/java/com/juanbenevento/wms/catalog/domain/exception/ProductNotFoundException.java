package com.juanbenevento.wms.catalog.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

/**
 * Excepción lanzada cuando se intenta operar con un producto que no existe.
 */
public class ProductNotFoundException extends DomainException {
    
    private final String sku;
    
    public ProductNotFoundException(String sku) {
        super(String.format("Producto con SKU '%s' no encontrado", sku));
        this.sku = sku;
    }
    
    public String getSku() {
        return sku;
    }
}


