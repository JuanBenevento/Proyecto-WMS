package com.juanbenevento.wms.catalog.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

public class ProductInUseException extends DomainException {
    public ProductInUseException(String sku, String reason) {
        super(String.format("No se puede modificar/eliminar el producto %s. Motivo: %s", sku, reason));
    }
}