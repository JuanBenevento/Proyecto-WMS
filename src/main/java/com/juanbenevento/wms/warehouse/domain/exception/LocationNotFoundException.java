package com.juanbenevento.wms.warehouse.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

/**
 * Excepción lanzada cuando se intenta operar con una ubicación que no existe.
 */
public class LocationNotFoundException extends DomainException {
    
    private final String locationCode;
    
    public LocationNotFoundException(String locationCode) {
        super(String.format("Ubicación con código '%s' no encontrada", locationCode));
        this.locationCode = locationCode;
    }
    
    public LocationNotFoundException(String locationCode, String context) {
        super(String.format("Ubicación con código '%s' no encontrada. Contexto: %s", locationCode, context));
        this.locationCode = locationCode;
    }
    
    public String getLocationCode() {
        return locationCode;
    }
}


