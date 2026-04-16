package com.juanbenevento.wms.shared.domain.valueobject;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

import java.math.BigDecimal;

public record Dimensions(
        BigDecimal width, BigDecimal height, BigDecimal depth, BigDecimal weight
) {
    public Dimensions {
        if (width == null || width.compareTo(BigDecimal.ZERO) <= 0 ||
                height == null || height.compareTo(BigDecimal.ZERO) <= 0 ||
                depth == null || depth.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Las dimensiones deben ser positivas y no nulas");
        }

        if (weight == null || weight.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("El peso no puede ser negativo");
        }
    }

    public BigDecimal calculateVolume() {
        return width.multiply(height).multiply(depth);
    }

    public boolean isHeavyLoad() {
        BigDecimal limit = new BigDecimal("20.0");
        return weight.compareTo(limit) > 0;
    }
}