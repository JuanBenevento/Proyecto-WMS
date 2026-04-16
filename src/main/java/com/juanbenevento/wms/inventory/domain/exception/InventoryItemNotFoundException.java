package com.juanbenevento.wms.inventory.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

public class InventoryItemNotFoundException extends DomainException {
    
    private final String lpn;
    
    public InventoryItemNotFoundException(String lpnCode) {
        super(String.format("Item de inventario con LPN '%s' no encontrado", lpnCode));
        this.lpn = lpnCode;
    }
    
    public String getLpn() {
        return lpn;
    }
}

