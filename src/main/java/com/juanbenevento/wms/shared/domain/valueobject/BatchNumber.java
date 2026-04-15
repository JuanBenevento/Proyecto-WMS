package com.juanbenevento.wms.shared.domain.valueobject;

import java.util.Objects;

/**
 * Value Object para BatchNumber.
 * Representa el identificador de lote de un producto para trazabilidad.
 * 
 * El formato es libre pero con restricciones:
 * - No puede ser vacío
 * - Longitud máxima de 50 caracteres
 * - Caracteres alfanuméricos, guiones y guiones bajos permitidos
 * 
 * Esta clase es INMUTABLE — una vez creada no puede modificarse.
 */
public final class BatchNumber {

    private static final int MAX_LENGTH = 50;
    private static final String VALID_CHARS_PATTERN = "^[A-Za-z0-9_-]+$";

    private final String value;

    // Private constructor — usar factory methods
    private BatchNumber(String value) {
        this.value = value;
    }

    /**
     * Crea un BatchNumber validando el formato.
     * 
     * @param value el string del batch number
     * @return nueva instancia de BatchNumber
     * @throws IllegalArgumentException si el formato es inválido
     */
    public static BatchNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BatchNumber no puede ser nulo o vacío");
        }
        
        String trimmed = value.trim();
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "BatchNumber excede la longitud máxima de " + MAX_LENGTH + " caracteres"
            );
        }
        
        if (!trimmed.matches(VALID_CHARS_PATTERN)) {
            throw new IllegalArgumentException(
                "BatchNumber contiene caracteres inválidos. " +
                "Solo se permiten letras, números, guiones (-) y guiones bajos (_)"
            );
        }
        
        return new BatchNumber(trimmed);
    }

    /**
     * Crea un BatchNumber sin validación (para datos existentes en DB).
     * Útil para reconstrucciones desde repositorio.
     */
    public static BatchNumber fromRaw(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BatchNumber no puede ser nulo o vacío");
        }
        return new BatchNumber(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchNumber that = (BatchNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
