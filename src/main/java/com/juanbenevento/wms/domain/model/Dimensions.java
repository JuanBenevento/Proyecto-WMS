package com.juanbenevento.wms.domain.model;

import com.juanbenevento.wms.domain.exception.DomainException;

public record Dimensions(
        Double width,  Double height, Double depth, Double weight
) {
    public Dimensions {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new DomainException("Las dimensiones deben ser positivas");
        }
        if (weight < 0) {
            throw new DomainException("El peso no puede ser negativo");
        }
    }

    public Double calculateVolume() {
        return width * height * depth;
    }

    public boolean isHeavyLoad() {
        return weight > 20.0;
    }
}