package com.juanbenevento.wms.shared.domain.exception;

import java.math.BigDecimal;

public class LocationCapacityExceededException extends DomainException {
    
    private final String locationCode;
    private final BigDecimal attemptedWeight;
    private final BigDecimal attemptedVolume;
    private final BigDecimal maxWeight;
    private final BigDecimal maxVolume;
    
    public LocationCapacityExceededException(
            String locationCode,
            BigDecimal attemptedWeight,
            BigDecimal attemptedVolume,
            BigDecimal maxWeight,
            BigDecimal maxVolume
    ) {
        super(String.format(
                "La ubicación %s no puede soportar la carga solicitada. " +
                        "Intento: %s kg / %s m³. Capacidad máxima: %s kg / %s m³",
                locationCode,
                attemptedWeight != null ? attemptedWeight.toPlainString() : "0",
                attemptedVolume != null ? attemptedVolume.toPlainString() : "0",
                maxWeight != null ? maxWeight.toPlainString() : "0",
                maxVolume != null ? maxVolume.toPlainString() : "0"
        ));
        this.locationCode = locationCode;
        this.attemptedWeight = attemptedWeight;
        this.attemptedVolume = attemptedVolume;
        this.maxWeight = maxWeight;
        this.maxVolume = maxVolume;
    }
    
    public String getLocationCode() {
        return locationCode;
    }
    
    public BigDecimal getAttemptedWeight() {
        return attemptedWeight;
    }
    
    public BigDecimal getAttemptedVolume() {
        return attemptedVolume;
    }
    
    public BigDecimal getMaxWeight() {
        return maxWeight;
    }
    
    public BigDecimal getMaxVolume() {
        return maxVolume;
    }
}


