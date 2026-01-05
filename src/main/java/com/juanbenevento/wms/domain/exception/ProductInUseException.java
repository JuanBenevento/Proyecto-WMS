package com.juanbenevento.wms.domain.exception;

public class ProductInUseException extends DomainException {
    public ProductInUseException(String sku, String reason) {
        super(String.format("No se puede modificar/eliminar el producto %s. Motivo: %s", sku, reason));
    }
}