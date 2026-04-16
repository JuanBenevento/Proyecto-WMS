package com.juanbenevento.wms.shared.infrastructure.persistence;

import com.juanbenevento.wms.shared.domain.valueobject.BatchNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter para persistir BatchNumber como String en la base de datos.
 */
@Converter
public class BatchNumberConverter implements AttributeConverter<BatchNumber, String> {
    
    @Override
    public String convertToDatabaseColumn(BatchNumber batchNumber) {
        return batchNumber != null ? batchNumber.getValue() : null;
    }

    @Override
    public BatchNumber convertToEntityAttribute(String value) {
        return value != null ? BatchNumber.fromRaw(value) : null;
    }
}
