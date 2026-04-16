package com.juanbenevento.wms.catalog.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

public class ProductAlreadyExistsException extends DomainException {
    public ProductAlreadyExistsException(String sku) {
        super("Ya existe un producto registrado con el SKU: " + sku);
    }
}