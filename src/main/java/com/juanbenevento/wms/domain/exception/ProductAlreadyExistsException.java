package com.juanbenevento.wms.domain.exception;

public class ProductAlreadyExistsException extends DomainException {
    public ProductAlreadyExistsException(String sku) {
        super("Ya existe un producto registrado con el SKU: " + sku);
    }
}