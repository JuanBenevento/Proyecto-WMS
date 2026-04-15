package com.juanbenevento.wms.shared.infrastructure.persistence;

import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter para persistir Lpn como String en la base de datos.
 */
@Converter
public class LpnConverter implements AttributeConverter<Lpn, String> {
    
    @Override
    public String convertToDatabaseColumn(Lpn lpn) {
        return lpn != null ? lpn.getValue() : null;
    }

    @Override
    public Lpn convertToEntityAttribute(String value) {
        return value != null ? Lpn.fromRaw(value) : null;
    }
}
