package com.juanbenevento.wms.shared.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object para License Plate Number (LPN).
 * Representa el identificador único de una unidad de inventario en el almacén.
 * 
 * Formatos válidos:
 * - LPN-{8-CHARS} — LPN de recepción (ej: LPN-A1B2C3D4)
 * - PICK-{8-CHARS} — LPN reservado para picking (ej: PICK-X9Y8Z7W6)
 * - LPN-UNKNOWN — Sentinel para LPN desconocido
 * - VARIOUS — Sentinel para múltiples LPNs
 * 
 * Esta clase es INMUTABLE — una vez creada no puede modificarse.
 */
public final class Lpn {

    private static final Pattern LPN_PATTERN = Pattern.compile("^(LPN|PICK)-[A-Z0-9]{8}$");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    private final String value;

    // Private constructor — usar factory methods
    private Lpn(String value) {
        this.value = value;
    }

    /**
     * Crea un Lpn validando el formato.
     * 
     * @param value el string del LPN
     * @return nueva instancia de Lpn
     * @throws IllegalArgumentException si el formato es inválido
     */
    public static Lpn of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LPN no puede ser nulo o vacío");
        }
        
        // Validar formato estándar o sentinels
        if (isValidFormat(value)) {
            return new Lpn(value);
        }
        
        throw new IllegalArgumentException(
            "Formato de LPN inválido: " + value + 
            ". Formatos válidos: LPN-{8-CHARS}, PICK-{8-CHARS}, LPN-UNKNOWN, VARIOUS"
        );
    }

    /**
     * Crea un Lpn sin validación (para datos existentes en DB).
     * Útil para reconstrucciones desde repositorio.
     */
    public static Lpn fromRaw(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LPN no puede ser nulo o vacío");
        }
        return new Lpn(value);
    }

    /**
     * Factory method para el sentinel LPN-UNKNOWN.
     */
    public static Lpn unknown() {
        return new Lpn(WmsConstants.LPN_UNKNOWN);
    }

    /**
     * Factory method para el sentinel VARIOUS.
     */
    public static Lpn various() {
        return new Lpn(WmsConstants.LPN_VARIOUS);
    }

    /**
     * Genera un nuevo LPN con prefijo LPN- y 8 caracteres aleatorios.
     */
    public static Lpn generate() {
        String uuid = java.util.UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return new Lpn(WmsConstants.LPN_PREFIX + uuid);
    }

    private static boolean isValidFormat(String value) {
        // Validar sentinels primero
        if (WmsConstants.LPN_UNKNOWN.equals(value) || WmsConstants.LPN_VARIOUS.equals(value)) {
            return true;
        }
        // Validar formato estándar
        return LPN_PATTERN.matcher(value).matches();
    }

    /**
     * Verifica si este LPN es un sentinel (UNKNOWN o VARIOUS).
     */
    public boolean isSentinel() {
        return WmsConstants.LPN_UNKNOWN.equals(value) || WmsConstants.LPN_VARIOUS.equals(value);
    }

    /**
     * Verifica si este LPN es de picking.
     */
    public boolean isPickingLpn() {
        return value != null && value.startsWith(WmsConstants.PICK_PREFIX);
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
        Lpn lpn = (Lpn) o;
        return Objects.equals(value, lpn.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
