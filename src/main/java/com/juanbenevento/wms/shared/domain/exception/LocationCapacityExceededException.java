package com.juanbenevento.wms.shared.domain.exception;

public class LocationCapacityExceededException extends DomainException {
    
    private final String locationCode;
    private final Double attemptedWeight;
    private final Double attemptedVolume;
    private final Double maxWeight;
    private final Double maxVolume;
    
    public LocationCapacityExceededException(
            String locationCode,
            Double attemptedWeight,
            Double attemptedVolume,
            Double maxWeight,
            Double maxVolume
    ) {
        super(String.format(
                "La ubicación %s no puede soportar la carga solicitada. " +
                "Intento: %.2f kg / %.2f m³. Capacidad máxima: %.2f kg / %.2f m³",
                locationCode, attemptedWeight, attemptedVolume, maxWeight, maxVolume
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
    
    public Double getAttemptedWeight() {
        return attemptedWeight;
    }
    
    public Double getAttemptedVolume() {
        return attemptedVolume;
    }
    
    public Double getMaxWeight() {
        return maxWeight;
    }
    
    public Double getMaxVolume() {
        return maxVolume;
    }
}


